package com.lifeleveling.domain.quest.weekly;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.List;
import java.util.Optional;

/**
 * WeeklyQuestType: Los 6 desafíos semanales rotativos.
 *
 * Cada semana se seleccionan 3 de estos 6 desafíos aleatoriamente.
 * No hay penalización por no completarlos - son "bonus tracks".
 *
 * Reset: Lunes 00:00 (hora local)
 * Selección: 3 misiones aleatorias del pool
 */
public enum WeeklyQuestType {

    /**
     * 🐻 EL OSO HIBERNADOR: Descanso consistente.
     *
     * Condición: SLEEP > 7h durante 6 días de la semana.
     * Reward: 1,000 G
     * Objetivo de diseño: Premiar el descanso constante.
     */
    HIBERNATOR(
            "🐻",
            "El Oso Hibernador",
            "Dormir más de 7 horas durante 6 días de la semana.",
            WeeklyConditionType.DAILY_HABIT_COUNT,
            DailyQuestType.SLEEP,
            6,      // 6 días
            0,      // No aplica
            QuestReward.builder()
                    .setGold(1_000)
                    .build()
    ),

    /**
     * 🧘 TEMPLO PURO: Cuidado personal consistente.
     *
     * Condición: DIET (6 días) + SKINCARE (6 días).
     * Reward: +500 CHA XP + 1,000 G
     * Objetivo de diseño: Recuperación y estética.
     */
    PURE_TEMPLE(
            "🧘",
            "Templo Puro",
            "Completar DIET y SKINCARE durante 6 días de la semana.",
            WeeklyConditionType.DUAL_DAILY_HABIT_COUNT,
            null,   // Usa habitsRequired en su lugar
            6,      // 6 días cada uno
            0,      // No aplica
            QuestReward.builder()
                    .addStatXP(StatType.CHARISMA, 500)
                    .setGold(1_000)
                    .build()
    ) {
        @Override
        public List<DailyQuestType> getRequiredHabits() {
            return List.of(DailyQuestType.DIET, DailyQuestType.SKINCARE);
        }
    },

    /**
     * 🏋️ SEMANA DE HIERRO: Consistencia física.
     *
     * Condición: Completar GYM 5 veces en la semana.
     * Reward: +600 STR XP + 1,000 G
     * Objetivo de diseño: Consistencia física.
     */
    IRON_WEEK(
            "🏋️",
            "Semana de Hierro",
            "Completar GYM 5 veces durante la semana.",
            WeeklyConditionType.DAILY_HABIT_COUNT,
            DailyQuestType.GYM,
            5,      // 5 veces
            0,      // No aplica
            QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 600)
                    .setGold(1_000)
                    .build()
    ),

    /**
     * 💻 LA MARATÓN DEL CÓDIGO: Productividad intensiva.
     *
     * Condición: Acumular 12 horas de CODE en la semana.
     * Reward: +800 INT XP + 1,500 G
     * Objetivo de diseño: Forzar avance profesional.
     */
    CODE_MARATHON(
            "💻",
            "La Maratón del Código",
            "Acumular 12 horas de programación durante la semana.",
            WeeklyConditionType.ACCUMULATED_HOURS,
            DailyQuestType.CODE,
            0,      // No aplica (días)
            12,     // 12 horas acumuladas
            QuestReward.builder()
                    .addStatXP(StatType.INTELLECT, 800)
                    .setGold(1_500)
                    .build()
    ),

    /**
     * 📚 EL ERUDITO: Lectura intensiva.
     *
     * Condición: Leer 150 páginas durante la semana.
     * Reward: +600 WIS XP + 1,000 G
     * Objetivo de diseño: Lectura intensiva.
     */
    THE_SCHOLAR(
            "📚",
            "El Erudito",
            "Leer 150 páginas durante la semana.",
            WeeklyConditionType.ACCUMULATED_PAGES,
            DailyQuestType.READ,
            0,      // No aplica (días)
            150,    // 150 páginas
            QuestReward.builder()
                    .addStatXP(StatType.WISDOM, 600)
                    .setGold(1_000)
                    .build()
    ),

    /**
     * 🔥 THE STREAK (LA RACHA): Excelencia total.
     *
     * Condición: Completar 5 Perfect Days (7/7 hábitos) en la semana.
     * Reward: +2,000 XP General + 3,000 G
     * Objetivo de diseño: Premio gordo por excelencia total.
     */
    THE_STREAK(
            "🔥",
            "The Streak",
            "Conseguir 5 días perfectos (7/7 hábitos) durante la semana.",
            WeeklyConditionType.PERFECT_DAYS_COUNT,
            null,   // No aplica a un hábito específico
            5,      // 5 Perfect Days
            0,      // No aplica
            QuestReward.builder()
                    .setGeneralXP(2_000)
                    .setGold(3_000)
                    .build()
    );

    /**
     * Tipos de condición para Weekly Quests.
     */
    public enum WeeklyConditionType {
        /** Contar días que se completó un hábito específico */
        DAILY_HABIT_COUNT,
        /** Contar días que se completaron múltiples hábitos */
        DUAL_DAILY_HABIT_COUNT,
        /** Acumular horas de una actividad */
        ACCUMULATED_HOURS,
        /** Acumular páginas leídas */
        ACCUMULATED_PAGES,
        /** Contar Perfect Days (7/7) */
        PERFECT_DAYS_COUNT
    }

    private final String icon;
    private final String name;
    private final String description;
    private final WeeklyConditionType conditionType;
    private final DailyQuestType relatedHabit;
    private final int daysRequired;
    private final int accumulatedTarget;
    private final QuestReward reward;

    WeeklyQuestType(
            String icon,
            String name,
            String description,
            WeeklyConditionType conditionType,
            DailyQuestType relatedHabit,
            int daysRequired,
            int accumulatedTarget,
            QuestReward reward
    ) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.conditionType = conditionType;
        this.relatedHabit = relatedHabit;
        this.daysRequired = daysRequired;
        this.accumulatedTarget = accumulatedTarget;
        this.reward = reward;
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

    public WeeklyConditionType getConditionType() {
        return conditionType;
    }

    public DailyQuestType getRelatedHabit() {
        return relatedHabit;
    }

    public int getDaysRequired() {
        return daysRequired;
    }

    public int getAccumulatedTarget() {
        return accumulatedTarget;
    }

    public QuestReward getReward() {
        return reward;
    }

    /**
     * Retorna los hábitos requeridos (para DUAL_DAILY_HABIT_COUNT).
     * Por defecto retorna lista vacía o el hábito relacionado.
     */
    public List<DailyQuestType> getRequiredHabits() {
        if (relatedHabit != null) {
            return List.of(relatedHabit);
        }
        return List.of();
    }

    /**
     * Calcula el objetivo numérico para mostrar progreso.
     */
    public int getTarget() {
        return switch (conditionType) {
            case DAILY_HABIT_COUNT, DUAL_DAILY_HABIT_COUNT, PERFECT_DAYS_COUNT -> daysRequired;
            case ACCUMULATED_HOURS, ACCUMULATED_PAGES -> accumulatedTarget;
        };
    }

    /**
     * Obtiene la unidad de medida para mostrar en UI.
     */
    public String getTargetUnit() {
        return switch (conditionType) {
            case DAILY_HABIT_COUNT, DUAL_DAILY_HABIT_COUNT -> "días";
            case PERFECT_DAYS_COUNT -> "Perfect Days";
            case ACCUMULATED_HOURS -> "horas";
            case ACCUMULATED_PAGES -> "páginas";
        };
    }

    /**
     * Verifica si la condición se cumple dado el progreso actual.
     */
    public boolean isConditionMet(int currentProgress) {
        return currentProgress >= getTarget();
    }

    /**
     * Calcula el progreso como porcentaje (0.0 a 1.0).
     */
    public double getProgressPercentage(int currentProgress) {
        int target = getTarget();
        if (target == 0) return 1.0;
        return Math.min(1.0, (double) currentProgress / target);
    }

    // ========================================================================================
    // UI HELPERS
    // ========================================================================================

    public String toDisplayString() {
        return icon + " " + name;
    }

    public String formatProgress(int current) {
        return String.format("%d/%d %s", current, getTarget(), getTargetUnit());
    }

    public QuestRank getRank() {
        // Weekly Quests son de dificultad media-alta
        return this == THE_STREAK ? QuestRank.B : QuestRank.C;
    }

    // ========================================================================================
    // SEARCH
    // ========================================================================================

    public static Optional<WeeklyQuestType> fromString(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim().toUpperCase();
        try {
            return Optional.of(WeeklyQuestType.valueOf(normalized));
        } catch (IllegalArgumentException e) {
            for (WeeklyQuestType type : values()) {
                if (type.name.equalsIgnoreCase(name.trim())) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }
}
