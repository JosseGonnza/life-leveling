package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.quest.system.SystemQuestType;

/**
 * PreviousGateRequired: Condición que requiere que una gate anterior esté completada.
 *
 * Ejemplo:
 *   - Gate 2 requiere Gate 1 completada
 *   - Gate 3 requiere Gate 2 completada
 *   - Gate 4 requiere Gate 3 completada
 *
 * Esta condición es:
 *   - Automática (se valida sin acción del jugador)
 *   - Sin contador visible (es binario: completada o no)
 *   - No se puede resetear
 */
public record PreviousGateRequired(SystemQuestType previousGate) implements GateCondition {

    public PreviousGateRequired {
        if (previousGate == null) {
            throw new IllegalArgumentException("La gate anterior no puede ser null");
        }
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.tracker().isGateCompleted(previousGate);
    }

    @Override
    public double getProgress(ConditionContext context) {
        return isMet(context) ? 1.0 : 0.0;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        if (isMet(context)) {
            return previousGate.getName() + " ✓";
        }

        return previousGate.getName() + " (Pendiente)";
    }

    @Override
    public String getDescription() {
        return String.format("Completar %s", previousGate.getName());
    }

    @Override
    public boolean hasVisibleCounter() {
        return false; // No necesita contador, solo muestra "✓" o "Pendiente"
    }

    @Override
    public ConditionType getType() {
        return ConditionType.PREVIOUS_GATE;
    }
}