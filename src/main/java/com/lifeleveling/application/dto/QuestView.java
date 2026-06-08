package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.user.UserQuest;

/**
 * Read model de una User Quest para la pantalla de Quests (lista + detalle).
 * deadline es null si la quest no tiene fecha límite.
 * `playableNow`: si el estado de HP actual permite acometer/completar este rango (Biblia: TIRED bloquea B+).
 */
public record QuestView(
        String id,
        String name,
        String description,
        String rank,
        String deadline,
        String status,
        int rewardXP,
        int penaltyHP,
        boolean playableNow
) {
    public static QuestView from(UserQuest q, HPState state) {
        QuestReward r = q.reward();
        int rewardXP = r.generalXP() + r.statXP().values().stream().mapToInt(Integer::intValue).sum();
        return new QuestView(
                q.id().toString(),
                q.name(),
                q.description(),
                q.rank().getLetter(),
                q.getDeadline() == null ? null : q.getDeadline().toString(),
                q.status().name(),
                rewardXP,
                q.rank().getMoralDamage(),
                q.rank().canStartWith(state)
        );
    }
}
