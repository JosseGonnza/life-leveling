package com.lifeleveling.domain.quest.condition;

/**
 * FinancialDiscipline: Verifica que el jugador haya mantenido la disciplina financiera.
 * Usa el contador 'consecutiveDaysAbove20k' del GateTracker.
 */
public record FinancialDiscipline(int requiredDays) implements GateCondition {

    public FinancialDiscipline {
        if (requiredDays <= 0) throw new IllegalArgumentException("Días deben ser > 0");
    }

    @Override
    public boolean isMet(ConditionContext context) {
        // Aquí conectamos con tu Fase 4.2
        return context.tracker().getConsecutiveDaysAbove20k() >= requiredDays;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int current = context.tracker().getConsecutiveDaysAbove20k();
        return Math.min(1.0, (double) current / requiredDays);
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int current = context.tracker().getConsecutiveDaysAbove20k();
        // Feedback visual tipo: "Racha: 3/7 días"
        return String.format("Racha >20k G: %d/%d días", current, requiredDays);
    }

    @Override
    public String getDescription() {
        return String.format("Mantener >20k G sin lujos durante %d días seguidos", requiredDays);
    }

    @Override
    public boolean canReset() {
        return true; // Si fallas un día (gastas o bajas saldo), el tracker se pone a 0
    }

    @Override
    public ConditionType getType() {
        // Puedes reutilizar GOLD_THRESHOLD o crear uno nuevo WEALTH_MAINTENANCE en tu Enum ConditionType
        return ConditionType.GOLD_THRESHOLD;
    }
}