package com.lifeleveling.domain.quest.condition;

/**
 * TimeLimit: Límite de tiempo para completar (se reinicia).
 */
public final class TimeLimit implements GateCondition {
    private final int days;

    public TimeLimit(int days) {
        this.days = days;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        // Esta condición solo verifica si aún hay tiempo
        // La completación real depende de otras condiciones
        var startDate = context.tracker().getTimeLimitStartDate(context.getGateId());
        if (startDate == null) return false;

        var daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, context.currentDate());
        return daysSinceStart < days;
    }

    @Override
    public double getProgress(ConditionContext context) {
        var startDate = context.tracker().getTimeLimitStartDate(context.getGateId());
        if (startDate == null) return 0.0;

        var daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, context.currentDate());
        return Math.min(1.0, (double) daysSinceStart / days);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        var startDate = context.tracker().getTimeLimitStartDate(context.getGateId());
        if (startDate == null) return "No iniciado";

        var daysSinceStart = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, context.currentDate());
        int daysLeft = days - daysSinceStart;

        if (daysLeft > 0) {
            return String.format("Día %d/%d (%d días restantes)", daysSinceStart + 1, days, daysLeft);
        } else {
            return "Tiempo agotado (se reiniciará)";
        }
    }

    @Override
    public String getDescription() {
        return String.format("Completar en %d días (se reinicia si fallas)", days);
    }

    @Override
    public boolean canReset() {
        return true;
    }

    @Override
    public ConditionType getType() {
        return ConditionType.TIME_LIMIT;
    }
}