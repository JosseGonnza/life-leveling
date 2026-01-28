package com.lifeleveling.domain.quest.condition;

/**
 * WeeklyQuestsCompleted: Semanas consecutivas con 100% Weekly Quests completadas.
 *
 * Utilizada por:
 *   - ELDER_6 (La Cruzada): 4 semanas consecutivas con 3/3 Weekly Quests
 *
 * Comportamiento:
 *   - Cada semana se evalúa si se completaron las 3 Weekly Quests activas
 *   - Si fallas una semana, el contador vuelve a 0
 *   - Se evalúa al cierre de cada semana (Domingo 23:59 o Lunes 00:00)
 */
public final class WeeklyQuestsCompleted implements GateCondition {

    private final int requiredWeeks;

    /**
     * Crea una condición de semanas consecutivas con Weekly Quests completas.
     *
     * @param requiredWeeks Número de semanas consecutivas requeridas con 100% Weekly Quests
     */
    public WeeklyQuestsCompleted(int requiredWeeks) {
        if (requiredWeeks <= 0) {
            throw new IllegalArgumentException("requiredWeeks debe ser > 0");
        }
        this.requiredWeeks = requiredWeeks;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getConsecutiveWeeksWithFullWeeklies() >= requiredWeeks;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int currentWeeks = context.tracker().getConsecutiveWeeksWithFullWeeklies();
        return Math.min(1.0, (double) currentWeeks / requiredWeeks);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int currentWeeks = context.tracker().getConsecutiveWeeksWithFullWeeklies();
        return String.format("%d/%d semanas con 100%% Weeklies", currentWeeks, requiredWeeks);
    }

    @Override
    public String getDescription() {
        return String.format("Completar 100%% de Weekly Quests durante %d semanas consecutivas", requiredWeeks);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.WEEKLY_QUESTS_COMPLETED;
    }

    @Override
    public boolean canReset() {
        return true; // Se resetea si fallas una semana
    }

    public int getRequiredWeeks() {
        return requiredWeeks;
    }
}
