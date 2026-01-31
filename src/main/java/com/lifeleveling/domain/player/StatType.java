package com.lifeleveling.domain.player;

import java.util.Arrays;
import java.util.Optional;

public enum StatType {

    /*
     * FUERZA: Representa la salud física y el cuidado del cuerpo.
     * Título de Maestría (Lvl 50): "Titán" (+5% STR XP)
     */
    STRENGTH(
            "💪",
            "Fuerza",
            "Salud física, fitness y nutrición"
    ),

    /*
     * INTELIGENCIA: Representa capacidad cognitiva y habilidades técnicas.
     * Título de Maestría (Lvl 50): "Cyborg" (+5% INT XP)
     */
    INTELLECT(
            "🧠",
            "Inteligencia",
            "Programación, lógica y resolución de problemas"
    ),

    /*
     * SABIDURÍA: Representa gestión de la vida adulta y conocimiento general.
     * Título de Maestría (Lvl 50): "Oráculo" (+5% WIS XP)
     */
    WISDOM(
            "🦉",
            "Sabiduría",
            "Adulting, finanzas, lectura y organización"
    ),

    /*
     * DISCIPLINA: Representa fuerza de voluntad y consistencia.
     * Título de Maestría (Lvl 50): "General" (+5% DIS XP)
     */
    DISCIPLINE(
            "🛡️",
            "Disciplina",
            "Fuerza de voluntad, consistencia y hábitos"
    ),

    /*
     * CARISMA: Representa habilidades sociales y cuidado personal.
     * Título de Maestría (Lvl 50): "Estrella" (+5% CHA XP)
     */
    CHARISMA(
            "🗣️",
            "Carisma",
            "Habilidades sociales, cuidado personal y relaciones"
    );

    private final String icon;
    private final String displayName;
    private final String description;

    // [CAMBIO CLAVE] Dificultad "Monarca" (30.0) en lugar de multiplicador simple (10)
    // Esto hace que los niveles altos cuesten mucho más esfuerzo.
    private static final double LEVELING_DIFFICULTY = 7.7;

    // Total XP para completar el juego (Nivel 100) con la nueva fórmula: 30 * 100^2
    public static final int MAX_TOTAL_XP = 77_000;

    StatType(String icon, String displayName, String description) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }

    public static Optional<StatType> fromString(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del stat no puede ser null o vacío");
        }

        String normalizedName = name.trim().toUpperCase();
        try {
            return Optional.of(StatType.valueOf(normalizedName));
        } catch (IllegalArgumentException e) {
            return Arrays.stream(StatType.values())
                    .filter(stat -> stat.displayName.equalsIgnoreCase(name.trim()))
                    .findFirst();
        }
    }

    /**
     * Calcula la XP necesaria para completar el nivel actual y pasar al siguiente.
     * Fórmula Delta: XP_Total(Nivel+1) - XP_Total(Nivel)
     */
    public static int getXPRequiredForNextLevel(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= 100) {
            // En nivel 100 permitimos "llenar la barra" una última vez para efectos visuales (Bonus Mastery)
            if (currentLevel == 100) return 5000;

            throw new IllegalArgumentException(
                    String.format("Nivel inválido: %d. Debe estar entre 1 y 100.", currentLevel)
            );
        }

        // Calculamos cuánto cuesta saltar al siguiente escalón
        int xpCurrent = getTotalXPForLevel(currentLevel);
        int xpNext = getTotalXPForLevel(currentLevel + 1);

        return xpNext - xpCurrent;
    }

    /**
     * Calcula la XP Total acumulada necesaria para ALCANZAR un nivel (empezando desde 0).
     * Fórmula Cuadrática: DIFICULTAD * Nivel^2
     */
    public static int getTotalXPForLevel(int targetLevel) {
        if (targetLevel < 1 || targetLevel > 100) {
            throw new IllegalArgumentException(
                    String.format("Nivel inválido: %d. Debe estar entre 1 y 100.", targetLevel)
            );
        }

        if (targetLevel == 1) return 0;

        return (int) (LEVELING_DIFFICULTY * Math.pow(targetLevel, 2));
    }

    public static int getTotalXPToMaster() {
        return MAX_TOTAL_XP;
    }

    public boolean hasMasteryLevel(int currentLevel) {
        return currentLevel >= 50;
    }

    public String getMasteryTitle() {
        return switch (this) {
            case STRENGTH -> "Titán";
            case INTELLECT -> "Cyborg";
            case WISDOM -> "Oráculo";
            case DISCIPLINE -> "General";
            case CHARISMA -> "Estrella";
        };
    }

    public String toDisplayString() {
        return icon + " " + displayName;
    }

    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}