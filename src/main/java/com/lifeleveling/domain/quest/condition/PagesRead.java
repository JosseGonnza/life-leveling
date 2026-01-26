package com.lifeleveling.domain.quest.condition;

/**
 * PagesRead: Páginas leídas en periodo.
 */
public final class PagesRead implements GateCondition {
    private final int requiredPages;
    private final int days;

    public PagesRead(int requiredPages, int days) {
        this.requiredPages = requiredPages;
        this.days = days;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getPagesReadInLastDays(days) >= requiredPages;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getPagesReadInLastDays(days);
        return Math.min(1.0, (double) current / requiredPages);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getPagesReadInLastDays(days);
        return String.format("Páginas (%d días): %d/%d", days, current, requiredPages);
    }

    @Override
    public String getDescription() {
        return String.format("Leer %d páginas en %d días", requiredPages, days);
    }

    @Override
    public boolean canReset() {
        return true; // Rolling window
    }

    @Override
    public ConditionType getType() {
        return ConditionType.PAGES_READ;
    }
}