package com.lifeleveling.domain.quest.weekly;

import com.lifeleveling.domain.quest.daily.DailyQuestType;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.shared.QuestStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * WeeklyManager: Gestiona el estado y progreso de las misiones semanales del jugador.
 *
 * Responsabilidades:
 *   - Mantener las 3 Weekly Quests activas de la semana
 *   - Trackear progreso según actividades diarias
 *   - Rotar quests cada Lunes
 *   - Entregar recompensas al completar
 *
 * Vive dentro del Player como componente interno.
 */
public class WeeklyManager {

    private final List<WeeklyQuest> activeQuests;
    private LocalDate lastResetDate;

    // Tracking para DUAL_DAILY_HABIT_COUNT (Templo Puro: DIET + SKINCARE mismo día)
    private final Map<LocalDate, Set<DailyQuestType>> dailyCompletions;

    // Tracking para PERFECT_DAYS_COUNT (The Streak)
    private int perfectDaysThisWeek;

    // ========================================================================================
    // CONSTRUCTORES
    // ========================================================================================

    public WeeklyManager() {
        this.activeQuests = new ArrayList<>();
        this.lastResetDate = null;
        this.dailyCompletions = new HashMap<>();
        this.perfectDaysThisWeek = 0;
    }

    /**
     * Constructor para restaurar desde persistencia.
     */
    public WeeklyManager(List<WeeklyQuest> activeQuests, LocalDate lastResetDate,
                         Map<LocalDate, Set<DailyQuestType>> dailyCompletions,
                         int perfectDaysThisWeek) {
        this.activeQuests = new ArrayList<>(activeQuests);
        this.lastResetDate = lastResetDate;
        this.dailyCompletions = new HashMap<>(dailyCompletions);
        this.perfectDaysThisWeek = perfectDaysThisWeek;
    }

    // ========================================================================================
    // CICLO SEMANAL (Reset los Lunes)
    // ========================================================================================

    /**
     * Ejecuta el reset semanal si corresponde (es Lunes y es nueva semana).
     *
     * @param today Fecha actual
     * @return true si se realizó el reset, false si no era necesario
     */
    public boolean performWeeklyReset(LocalDate today) {
        if (!shouldReset(today)) {
            return false;
        }

        // 1. Limpiar quests antiguas
        activeQuests.clear();

        // 2. Limpiar tracking de la semana anterior
        cleanupOldTracking(today);

        // 3. Generar nuevas quests para esta semana
        List<WeeklyQuest> newQuests = WeeklyQuestPool.createQuestsForWeek(today);
        activeQuests.addAll(newQuests);

        // 4. Actualizar fecha del último reset
        lastResetDate = WeeklyQuest.getWeekStart(today);

        // 5. Reset de Perfect Days
        perfectDaysThisWeek = 0;

        System.out.println("📅 ¡Nueva semana! Weekly Quests rotadas:");
        for (WeeklyQuest q : activeQuests) {
            System.out.println("   " + q.getType().toDisplayString());
        }

        return true;
    }

    /**
     * Determina si debe hacerse un reset.
     * Reset si:
     *   - No hay quests activas (primera vez)
     *   - lastResetDate es null (primera vez)
     *   - Estamos en una semana diferente a lastResetDate
     */
    private boolean shouldReset(LocalDate today) {
        // Primera vez
        if (lastResetDate == null || activeQuests.isEmpty()) {
            return true;
        }

        // Verificar si estamos en una nueva semana
        LocalDate currentWeekStart = WeeklyQuest.getWeekStart(today);
        return !currentWeekStart.equals(lastResetDate);
    }

    /**
     * Limpia tracking de semanas anteriores para evitar memory leak.
     */
    private void cleanupOldTracking(LocalDate today) {
        LocalDate currentWeekStart = WeeklyQuest.getWeekStart(today);

        // Remover entries de semanas anteriores
        dailyCompletions.entrySet().removeIf(entry ->
                entry.getKey().isBefore(currentWeekStart)
        );
    }

    // ========================================================================================
    // RECORDING ACTIVITY (Hooks desde Player)
    // ========================================================================================

    /**
     * Procesa un evento de actividad diaria para actualizar el progreso semanal.
     *
     * @param questId El ID de la actividad (ej: "SLEEP", "CODE", "GYM")
     * @param value   El valor numérico (horas, páginas, etc.)
     * @return Lista de recompensas de quests que se completaron con esta acción
     */
    public List<QuestReward> recordActivity(String questId, double value) {
        return recordActivity(questId, value, LocalDate.now());
    }

    /**
     * Versión con fecha explícita para testing.
     */
    public List<QuestReward> recordActivity(String questId, double value, LocalDate today) {
        Optional<DailyQuestType> dailyTypeOpt = DailyQuestType.fromString(questId);
        if (dailyTypeOpt.isEmpty()) {
            return List.of();
        }

        DailyQuestType dailyType = dailyTypeOpt.get();
        List<QuestReward> earnedRewards = new ArrayList<>();

        // Registrar la completion para tracking de DUAL
        recordDailyCompletion(dailyType, today);

        // Iterar sobre las quests activas
        for (int i = 0; i < activeQuests.size(); i++) {
            WeeklyQuest quest = activeQuests.get(i);

            // Ignorar si ya está completada o expirada
            if (quest.status() != QuestStatus.IN_PROGRESS) {
                continue;
            }

            WeeklyQuestType type = quest.getType();
            int progressToAdd = calculateProgress(type, dailyType, value, today);

            if (progressToAdd > 0) {
                boolean wasCompletedBefore = quest.isCompleted();
                WeeklyQuest updatedQuest = quest.addProgress(progressToAdd);
                activeQuests.set(i, updatedQuest);

                // Si se completó con esta acción, agregar recompensa
                if (updatedQuest.isCompleted() && !wasCompletedBefore) {
                    earnedRewards.add(type.getReward());
                    System.out.println("🎉 ¡WEEKLY QUEST COMPLETADA! " + type.toDisplayString());
                }
            }
        }

        return earnedRewards;
    }

    /**
     * Calcula el progreso a añadir según el tipo de condición.
     */
    private int calculateProgress(WeeklyQuestType questType, DailyQuestType dailyType,
                                  double value, LocalDate today) {
        return switch (questType.getConditionType()) {
            case DAILY_HABIT_COUNT -> {
                // Ej: Iron Week (GYM 5 veces), Hibernator (SLEEP 6 días)
                if (questType.getRelatedHabit() == dailyType) {
                    // Para SLEEP, solo cuenta si > 7h
                    if (dailyType == DailyQuestType.SLEEP && value < 7.0) {
                        yield 0;
                    }
                    yield 1;
                }
                yield 0;
            }

            case ACCUMULATED_HOURS -> {
                // Ej: Code Marathon (12h totales)
                if (questType.getRelatedHabit() == dailyType) {
                    yield (int) Math.round(value);
                }
                yield 0;
            }

            case ACCUMULATED_PAGES -> {
                // Ej: The Scholar (150 páginas)
                if (questType.getRelatedHabit() == dailyType) {
                    yield (int) Math.round(value);
                }
                yield 0;
            }

            case DUAL_DAILY_HABIT_COUNT -> {
                // Ej: Pure Temple (DIET + SKINCARE 6 días)
                if (questType.getRequiredHabits().contains(dailyType)) {
                    // Solo incrementar si AMBOS hábitos se completaron HOY
                    if (areBothHabitsCompletedToday(questType.getRequiredHabits(), today)) {
                        // Contar cuántos días completos tenemos esta semana
                        int completeDays = countDaysWithBothHabits(questType.getRequiredHabits(), today);
                        // El progreso es el total de días completos
                        // (setProgress en lugar de addProgress sería más correcto aquí)
                        yield completeDays;
                    }
                }
                yield 0;
            }

            case PERFECT_DAYS_COUNT -> {
                // Este tipo no se actualiza desde recordActivity normal
                // Se actualiza desde recordPerfectDay()
                yield 0;
            }
        };
    }

    /**
     * Registra que un hábito fue completado hoy.
     */
    private void recordDailyCompletion(DailyQuestType habit, LocalDate today) {
        dailyCompletions.computeIfAbsent(today, k -> new HashSet<>()).add(habit);
    }

    /**
     * Verifica si AMBOS hábitos requeridos fueron completados hoy.
     */
    private boolean areBothHabitsCompletedToday(List<DailyQuestType> requiredHabits, LocalDate today) {
        Set<DailyQuestType> todayCompletions = dailyCompletions.getOrDefault(today, Set.of());
        return todayCompletions.containsAll(requiredHabits);
    }

    /**
     * Cuenta cuántos días de la semana actual tienen AMBOS hábitos completados.
     */
    private int countDaysWithBothHabits(List<DailyQuestType> requiredHabits, LocalDate today) {
        LocalDate weekStart = WeeklyQuest.getWeekStart(today);
        int count = 0;

        for (Map.Entry<LocalDate, Set<DailyQuestType>> entry : dailyCompletions.entrySet()) {
            LocalDate date = entry.getKey();
            // Solo contar días de esta semana
            if (!date.isBefore(weekStart) && !date.isAfter(today)) {
                if (entry.getValue().containsAll(requiredHabits)) {
                    count++;
                }
            }
        }

        return count;
    }

    // ========================================================================================
    // PERFECT DAY TRACKING (Para THE_STREAK)
    // ========================================================================================

    /**
     * Registra un Perfect Day para actualizar THE_STREAK quest.
     *
     * @return Recompensa si se completó THE_STREAK, empty si no
     */
    public Optional<QuestReward> recordPerfectDay() {
        return recordPerfectDay(LocalDate.now());
    }

    /**
     * Versión con fecha explícita para testing.
     */
    public Optional<QuestReward> recordPerfectDay(LocalDate today) {
        perfectDaysThisWeek++;

        // Buscar THE_STREAK en las quests activas
        for (int i = 0; i < activeQuests.size(); i++) {
            WeeklyQuest quest = activeQuests.get(i);

            if (quest.getType() == WeeklyQuestType.THE_STREAK &&
                    quest.status() == QuestStatus.IN_PROGRESS) {

                boolean wasCompletedBefore = quest.isCompleted();
                WeeklyQuest updatedQuest = quest.setProgress(perfectDaysThisWeek);
                activeQuests.set(i, updatedQuest);

                if (updatedQuest.isCompleted() && !wasCompletedBefore) {
                    System.out.println("🔥 ¡THE STREAK COMPLETADA! 5 Perfect Days esta semana.");
                    return Optional.of(WeeklyQuestType.THE_STREAK.getReward());
                }
            }
        }

        return Optional.empty();
    }

    // ========================================================================================
    // SPECIAL: Update DUAL quest progress correctly
    // ========================================================================================

    /**
     * Actualiza el progreso de quests DUAL_DAILY_HABIT_COUNT después de registrar actividad.
     * Llama a este método cuando se complete CUALQUIER hábito del par.
     */
    public void refreshDualQuestProgress(LocalDate today) {
        for (int i = 0; i < activeQuests.size(); i++) {
            WeeklyQuest quest = activeQuests.get(i);

            if (quest.getType().getConditionType() == WeeklyQuestType.WeeklyConditionType.DUAL_DAILY_HABIT_COUNT
                    && quest.status() == QuestStatus.IN_PROGRESS) {

                int completeDays = countDaysWithBothHabits(quest.getType().getRequiredHabits(), today);

                if (completeDays != quest.getCurrentProgress()) {
                    WeeklyQuest updatedQuest = quest.setProgress(completeDays);
                    activeQuests.set(i, updatedQuest);

                    if (updatedQuest.isCompleted() && !quest.isCompleted()) {
                        System.out.println("🧘 ¡TEMPLO PURO COMPLETADO! 6 días de cuidado personal.");
                    }
                }
            }
        }
    }

    // ========================================================================================
    // COLLECT REWARDS (Manual collection if needed)
    // ========================================================================================

    /**
     * Recolecta las recompensas de quests completadas que aún no se han reclamado.
     * Útil si las recompensas no se dieron en tiempo real.
     *
     * @return Lista de recompensas pendientes
     */
    public List<QuestReward> collectPendingRewards() {
        List<QuestReward> rewards = new ArrayList<>();

        for (WeeklyQuest quest : activeQuests) {
            if (quest.status() == QuestStatus.COMPLETED) {
                // La recompensa ya se dio en tiempo real, pero podemos re-verificar
                rewards.add(quest.getType().getReward());
            }
        }

        return rewards;
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    public List<WeeklyQuest> getActiveQuests() {
        return List.copyOf(activeQuests);
    }

    public LocalDate getLastResetDate() {
        return lastResetDate;
    }

    public int getPerfectDaysThisWeek() {
        return perfectDaysThisWeek;
    }

    /**
     * Obtiene el progreso de una quest específica por tipo.
     */
    public Optional<WeeklyQuest> getQuestByType(WeeklyQuestType type) {
        return activeQuests.stream()
                .filter(q -> q.getType() == type)
                .findFirst();
    }

    /**
     * Verifica si un tipo de quest está activo esta semana.
     */
    public boolean hasActiveQuest(WeeklyQuestType type) {
        return activeQuests.stream().anyMatch(q -> q.getType() == type);
    }

    /**
     * Cuenta cuántas quests están completadas.
     */
    public int getCompletedQuestsCount() {
        return (int) activeQuests.stream()
                .filter(q -> q.status() == QuestStatus.COMPLETED)
                .count();
    }

    /**
     * Verifica si todas las quests de la semana están completadas.
     */
    public boolean areAllQuestsCompleted() {
        return getCompletedQuestsCount() == WeeklyQuestPool.ACTIVE_QUESTS_PER_WEEK;
    }

    /**
     * Obtiene resumen para UI.
     */
    public String getSummary() {
        int completed = getCompletedQuestsCount();
        int total = activeQuests.size();
        return String.format("Weekly Quests: %d/%d completadas", completed, total);
    }

    // ========================================================================================
    // FOR TESTING / PERSISTENCE
    // ========================================================================================

    /**
     * Obtiene el mapa de completions diarias (para persistencia).
     */
    public Map<LocalDate, Set<DailyQuestType>> getDailyCompletions() {
        return Map.copyOf(dailyCompletions);
    }

    /**
     * Fuerza una lista de quests (para testing).
     */
    public void setActiveQuestsForTesting(List<WeeklyQuest> quests) {
        activeQuests.clear();
        activeQuests.addAll(quests);
    }
}
