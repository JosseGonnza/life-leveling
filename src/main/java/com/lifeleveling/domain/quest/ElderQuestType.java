package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.*;

import java.util.List;
import java.util.Map;

public enum ElderQuestType {

    /**
     * ELDER_1: La Mente de Titanio (Code Focus)
     * Objetivo: 100 horas de código en 30 días.
     */
    ELDER_1(
            "🧠",
            "La Mente de Titanio",
            "El acero se forja en el fuego; la mente, en el código.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    // TODO: Crear condición CareerHoursInPeriod(100.0, 30)
                    // De momento usamos un placeholder o la total si prefieres
                    new ManualConfirmation("CHECK_ELDER_1", "Registrar 100h Code este mes")
            ),
            QuestReward.builder()
                    .addStatXP(StatType.INTELLECT, 25_000)
                    .build()
    ),

    /**
     * ELDER_2: El Voto de Pobreza (Financial Discipline)
     * Objetivo: 30 días sin gastos de lujo.
     */
    ELDER_2(
            "🧘",
            "El Voto de Pobreza",
            "La riqueza no es tener mucho, sino necesitar poco.",
            ElderQuestFrequency.UNIQUE,
            List.of(
                    new ConsumableAbstinence("LUXURY_CATEGORY", 30)
            ),
            QuestReward.builder()
                    .addStatXP(StatType.DISCIPLINE, 20_000)
                    .build()
    ),

    /**
     * ELDER_3: La Gran Biblioteca (Wisdom)
     * Objetivo: 1,000 páginas en 30 días.
     */
    ELDER_3(
            "📚",
            "La Gran Biblioteca",
            "Un Rey debe conocer la historia para no repetirla.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    new PagesRead(1_000, 30)
            ),
            QuestReward.builder()
                    .addStatXP(StatType.WISDOM, 20_000)
                    .build()
    ),

    /**
     * ELDER_4: El Cuerpo Perfecto (Health)
     * Objetivo: 25 Gyms + 0 Comida Basura.
     */
    ELDER_4(
            "💪",
            "El Cuerpo Perfecto",
            "Tu cuerpo es el único lugar que tienes para vivir.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    new UserQuestsCompleted(25, 30, QuestRank.D), // Asumimos que GYM es una UserQuest recurrente o Daily
                    new ConsumableAbstinence("JUNK_FOOD", 30)
            ),
            QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 25_000)
                    .build()
    ),

    /**
     * ELDER_7: La Ascensión (Consistency)
     * Objetivo: 20 Perfect Days en un mes.
     */
    ELDER_7(
            "👑",
            "La Ascensión",
            "Solo un verdadero Monarca conquista sus días por completo.",
            ElderQuestFrequency.MONTHLY,
            List.of(
                    new PerfectDayStreak(20) // Nota: Esto pide 20 seguidos. Si el libro permite alternos, necesitaríamos una condición "PerfectDaysCount(20, 30)"
            ),
            QuestReward.builder()
                    .setGeneralXP(40_000)
                    .build()
    );

    // ... Faltan ELDER_5 y ELDER_6, se pueden añadir con la misma lógica

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

    public QuestReward getReward() {
        return reward;
    }

    public List<GateCondition> getConditions() {
        return conditions;
    }

    public ElderQuestFrequency getFrequency() {
        return frequency;
    }

    public String toDisplayString() {
        return icon + " " + name + " [" + frequency.getDisplayName() + "]";
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}