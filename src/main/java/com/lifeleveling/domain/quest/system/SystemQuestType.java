package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.CareerHoursInPeriod;
import com.lifeleveling.domain.quest.condition.FinancialDiscipline;
import com.lifeleveling.domain.quest.condition.GateCondition;
import com.lifeleveling.domain.quest.condition.HPThreshold;
import com.lifeleveling.domain.quest.condition.LevelRequirement;
import com.lifeleveling.domain.quest.condition.ManualConfirmation;
import com.lifeleveling.domain.quest.condition.NoBurnout;
import com.lifeleveling.domain.quest.condition.PagesRead;
import com.lifeleveling.domain.quest.condition.PerfectDayStreak;
import com.lifeleveling.domain.quest.condition.UserQuestsCompleted;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.List;

/**
 * Las Gates (System Quests): hitos obligatorios de ascenso de rango profesional.
 * Cadena fiel a La Biblia (cap 2.4): E → D → C → C+ → B → A → S.
 * Vault y Redemption son especiales (fuera de la cadena de ascenso).
 */
public enum SystemQuestType {

    GATE_E_TO_D(
            "🟢", "El Portal del Novato",
            "Completar 7 Perfect Days consecutivos.",
            10, null, PlayerRank.D, 500, 500, null,
            List.of(new PerfectDayStreak(7))
    ),
    GATE_D_TO_C(
            "🔵", "El Despertar",
            "Acumular 20h de CODE + 3 User Quests completadas en 7 días.",
            25, GATE_E_TO_D, PlayerRank.C, 1_500, 1_000, null,
            List.of(new CareerHoursInPeriod(20, 7), new UserQuestsCompleted(3, 7, QuestRank.E))
    ),
    GATE_C_TO_C_PLUS(
            "🟣", "La Barrera (Teoría)",
            "Leer 500 páginas en 7 días.",
            35, GATE_D_TO_C, PlayerRank.C_PLUS, 1_000, 0, StatType.INTELLECT,
            List.of(new PagesRead(500, 7))
    ),
    GATE_C_PLUS_TO_B(
            "🟣", "La Prueba (Práctica)",
            "Crear y completar una User Quest de Rango B en 7 días sin Burnout.",
            35, GATE_C_TO_C_PLUS, PlayerRank.B, 3_000, 3_000, null,
            List.of(new UserQuestsCompleted(1, 7, QuestRank.B), new NoBurnout())
    ),
    GATE_B_TO_A(
            "🟠", "La Conquista",
            "Conseguir un nuevo empleo o cliente.",
            50, GATE_C_PLUS_TO_B, PlayerRank.A, 5_000, 0, null,
            List.of(new ManualConfirmation("gate_conquista", "Nuevo empleo o cliente conseguido"))
    ),
    GATE_A_TO_S(
            "🟡", "Independencia",
            "1 año de sueldo ahorrado de colchón.",
            75, GATE_B_TO_A, PlayerRank.S, 0, 0, null,
            List.of(new ManualConfirmation("gate_independencia", "1 año de sueldo ahorrado"))
    ),
    GATE_ENDGAME(
            "🌌", "El Monarca de las Sombras",
            "Alcanzar Nivel 100.",
            100, GATE_A_TO_S, PlayerRank.S, 0, 0, null,
            List.of(new LevelRequirement(100))
    ),

    // ---- Especiales (fuera de la cadena de ascenso) ----
    GATE_VAULT(
            "💰", "The Vault (La Bóveda)",
            "Mantener 20.000 G durante 7 días sin caprichos.",
            40, null, null, 0, 10_000, null,
            List.of(new FinancialDiscipline(7))
    ),
    GATE_REDEMPTION(
            "💀", "Salir del Abismo",
            "Mantener HP > 80 durante 14 días tras caer en desgracia.",
            0, null, null, 1_000, 0, null,
            // Aparición (caer: 3 burnouts/mes) la gobierna GateService.hasAppeared, NO la lista de
            // condiciones. Aquí solo "salir del abismo": recuperar y sostener el HP 14 días.
            List.of(new HPThreshold(80, 14))
    );

    private final String icon;
    private final String name;
    private final String description;
    private final int levelRequirement;
    private final SystemQuestType previousGate;
    private final PlayerRank rankUnlocked;
    private final int baseXP;
    private final int baseGold;
    private final StatType rewardStat;
    private final List<GateCondition> conditions;

    SystemQuestType(String icon, String name, String description, int levelRequirement,
                    SystemQuestType previousGate, PlayerRank rankUnlocked, int baseXP, int baseGold,
                    StatType rewardStat, List<GateCondition> conditions) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.levelRequirement = levelRequirement;
        this.previousGate = previousGate;
        this.rankUnlocked = rankUnlocked;
        this.baseXP = baseXP;
        this.baseGold = baseGold;
        this.rewardStat = rewardStat;
        this.conditions = conditions;
    }

    public boolean isFirstGate() { return this == GATE_E_TO_D; }
    public boolean hasPreviousGate() { return previousGate != null; }
    public boolean isSpecialGate() { return this == GATE_REDEMPTION || this == GATE_VAULT; }
    public boolean isEndgame() { return this == GATE_ENDGAME; }
    public boolean meetsLevelRequirement(int lvl) { return lvl >= levelRequirement; }

    public QuestReward getBaseReward() {
        QuestReward.Builder builder = QuestReward.builder().setGold(baseGold);
        if (rewardStat != null) {
            builder.addStatXP(rewardStat, baseXP);
        } else {
            builder.setGeneralXP(baseXP);
        }
        return builder.build();
    }

    public List<GateCondition> getConditions() { return conditions; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getLevelRequirement() { return levelRequirement; }
    public SystemQuestType getPreviousGate() { return previousGate; }
    public PlayerRank getRankUnlocked() { return rankUnlocked; }
    public int getBaseXP() { return baseXP; }
    public int getBaseGold() { return baseGold; }
    public StatType getRewardStat() { return rewardStat; }

    public String toDisplayString() { return icon + " " + name; }
}
