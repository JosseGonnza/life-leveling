package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.item.ItemCategory;
import com.lifeleveling.domain.quest.daily.DailyQuestType; // [NUEVO] Import necesario
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.SystemQuestType;

import java.time.LocalDate;
import java.util.*;

/**
 * GateTracker: El cerebro histórico del jugador.
 * Gestiona el progreso de Gates, Rachas y Estadísticas acumuladas.
 * Provee datos a las GateConditions y al TitleUnlockChecker.
 */
public class GateTracker {

    // Historial de días pasados (Persistencia)
    private final TreeMap<LocalDate, DailyHistory> history = new TreeMap<>();

    // Estado de Gates y Confirmaciones
    private final Set<SystemQuestType> completedGates = new HashSet<>();
    private final Set<String> manualConfirmations = new HashSet<>();
    private final Map<String, LocalDate> timeLimitStartDates = new HashMap<>();

    // --- CONTADORES EN TIEMPO REAL (STATEFUL) ---
    private double currentTotalCareerHours = 0.0;

    // [NUEVO FASE 7] Contador de gasto total para títulos de Riqueza (Ballena)
    private long totalGoldSpent = 0;

    // ========================================================================================
    // FASE 4.2: TRACKER FINANCIERO (THE VAULT)
    // ========================================================================================

    private int consecutiveDaysAbove20k = 0;
    private LocalDate lastLuxuryPurchaseDate = null;

    // ========================================================================================

    // Rachas vivas
    private int consecutiveWeeksWithFullWeeklies = 0;
    private int currentDebuffFreeStreak = 0;
    private int currentPerfectDayStreak = 0;
    private int maxPerfectDayStreak = 0;

    // Flags diarios
    private boolean perfectDayAchievedToday = false;
    private boolean burnoutOccurredToday = false;

    // Acumuladores de la actividad del DÍA EN CURSO. Se vuelcan en un DailyHistory al cerrar el día
    // (closeDay) y se resetean. Sin esto, las ventanas temporales (getXInLastDays) leen histórico vacío.
    private int todayPagesRead = 0;
    private double todayCareerHours = 0.0;
    private int todayMinHP = 100;
    private boolean todayHadDebuffsActive = false;
    private boolean todayJunkConsumed = false;
    private final List<String> todayConsumablesBought = new ArrayList<>();
    private final Set<String> todayCompletedQuestIds = new HashSet<>();
    private final Map<QuestRank, Integer> todayCompletedQuestsCount = new HashMap<>();

    public GateTracker() {}

    // ========================================================================================
    // CLASE INTERNA: DAILY SNAPSHOT
    // ========================================================================================
    public record DailyHistory(
            LocalDate date,
            boolean perfectDayAchieved,
            int minHP,
            int pagesRead,
            double careerHours,
            boolean hadBurnout,
            boolean hadDebuffsActive,
            List<String> consumablesBought,
            Set<String> completedQuestIds,
            Map<QuestRank, Integer> completedQuestsCount
    ) {}

    // ========================================================================================
    // MÉTODOS DE INGESTA
    // ========================================================================================

    public void addDailyHistory(DailyHistory day) {
        history.put(day.date(), day);
    }

    public void markGateAsCompleted(SystemQuestType gateType) {
        completedGates.add(gateType);
    }

    public void setTotalCareerHours(double hours) {
        this.currentTotalCareerHours = hours;
    }

    /**
     * [NUEVO FASE 7] Registra gasto de oro para estadísticas acumuladas.
     * Llamar desde Player.spendGold().
     */
    public void recordGoldSpent(int amount) {
        if (amount > 0) {
            this.totalGoldSpent += amount;
        }
    }

    public void recordDailyBalance(int endOfDayGold) {
        boolean luxuryBoughtToday = lastLuxuryPurchaseDate != null
                && lastLuxuryPurchaseDate.equals(LocalDate.now());

        if (endOfDayGold >= 20_000 && !luxuryBoughtToday) {
            consecutiveDaysAbove20k++;
        } else {
            consecutiveDaysAbove20k = 0;
        }
    }

    public void recordLuxuryPurchase() {
        this.lastLuxuryPurchaseDate = LocalDate.now();
        this.consecutiveDaysAbove20k = 0;
    }

    // ---- Acumuladores de actividad del día en curso ----

    public void recordPagesRead(int pages) {
        if (pages > 0) this.todayPagesRead += pages;
    }

    public void recordCareerHoursToday(double hours) {
        if (hours > 0) this.todayCareerHours += hours;
    }

    public void recordConsumableBought(String itemId) {
        if (itemId != null) this.todayConsumablesBought.add(itemId);
    }

    public void recordUserQuestCompleted(QuestRank rank, String questId) {
        if (questId != null) this.todayCompletedQuestIds.add(questId);
        if (rank != null) this.todayCompletedQuestsCount.merge(rank, 1, Integer::sum);
    }

    public void recordDailyQuestCompleted(String questId) {
        if (questId != null) this.todayCompletedQuestIds.add(questId);
    }

    /** Marca que hoy se ha consumido Comida Basura (Logic-Lock de DIET, Biblia 2.1.2). */
    public void recordJunkFoodConsumed() {
        this.todayJunkConsumed = true;
    }

    public boolean wasJunkConsumedToday() {
        return todayJunkConsumed;
    }

    /** Cuántos de los 7 hábitos diarios se han completado hoy (fuente del Perfect Day 7/7). */
    public int getDailyHabitsCompletedTodayCount() {
        int count = 0;
        for (DailyQuestType habit : DailyQuestType.values()) {
            if (todayCompletedQuestIds.contains(habit.name())) count++;
        }
        return count;
    }

    /** ¿Se ha completado hoy este hábito diario? (para el checklist de la Home). */
    public boolean isHabitCompletedToday(DailyQuestType habit) {
        return todayCompletedQuestIds.contains(habit.name());
    }

    public void recordHPSnapshot(int hp) {
        if (hp < this.todayMinHP) this.todayMinHP = hp;
    }

    public void setDebuffsActiveToday(boolean active) {
        if (active) this.todayHadDebuffsActive = true;
    }

    /**
     * Cierra el día: vuelca la actividad acumulada en un DailyHistory consultable por las
     * GateConditions y reinicia los acumuladores. El orquestador (capa app) lo invoca en el rollover.
     */
    public void closeDay(LocalDate date, int endOfDayGold) {
        DailyHistory snapshot = new DailyHistory(
                date,
                perfectDayAchievedToday,
                todayMinHP,
                todayPagesRead,
                todayCareerHours,
                burnoutOccurredToday,
                todayHadDebuffsActive,
                List.copyOf(todayConsumablesBought),
                Set.copyOf(todayCompletedQuestIds),
                Map.copyOf(todayCompletedQuestsCount)
        );
        addDailyHistory(snapshot);
        recordDailyBalance(endOfDayGold);
        resetDailyAccumulators();
    }

    private void resetDailyAccumulators() {
        this.todayPagesRead = 0;
        this.todayCareerHours = 0.0;
        this.todayMinHP = 100;
        this.todayHadDebuffsActive = false;
        this.todayJunkConsumed = false;
        this.todayConsumablesBought.clear();
        this.todayCompletedQuestIds.clear();
        this.todayCompletedQuestsCount.clear();
    }

    // ========================================================================================
    // QUERIES HISTÓRICAS BÁSICAS
    // ========================================================================================

    /** Días cerrados de los últimos {@code days}, del más reciente al más antiguo (para el Journal). */
    public List<DailyHistory> getRecentDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<DailyHistory> recent = new ArrayList<>(history.tailMap(startDate).values());
        Collections.reverse(recent);
        return recent;
    }

    public int getMinHPInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(DailyHistory::minHP)
                .min().orElse(100);
    }

    public int getBurnoutsInLastMonth() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        int count = (int) history.tailMap(startDate).values().stream()
                .filter(DailyHistory::hadBurnout)
                .count();
        if (burnoutOccurredToday) count++;
        return count;
    }

    public boolean hadBurnoutDuringGate(String gateId) {
        LocalDate startDate = getTimeLimitStartDate(gateId);
        if (startDate == null) return false;
        boolean historic = history.tailMap(startDate).values().stream()
                .anyMatch(DailyHistory::hadBurnout);
        boolean today = burnoutOccurredToday && !LocalDate.now().isBefore(startDate);
        return historic || today;
    }

    public boolean hasCategoryPurchaseInLastDays(ItemCategory category, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .flatMap(day -> day.consumablesBought().stream())
                .map(ItemCatalog::findById)
                .flatMap(Optional::stream)
                .anyMatch(item -> item.category() == category);
    }

    public double getCareerHoursInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToDouble(DailyHistory::careerHours)
                .sum();
    }

    public int getDailyQuestCompletionsInLastDays(String questId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return (int) history.tailMap(startDate).values().stream()
                .filter(day -> day.completedQuestIds() != null && day.completedQuestIds().contains(questId))
                .count();
    }

    public int getPerfectDaysCountInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        int count = (int) history.tailMap(startDate).values().stream()
                .filter(DailyHistory::perfectDayAchieved)
                .count();
        if (perfectDayAchievedToday) count++;
        return count;
    }

    public int getUserQuestsCompletedInLastDays(int days, QuestRank minRank) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(day -> day.completedQuestsCount().entrySet().stream()
                        .filter(entry -> entry.getKey().isAtLeast(minRank))
                        .mapToInt(Map.Entry::getValue)
                        .sum())
                .sum();
    }

    public int getPagesReadInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(DailyHistory::pagesRead)
                .sum();
    }

    public boolean hasConsumablePurchaseInLastDays(String itemIdOrCategory, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .anyMatch(day -> day.consumablesBought().contains(itemIdOrCategory));
    }

    public boolean hasConsumedItemInLastDays(String itemId, int days) {
        return hasConsumablePurchaseInLastDays(itemId, days);
    }

    // ========================================================================================
    // QUERIES AVANZADAS (FASE 7 - TITLE UNLOCK CHECKER)
    // ========================================================================================

    public long getTotalGoldSpent() {
        return totalGoldSpent;
    }

    /**
     * Cuenta cuántas veces se ha completado una quest en toda la historia.
     * Útil para títulos como "Lobo Solitario" (50 GYM).
     */
    public int getTotalCompletions(String questId) {
        return (int) history.values().stream()
                .filter(day -> day.completedQuestIds() != null && day.completedQuestIds().contains(questId))
                .count();
    }

    public int getTotalPagesRead() {
        return history.values().stream()
                .mapToInt(DailyHistory::pagesRead)
                .sum();
    }

    /**
     * Calcula la racha actual de un hábito específico.
     * Mira hacia atrás desde hoy/ayer hasta que se rompe la cadena.
     */
    public int getCurrentStreak(String questId) {
        int streak = 0;
        LocalDate cursor = LocalDate.now();

        // 1. Revisar HOY (si ya lo hizo hoy, la racha cuenta hoy)
        // Nota: completedQuestIds() en history solo tiene lo de AYER hacia atrás.
        // Hoy lo verificamos contra los flags o asumiendo que el Player.updateState aún no ha volcado hoy al historial.
        // Para simplificar y ser robustos, miramos el historial:

        // Empezamos mirando ayer, porque el historial se guarda al final del día.
        // Si quisieramos incluir "hoy en tiempo real", necesitaríamos acceder al estado actual del Player,
        // pero GateTracker suele mirar datos consolidados.

        // Ajuste: Si miramos desde ayer hacia atrás.
        cursor = cursor.minusDays(1);

        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            if (day.completedQuestIds() != null && day.completedQuestIds().contains(questId)) {
                streak++;
            } else {
                break; // Racha rota
            }
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * Calcula racha de DOS hábitos simultáneos (para Templo Sagrado).
     */
    public int getDualHabitStreak(DailyQuestType h1, DailyQuestType h2) {
        if (h1 == null || h2 == null) return 0;
        String id1 = h1.name();
        String id2 = h2.name();

        int streak = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);

        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            Set<String> completed = day.completedQuestIds();
            if (completed != null && completed.contains(id1) && completed.contains(id2)) {
                streak++;
            } else {
                break;
            }
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * Calcula días consecutivos con HP bajo (Zombie Title).
     */
    public int getCurrentLowHPStreak(int maxThreshold) {
        int streak = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);

        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            if (day.minHP() < maxThreshold) {
                streak++;
            } else {
                break;
            }
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * Calcula días consecutivos con HP alto (Inmortal Title).
     */
    public int getCurrentHighHPStreak(int minThreshold) {
        int streak = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);

        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            // Asumimos que si no hubo burnout y el minHP fue alto, cuenta.
            if (day.minHP() >= minThreshold && !day.hadBurnout()) {
                streak++;
            } else {
                break;
            }
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public int getBurnoutsRecoveredCount() {
        return (int) history.values().stream().filter(DailyHistory::hadBurnout).count();
    }

    public boolean isElderQuestCompleted(int elderNumber) {
        String elderId = "ELDER_" + elderNumber;
        // Buscamos si alguna vez se completó esta Elder Quest
        return history.values().stream()
                .anyMatch(day -> day.completedQuestIds() != null && day.completedQuestIds().contains(elderId));
    }

    // ========================================================================================
    // RACHAS GENERALES (STREAKS)
    // ========================================================================================

    public int getDaysSinceLastQuestCompletion(String questId) {
        int days = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);
        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            if (day.completedQuestIds() != null && day.completedQuestIds().contains(questId)) break;
            days++;
            cursor = cursor.minusDays(1);
        }
        return days;
    }

    public int getConsecutiveWorkDays() {
        int streak = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);
        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            boolean worked = day.careerHours() > 0 || (day.completedQuestIds() != null && day.completedQuestIds().contains("CODE"));
            if (worked) streak++; else break;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public int getPerfectDayStreak() { return currentPerfectDayStreak; }

    public void incrementPerfectDayStreak() {
        currentPerfectDayStreak++;
        if (currentPerfectDayStreak > maxPerfectDayStreak) {
            maxPerfectDayStreak = currentPerfectDayStreak;
        }
    }

    public void resetPerfectDayStreak() { currentPerfectDayStreak = 0; }
    public int getMaxPerfectDayStreak() { return maxPerfectDayStreak; }

    public int calculatePerfectDayStreakFromHistory() {
        LocalDate dateCursor = LocalDate.now().minusDays(1);
        int streak = 0;
        while (history.containsKey(dateCursor)) {
            if (history.get(dateCursor).perfectDayAchieved()) streak++; else break;
            dateCursor = dateCursor.minusDays(1);
        }
        if (perfectDayAchievedToday) streak++;
        return streak;
    }

    // ========================================================================================
    // GETTERS & SETTERS SIMPLES
    // ========================================================================================

    public boolean isPerfectDayAchievedToday() { return perfectDayAchievedToday; }
    public void setPerfectDayAchievedToday(boolean v) { this.perfectDayAchievedToday = v; }

    public void recordBurnoutToday() {
        this.burnoutOccurredToday = true;
        notifyDebuffReceived();
    }
    public boolean didBurnoutOccurToday() { return burnoutOccurredToday; }

    public void resetDailyFlags() {
        this.perfectDayAchievedToday = false;
        this.burnoutOccurredToday = false;
    }

    public int getDebuffFreeStreak() { return currentDebuffFreeStreak; }
    public void notifyDebuffReceived() { this.currentDebuffFreeStreak = 0; }
    public void incrementDebuffFreeStreak() { this.currentDebuffFreeStreak++; }
    public void setDebuffFreeStreak(int s) { this.currentDebuffFreeStreak = s; }

    public double getTotalCareerEngineHours() { return currentTotalCareerHours; }
    public boolean isGateCompleted(SystemQuestType gateType) { return completedGates.contains(gateType); }
    public Set<SystemQuestType> getCompletedGates() { return Set.copyOf(completedGates); }

    /** Restaura las rachas de Perfect Day al cargar partida (persistencia). */
    public void restorePerfectDayStreaks(int current, int max) {
        this.currentPerfectDayStreak = current;
        this.maxPerfectDayStreak = max;
    }

    public boolean hasManualConfirmation(String id) { return manualConfirmations.contains(id); }
    public void setManualConfirmation(String id, boolean v) {
        if(v) manualConfirmations.add(id); else manualConfirmations.remove(id);
    }

    public LocalDate getTimeLimitStartDate(String id) { return timeLimitStartDates.get(id); }
    public void setTimeLimitStartDate(String id, LocalDate d) { timeLimitStartDates.put(id, d); }
    public void clearTimeLimit(String id) { timeLimitStartDates.remove(id); }

    public int getConsecutiveWeeksWithFullWeeklies() { return consecutiveWeeksWithFullWeeklies; }
    public void setConsecutiveWeeksWithFullWeeklies(int w) { this.consecutiveWeeksWithFullWeeklies = w; }

    public void recordWeeklyQuestResult(boolean allCompleted) {
        if (allCompleted) consecutiveWeeksWithFullWeeklies++;
        else consecutiveWeeksWithFullWeeklies = 0;
    }

    public int getConsecutiveDaysAbove20k() { return consecutiveDaysAbove20k; }
    public LocalDate getLastLuxuryPurchaseDate() { return lastLuxuryPurchaseDate; }
}