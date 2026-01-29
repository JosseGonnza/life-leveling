package com.lifeleveling.domain.quest.condition;

public record GymSessionsInPeriod(int requiredSessions, int periodDays) implements GateCondition {
    @Override
    public boolean isMet(ConditionContext context) {
        // "GYM" es el ID de la DailyQuestType.GYM
        return context.tracker().getDailyQuestCompletionsInLastDays("GYM", periodDays) >= requiredSessions;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getDailyQuestCompletionsInLastDays("GYM", periodDays);
        return Math.min(1.0, (double) current / requiredSessions);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getDailyQuestCompletionsInLastDays("GYM", periodDays);
        return String.format("Gym (30d): %d/%d", current, requiredSessions);
    }

    @Override
    public String getDescription() {
        return String.format("Completar %d sesiones de Gym en los últimos %d días", requiredSessions, periodDays);
    }

    @Override
    public ConditionType getType() { return ConditionType.USER_QUESTS_COMPLETED; }
}