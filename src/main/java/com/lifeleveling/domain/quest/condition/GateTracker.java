package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.SystemQuestType;

import java.time.LocalDate;
import java.util.*;

/**
 * GateTracker: El cerebro histórico del jugador.
 * * Almacena resúmenes diarios (Snapshots) para poder calcular rachas, promedios
 * y acumulados sin tener que recalcular desde cero cada vez.
 */
public class GateTracker {

    // Historial de días pasados (Clave: Fecha, Valor: Resumen del día)
    private final TreeMap<LocalDate, DailyHistory> history = new TreeMap<>();

    // Estado actual de Gates (Gates ya completadas)
    private final Set<SystemQuestType> completedGates = new HashSet<>();

    // Estado de confirmaciones manuales ("Checks" persistentes)
    private final Set<String> manualConfirmations = new HashSet<>();

    // Fechas de inicio para condiciones con TimeLimit
    private final Map<String, LocalDate> timeLimitStartDates = new HashMap<>();

    // Contadores en tiempo real (para el día actual/acumulados globales)
    private double currentTotalCareerHours = 0.0;

    // Constructor vacío
    public GateTracker() {}

    // ========================================================================================
    // CLASE INTERNA: LA FOTO DEL DÍA (LO QUE SE PERSISTE)
    // ========================================================================================
    public record DailyHistory(
            LocalDate date,
            boolean perfectDayAchieved,   // ¿Hizo 7/7?
            int minHP,                    // HP más bajo registrado ese día
            int pagesRead,                // Páginas leídas ese día
            double careerHours,           // Horas trabajadas ese día
            boolean hadBurnout,           // ¿Entró en burnout?
            boolean hadDebuffsActive,     // ¿Tuvo debuffs activos al final del día?
            List<String> consumablesBought, // IDs de items comprados
            Map<QuestRank, Integer> completedQuestsCount // Cuántas quests de cada rango hizo
    ) {}

    // ========================================================================================
    // MÉTODOS DE INGESTA (Para cargar datos desde DB o al cerrar el día)
    // ========================================================================================

    public void addDailyHistory(DailyHistory day) {
        history.put(day.date(), day);
        // También actualizamos acumulados globales si es necesario
    }

    public void markGateAsCompleted(SystemQuestType gateType) {
        completedGates.add(gateType);
    }

    public void setTotalCareerHours(double hours) {
        this.currentTotalCareerHours = hours;
    }

    // ========================================================================================
    // LÓGICA DE NEGOCIO (Responde a las Condiciones)
    // ========================================================================================

    /**
     * Calcula la racha actual de Perfect Days contando hacia atrás desde ayer.
     */
    public int getPerfectDayStreak() {
        LocalDate dateCursor = LocalDate.now().minusDays(1); // Empezamos a mirar desde ayer
        int streak = 0;

        while (history.containsKey(dateCursor)) {
            DailyHistory day = history.get(dateCursor);
            if (day.perfectDayAchieved()) {
                streak++;
                dateCursor = dateCursor.minusDays(1);
            } else {
                break; // Racha rota
            }
        }
        return streak;
    }

    /**
     * Obtiene el HP mínimo registrado en los últimos N días.
     */
    public int getMinHPInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        return history.tailMap(startDate).values().stream()
                .mapToInt(DailyHistory::minHP)
                .min()
                .orElse(100); // Si no hay datos, asumimos que estuvo sano (100)
    }

    /**
     * Obtiene el número de User Quests de rango >= minRank completadas en los últimos N días.
     */
    public int getUserQuestsCompletedInLastDays(int days, QuestRank minRank) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        return history.tailMap(startDate).values().stream()
                .mapToInt(day -> day.completedQuestsCount().entrySet().stream()
                        .filter(entry -> entry.getKey().isAtLeast(minRank))
                        .mapToInt(Map.Entry::getValue)
                        .sum())
                .sum();
    }

    /**
     * Páginas leídas en los últimos N días.
     */
    public int getPagesReadInLastDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return history.tailMap(startDate).values().stream()
                .mapToInt(DailyHistory::pagesRead)
                .sum();
    }

    /**
     * Verifica si se compró un consumible específico en los últimos N días.
     */
    public boolean hasConsumablePurchaseInLastDays(String consumableId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        return history.tailMap(startDate).values().stream()
                .anyMatch(day -> day.consumablesBought().contains(consumableId));
    }

    /**
     * Burnouts acumulados en los últimos 30 días.
     */
    public int getBurnoutsInLastMonth() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        return (int) history.tailMap(startDate).values().stream()
                .filter(DailyHistory::hadBurnout)
                .count();
    }

    /**
     * Verifica si hubo burnout desde que se inició la Gate específica.
     */
    public boolean hadBurnoutDuringGate(String gateId) {
        LocalDate startDate = getTimeLimitStartDate(gateId);
        if (startDate == null) return false; // No iniciada, no hay burnout

        return history.tailMap(startDate).values().stream()
                .anyMatch(DailyHistory::hadBurnout);
    }

    public double getTotalCareerEngineHours() {
        return currentTotalCareerHours;
    }

    public boolean isGateCompleted(SystemQuestType gateType) {
        return completedGates.contains(gateType);
    }

    // ========================================================================================
    // GESTIÓN DE MANUAL CONFIRMATIONS & TIME LIMITS
    // ========================================================================================

    public boolean hasManualConfirmation(String confirmationId) {
        return manualConfirmations.contains(confirmationId);
    }

    public void setManualConfirmation(String confirmationId, boolean confirmed) {
        if (confirmed) {
            manualConfirmations.add(confirmationId);
        } else {
            manualConfirmations.remove(confirmationId);
        }
    }

    public LocalDate getTimeLimitStartDate(String gateId) {
        return timeLimitStartDates.get(gateId);
    }

    public void setTimeLimitStartDate(String gateId, LocalDate startDate) {
        timeLimitStartDates.put(gateId, startDate);
    }

    // Resetear el timer de una gate (útil si falla y reintenta)
    public void clearTimeLimit(String gateId) {
        timeLimitStartDates.remove(gateId);
    }

    // ========================================================================================
    // NUEVOS MÉTODOS PARA ELDER_5 Y ELDER_6
    // ========================================================================================

    /**
     * Calcula la racha actual de días sin debuffs activos.
     * Cuenta hacia atrás desde ayer.
     *
     * @return Número de días consecutivos sin debuffs
     */
    public int getDebuffFreeStreak() {
        LocalDate dateCursor = LocalDate.now().minusDays(1);
        int streak = 0;

        while (history.containsKey(dateCursor)) {
            DailyHistory day = history.get(dateCursor);
            if (!day.hadDebuffsActive()) {
                streak++;
                dateCursor = dateCursor.minusDays(1);
            } else {
                break; // Racha rota
            }
        }
        return streak;
    }

    /**
     * Calcula el número de semanas consecutivas con 100% Weekly Quests completadas.
     * Cuenta hacia atrás desde la semana pasada.
     *
     * @return Número de semanas consecutivas con todas las Weekly Quests completadas
     */
    public int getConsecutiveWeeksWithFullWeeklies() {
        return consecutiveWeeksWithFullWeeklies;
    }

    /**
     * Registra el resultado de una semana de Weekly Quests.
     *
     * @param allCompleted true si se completaron 3/3 Weekly Quests esa semana
     */
    public void recordWeeklyQuestResult(boolean allCompleted) {
        if (allCompleted) {
            consecutiveWeeksWithFullWeeklies++;
        } else {
            consecutiveWeeksWithFullWeeklies = 0; // Reset de racha
        }
    }

    /**
     * Setter para reconstruir desde persistencia.
     */
    public void setConsecutiveWeeksWithFullWeeklies(int weeks) {
        this.consecutiveWeeksWithFullWeeklies = weeks;
    }

    // Contador de semanas consecutivas con 100% Weekly Quests
    private int consecutiveWeeksWithFullWeeklies = 0;
}