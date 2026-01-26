package com.lifeleveling.domain.quest.condition;

/**
 * ConsumableAbstinence: No comprar consumible durante periodo.
 */
public final class ConsumableAbstinence implements GateCondition {
    private final String consumableId;
    private final int days;

    public ConsumableAbstinence(String consumableId, int days) {
        this.consumableId = consumableId;
        this.days = days;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return !context.tracker().hasConsumablePurchaseInLastDays(consumableId, days);
    }

    @Override
    public double getProgress(ConditionContext context) {
        return isMet(context) ? 1.0 : 0.0;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        boolean met = isMet(context);
        return String.format("Sin %s (%d días): %s", consumableId, days, met ? "✓" : "✗");
    }

    @Override
    public String getDescription() {
        return String.format("No comprar %s durante %d días", consumableId, days);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.CONSUMABLE_ABSTINENCE;
    }
}