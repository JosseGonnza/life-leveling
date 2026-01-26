package com.lifeleveling.domain.quest;

/*
 * Características:
 *   - Secuenciales: Solo visible si cumples nivel Y gate anterior
 *   - Únicas: Solo se pueden completar una vez
 *   - Épicas: Recompensas masivas y desbloqueos importantes
 *   - Narrativa: Cada una cuenta una historia de progresión
 */

public enum SystemQuestType {

    /**
     * 🟢 GATE 1: E → D - "La Semana del Infierno"
     * <p>
     * La primera prueba de disciplina real.
     * Demuestra que puedes mantener la consistencia.
     * <p>
     * Requisitos:
     * - Nivel 10
     * - Completar TODAS las Daily Quests (7/7) durante 7 días consecutivos
     * - HP nunca debe bajar de 50 (Mantenerse HEALTHY)
     * <p>
     * Recompensa:
     * - 🔓 Rango D desbloqueado
     * - +500 XP General
     * - +1,000 Gold
     * - Título: "Disciplinado" (+5% DIS XP)
     */
    GATE_E_TO_D(
            "🟢",
            "La Semana del Infierno",
            "Completar 7/7 Daily Quests durante 7 días sin bajar de HP 50",
            10,        // Nivel requerido
            null,      // Sin gate anterior (es la primera)
            QuestRank.D,
            500,       // XP reward
            1_000      // Gold reward
    ),

    /**
     * 🔵 GATE 2: D → C - "El Primer Encargo"
     * <p>
     * Deja de solo "estar", empieza a "hacer".
     * Las tareas rutinarias ya no son suficientes.
     * <p>
     * Requisitos:
     * - Nivel 25
     * - Gate E→D completada
     * - Completar 3 User Quests de Rango D+ en una semana
     * - Acumular 20 horas de código (DQ_CODE) totales
     * <p>
     * Recompensa:
     * - 🔓 Rango C desbloqueado
     * - +1,500 XP General
     * - +2,000 Gold
     * - Item: "Beca de Estudios" (Descuento 10% tienda por 24h)
     */
    GATE_D_TO_C(
            "🔵",
            "El Primer Encargo",
            "Ya tienes hábitos. Ahora úsalos para construir algo.",
            25,
            GATE_E_TO_D,  // Requiere completar gate anterior
            QuestRank.C,
            1_500,
            2_000
    ),

    /**
     * 🟣 GATE 3: C → B (Fase 1) - "La Biblioteca de Babel"
     * <p>
     * No puedes programar lo que no entiendes.
     * Antes de la práctica, la teoría.
     * <p>
     * Requisitos:
     * - Nivel 35
     * - Gate D→C completada
     * - Completar 1 Proyecto Personal (User Quest Rango B) SIN fecha límite
     * - DQ_READ: Acumular 100 páginas en los últimos 5 días antes de completar
     * <p>
     * Recompensa:
     * - Acceso a Fase 2 (Gate 4)
     * - +1,000 XP Inteligencia
     */
    GATE_C_TO_B_PHASE_1(
            "🟣",
            "La Biblioteca de Babel (Teoría)",
            "Completar un Proyecto Personal sin fecha límite + Leer 100 páginas en los últimos 5 días",
            35,
            GATE_D_TO_C,
            QuestRank.C,
            1_000,
            0  // No da gold, solo XP INT
    ),

    /**
     * 🟣 GATE 4: C → B (Fase 2) - "Hackathon Personal"
     * <p>
     * Demostrar que aguantas el ritmo de un profesional.
     * Construye. Rompe. Arregla. Repite.
     * <p>
     * Requisitos:
     * - Nivel 35
     * - Gate C→B Fase 1 completada
     * - Completar 1 Proyecto Personal (User Quest Rango B) en 7 días (se reinicia cada 7 días)
     * - CONDICIÓN CRÍTICA: No entrar en BURNOUT durante el proceso
     * <p>
     * Recompensa:
     * - 🔓 Rango B desbloqueado (Élite)
     * - +3,000 XP General
     * - +5,000 Gold
     * - Título: "Constructor" (+5% DIS XP)
     */
    GATE_C_TO_B_PHASE_2(
            "🟣",
            "Hackathon Personal (Práctica)",
            "Proyecto Personal en 7 días sin BURNOUT (se reinicia cada 7 días)",
            35,
            GATE_C_TO_B_PHASE_1,
            QuestRank.B,
            3_000,
            5_000
    ),

    /**
     * 🔴 GATE 5: Gate Económica - "Capital Semilla"
     * <p>
     * Una prueba intermedia para asegurar que gestionas recursos.
     * El dinero da libertad. Demuestra control.
     * <p>
     * Requisitos:
     * - Nivel 40
     * - Gate C→B Fase 2 completada
     * - Tener 20,000 Gold en la Wallet simultáneamente
     * - No haber comprado "Festín Trampa" en los últimos 10 días
     * <p>
     * Recompensa:
     * - Título: "Lobo de Wall Street" (+5% Gold Gain)
     * - Desbloqueo item tienda: "Escritorio Elevable"
     */
    GATE_WEALTH(
            "🔴",
            "Capital Semilla",
            "El dinero da libertad. Demuestra que controlas tus finanzas.",
            40,
            GATE_C_TO_B_PHASE_2,
            QuestRank.A,
            0,
            0  // No da XP/Gold, da título
    ),

    /**
     * 🟠 GATE 6: B → A - "La Cacería de Empleo"
     * <p>
     * El salto al mundo profesional real.
     * Prepara tus armas. El mundo real no tiene piedad.
     * <p>
     * Requisitos:
     * - Nivel 50
     * - Gate Wealth completada
     * - Completar User Quest: "Actualizar LinkedIn/Portfolio al 100%"
     * - Completar User Quest: "Aplicar a 10 ofertas" O "3 entrevistas técnicas simuladas"
     * <p>
     * Recompensa:
     * - 🔓 Rango A desbloqueado (Heroico)
     * - +5,000 XP General
     * - Item: "Traje de Entrevista" (+10 Carisma)
     */
    GATE_B_TO_A(
            "🟠",
            "La Cacería de Empleo (Simulacro)",
            "Prepara tus armas. El mundo real no tiene piedad.",
            50,
            GATE_WEALTH,
            QuestRank.A,
            5_000,
            0
    ),

    /**
     * 🟡 GATE 7: A → S - "Cambio de Clase: Junior Developer"
     * <p>
     * ⭐ EL JEFE FINAL ⭐
     * El objetivo principal del documento: Conseguir el trabajo.
     * <p>
     * Requisitos:
     * - Nivel 60
     * - Gate B→A completada
     * - HITO ÚNICO: Conseguir un empleo o primer cliente freelance REAL
     * - Tiempo límite: Indefinido (permanece activa hasta lograrlo)
     * <p>
     * Recompensa:
     * - 🔓 Rango S desbloqueado (Legendario)
     * - 🔓 Clase Evolucionada: Job Engine Gold Gain aumenta
     * - +20,000 XP General
     * - +50,000 Gold
     * - Título: "Profesional" (x2 Job Gold Gain)
     */
    GATE_A_TO_S(
            "🟡",
            "Cambio de Clase: Junior Developer",
            "Conseguir empleo como desarrollador o primer cliente freelance real",
            60,
            GATE_B_TO_A,
            QuestRank.S,
            20_000,
            50_000
    ),

    /**
     * ✨ GATE 8: S → S+ - "Territorio Propio"
     * <p>
     * Vivir solo. El sueño adulto.
     * Un rey necesita su castillo.
     * <p>
     * Requisitos:
     * - Nivel 75
     * - Gate A→S completada
     * - HITO: Firmar contrato alquiler/compra O independencia financiera 12 meses
     * <p>
     * Recompensa:
     * - 🔓 Rango S+ desbloqueado (Mítico)
     * - +50,000 XP General
     * - Desbloqueo Tienda Secreta (Items de Decoración de Lujo)
     */
    GATE_S_TO_S_PLUS(
            "✨",
            "Territorio Propio",
            "Firmar contrato de alquiler/compra O independencia financiera 12 meses",
            75,
            GATE_A_TO_S,
            QuestRank.S_PLUS,
            50_000,
            0
    ),

    /**
     * 💀 GATE 9: Redención - "Salir del Abismo"
     * <p>
     * Quest de castigo. Solo aparece si caes muy bajo.
     * Una oportunidad de redención.
     * <p>
     * Trigger:
     * - Entrar en BURNOUT (HP=0) 3 veces en un mes
     * <p>
     * Requisitos:
     * - Mantener HP > 80 durante 14 días consecutivos
     * - Prohibido hacer User Quests Rango C+ durante este periodo
     * <p>
     * Recompensa:
     * - Eliminación de debuffs permanentes
     */
    GATE_REDEMPTION(
            "💀",
            "Salir del Abismo",
            "Mantener HP > 80 durante 14 días (prohibido User Quests C+)",
            0,  // Sin requisito de nivel
            null,  // Sin gate anterior (se triggerea por evento)
            QuestRank.B,
            0,
            0
    ),

    /**
     * 🌌 GATE 10: S++ Endgame - "El Monarca de las Sombras"
     * <p>
     * El final del juego actual.
     * No te detuviste hasta conseguirlo.
     * <p>
     * Requisitos:
     * - Nivel 100
     * - Gate S→S+ completada
     * - Acumular > 1,000,000 Gold históricamente
     * <p>
     * Recompensa:
     * - Título Único: "Monarca" (+10% ALL STATS)
     * - Modo "New Game+" (Reiniciar manteniendo títulos)
     */
    GATE_ENDGAME(
            "🌌",
            "El Monarca de las Sombras",
            "No te detuviste hasta conseguirlo.",
            100,
            GATE_S_TO_S_PLUS,
            QuestRank.S_PLUS_PLUS,
            100_000,
            500_000
    ),
    ;

    // ========================================================================================
    // CAMPOS
    // ========================================================================================

    private final String icon;
    private final String name;
    private final String description;
    private final int levelRequirement;
    private final SystemQuestType previousGate;  // null si es la primera
    private final QuestRank rankUnlocked;
    private final int baseXP;
    private final int baseGold;

    SystemQuestType(
            String icon,
            String name,
            String description,
            int levelRequirement,
            SystemQuestType previousGate,
            QuestRank rankUnlocked,
            int baseXP,
            int baseGold
    ) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.levelRequirement = levelRequirement;
        this.previousGate = previousGate;
        this.rankUnlocked = rankUnlocked;
        this.baseXP = baseXP;
        this.baseGold = baseGold;
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    /**
     * Verifica si es la primera gate (no requiere gate anterior).
     */
    public boolean isFirstGate() {
        return previousGate == null;
    }

    /**
     * Verifica si tiene una gate anterior requerida.
     */
    public boolean hasPreviousGate() {
        return previousGate != null;
    }

    /**
     * Verifica si es una gate especial (triggered por evento).
     */
    public boolean isSpecialGate() {
        return this == GATE_REDEMPTION;
    }

    /**
     * Verifica si es la gate final (endgame).
     */
    public boolean isEndgame() {
        return this == GATE_ENDGAME;
    }

    /**
     * Verifica si el jugador cumple los requisitos de nivel.
     */
    public boolean meetsLevelRequirement(int playerLevel) {
        return playerLevel >= levelRequirement;
    }

    /**
     * Obtiene la recompensa base (sin buffs).
     */
    public QuestReward getBaseReward() {
        return QuestReward.builder()
                .setGeneralXP(baseXP)
                .setGold(baseGold)
                .build();
    }

    // ========================================================================================
    // UI FORMAT
    // ========================================================================================

    public String toDisplayString() {
        return icon + " " + name;
    }

    // ========================================================================================
    // GETTERS
    // ========================================================================================

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public SystemQuestType getPreviousGate() {
        return previousGate;
    }

    public QuestRank getRankUnlocked() {
        return rankUnlocked;
    }

    public int getBaseXP() {
        return baseXP;
    }

    public int getBaseGold() {
        return baseGold;
    }
}