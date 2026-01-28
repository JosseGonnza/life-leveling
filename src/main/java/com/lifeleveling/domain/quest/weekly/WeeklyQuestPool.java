package com.lifeleveling.domain.quest.weekly;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WeeklyQuestPool: Gestiona la selección y rotación de Weekly Quests.
 *
 * Cada semana se seleccionan 3 quests aleatorias del pool de 6.
 * El seed de selección se basa en la fecha del Lunes para garantizar
 * consistencia durante toda la semana.
 *
 * Reglas:
 *   - Selección: Lunes 00:00 (automática)
 *   - Cantidad: 3 de 6 posibles
 *   - Reset: Lunes siguiente a las 00:00
 *   - Determinismo: Misma semana = misma selección (basado en fecha)
 */
public final class WeeklyQuestPool {

    /** Número de quests activas por semana */
    public static final int ACTIVE_QUESTS_PER_WEEK = 3;

    /** Todas las quests disponibles en el pool */
    public static final List<WeeklyQuestType> ALL_QUEST_TYPES =
            List.of(WeeklyQuestType.values());

    private WeeklyQuestPool() {
        // Utility class
    }

    // ========================================================================================
    // SELECTION LOGIC
    // ========================================================================================

    /**
     * Selecciona 3 tipos de quest para la semana actual.
     * La selección es determinística basada en la fecha.
     */
    public static List<WeeklyQuestType> selectForCurrentWeek() {
        return selectForWeek(LocalDate.now());
    }

    /**
     * Selecciona 3 tipos de quest para una semana específica.
     * Usa el Lunes de esa semana como seed para garantizar consistencia.
     *
     * @param referenceDate Cualquier fecha de la semana deseada
     * @return Lista de 3 tipos de quest seleccionados
     */
    public static List<WeeklyQuestType> selectForWeek(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "Reference date no puede ser null");

        LocalDate monday = WeeklyQuest.getWeekStart(referenceDate);
        long seed = monday.toEpochDay();

        return selectWithSeed(seed);
    }

    /**
     * Selecciona quests usando un seed específico.
     * Útil para testing o selección personalizada.
     */
    public static List<WeeklyQuestType> selectWithSeed(long seed) {
        List<WeeklyQuestType> pool = new ArrayList<>(ALL_QUEST_TYPES);
        Random random = new Random(seed);

        // Shuffle determinístico basado en seed
        Collections.shuffle(pool, random);

        // Tomar los primeros 3
        return pool.subList(0, ACTIVE_QUESTS_PER_WEEK);
    }

    /**
     * Selecciona quests completamente aleatorias (sin seed).
     * Útil para testing o modos especiales.
     */
    public static List<WeeklyQuestType> selectRandom() {
        List<WeeklyQuestType> pool = new ArrayList<>(ALL_QUEST_TYPES);
        Collections.shuffle(pool);
        return pool.subList(0, ACTIVE_QUESTS_PER_WEEK);
    }

    // ========================================================================================
    // QUEST CREATION
    // ========================================================================================

    /**
     * Crea las 3 Weekly Quests para la semana actual.
     */
    public static List<WeeklyQuest> createQuestsForCurrentWeek() {
        return createQuestsForWeek(LocalDate.now());
    }

    /**
     * Crea las 3 Weekly Quests para una semana específica.
     *
     * @param referenceDate Cualquier fecha de la semana deseada
     * @return Lista de 3 WeeklyQuest creadas
     */
    public static List<WeeklyQuest> createQuestsForWeek(LocalDate referenceDate) {
        List<WeeklyQuestType> selectedTypes = selectForWeek(referenceDate);

        return selectedTypes.stream()
                .map(type -> WeeklyQuest.create(type, referenceDate))
                .collect(Collectors.toList());
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    /**
     * Verifica si un tipo de quest está activo esta semana.
     */
    public static boolean isActiveThisWeek(WeeklyQuestType type) {
        return isActiveForWeek(type, LocalDate.now());
    }

    /**
     * Verifica si un tipo de quest está activo para una semana específica.
     */
    public static boolean isActiveForWeek(WeeklyQuestType type, LocalDate referenceDate) {
        Objects.requireNonNull(type, "Type no puede ser null");
        return selectForWeek(referenceDate).contains(type);
    }

    /**
     * Obtiene los tipos de quest que NO están activos esta semana.
     */
    public static List<WeeklyQuestType> getInactiveForCurrentWeek() {
        return getInactiveForWeek(LocalDate.now());
    }

    /**
     * Obtiene los tipos de quest que NO están activos para una semana.
     */
    public static List<WeeklyQuestType> getInactiveForWeek(LocalDate referenceDate) {
        Set<WeeklyQuestType> active = new HashSet<>(selectForWeek(referenceDate));

        return ALL_QUEST_TYPES.stream()
                .filter(type -> !active.contains(type))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene información sobre la próxima rotación.
     */
    public static WeekRotationInfo getRotationInfo() {
        return getRotationInfo(LocalDate.now());
    }

    /**
     * Obtiene información sobre la rotación para una fecha.
     */
    public static WeekRotationInfo getRotationInfo(LocalDate referenceDate) {
        LocalDate weekStart = WeeklyQuest.getWeekStart(referenceDate);
        LocalDate weekEnd = WeeklyQuest.getWeekEnd(referenceDate);
        LocalDate nextWeekStart = weekStart.plusWeeks(1);

        List<WeeklyQuestType> currentSelection = selectForWeek(referenceDate);
        List<WeeklyQuestType> nextSelection = selectForWeek(nextWeekStart);

        int daysRemaining = (int) (weekEnd.toEpochDay() - referenceDate.toEpochDay()) + 1;

        return new WeekRotationInfo(
                weekStart,
                weekEnd,
                nextWeekStart,
                daysRemaining,
                currentSelection,
                nextSelection
        );
    }

    // ========================================================================================
    // ROTATION INFO RECORD
    // ========================================================================================

    /**
     * Información sobre la rotación de quests.
     */
    public record WeekRotationInfo(
            LocalDate weekStart,
            LocalDate weekEnd,
            LocalDate nextRotationDate,
            int daysRemaining,
            List<WeeklyQuestType> currentQuests,
            List<WeeklyQuestType> nextWeekQuests
    ) {
        public String formatDaysRemaining() {
            if (daysRemaining == 1) {
                return "Último día";
            } else if (daysRemaining == 0) {
                return "Rotación hoy";
            }
            return daysRemaining + " días restantes";
        }

        public boolean isLastDay() {
            return daysRemaining == 1;
        }

        public boolean isRotationDay() {
            return daysRemaining == 0;
        }
    }

    // ========================================================================================
    // VALIDATION
    // ========================================================================================

    /**
     * Valida que una lista de quests sea una selección válida.
     */
    public static boolean isValidSelection(List<WeeklyQuestType> selection) {
        if (selection == null || selection.size() != ACTIVE_QUESTS_PER_WEEK) {
            return false;
        }

        // No debe haber duplicados
        Set<WeeklyQuestType> unique = new HashSet<>(selection);
        if (unique.size() != selection.size()) {
            return false;
        }

        // Todos deben ser tipos válidos
        return ALL_QUEST_TYPES.containsAll(selection);
    }
}
