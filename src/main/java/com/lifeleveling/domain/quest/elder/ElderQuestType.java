package com.lifeleveling.domain.quest.elder;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.ConsumableAbstinence;
import com.lifeleveling.domain.quest.condition.DebuffFreeStreak;
import com.lifeleveling.domain.quest.condition.GateCondition;
import com.lifeleveling.domain.quest.condition.ManualConfirmation;
import com.lifeleveling.domain.quest.condition.PagesRead;
import com.lifeleveling.domain.quest.condition.PerfectDayStreak;
import com.lifeleveling.domain.quest.condition.UserQuestsCompleted;
import com.lifeleveling.domain.quest.condition.WeeklyQuestsCompleted;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.List;

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
     * ELDER_5: El Intocable (Perfect Health Management)
     * Objetivo: 0 debuffs activos durante 30 días consecutivos.
     *
     * Debuffs a evitar: Fatiga, Caos, Pesadez, Taquicardia, Aburrimiento
     *
     * Recompensa según biblia:
     *   - +30,000 XP General
     *   - Título: "Mente de Acero" (Inmunidad al debuff "Caos" de por vida)
     */
    ELDER_5(
            "🛡️",
            "El Intocable",
            "El caos no te toca. La fatiga no te alcanza. Eres una fortaleza impenetrable.",
            ElderQuestFrequency.UNIQUE,
            List.of(
                    new DebuffFreeStreak(30)
            ),
            QuestReward.builder()
                    .setGeneralXP(30_000)
                    .build()
    ),

    /**
     * ELDER_6: La Cruzada (Weekly Quest Mastery)
     * Objetivo: 100% Weekly Quests (3/3) durante 4 semanas consecutivas.
     *
     * Total: 12 Weekly Quests completadas sin fallar ninguna semana.
     *
     * Recompensa según biblia:
     *   - +50,000 XP General
     *   - Item: "Llave Maestra" (Permite hacer "Reroll" de 1 Weekly Quest a la semana)
     */
    ELDER_6(
            "⚔️",
            "La Cruzada",
            "La constancia no es hacerlo bien un día. Es hacerlo bien cuando nadie mira, semana tras semana.",
            ElderQuestFrequency.UNIQUE,
            List.of(
                    new WeeklyQuestsCompleted(4)
            ),
            QuestReward.builder()
                    .setGeneralXP(50_000)
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