package com.lifeleveling.domain.quest.condition;

/**
 * DebuffFreeStreak: Mantener 0 debuffs activos durante N días consecutivos.
 *
 * Utilizada por:
 *   - ELDER_5 (El Intocable): 30 días sin debuffs
 *
 * Debuffs trackeable:
 *   - FATIGA (SLEEP < 6h)
 *   - CAOS (3 días sin TIDY)
 *   - PESADEZ (Hamburguesa)
 *   - TAQUICARDIA (3+ Monsters/semana)
 *   - ABURRIMIENTO (7 días trabajo sin descanso)
 *
 * Comportamiento:
 *   - Si recibes cualquier debuff, el contador vuelve a 0
 *   - Se evalúa al final de cada día
 */
public final class DebuffFreeStreak implements GateCondition {

    private final int requiredDays;

    /**
     * Crea una condición de racha sin debuffs.
     *
     * @param requiredDays Número de días consecutivos sin debuffs requeridos
     */
    public DebuffFreeStreak(int requiredDays) {
        if (requiredDays <= 0) {
            throw new IllegalArgumentException("requiredDays debe ser > 0");
        }
        this.requiredDays = requiredDays;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().getDebuffFreeStreak() >= requiredDays;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int currentStreak = context.tracker().getDebuffFreeStreak();
        return Math.min(1.0, (double) currentStreak / requiredDays);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int currentStreak = context.tracker().getDebuffFreeStreak();
        return String.format("%d/%d días sin debuffs", currentStreak, requiredDays);
    }

    @Override
    public String getDescription() {
        return String.format("Mantener 0 debuffs activos durante %d días consecutivos", requiredDays);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.DEBUFF_FREE_STREAK;
    }

    @Override
    public boolean canReset() {
        return true; // Se resetea si recibes un debuff
    }

    public int getRequiredDays() {
        return requiredDays;
    }
}
