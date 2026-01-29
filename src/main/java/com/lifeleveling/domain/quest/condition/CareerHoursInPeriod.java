package com.lifeleveling.domain.quest.condition;

public record CareerHoursInPeriod(double requiredHours, int periodDays) implements GateCondition {
    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getCareerHoursInLastDays(periodDays) >= requiredHours;
    }

    @Override
    public double getProgress(ConditionContext context) {
        double current = context.tracker().getCareerHoursInLastDays(periodDays);
        return Math.min(1.0, current / requiredHours);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        double current = context.tracker().getCareerHoursInLastDays(periodDays);
        return String.format("Code (30d): %.1f/%.1f h", current, requiredHours);
    }

    @Override
    public String getDescription() {
        return String.format("Registrar %.0f horas de código en los últimos %d días", requiredHours, periodDays);
    }

    @Override
    public ConditionType getType() { return ConditionType.CAREER_ENGINE_HOURS; }
}