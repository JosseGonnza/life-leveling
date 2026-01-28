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

    // [FASE 9] Rachas vivas (se actualizan al momento, no solo al final del día)
    private int consecutiveWeeksWithFullWeeklies = 0;
    private int currentDebuffFreeStreak = 0; // Nueva variable de estado

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

        // [FASE 9] Lógica de backup: Si por lo que sea el contador vivo falla,
        // podríamos recalcular o validar aquí, pero confiamos en el update diario.
    }

    public void markGateAsCompleted(SystemQuestType gateType) {
        completedGates.add(gateType);
    }

    public void setTotalCareerHours(double hours) {
        this.currentTotalCareerHours = hours;
    }

    // ========================================================================================
    // GESTIÓN DE RACHA DE DEBUFFS (FASE 9 - LIVE UPDATES)
    // ========================================================================================

    /**
     * Obtiene la racha actual de días sin debuffs.
     * Es instantáneo y refleja si fallaste hoy.
     */
    public int getDebuffFreeStreak() {
        return currentDebuffFreeStreak;
    }

    /**
     * [CRÍTICO] Se llama DESDE EL PLAYER/DEBUFF_TRACKER cuando se aplica un castigo.
     * Rompe la racha inmediatamente.
     */
    public void notifyDebuffReceived() {
        if (this.currentDebuffFreeStreak > 0) {
            // System.out.println("💔 Racha de pureza rota. Vuelves a 0 días."); // Log opcional
        }
        this.currentDebuffFreeStreak = 0;
    }

    /**
     * Se llama al cierre del día (Daily Reset) si el jugador terminó limpio.
     */
    public void incrementDebuffFreeStreak() {
        this.currentDebuffFreeStreak++;
    }

    /**
     * Setter para restaurar desde persistencia (JSON).
     */
    public void setDebuffFreeStreak(int streak) {
        this.currentDebuffFreeStreak = streak;
    }

    // ========================================================================================
    // LÓGICA DE NEGOCIO (QUERIES)
    // ========================================================================================

    public int getPerfectDayStreak() {
        LocalDate dateCursor = LocalDate.now().minusDays(1);
        int streak = 0;
        while (history.containsKey(dateCursor)) {
            if (history.get(dateCursor).perfectDayAchieved()) {
                streak++;
                dateCursor = dateCursor.minusDays(1);
            } else {
                break;
            }
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
        return (int) history.tailMap(startDate).values().stream()
                .filter(DailyHistory::hadBurnout).count();
    }

    public boolean hadBurnoutDuringGate(String gateId) {
        LocalDate startDate = getTimeLimitStartDate(gateId);
        if (startDate == null) return false;
        return history.tailMap(startDate).values().stream()
                .anyMatch(DailyHistory::hadBurnout);
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

    // Racha de Semanas (Elder Quest 6)
    public int getConsecutiveWeeksWithFullWeeklies() { return consecutiveWeeksWithFullWeeklies; }
    public void setConsecutiveWeeksWithFullWeeklies(int w) { this.consecutiveWeeksWithFullWeeklies = w; }

    public void recordWeeklyQuestResult(boolean allCompleted) {
        if (allCompleted) consecutiveWeeksWithFullWeeklies++;
        else consecutiveWeeksWithFullWeeklies = 0;
    }
}