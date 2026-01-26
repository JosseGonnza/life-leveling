package com.lifeleveling.domain.quest.condition;

/**
 * PerfectDayStreak: Condición que requiere X días perfectos (7/7 hábitos) consecutivos.
 *
 * Usado en: Gate 1 (7 días perfectos)
 *
 * Características:
 *   - Automática
 *   - Con contador visible (6/7 días)
 *   - Se puede resetear (si fallas un día, vuelve a 0)
 */
public record PerfectDayStreak(int requiredDays) implements GateCondition {

    public PerfectDayStreak {
        if (requiredDays < 1) {
            throw new IllegalArgumentException("Días requeridos debe ser > 0");
        }
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getPerfectDayStreak() >= requiredDays;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getPerfectDayStreak();
        return Math.min(1.0, (double) current / requiredDays);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getPerfectDayStreak();

        if (current >= requiredDays) {
            return String.format("%d/%d días ✓", requiredDays, requiredDays);
        }

        return String.format("%d/%d días", current, requiredDays);
    }

    @Override
    public String getDescription() {
        return String.format("Completar %d días perfectos consecutivos (7/7 hábitos)", requiredDays);
    }

    @Override
    public boolean canReset() {
        return true; // Se resetea si fallas un día
    }

    @Override
    public ConditionType getType() {
        return ConditionType.PERFECT_DAY_STREAK;
    }
}