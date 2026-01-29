package com.lifeleveling.domain.quest.condition;

/**
 * GateCondition: Representa una condición que debe cumplirse para completar una SystemQuest.
 *
 * Filosofía:
 * Cada condición es inmutable y puede evaluarse independientemente.
 * Las condiciones se combinan en una Gate usando lógica AND.
 */
public sealed interface GateCondition
        permits
        // Condiciones Originales
        LevelRequirement,
        PreviousGateRequired,
        PerfectDayStreak,
        HPThreshold,
        UserQuestsCompleted,
        CareerEngineHours,
        GoldThreshold,
        ConsumableAbstinence,
        ManualConfirmation,
        TimeLimit,
        BurnoutTrigger,
        PagesRead,
        NoBurnout,
        DebuffFreeStreak,
        WeeklyQuestsCompleted,
        // [NUEVO] Nuevas condiciones específicas Fase 3
        CareerHoursInPeriod,
        GymSessionsInPeriod,
        PerfectDaysInPeriod {

    /**
     * Evalúa si la condición está cumplida.
     * @param context Contexto con toda la información necesaria
     * @return true si la condición está cumplida
     */
    boolean isMet(ConditionContext context);

    /**
     * Obtiene el progreso actual de la condición.
     * @param context Contexto con toda la información necesaria
     * @return Progreso de 0.0 a 1.0 (100%)
     */
    double getProgress(ConditionContext context);

    /**
     * Obtiene texto descriptivo del progreso actual.
     */
    String getProgressText(ConditionContext context);

    /**
     * Obtiene descripción legible de la condición.
     */
    String getDescription();

    /**
     * Indica si esta condición se valida automáticamente.
     */
    default boolean isAutomatic() {
        return true;
    }

    /**
     * Indica si esta condición tiene un contador visible.
     */
    default boolean hasVisibleCounter() {
        return true;
    }

    /**
     * Indica si esta condición puede resetear el progreso.
     */
    default boolean canReset() {
        return false;
    }

    /**
     * Obtiene el tipo de esta condición.
     */
    ConditionType getType();
}