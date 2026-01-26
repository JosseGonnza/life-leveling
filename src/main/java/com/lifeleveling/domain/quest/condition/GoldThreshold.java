package com.lifeleveling.domain.quest.condition;

/**
 * GoldThreshold: Oro mínimo en wallet.
 */
public final class GoldThreshold implements GateCondition {
    private final int requiredGold;

    public GoldThreshold(int requiredGold) {
        this.requiredGold = requiredGold;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.getCurrentGold() >= requiredGold;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.getCurrentGold();
        return Math.min(1.0, (double) current / requiredGold);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        return String.format("%,d/%,d G", context.getCurrentGold(), requiredGold);
    }

    @Override
    public String getDescription() {
        return String.format("Acumular %,d Gold", requiredGold);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.GOLD_THRESHOLD;
    }
}