package com.lifeleveling.domain.player;

/*
 * Mecánicas de Progresión:
 * - Nivel: 1 (inicio) a 100 (máximo)
 * - XP por Nivel: Nivel_Actual × 100 (ej: Nivel 10→11 requiere 1,000 XP)
 * - XP Total para Maxear: 495,000 XP (suma de 100+200+...+9,900)
 * - Maestría: Al nivel 50 se desbloquea el Título de Maestría
 */
public record Stat(StatType type, int currentLevel, int currentXP) {

    public static final int INITIAL_LEVEL = 1;
    public static final int MAX_LEVEL = 100;
    public static final int MASTERY_LEVEL = 50;

    public Stat {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }
        if (currentLevel < INITIAL_LEVEL || currentLevel > MAX_LEVEL) {
            throw new IllegalArgumentException(
                    String.format("Nivel inválido: %d. Debe estar entre %d y %d",
                            currentLevel, INITIAL_LEVEL, MAX_LEVEL)
            );
        }
        if (currentXP < 0) {
            throw new IllegalArgumentException(
                    String.format("XP no puede ser negativa: %d", currentXP)
            );
        }
        // Si está al nivel máximo, la XP debe ser 0 (no puede seguir subiendo)
        if (currentLevel == MAX_LEVEL && currentXP > 0) {
            throw new IllegalArgumentException(
                    String.format("Un stat en nivel máximo (%d) no puede tener XP acumulada", MAX_LEVEL)
            );
        }
    }

    public static Stat initial(StatType type) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }
        return new Stat(type, INITIAL_LEVEL, 0);
    }

    public static Stat atLevel(StatType type, int level) {
        return new Stat(type, level, 0);
    }

    public static Stat maxed(StatType type) {
        return new Stat(type, MAX_LEVEL, 0);
    }

    public boolean isInitial() {
        return currentLevel == INITIAL_LEVEL;
    }

    public boolean isMaxed() {
        return currentLevel == MAX_LEVEL;
    }

    public boolean hasMastery() {
        return currentLevel >= MASTERY_LEVEL;
    }

    public boolean canLevelUp() {
        if (isMaxed()) {
            return false;
        }
        return currentXP >= getXPRequiredForNextLevel();
    }

    public int getXPRequiredForNextLevel() {
        if (isMaxed()) {
            return 0;
        }
        return StatType.getXPRequiredForNextLevel(currentLevel);
    }

    public double getProgressPercentage() {
        if (isMaxed()) {
            return 100.0;
        }
        int xpNeeded = getXPRequiredForNextLevel();
        if (xpNeeded == 0) {
            return 100.0;
        }
        return (currentXP * 100.0) / xpNeeded;
    }

    /**
     * Calcula la XP total histórica acumulada (Niveles pasados + XP actual).
     * Retorna long para evitar desbordamiento en cálculos agregados.
     */
    public long getTotalAccumulatedXP() {
        // Obtenemos la XP necesaria para llegar al nivel actual (la suma de todos los niveles anteriores)
        long xpFromPreviousLevels = StatType.getTotalXPForLevel(currentLevel);
        return xpFromPreviousLevels + currentXP;
    }

    public String getMasteryTitle() {
        return type.getMasteryTitle();
    }

    public Stat addXP(int xpGained) {
        if (xpGained < 0) {
            throw new IllegalArgumentException(
                    String.format("No se puede añadir XP negativa: %d", xpGained)
            );
        }
        if (isMaxed()) {
            return this;
        }

        // Añadimos la XP y procesamos level-ups recursivos
        int newXP = this.currentXP + xpGained;
        int newLevel = this.currentLevel;

        // Procesamos level-ups mientras sea posible
        while (newLevel < MAX_LEVEL) {
            int xpNeeded = StatType.getXPRequiredForNextLevel(newLevel);
            if (newXP >= xpNeeded) {
                newXP -= xpNeeded;
                newLevel++;
            } else {
                break;
            }
        }

        // Si llegamos al nivel máximo, la XP sobrante se descarta
        if (newLevel == MAX_LEVEL) {
            newXP = 0;
        }

        return new Stat(type, newLevel, newXP);
    }

    public Stat forceLevel(int levels) {
        if (levels < 0) {
            throw new IllegalArgumentException("No se pueden subir niveles negativos");
        }
        int newLevel = Math.min(currentLevel + levels, MAX_LEVEL);
        int newXP = 0;

        return new Stat(type, newLevel, newXP);
    }

    public Stat reset() {
        return initial(type);
    }

    // ========================================================================================
    // MÉTODOS DE FORMATEO (UI)
    // ========================================================================================

    // Ejemplo: "💪 Fuerza Lvl 23 (1,450/2,300 XP) [63%]"
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toDisplayString());
        sb.append(" Lvl ").append(currentLevel);

        if (isMaxed()) {
            sb.append(" [MAX]");
        } else {
            sb.append(String.format(" (%,d/%,d XP)", currentXP, getXPRequiredForNextLevel()));
            sb.append(String.format(" [%.0f%%]", getProgressPercentage()));

            if (hasMastery()) {
                sb.append(" 👑");
            }
        }

        return sb.toString();
    }

    // Ejemplo: "💪 Lvl 23"
    public String toCompactString() {
        return String.format("%s Lvl %d", type.getIcon(), currentLevel);
    }

    @Override
    public String toString() {
        return String.format("Stat[type=%s, level=%d, xp=%d/%d, totalXP=%d]",
                type.name(), currentLevel, currentXP, getXPRequiredForNextLevel(),
                getTotalAccumulatedXP());
    }
}