package com.lifeleveling.domain.quest.condition;

/**
 * GateCondition: Representa una condición que debe cumplirse para completar una SystemQuest.
 *
 * Tipos de Condiciones:
 *   - LEVEL_REQUIREMENT: Nivel mínimo del jugador
 *   - PREVIOUS_GATE: Gate anterior completada
 *   - PERFECT_DAY_STREAK: Racha de días perfectos (7/7 hábitos)
 *   - HP_THRESHOLD: HP mínimo sostenido durante periodo
 *   - USER_QUESTS_COMPLETED: Número de User Quests completadas en periodo
 *   - CAREER_ENGINE_HOURS: Horas acumuladas de código
 *   - GOLD_THRESHOLD: Oro mínimo en wallet
 *   - CONSUMABLE_ABSTINENCE: No comprar consumible específico durante periodo
 *   - MANUAL_CONFIRMATION: Usuario marca checkbox manualmente
 *   - TIME_LIMIT: Límite de tiempo activo (se reinicia si falla)
 *   - BURNOUT_TRIGGER: Evento especial (3 burnouts en mes)
 *   - PAGES_READ: Páginas leídas en periodo específico
 *   - NO_BURNOUT: Sin entrar en burnout durante periodo activo
 *   - DEBUFF_FREE_STREAK: Mantener 0 debuffs activos durante periodo consecutivo
 *   - WEEKLY_QUESTS_COMPLETED: Semanas consecutivas con 100% Weekly Quests
 *
 * Filosofía:
 *   Cada condición es inmutable y puede evaluarse independientemente.
 *   Las condiciones se combinan en una Gate usando lógica AND.
 */
public sealed interface GateCondition
        permits LevelRequirement,
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
        WeeklyQuestsCompleted {

    /**
     * Evalúa si la condición está cumplida.
     *
     * @param context Contexto con toda la información necesaria
     * @return true si la condición está cumplida
     */
    boolean isMet(ConditionContext context);

    /**
     * Obtiene el progreso actual de la condición.
     *
     * @param context Contexto con toda la información necesaria
     * @return Progreso de 0.0 a 1.0 (100%)
     */
    double getProgress(ConditionContext context);

    /**
     * Obtiene texto descriptivo del progreso actual.
     *
     * Ejemplos:
     *   - "6/7 días"
     *   - "17,004/20,000 G"
     *   - "2/3 User Quests"
     *   - "67/100 páginas"
     *
     * @param context Contexto con toda la información necesaria
     * @return Texto de progreso para mostrar en UI
     */
    String getProgressText(ConditionContext context);

    /**
     * Obtiene descripción legible de la condición.
     *
     * Ejemplos:
     *   - "Completar 7 días perfectos consecutivos"
     *   - "Mantener HP ≥ 50 durante el periodo"
     *   - "Acumular 20,000 Gold"
     *
     * @return Descripción de la condición
     */
    String getDescription();

    /**
     * Indica si esta condición se valida automáticamente.
     *
     * @return true si es automática, false si requiere acción del jugador
     */
    default boolean isAutomatic() {
        return true;
    }

    /**
     * Indica si esta condición tiene un contador visible.
     *
     * @return true si debe mostrarse contador en UI
     */
    default boolean hasVisibleCounter() {
        return true;
    }

    /**
     * Indica si esta condición puede resetear el progreso.
     *
     * Ejemplo: PerfectDayStreak se resetea si fallas un día
     *
     * @return true si puede resetearse
     */
    default boolean canReset() {
        return false;
    }

    /**
     * Obtiene el tipo de esta condición.
     *
     * @return Tipo de condición
     */
    ConditionType getType();
}