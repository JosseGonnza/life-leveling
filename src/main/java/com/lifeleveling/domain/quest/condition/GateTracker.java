package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.item.ItemCategory;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.SystemQuestType;

import java.time.LocalDate;
import java.util.*;

/**
 * GateTracker: El cerebro histórico del jugador.
 * Gestiona el progreso de Gates, Rachas y Estadísticas acumuladas.
 * Provee datos a las GateConditions para evaluar el éxito.
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

    // ========================================================================================
    // [NUEVO] FASE 4.2: TRACKER FINANCIERO (THE VAULT)
    // ========================================================================================

    /** Días consecutivos cumpliendo la regla: >= 20k Gold Y Sin compras de Lujo. */
    private int consecutiveDaysAbove20k = 0;

    /** Fecha de la última compra de categoría LUXURY (para resetear la racha). */
    private LocalDate lastLuxuryPurchaseDate = null;

    // ========================================================================================

    // Rachas vivas
    private int consecutiveWeeksWithFullWeeklies = 0;
    private int currentDebuffFreeStreak = 0;
    private int currentPerfectDayStreak = 0;
    private int maxPerfectDayStreak = 0;  // Récord histórico

    // Flags diarios (Memoria a corto plazo, se resetean a las 00:00)
    private boolean perfectDayAchievedToday = false;
    private boolean burnoutOccurredToday = false;

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
            Set<String> completedQuestIds, // IDs de quests completadas (ej: "TIDY", "GYM")
            Map<QuestRank, Integer> completedQuestsCount
    ) {}

    // ========================================================================================
    // MÉTODOS DE INGESTA (GUARDAR DATOS)
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

    // ========================================================================================
    // LÓGICA DE ACTUALIZACIÓN DIARIA (Fase 4.2 y Rachas)
    // ========================================================================================

    /**
     * [NUEVO] FASE 4.2: Registra el balance final del día para 'The Vault'.
     * Incrementa la racha si tienes >= 20,000 G y NO has comprado lujos hoy.
     * @param endOfDayGold El oro que tiene el jugador al momento del reset (00:00).
     */
    public void recordDailyBalance(int endOfDayGold) {
        // Verificamos si hoy (la fecha actual del sistema) se compró lujo
        boolean luxuryBoughtToday = lastLuxuryPurchaseDate != null
                && lastLuxuryPurchaseDate.equals(LocalDate.now());

        if (endOfDayGold >= 20_000 && !luxuryBoughtToday) {
            consecutiveDaysAbove20k++;
            // System.out.println("💰 The Vault Tracker: Día " + consecutiveDaysAbove20k + "/7 completado.");
        } else {
            consecutiveDaysAbove20k = 0;
            // System.out.println("💰 The Vault Tracker: Racha reiniciada (Saldo bajo o Lujo comprado).");
        }
    }

    /**
     * [NUEVO] FASE 4.2: Registra una compra de lujo.
     * Este método debe llamarse desde Inventory.addItem() si la categoría es LUXURY.
     */
    public void recordLuxuryPurchase() {
        this.lastLuxuryPurchaseDate = LocalDate.now();
        this.consecutiveDaysAbove20k = 0; // Castigo inmediato: Adiós racha
    }

    // ========================================================================================
    // QUERIES HISTÓRICAS
    // ========================================================================================

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
        if (burnoutOccurredToday) {
            count++;
        }
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
    // RACHAS (STREAKS)
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

    public int getPerfectDayStreak() {
        return currentPerfectDayStreak;
    }

    public void incrementPerfectDayStreak() {
        currentPerfectDayStreak++;
        if (currentPerfectDayStreak > maxPerfectDayStreak) {
            maxPerfectDayStreak = currentPerfectDayStreak;
        }
    }

    public void resetPerfectDayStreak() {
        currentPerfectDayStreak = 0;
    }

    public int getMaxPerfectDayStreak() {
        return maxPerfectDayStreak;
    }

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
    // GETTERS & SETTERS SIMPLES (FLAGS DIARIOS)
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

    // [NUEVO] Getters para The Vault
    public int getConsecutiveDaysAbove20k() { return consecutiveDaysAbove20k; }
    public LocalDate getLastLuxuryPurchaseDate() { return lastLuxuryPurchaseDate; }
}