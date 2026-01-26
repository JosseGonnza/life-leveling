package com.lifeleveling.domain.quest.condition;

/**
 * NoBurnout: Sin entrar en burnout durante periodo activo.
 */
public final class NoBurnout implements GateCondition {

    @Override
    public boolean isMet(ConditionContext context) {
        String gateId = context.getGateId();
        return !context.tracker().hadBurnoutDuringGate(gateId);
    }

    @Override
    public double getProgress(ConditionContext context) {
        return isMet(context) ? 1.0 : 0.0;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        boolean met = isMet(context);
        return met ? "⚠️ Sin BURNOUT ✓" : "⚠️ BURNOUT DETECTADO ✗";
    }

    @Override
    public String getDescription() {
        return "No entrar en BURNOUT durante el proceso";
    }

    @Override
    public ConditionType getType() {
        return ConditionType.NO_BURNOUT;
    }
}