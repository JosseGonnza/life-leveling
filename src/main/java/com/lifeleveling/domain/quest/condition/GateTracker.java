package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.SystemQuestType;

import java.time.LocalDate;
import java.util.*;

/**
 * GateTracker: El cerebro histórico del jugador.
 * Gestiona el progreso de Gates, Rachas y Estadísticas acumuladas.
 */
public class GateTracker {

    // Historial de días pasados
    private final TreeMap<LocalDate, DailyHistory> history = new TreeMap<>();

    // Estado de Gates
    private final Set<SystemQuestType> completedGates = new HashSet<>();
    private final Set<String> manualConfirmations = new HashSet<>();
    private final Map<String, LocalDate> timeLimitStartDates = new HashMap<>();

    // --- CONTADORES EN TIEMPO REAL (STATEFUL) ---
    private double currentTotalCareerHours = 0.0;

    // Rachas vivas
    private int consecutiveWeeksWithFullWeeklies = 0;
    private int currentDebuffFreeStreak = 0;

    // [FASE 11] Cerrojo de Perfect Day (Memoria a corto plazo)
    private boolean perfectDayAchievedToday = false;

    // [FASE 1.2] Flag para Burnout ocurrido HOY (Memoria a corto plazo)
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

    // ========================================================================================
    // GESTIÓN DE PERFECT DAY & FLAGS DIARIOS
    // ========================================================================================

    public boolean isPerfectDayAchievedToday() {
        return perfectDayAchievedToday;
    }

    public void setPerfectDayAchievedToday(boolean achieved) {
        this.perfectDayAchievedToday = achieved;
    }

    /**
     * IMPORTANTE: Llamar a esto en el Daily Reset (00:00) para limpiar memoria a corto plazo.
     */
    public void resetDailyFlags() {
        this.perfectDayAchievedToday = false;
        this.burnoutOccurredToday = false; // [NUEVO] Reseteamos el flag de burnout diario
    }

    // ========================================================================================
    // GESTIÓN DE BURNOUT (FASE 1.2)
    // ========================================================================================

    /**
     * Registra que ha ocurrido un Burnout HOY.
     * Llamado desde Player.triggerBurnout().
     */
    public void recordBurnoutToday() {
        this.burnoutOccurredToday = true;
        // Al quemarse, también se rompe la racha de pureza inmediatamente
        notifyDebuffReceived();
    }

    public boolean didBurnoutOccurToday() {
        return burnoutOccurredToday;
    }

    // ========================================================================================
    // GESTIÓN DE RACHA DE DEBUFFS
    // ========================================================================================

    public int getDebuffFreeStreak() {
        return currentDebuffFreeStreak;
    }

    public void notifyDebuffReceived() {
        this.currentDebuffFreeStreak = 0;
    }

    public void incrementDebuffFreeStreak() {
        this.currentDebuffFreeStreak++;
    }

    public void setDebuffFreeStreak(int streak) {
        this.currentDebuffFreeStreak = streak;
    }

    // ========================================================================================
    // LÓGICA DE NEGOCIO (QUERIES)
    // ========================================================================================

    public int getPerfectDayStreak() {
        LocalDate dateCursor = LocalDate.now().minusDays(1);
        int streak = 0;

        // 1. Historia pasada
        while (history.containsKey(dateCursor)) {
            if (history.get(dateCursor).perfectDayAchieved()) {
                streak++;
                dateCursor = dateCursor.minusDays(1);
            } else {
                break;
            }
        }

        // 2. Si hoy ya lo conseguimos, lo sumamos visualmente
        if (perfectDayAchievedToday) {
            streak++;
        }

        return streak;
    }

    public int getMinHPInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(DailyHistory::minHP)
                .min().orElse(100);
    }

    public int getUserQuestsCompletedInLastDays(int days, QuestRank minRank) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(day -> day.completedQuestsCount().entrySet().stream()
                        .filter(entry -> entry.getKey().isAtLeast(minRank))
                        .mapToInt(Map.Entry::getValue).sum())
                .sum();
    }

    public int getPagesReadInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(DailyHistory::pagesRead).sum();
    }

    public boolean hasConsumablePurchaseInLastDays(String consumableId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .anyMatch(day -> day.consumablesBought().contains(consumableId));
    }

    public int getBurnoutsInLastMonth() {
        LocalDate startDate = LocalDate.now().minusDays(30);

        // 1. Contar históricos (ayer hacia atrás)
        int count = (int) history.tailMap(startDate).values().stream()
                .filter(DailyHistory::hadBurnout)
                .count();

        // 2. [FIX] Sumar el de hoy si acaba de ocurrir
        if (burnoutOccurredToday) {
            count++;
        }

        return count;
    }

    public boolean hadBurnoutDuringGate(String gateId) {
        LocalDate startDate = getTimeLimitStartDate(gateId);
        if (startDate == null) return false;

        // Revisar histórico
        boolean historicBurnout = history.tailMap(startDate).values().stream()
                .anyMatch(DailyHistory::hadBurnout);

        // Revisar hoy (si la gate empezó hoy o antes)
        if (burnoutOccurredToday && !LocalDate.now().isBefore(startDate)) {
            return true;
        }

        return historicBurnout;
    }

    // ========================================================================================
    // GETTERS & SETTERS SIMPLES
    // ========================================================================================

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
}