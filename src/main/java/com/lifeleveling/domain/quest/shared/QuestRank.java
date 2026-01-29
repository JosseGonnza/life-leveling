package com.lifeleveling.domain.quest.shared;

import com.lifeleveling.domain.player.HPState;

import java.util.Arrays;
import java.util.Optional;

/*
 * Representa los ocho rangos de dificultad de las misiones
 * El sistema de rangos determina:
 * Recompensas base (XP y Gold)
 * Tiempo estimado de completación
 * Penalización por fallo (daño moral en User Quests)
 * Requisitos de HP para iniciar la misión
 */

public enum QuestRank {

    E(
            "🟢", "Rutinaria", "5-15 min",
            10, 15, 0, null
    ),
    D(
            "🔵", "Común", "30-60 min",
            50, 50, 5, null
    ),
    C(
            "🟣", "Rara", "1-2 horas",
            150, 150, 15, HPState.TIRED
    ),
    B(
            "🟠", "Élite", "3-5 horas",
            500, 400, 20, HPState.HEALTHY
    ),
    A(
            "🔴", "Heroica", "Días/Hitos",
            1_500, 2_000, 30, HPState.HEALTHY
    ),
    S(
            "🟡", "Legendaria", "Meses",
            5_000, 10_000, 50, HPState.HEALTHY
    ),
    S_PLUS(
            "⭐", "Mítica", "1-2 años",
            20_000, 50_000, 50, HPState.HEALTHY
    ),
    S_PLUS_PLUS(
            "🌌", "Divina", "Endgame",
            100_000, 500_000, 50, HPState.HEALTHY
    );

    private final String icon;
    private final String difficultyName;
    private final String estimatedTime;
    private final int baseXP;
    private final int baseGold;
    private final int moralDamage;     // Daño HP al fallar
    private final HPState minHPState;  // null = sin restricción

    QuestRank(String icon, String difficultyName, String estimatedTime,
              int baseXP, int baseGold, int moralDamage, HPState minHPState) {
        this.icon = icon;
        this.difficultyName = difficultyName;
        this.estimatedTime = estimatedTime;
        this.baseXP = baseXP;
        this.baseGold = baseGold;
        this.moralDamage = moralDamage;
        this.minHPState = minHPState;
    }

    public static Optional<QuestRank> fromString(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        try {
            return Optional.of(QuestRank.valueOf(name.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Arrays.stream(QuestRank.values())
                    .filter(rank -> rank.difficultyName.equalsIgnoreCase(name.trim()))
                    .findFirst();
        }
    }

    /**
     * Verifica si un jugador en cierto estado de salud puede iniciar una quest de este rango.
     * @param currentHPState Estado actual del jugador
     * @return true si cumple el requisito
     */
    public boolean canStartWith(HPState currentHPState) {
        if (currentHPState == null) {
            throw new IllegalArgumentException("El estado de HP no puede ser null");
        }
        if (minHPState == null) {
            return true; // Sin requisitos
        }
        // Comparación de enum: HEALTHY > TIRED > CRITICAL
        return currentHPState.compareTo(minHPState) >= 0;
    }

    public boolean isHighRank() {
        return this.ordinal() >= B.ordinal();
    }

    public int getGoldDamageOnBurnout() {
        return moralDamage * 10;
    }

    // Getters y Display
    public String toDisplayString() {
        String enumName = this.name().replace("_PLUS_PLUS", "++").replace("_PLUS", "+");
        return String.format("%s %s (Rango %s)", icon, difficultyName, enumName);
    }

    public String getIcon() { return icon; }
    public String getDifficultyName() { return difficultyName; }
    public int getBaseXP() { return baseXP; }
    public int getBaseGold() { return baseGold; }
    public int getMoralDamage() { return moralDamage; }
    public Optional<HPState> getMinHPState() { return Optional.ofNullable(minHPState); }
}