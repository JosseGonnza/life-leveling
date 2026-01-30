package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.quest.condition.FinancialDiscipline;
import com.lifeleveling.domain.quest.condition.GateCondition;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.List;

public enum SystemQuestType {

    GATE_E_TO_D(
            "🟢", "La Semana del Infierno",
            "Completar 7/7 Daily Quests durante 7 días sin bajar de HP 50",
            10, null, PlayerRank.D, 500, 1_000,
            List.of(
                    // Deberíamos añadir: new PerfectDayStreak(7), new HPThreshold(50, 7)
            )
    ),
    GATE_D_TO_C(
            "🔵", "El Primer Encargo",
            "Ya tienes hábitos. Ahora úsalos para construir algo.",
            25, GATE_E_TO_D, PlayerRank.C, 1_500, 2_000,
            List.of()
    ),
    GATE_C_TO_B_PHASE_1(
            "🟣", "La Biblioteca de Babel (Teoría)",
            "Completar un Proyecto Personal sin fecha límite + Leer 100 páginas",
            35, GATE_D_TO_C, PlayerRank.C, 1_000, 0,
            List.of(
                    // Deberíamos añadir: new PagesRead(100, 30)
            )
    ),
    GATE_C_TO_B_PHASE_2(
            "🟣", "Hackathon Personal (Práctica)",
            "Proyecto Personal en 7 días sin BURNOUT",
            35, GATE_C_TO_B_PHASE_1, PlayerRank.B, 3_000, 5_000,
            List.of(
                    // Deberíamos añadir: new NoBurnout(), new TimeLimit(7)
            )
    ),

    GATE_VAULT(
            "💰", "The Vault (La Bóveda)",
            "Demuestra tu disciplina financiera: mantén 20k G durante 7 días sin caprichos.",
            40, GATE_C_TO_B_PHASE_2, PlayerRank.A, 2_000, 5_000,
            List.of(
                    // [CAMBIO] Usamos la nueva condición que verifica la RACHA del Tracker
                    new FinancialDiscipline(7)
            )
    ),

    GATE_B_TO_A(
            "🟠", "La Cacería de Empleo (Simulacro)",
            "Aplicar a 10 ofertas o 3 entrevistas.",
            50, GATE_VAULT, PlayerRank.A, 5_000, 0, // Actualizado previo a GATE_VAULT
            List.of()
    ),
    GATE_A_TO_S(
            "🟡", "Cambio de Clase: Junior Developer",
            "Conseguir empleo o primer cliente freelance REAL",
            60, GATE_B_TO_A, PlayerRank.S, 20_000, 50_000,
            List.of()
    ),
    GATE_S_TO_S_PLUS(
            "✨", "Territorio Propio",
            "Independencia habitacional.",
            75, GATE_A_TO_S, PlayerRank.S_PLUS, 50_000, 0,
            List.of()
    ),
    GATE_REDEMPTION(
            "💀", "Salir del Abismo",
            "Mantener HP > 80 durante 14 días tras caer en desgracia.",
            0, null, PlayerRank.B, 1_000, 2_000,
            List.of(
                    // new HPThreshold(80, 14)
            )
    ),
    GATE_ENDGAME(
            "🌌", "El Monarca de las Sombras",
            "Game Over.",
            100, GATE_S_TO_S_PLUS, PlayerRank.S_PLUS_PLUS, 100_000, 500_000,
            List.of()
    );

    private final String icon;
    private final String name;
    private final String description;
    private final int levelRequirement;
    private final SystemQuestType previousGate;
    private final PlayerRank rankUnlocked;
    private final int baseXP;
    private final int baseGold;
    private final List<GateCondition> conditions; // [NUEVO]

    SystemQuestType(String icon, String name, String description, int levelRequirement,
                    SystemQuestType previousGate, PlayerRank rankUnlocked, int baseXP, int baseGold,
                    List<GateCondition> conditions) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.levelRequirement = levelRequirement;
        this.previousGate = previousGate;
        this.rankUnlocked = rankUnlocked;
        this.baseXP = baseXP;
        this.baseGold = baseGold;
        this.conditions = conditions;
    }

    public boolean isFirstGate() { return previousGate == null; }
    public boolean hasPreviousGate() { return previousGate != null; }
    public boolean isSpecialGate() { return this == GATE_REDEMPTION; }
    public boolean isEndgame() { return this == GATE_ENDGAME; }
    public boolean meetsLevelRequirement(int lvl) { return lvl >= levelRequirement; }

    public QuestReward getBaseReward() {
        return QuestReward.builder().setGeneralXP(baseXP).setGold(baseGold).build();
    }

    // Getter para las condiciones
    public List<GateCondition> getConditions() { return conditions; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getLevelRequirement() { return levelRequirement; }
    public SystemQuestType getPreviousGate() { return previousGate; }
    public PlayerRank getRankUnlocked() { return rankUnlocked; }
    public int getBaseXP() { return baseXP; }
    public int getBaseGold() { return baseGold; }
}