package com.lifeleveling.domain.quest.elder;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.*;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.List;

public enum ElderQuestType {

    ELDER_1(
            "🧠", "La Mente de Titanio",
            "El acero se forja en el fuego; la mente, en el código.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    // [NUEVO] 100 horas en 30 días
                    new CareerHoursInPeriod(100.0, 30)
            ),
            QuestReward.builder().addStatXP(StatType.INTELLECT, 25_000).build()
    ),

    ELDER_2(
            "🧘", "El Voto de Pobreza",
            "La riqueza no es tener mucho, sino necesitar poco.",
            ElderQuestFrequency.UNIQUE,
            List.of(
                    // [NUEVO] Sin gastos categoría LUXURY en 30 días
                    new ConsumableAbstinence("LUXURY", 30)
            ),
            QuestReward.builder().addStatXP(StatType.DISCIPLINE, 20_000).build()
    ),

    ELDER_3(
            "📚", "La Gran Biblioteca",
            "Un Rey debe conocer la historia para no repetirla.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    // [EXISTENTE] Ya lo tenías, ahora usa el nuevo tracker
                    new PagesRead(1_000, 30)
            ),
            QuestReward.builder().addStatXP(StatType.WISDOM, 20_000).build()
    ),

    ELDER_4(
            "💪", "El Cuerpo Perfecto",
            "Tu cuerpo es el único lugar que tienes para vivir.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    // [NUEVO] 25 Gyms en 30 días
                    new GymSessionsInPeriod(25, 30),
                    // [NUEVO] Sin comida basura (ID: "consumable_burger")
                    new ConsumableAbstinence("consumable_burger", 30)
            ),
            QuestReward.builder().addStatXP(StatType.STRENGTH, 25_000).build()
    ),

    ELDER_5(
            "🛡️", "El Intocable",
            "El caos no te toca. La fatiga no te alcanza.",
            ElderQuestFrequency.UNIQUE,
            List.of(
                    new DebuffFreeStreak(30)
            ),
            QuestReward.builder().setGeneralXP(30_000).build()
    ),

    ELDER_6(
            "⚔️", "La Cruzada",
            "La constancia no es hacerlo bien un día. Es hacerlo bien siempre.",
            ElderQuestFrequency.UNIQUE,
            List.of(
                    new WeeklyQuestsCompleted(4)
            ),
            QuestReward.builder().setGeneralXP(50_000).build()
    ),

    ELDER_7(
            "👑", "La Ascensión",
            "Solo un verdadero Monarca conquista sus días por completo.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    // [NUEVO] 20 Perfect Days en 30 días (No consecutivos)
                    new PerfectDaysInPeriod(20, 30)
            ),
            QuestReward.builder().setGeneralXP(40_000).build()
    );

    private final String icon;
    private final String name;
    private final String description;
    private final ElderQuestFrequency frequency;
    private final List<GateCondition> conditions;
    private final QuestReward reward;

    ElderQuestType(String icon, String name, String description, ElderQuestFrequency frequency, List<GateCondition> conditions, QuestReward reward) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.conditions = conditions;
        this.reward = reward;
    }

    public QuestReward getReward() { return reward; }
    public List<GateCondition> getConditions() { return conditions; }
    public ElderQuestFrequency getFrequency() { return frequency; }
    public String toDisplayString() { return icon + " " + name + " [" + frequency.getDisplayName() + "]"; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}