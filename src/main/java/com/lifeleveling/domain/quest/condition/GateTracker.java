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

    // [FASE 1.2] Flag para Burnout ocurrido HOY
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
            Set<String> completedQuestIds, // [NUEVO] IDs de quests completadas (ej: "TIDY", "GYM")
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
    // GESTIÓN DE FLAGS DIARIOS
    // ========================================================================================

    public boolean isPerfectDayAchievedToday() { return perfectDayAchievedToday; }
    public void setPerfectDayAchievedToday(boolean achieved) { this.perfectDayAchievedToday = achieved; }

    public void recordBurnoutToday() {
        this.burnoutOccurredToday = true;
        notifyDebuffReceived(); // Rompe racha de pureza
    }

    public boolean didBurnoutOccurToday() { return burnoutOccurredToday; }

    /**
     * IMPORTANTE: Llamar a esto en el Daily Reset (00:00).
     */
    public void resetDailyFlags() {
        this.perfectDayAchievedToday = false;
        this.burnoutOccurredToday = false;
    }

    // ========================================================================================
    // LÓGICA DE NEGOCIO (RACHAS Y CASTIGOS)
    // ========================================================================================

    /**
     * Calcula cuántos días consecutivos (hacia atrás desde ayer) NO se ha completado una quest.
     * Útil para triggers de castigo (ej: 3 días sin TIDY -> CAOS).
     */
    public int getDaysSinceLastQuestCompletion(String questId) {
        int days = 0;
        LocalDate cursor = LocalDate.now().minusDays(1); // Empezamos a mirar desde ayer

        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            if (day.completedQuestIds().contains(questId)) {
                break; // Encontramos el día que sí lo hizo, paramos de contar
            }
            days++;
            cursor = cursor.minusDays(1);
        }
        return days;
    }

    /**
     * Calcula cuántos días consecutivos se ha trabajado (CODE o CareerHours > 0).
     * Útil para trigger de BOREDOM (7 días seguidos).
     */
    public int getConsecutiveWorkDays() {
        int streak = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);

        while (history.containsKey(cursor)) {
            DailyHistory day = history.get(cursor);
            boolean worked = day.careerHours() > 0 || day.completedQuestIds().contains("CODE");

            if (worked) {
                streak++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    // ========================================================================================
    // OTRAS QUERIES
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
        if (perfectDayAchievedToday) streak++;
        return streak;
    }

    public int getDebuffFreeStreak() { return currentDebuffFreeStreak; }
    public void notifyDebuffReceived() { this.currentDebuffFreeStreak = 0; }
    public void incrementDebuffFreeStreak() { this.currentDebuffFreeStreak++; }
    public void setDebuffFreeStreak(int s) { this.currentDebuffFreeStreak = s; }

    // Getters simples...
    public double getTotalCareerEngineHours() { return currentTotalCareerHours; }
    public boolean isGateCompleted(SystemQuestType gateType) { return completedGates.contains(gateType); }
    public LocalDate getTimeLimitStartDate(String id) { return timeLimitStartDates.get(id); }
    public void setTimeLimitStartDate(String id, LocalDate d) { timeLimitStartDates.put(id, d); }
    public void clearTimeLimit(String id) { timeLimitStartDates.remove(id); }
    public int getConsecutiveWeeksWithFullWeeklies() { return consecutiveWeeksWithFullWeeklies; }
    public void setConsecutiveWeeksWithFullWeeklies(int w) { this.consecutiveWeeksWithFullWeeklies = w; }
}