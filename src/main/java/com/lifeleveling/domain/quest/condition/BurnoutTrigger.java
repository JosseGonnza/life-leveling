package com.lifeleveling.domain.quest.condition;

/**
 * BurnoutTrigger: Evento de 3 burnouts en mes.
 */
public final class BurnoutTrigger implements GateCondition {
    private final int requiredBurnouts;

    public BurnoutTrigger(int requiredBurnouts) {
        this.requiredBurnouts = requiredBurnouts;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getBurnoutsInLastMonth() >= requiredBurnouts;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getBurnoutsInLastMonth();
        return Math.min(1.0, (double) current / requiredBurnouts);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getBurnoutsInLastMonth();
        return String.format("Burnouts (último mes): %d/%d", current, requiredBurnouts);
    }

    @Override
    public String getDescription() {
        return String.format("Trigger: %d burnouts en mes", requiredBurnouts);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.BURNOUT_TRIGGER;
    }
}