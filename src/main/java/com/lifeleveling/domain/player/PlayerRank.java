package com.lifeleveling.domain.player;

import com.lifeleveling.domain.quest.shared.QuestRank;

/**
 * Rango Profesional del Jugador.
 * Define el estatus social y el multiplicador de ingresos.
 *
 * Referencia Biblia: Cap 3.1 (Ingeniería Financiera) & Cap 4.3 (Clases).
 */
public enum PlayerRank {

    E("Novato", "🌱", 1.0),
    D("Iniciado", "🐣", 1.0),
    C("Junior", "🔨", 1.5),
    C_PLUS("Junior Adv.", "⚙️", 2.0),
    B("Mid-Level", "🔧", 2.5),
    A("Senior", "🎩", 4.0),
    S("Architect", "🏗️", 8.0);

    private final String displayName;
    private final String icon;
    private final double goldMultiplier;

    PlayerRank(String displayName, String icon, double goldMultiplier) {
        this.displayName = displayName;
        this.icon = icon;
        this.goldMultiplier = goldMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public double getGoldMultiplier() { return goldMultiplier; }

    /** Letra del rango para la UI (E, D, C, C+, B, A, S). */
    public String getLetter() {
        return this == C_PLUS ? "C+" : name();
    }

    public boolean isAtLeast(PlayerRank other) {
        return this.ordinal() >= other.ordinal();
    }

    /**
     * Convierte un Rango de Quest (Dificultad) en un Rango de Jugador (Estatus).
     * Se usa al completar Gates para determinar el nuevo rango del jugador.
     */
    public static PlayerRank fromQuestRank(QuestRank questRank) {
        if (questRank == null) return E;
        try {
            // Mapeo directo por nombre (E -> E, S_PLUS -> S_PLUS)
            return PlayerRank.valueOf(questRank.name());
        } catch (IllegalArgumentException e) {
            // Fallback por seguridad
            return E;
        }
    }
}