package com.lifeleveling.domain.career;

/**
 * Constantes del Career Engine según la Biblia (Cap 1.2 §4).
 *
 * Centraliza todos los valores mágicos del sistema de código
 * para facilitar el balanceo y evitar números hardcodeados.
 */
public final class CodeSessionConstants {

    private CodeSessionConstants() {
        // Utility class - no instanciable
    }

    // ========================================================================================
    // XP POR HORA
    // ========================================================================================

    /**
     * XP de Intelecto ganada por hora de código.
     * Biblia: "XP_INT = Horas × 60"
     */
    public static final int XP_INT_PER_HOUR = 60;

    /**
     * XP de Disciplina ganada por hora de código.
     * Biblia: "XP_DIS = Horas × 20"
     */
    public static final int XP_DIS_PER_HOUR = 20;

    // ========================================================================================
    // FLOW STATE (Deep Work Bonus)
    // ========================================================================================

    /**
     * Horas mínimas para activar el Flow State.
     * Biblia: "IF (Horas >= 3.0)"
     */
    public static final double FLOW_THRESHOLD_HOURS = 3.0;

    /**
     * XP de Sabiduría otorgada por alcanzar Flow State.
     * Biblia: "XP_WIS += 50 (Flat Bonus, no multiplica por hora)"
     */
    public static final int FLOW_BONUS_WIS = 50;

    // ========================================================================================
    // COSTE DE SALUD
    // ========================================================================================

    /**
     * HP perdido por hora de trabajo.
     * Biblia: "Daño Base: 3 HP por cada hora"
     */
    public static final int HP_COST_PER_HOUR = 3;

    /**
     * Daño mínimo por sesión (nunca puede dar HP positivo).
     * Biblia: "Daño Mínimo: 0"
     */
    public static final int HP_COST_MINIMUM = 0;

    // ========================================================================================
    // LÍMITES DIARIOS
    // ========================================================================================

    /**
     * Tope máximo de horas computables por día.
     * Biblia: "Tope diario: 12.0h"
     */
    public static final double MAX_DAILY_HOURS = 12.0;

    /**
     * Horas mínimas para que una sesión cuente como "completada".
     * Decisión de diseño: Al menos 1 hora para marcar la DailyQuest.
     */
    public static final double MIN_SESSION_HOURS = 0.5;

    // ========================================================================================
    // TABLA DE REFERENCIA (Documentación)
    // ========================================================================================

    /*
     * Tabla de Ejemplo del Career Engine (Biblia):
     *
     * | Duración | XP INT | XP DIS | XP WIS | TOTAL XP | Notas                    |
     * |----------|--------|--------|--------|----------|--------------------------|
     * | 1h       | 60     | 20     | 0      | 80       | Eficiente para INT       |
     * | 2h       | 120    | 40     | 0      | 160      | Buen ritmo               |
     * | 3h       | 180    | 60     | 50     | 290      | PUNTO DULCE (Flow!)      |
     * | 4h       | 240    | 80     | 50     | 370      | Máximo rendimiento       |
     */
}
