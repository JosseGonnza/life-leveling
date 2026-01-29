package com.lifeleveling.domain.player;

/**
 * Rango Profesional del Jugador.
 * Define el estatus social y el multiplicador de ingresos.
 *
 * Referencia Biblia: Cap 3.1 (Ingeniería Financiera) & Cap 4.3 (Clases).
 */
public enum PlayerRank {

    /*
     * Rango E: Novato / Becario
     * Salario Base: 1.0x
     * Gate: Inicio
     */
    E("Novato", "🌱", 1.0),

    /*
     * Rango D: Iniciado
     * Salario Base: 1.0x
     * Gate: Gate 1
     */
    D("Iniciado", "🐣", 1.0),

    /*
     * Rango C: Junior
     * Salario Base: 1.5x
     * Gate: Gate 2
     */
    C("Junior", "🔨", 1.5),

    /*
     * Rango B: Mid-Level
     * Salario Base: 2.5x
     * Gate: Gate 4
     */
    B("Mid-Level", "🔧", 2.5),

    /*
     * Rango A: Senior
     * Salario Base: 4.0x
     * Gate: Gate 7
     */
    A("Senior", "🎩", 4.0),

    /*
     * Rango S: Architect
     * Salario Base: 8.0x (Libertad Financiera)
     * Gate: Gate 8
     */
    S("Architect", "🏗️", 8.0),

    /*
     * Rango S+: Monarca (Endgame)
     * Salario Base: 8.0x (Tope económico)
     * Gate: Gate 10
     */
    S_PLUS("Monarca", "👑", 8.0),

    /*
     * Rango S++: Trascendente (New Game+)
     * Salario Base: 8.0x
     */
    S_PLUS_PLUS("Dios", "🌟", 8.0);

    private final String displayName;
    private final String icon;
    private final double goldMultiplier;

    PlayerRank(String displayName, String icon, double goldMultiplier) {
        this.displayName = displayName;
        this.icon = icon;
        this.goldMultiplier = goldMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public double getGoldMultiplier() {
        return goldMultiplier;
    }

    /**
     * Verifica si este rango es superior o igual a otro.
     * Útil para desbloquear contenido (ej: Misiones Rango B).
     */
    public boolean isAtLeast(PlayerRank other) {
        return this.ordinal() >= other.ordinal();
    }
}