package com.lifeleveling.domain.quest.condition;

/**
 * ManualConfirmation: Usuario debe marcar checkbox.
 */
public final class ManualConfirmation implements GateCondition {
    private final String confirmationId;
    private final String label;

    public ManualConfirmation(String confirmationId, String label) {
        this.confirmationId = confirmationId;
        this.label = label;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().hasManualConfirmation(confirmationId);
    }

    @Override
    public double getProgress(ConditionContext context) {
        return isMet(context) ? 1.0 : 0.0;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        return isMet(context) ? "✓ " + label : "☐ " + label;
    }

    @Override
    public String getDescription() {
        return label;
    }

    @Override
    public boolean isAutomatic() {
        return false; // Requiere acción del jugador
    }

    @Override
    public boolean hasVisibleCounter() {
        return false; // Solo checkbox
    }

    @Override
    public ConditionType getType() {
        return ConditionType.MANUAL_CONFIRMATION;
    }
}
