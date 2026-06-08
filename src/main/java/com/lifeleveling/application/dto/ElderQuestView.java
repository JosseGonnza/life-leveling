package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.ConditionContext;
import com.lifeleveling.domain.quest.condition.GateCondition;
import com.lifeleveling.domain.quest.elder.ElderQuestType;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read model de un Juicio del Monarca (Elder Quest) para la pantalla Juicios.
 * `objectives`/`progress`/`completed` salen de evaluar las condiciones contra el estado actual.
 * Read-only en v1: aún no hay "reclamar" (Judgment Day) ni Season Lock real.
 */
public record ElderQuestView(
        String icon,
        String name,
        String lore,
        String frequency,
        List<String> objectives,
        String reward,
        double progress,
        boolean completed
) {
    public static ElderQuestView from(ElderQuestType type, ConditionContext ctx) {
        List<GateCondition> conds = type.getConditions();
        List<String> objectives = new ArrayList<>();
        double sum = 0;
        boolean allMet = !conds.isEmpty();
        for (GateCondition c : conds) {
            String txt;
            try { txt = c.getProgressText(ctx); } catch (RuntimeException e) { txt = c.getDescription(); }
            objectives.add(txt);
            try {
                sum += c.getProgress(ctx);
                allMet &= c.isMet(ctx);
            } catch (RuntimeException e) {
                allMet = false;
            }
        }
        double progress = conds.isEmpty() ? 0 : sum / conds.size();
        return new ElderQuestView(type.getIcon(), type.getName(), type.getDescription(),
                type.getFrequency().getDisplayName(), objectives, formatReward(type.getReward()),
                progress, allMet);
    }

    private static String formatReward(QuestReward r) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<StatType, Integer> e : r.statXP().entrySet()) {
            parts.add("+" + String.format("%,d", e.getValue()) + " " + e.getKey().getAbbreviation() + " XP");
        }
        if (r.generalXP() > 0) parts.add("+" + String.format("%,d", r.generalXP()) + " XP General");
        if (r.gold() > 0) parts.add("+" + String.format("%,d", r.gold()) + " G");
        return parts.isEmpty() ? "—" : parts.stream().collect(Collectors.joining("  ·  "));
    }
}
