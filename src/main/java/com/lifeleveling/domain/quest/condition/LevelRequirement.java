package com.lifeleveling.domain.quest.condition;

/**
 * LevelRequirement: Condición que requiere un nivel mínimo del jugador.
 *
 * Ejemplo:
 *   - Gate 1: Nivel 10+
 *   - Gate 2: Nivel 25+
 *   - Gate 7: Nivel 60+
 *
 * Esta condición es:
 *   - Automática (se valida sin acción del jugador)
 *   - Sin contador visible (es binario: sí o no)
 *   - No se puede resetear
 */
public record LevelRequirement(int requiredLevel) implements GateCondition {

    public LevelRequirement {
        if (requiredLevel < 1 || requiredLevel > 100) {
            throw new IllegalArgumentException(
                    String.format("Nivel requerido inválido: %d. Debe estar entre 1 y 100", requiredLevel)
            );
        }
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return context.getPlayerLevel() >= requiredLevel;
    }

    @Override
    public double getProgress(ConditionContext context) {
        int currentLevel = context.getPlayerLevel();

        if (currentLevel >= requiredLevel) {
            return 1.0;
        }

        // Progreso basado en nivel actual
        // Si requiere nivel 25 y estás en 10, progreso = 10/25 = 40%
        return (double) currentLevel / requiredLevel;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        int currentLevel = context.getPlayerLevel();

        if (currentLevel >= requiredLevel) {
            return String.format("Nivel %d ✓", requiredLevel);
        }

        return String.format("Nivel %d/%d", currentLevel, requiredLevel);
    }

    @Override
    public String getDescription() {
        return String.format("Alcanzar Nivel %d", requiredLevel);
    }

    @Override
    public boolean hasVisibleCounter() {
        // Solo mostramos si NO está cumplida
        return true;
    }

    @Override
    public ConditionType getType() {
        return ConditionType.LEVEL_REQUIREMENT;
    }
}