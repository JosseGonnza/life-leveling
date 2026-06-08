package com.lifeleveling.application.dto;

import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.shared.QuestStatus;
import com.lifeleveling.domain.quest.weekly.WeeklyQuest;

import java.time.LocalDate;

/**
 * Read model de una Weekly Quest para la pantalla de Misiones (sección semanal).
 * Las semanales no penalizan: son "bonus tracks" que expiran al acabar la semana.
 */
public record WeeklyQuestView(
        String name,
        String description,
        int currentProgress,
        int target,
        String progressText,
        boolean completed,
        int rewardXP,
        int rewardGold,
        int daysRemaining
) {
    public static WeeklyQuestView from(WeeklyQuest q, LocalDate today) {
        QuestReward r = q.reward();
        int rewardXP = r.generalXP() + r.statXP().values().stream().mapToInt(Integer::intValue).sum();
        return new WeeklyQuestView(
                q.name(),
                q.description(),
                q.getCurrentProgress(),
                q.getTarget(),
                q.getProgressText(),
                q.status() == QuestStatus.COMPLETED,
                rewardXP,
                r.gold(),
                q.getDaysRemaining(today)
        );
    }
}
