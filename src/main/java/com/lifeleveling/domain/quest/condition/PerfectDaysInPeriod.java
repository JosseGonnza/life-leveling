package com.lifeleveling.domain.quest.condition;

public record PerfectDaysInPeriod(int requiredCount, int periodDays) implements GateCondition {
    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getPerfectDaysCountInLastDays(periodDays) >= requiredCount;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getPerfectDaysCountInLastDays(periodDays);
        return Math.min(1.0, (double) current / requiredCount);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getPerfectDaysCountInLastDays(periodDays);
        return String.format("Días Perfectos (30d): %d/%d", current, requiredCount);
    }

    @Override
    public String getDescription() {
        return String.format("Conseguir %d Días Perfectos en los últimos %d días", requiredCount, periodDays);
    }

    @Override
    public ConditionType getType() { return ConditionType.PERFECT_DAY_STREAK; }
}