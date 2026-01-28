package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.quest.shared.QuestRank;

/**
 * UserQuestsCompleted: Número de User Quests completadas en periodo.
 */
public final class UserQuestsCompleted implements GateCondition {
    private final int requiredCount;
    private final int days;
    private final QuestRank minRank;

    public UserQuestsCompleted(int requiredCount, int days, QuestRank minRank) {
        this.requiredCount = requiredCount;
        this.days = days;
        this.minRank = minRank;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getUserQuestsCompletedInLastDays(days, minRank) >= requiredCount;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getUserQuestsCompletedInLastDays(days, minRank);
        return Math.min(1.0, (double) current / requiredCount);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getUserQuestsCompletedInLastDays(days, minRank);
        return String.format("User Quests (%s+): %d/%d", minRank.name(), current, requiredCount);
    }

    @Override
    public String getDescription() {
        return String.format("Completar %d User Quests Rango %s+ en %d días",
                requiredCount, minRank.name(), days);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.USER_QUESTS_COMPLETED;
    }
}