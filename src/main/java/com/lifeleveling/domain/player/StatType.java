package com.lifeleveling.domain.player;

/*
 * Mecánicas de Progresión:
 *   - Cada stat tiene su propio nivel (1-100)
 *   - Coste por nivel: Nivel_Actual × 100 XP
 *   - XP Total para maxear un stat: 495,000 XP
 *   - Al llegar a nivel 50: Se desbloquea Título de Maestría
*/

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

    // Multiplicador base
    private static final int XP_MULTIPLIER = 10;

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
        // Primero intentamos match exacto con el enum name
        try {
            return Optional.of(StatType.valueOf(normalizedName));
        } catch (IllegalArgumentException e) {
            // Si falla, buscamos por displayName
            return Arrays.stream(StatType.values())
                    .filter(stat -> stat.displayName.equalsIgnoreCase(name.trim()))
                    .findFirst();
        }
    }

    public static int getXPRequiredForNextLevel(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= 100) {
            throw new IllegalArgumentException(
                    String.format("Nivel inválido: %d. Debe estar entre 1 y 99.", currentLevel)
            );
        }
        return currentLevel * XP_MULTIPLIER;
    }

    public static int getTotalXPForLevel(int targetLevel) {
        if (targetLevel < 1 || targetLevel > 100) {
            throw new IllegalArgumentException(
                    String.format("Nivel inválido: %d. Debe estar entre 1 y 100.", targetLevel)
            );
        }

        if (targetLevel == 1) {
            return 0; // Nivel 1 no requiere XP
        }
        // Fórmula de suma de serie aritmética: n(n-1)/2 × 100
        return (targetLevel * (targetLevel - 1) * XP_MULTIPLIER) / 2;
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

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
