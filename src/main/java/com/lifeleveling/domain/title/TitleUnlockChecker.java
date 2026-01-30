package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
import com.lifeleveling.domain.quest.shared.QuestRank;

import java.util.ArrayList;
import java.util.List;

/**
 * TitleUnlockChecker: Servicio de Dominio que verifica si el jugador merece nuevos títulos.
 *
 * Se invoca periódicamente (ej: al subir de nivel, al cerrar el día) para escanear
 * los requisitos de todos los títulos bloqueados.
 */
public class TitleUnlockChecker {

    /**
     * Revisa todos los títulos del juego y devuelve los que el jugador debería desbloquear ahora.
     * @param player El jugador a evaluar.
     * @return Lista de nuevos títulos desbloqueados (vacía si ninguno).
     */
    public List<TitleType> checkUnlockableTitles(Player player) {
        List<TitleType> newTitles = new ArrayList<>();
        GateTracker tracker = player.getGateTracker();

        for (TitleType title : TitleType.values()) {
            // 1. Si ya lo tiene, saltar
            if (player.hasTitle(title)) {
                continue;
            }

            // 2. Verificar requisito según el tipo
            if (meetsRequirement(player, tracker, title)) {
                newTitles.add(title);
            }
        }

        return newTitles;
    }

    private boolean meetsRequirement(Player player, GateTracker tracker, TitleType title) {
        int target = title.getRequirementValue();

        return switch (title.getRequirementType()) {
            // --- BÁSICOS ---
            case LEVEL -> player.getLevel() >= target;

            case STAT_LEVEL -> {
                StatType stat = title.getRelatedStat();
                // Asumimos que Stats tiene un método getLevel(StatType) o similar
                yield player.getBaseStats().getLevel(stat) >= target;
            }

            // --- ECONOMÍA ---
            case GOLD_THRESHOLD -> player.getCurrentGold() >= target;

            case TOTAL_SPENDING ->
                // Necesitamos que Wallet o GateTracker tenga este dato.
                // Por ahora usamos un método hipotético del tracker o player.
                    tracker.getTotalGoldSpent() >= target;

            // --- CARRERA (CODE) ---
            case ACCUMULATED_HOURS -> {
                if (title.getRelatedHabit() == DailyQuestType.CODE) {
                    yield player.getCareerEngine().getTotalCareerHours() >= target;
                }
                yield false;
            }

            case FLOW_SESSIONS -> player.getCareerEngine().getTotalFlowSessions() >= target;

            case FIRST_COMPLETION -> {
                if (title.getRelatedHabit() == DailyQuestType.CODE) {
                    yield player.getCareerEngine().getTotalSessions() >= 1;
                }
                yield false;
            }

            // --- HÁBITOS & RACHAS ---
            case ACCUMULATED_COUNT -> {
                if (title.getRelatedHabit() == null) yield false;
                String questId = title.getRelatedHabit().name();
                // GateTracker necesita un método para contar totales históricos
                yield tracker.getTotalCompletions(questId) >= target;
            }

            case ACCUMULATED_PAGES -> tracker.getTotalPagesRead() >= target;

            case HABIT_STREAK -> {
                if (title.getRelatedHabit() == DailyQuestType.SLEEP) {
                    // Lógica especial para SLEEP (quizás tracker.getSleepStreak())
                    // Por simplicidad, usamos el genérico si existe
                    yield tracker.getCurrentStreak(title.getRelatedHabit().name()) >= target;
                }
                yield tracker.getCurrentStreak(title.getRelatedHabit().name()) >= target;
            }

            case DUAL_HABIT_STREAK -> {
                // Templo Sagrado (Diet + Skincare)
                // Esto requeriría una lógica específica en GateTracker para intersección de rachas
                yield tracker.getDualHabitStreak(title.getRelatedHabit(), title.getSecondHabit()) >= target;
            }

            // --- SUPERVIVENCIA (HP) ---
            case LOW_HP_STREAK -> tracker.getCurrentLowHPStreak(title.getHpThreshold()) >= target;

            case HIGH_HP_STREAK -> tracker.getCurrentHighHPStreak(title.getHpThreshold()) >= target;

            case BURNOUT_RECOVERY -> tracker.getBurnoutsRecoveredCount() >= target;

            // --- ENDGAME ---
            case ELDER_QUEST -> {
                // Verificamos si la Elder Quest número X está completada
                // Esto asume que GateTracker o Player guarda el historial de Elder Quests
                yield tracker.isElderQuestCompleted(title.getElderQuestNumber());
            }
        };
    }
}