package com.lifeleveling.domain.quest.condition;

/**
 * HPThreshold: HP mínimo durante periodo.
 */
public record HPThreshold(int minimumHP, int days) implements GateCondition {

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getMinHPInLastDays(days) >= minimumHP;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int minHP = context.tracker().getMinHPInLastDays(days);
        return minHP >= minimumHP ? 1.0 : (double) minHP / minimumHP;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int minHP = context.tracker().getMinHPInLastDays(days);
        return String.format("HP Mínimo: %d/%d %s", minHP, minimumHP,
                minHP >= minimumHP ? "✓" : "");
    }

    @Override
    public String getDescription() {
        return String.format("Mantener HP ≥ %d durante %d días", minimumHP, days);
    }

    @Override
    public boolean canReset() {
        return true;
    }

    @Override
    public ConditionType getType() {
        return ConditionType.HP_THRESHOLD;
    }
}