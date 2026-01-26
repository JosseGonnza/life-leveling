package com.lifeleveling.domain.quest.condition;

/**
 * CareerEngineHours: Horas acumuladas de código.
 */
public final class CareerEngineHours implements GateCondition {
    private final double requiredHours;

    public CareerEngineHours(double requiredHours) {
        this.requiredHours = requiredHours;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getTotalCareerEngineHours() >= requiredHours;
    }

    @Override
    public double getProgress(ConditionContext context) {
        double current = context.tracker().getTotalCareerEngineHours();
        return Math.min(1.0, current / requiredHours);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        double current = context.tracker().getTotalCareerEngineHours();
        return String.format("Código: %.1f/%.1f horas", current, requiredHours);
    }

    @Override
    public String getDescription() {
        return String.format("Acumular %.0f horas de código", requiredHours);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.CAREER_ENGINE_HOURS;
    }
}
