package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.quest.QuestRank;
import com.lifeleveling.domain.quest.SystemQuestType;

import java.time.LocalDate;
import java.util.List;

/**
 * GateTracker: Rastrea el progreso y eventos necesarios para evaluar condiciones de gates.
 *
 * Este tracker mantiene:
 *   - Historial de Daily Quests completadas
 *   - Historial de User Quests completadas
 *   - Eventos de HP (para detectar si cayó por debajo de umbrales)
 *   - Historial de compras de consumibles
 *   - Eventos de burnout
 *   - Confirmaciones manuales del jugador
 *   - Contadores activos (páginas leídas, horas de código, etc.)
 *
 * IMPORTANTE: Esta es la versión inicial placeholder.
 * Implementaremos toda la lógica de tracking en una fase posterior.
 */
public class GateTracker {

    // Placeholder: Implementaremos estos métodos según necesidad

    /**
     * Obtiene la racha actual de días perfectos (7/7 hábitos).
     */
    public int getPerfectDayStreak() {
        return 0; // TODO: Implementar
    }

    /**
     * Obtiene el HP mínimo registrado en los últimos N días.
     */
    public int getMinHPInLastDays(int days) {
        return 100; // TODO: Implementar
    }

    /**
     * Obtiene el número de User Quests de rango específico completadas en los últimos N días.
     */
    public int getUserQuestsCompletedInLastDays(int days, QuestRank minRank) {
        return 0; // TODO: Implementar
    }

    /**
     * Obtiene las horas totales acumuladas en Career Engine.
     */
    public double getTotalCareerEngineHours() {
        return 0.0; // TODO: Implementar
    }

    /**
     * Obtiene las páginas leídas en los últimos N días.
     */
    public int getPagesReadInLastDays(int days) {
        return 0; // TODO: Implementar
    }

    /**
     * Obtiene las páginas leídas por día en los últimos N días.
     */
    public List<Integer> getPagesReadByDay(int days) {
        return List.of(); // TODO: Implementar
    }

    /**
     * Verifica si se ha comprado un consumible específico en los últimos N días.
     */
    public boolean hasConsumablePurchaseInLastDays(String consumableId, int days) {
        return false; // TODO: Implementar
    }

    /**
     * Obtiene el número de burnouts en el último mes.
     */
    public int getBurnoutsInLastMonth() {
        return 0; // TODO: Implementar
    }

    /**
     * Obtiene el número de días consecutivos con HP por encima de un umbral.
     */
    public int getConsecutiveDaysAboveHP(int threshold) {
        return 0; // TODO: Implementar
    }

    /**
     * Verifica si el jugador ha confirmado manualmente una condición.
     */
    public boolean hasManualConfirmation(String confirmationId) {
        return false; // TODO: Implementar
    }

    /**
     * Registra una confirmación manual del jugador.
     */
    public void setManualConfirmation(String confirmationId, boolean confirmed) {
        // TODO: Implementar
    }

    /**
     * Obtiene la fecha de inicio de una gate con límite de tiempo.
     */
    public LocalDate getTimeLimitStartDate(String gateId) {
        return null; // TODO: Implementar
    }

    /**
     * Establece la fecha de inicio de una gate con límite de tiempo.
     */
    public void setTimeLimitStartDate(String gateId, LocalDate startDate) {
        // TODO: Implementar
    }

    /**
     * Verifica si hubo burnout durante el periodo activo de una gate.
     */
    public boolean hadBurnoutDuringGate(String gateId) {
        return false; // TODO: Implementar
    }

    /**
     * Obtiene el oro total ganado históricamente (acumulado).
     */
    public int getHistoricalGoldEarned() {
        return 0; // TODO: Implementar
    }

    /**
     * Verifica si una gate anterior está completada.
     */
    public boolean isGateCompleted(SystemQuestType gateType) {
        return false; // TODO: Implementar
    }
}