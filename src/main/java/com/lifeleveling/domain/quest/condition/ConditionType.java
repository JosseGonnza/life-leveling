package com.lifeleveling.domain.quest.condition;

/**
 * ConditionType: Enumeración de todos los tipos de condiciones disponibles para SystemQuests.
 *
 * Cada tipo representa una mecánica diferente de validación que debe cumplirse
 * para completar una Gate (SystemQuest).
 *
 * Categorías:
 *   - Básicas: LEVEL_REQUIREMENT, PREVIOUS_GATE
 *   - Temporales: PERFECT_DAY_STREAK, TIME_LIMIT
 *   - Recursos: GOLD_THRESHOLD, HP_THRESHOLD
 *   - Actividad: CAREER_ENGINE_HOURS, USER_QUESTS_COMPLETED, PAGES_READ
 *   - Restricciones: CONSUMABLE_ABSTINENCE, NO_BURNOUT
 *   - Especiales: MANUAL_CONFIRMATION, BURNOUT_TRIGGER
 */
public enum ConditionType {

    /**
     * LEVEL_REQUIREMENT: Nivel mínimo del jugador.
     *
     * Ejemplos:
     *   - Gate 1: Nivel 10
     *   - Gate 2: Nivel 25
     *   - Gate 7: Nivel 60
     *
     * Características:
     *   - Automática
     *   - No resettable
     *   - Sin contador visible (es binario)
     */
    LEVEL_REQUIREMENT("🎯", "Nivel Requerido", "Requisito de nivel mínimo del jugador"),

    /**
     * PREVIOUS_GATE: Gate anterior completada.
     *
     * Ejemplos:
     *   - Gate 2 requiere Gate 1
     *   - Gate 3 requiere Gate 2
     *
     * Características:
     *   - Automática
     *   - No resettable
     *   - Sin contador visible (es binario)
     */
    PREVIOUS_GATE("🔗", "Gate Anterior", "Requiere completar la gate previa"),

    /**
     * PERFECT_DAY_STREAK: Racha de días perfectos (7/7 hábitos).
     *
     * Ejemplos:
     *   - Gate 1: 7 días perfectos consecutivos
     *
     * Características:
     *   - Automática
     *   - RESETTABLE (si fallas un día, vuelve a 0)
     *   - Con contador visible
     */
    PERFECT_DAY_STREAK("🔥", "Racha de Días Perfectos", "Días consecutivos con 7/7 hábitos completados"),

    /**
     * HP_THRESHOLD: HP mínimo sostenido durante periodo.
     *
     * Ejemplos:
     *   - Gate 1: Mantener HP ≥ 50 durante 7 días
     *   - REDEMPTION: Mantener HP > 80 durante 14 días
     *
     * Características:
     *   - Automática
     *   - RESETTABLE (si bajas del umbral, se reinicia)
     *   - Con contador visible
     */
    HP_THRESHOLD("❤️", "Umbral de HP", "Mantener HP por encima del mínimo durante periodo"),

    /**
     * USER_QUESTS_COMPLETED: Número de User Quests completadas en periodo.
     *
     * Ejemplos:
     *   - Gate 2: 3 User Quests Rango D+ en 7 días
     *
     * Características:
     *   - Automática
     *   - No resettable (acumulativo en ventana temporal)
     *   - Con contador visible
     */
    USER_QUESTS_COMPLETED("✅", "User Quests Completadas", "Número de tareas personales completadas"),

    /**
     * CAREER_ENGINE_HOURS: Horas acumuladas de código.
     *
     * Ejemplos:
     *   - Gate 2: 20 horas de CODE totales
     *   - ELDER_1: 100 horas en 30 días
     *
     * Características:
     *   - Automática
     *   - No resettable (acumulativo)
     *   - Con contador visible
     */
    CAREER_ENGINE_HOURS("💻", "Horas de Código", "Horas acumuladas en Career Engine"),

    /**
     * GOLD_THRESHOLD: Oro mínimo en wallet.
     *
     * Ejemplos:
     *   - GATE_WEALTH: Tener 20,000 G simultáneamente
     *
     * Características:
     *   - Automática
     *   - No resettable (umbral instantáneo)
     *   - Con contador visible
     */
    GOLD_THRESHOLD("💰", "Oro Requerido", "Cantidad mínima de Gold en wallet"),

    /**
     * CONSUMABLE_ABSTINENCE: No comprar consumible específico durante periodo.
     *
     * Ejemplos:
     *   - GATE_WEALTH: No comprar "Festín Trampa" en 10 días
     *   - ELDER_2: No comprar caprichos durante 30 días
     *
     * Características:
     *   - Automática
     *   - No resettable (binario: lo has comprado o no)
     *   - Sin contador visible (es binario)
     */
    CONSUMABLE_ABSTINENCE("🚫", "Abstinencia", "Evitar consumibles específicos"),

    /**
     * MANUAL_CONFIRMATION: Usuario marca checkbox manualmente.
     *
     * Ejemplos:
     *   - Gate 5/6/7: "He conseguido empleo" (checkbox)
     *   - Gate 5: "He actualizado LinkedIn" (checkbox)
     *
     * Características:
     *   - MANUAL (requiere acción del jugador)
     *   - No resettable
     *   - Sin contador (es checkbox)
     */
    MANUAL_CONFIRMATION("☑️", "Confirmación Manual", "Requiere que el usuario marque como completado"),

    /**
     * TIME_LIMIT: Límite de tiempo activo (se reinicia si falla).
     *
     * Ejemplos:
     *   - GATE_C_TO_B_PHASE_2: Completar proyecto en 7 días
     *
     * Características:
     *   - Automática
     *   - RESETTABLE (si se acaba el tiempo, vuelve a empezar)
     *   - Con contador visible (días restantes)
     */
    TIME_LIMIT("⏱️", "Límite de Tiempo", "Tiempo máximo para completar (se reinicia si falla)"),

    /**
     * BURNOUT_TRIGGER: Evento especial (3 burnouts en mes).
     *
     * Ejemplos:
     *   - GATE_REDEMPTION: Se activa al tener 3 burnouts en un mes
     *
     * Características:
     *   - Automática
     *   - No resettable (es un trigger de evento)
     *   - Con contador visible
     */
    BURNOUT_TRIGGER("💀", "Trigger de Burnout", "Evento especial por burnouts repetidos"),

    /**
     * PAGES_READ: Páginas leídas en periodo específico.
     *
     * Ejemplos:
     *   - GATE_C_TO_B_PHASE_1: 500 páginas en 7 días
     *   - ELDER_3: 1,000 páginas en 30 días
     *
     * Características:
     *   - Automática
     *   - RESETTABLE (ventana temporal móvil)
     *   - Con contador visible
     */
    PAGES_READ("📚", "Páginas Leídas", "Cantidad de páginas leídas en periodo"),

    /**
     * NO_BURNOUT: Sin entrar en burnout durante periodo activo.
     *
     * Ejemplos:
     *   - GATE_C_TO_B_PHASE_2: No entrar en BURNOUT durante los 7 días
     *
     * Características:
     *   - Automática
     *   - No resettable (binario: entraste en burnout o no)
     *   - Sin contador visible (es binario)
     */
    NO_BURNOUT("🛡️", "Sin Burnout", "Evitar estado BURNOUT durante el periodo");

    private final String icon;
    private final String displayName;
    private final String description;

    ConditionType(String icon, String displayName, String description) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Verifica si este tipo de condición es automática (no requiere acción del jugador).
     *
     * @return true si es automática
     */
    public boolean isAutomatic() {
        return this != MANUAL_CONFIRMATION;
    }

    /**
     * Verifica si este tipo de condición puede resetearse.
     *
     * @return true si puede resetearse
     */
    public boolean canReset() {
        return switch (this) {
            case PERFECT_DAY_STREAK, HP_THRESHOLD, TIME_LIMIT, PAGES_READ -> true;
            default -> false;
        };
    }

    /**
     * Verifica si este tipo de condición tiene un contador visible.
     *
     * @return true si debe mostrarse contador en UI
     */
    public boolean hasVisibleCounter() {
        return switch (this) {
            case LEVEL_REQUIREMENT, PREVIOUS_GATE, CONSUMABLE_ABSTINENCE,
                 MANUAL_CONFIRMATION, NO_BURNOUT -> false;
            default -> true;
        };
    }

    /**
     * Verifica si este tipo de condición es binaria (sí/no) sin progreso gradual.
     *
     * @return true si es binaria
     */
    public boolean isBinary() {
        return switch (this) {
            case LEVEL_REQUIREMENT, PREVIOUS_GATE, CONSUMABLE_ABSTINENCE,
                 MANUAL_CONFIRMATION, NO_BURNOUT -> true;
            default -> false;
        };
    }

    /**
     * Obtiene la categoría de esta condición para organización en UI.
     *
     * @return Categoría de la condición
     */
    public ConditionCategory getCategory() {
        return switch (this) {
            case LEVEL_REQUIREMENT, PREVIOUS_GATE -> ConditionCategory.BASIC;
            case PERFECT_DAY_STREAK, TIME_LIMIT -> ConditionCategory.TEMPORAL;
            case GOLD_THRESHOLD, HP_THRESHOLD -> ConditionCategory.RESOURCE;
            case CAREER_ENGINE_HOURS, USER_QUESTS_COMPLETED, PAGES_READ -> ConditionCategory.ACTIVITY;
            case CONSUMABLE_ABSTINENCE, NO_BURNOUT -> ConditionCategory.RESTRICTION;
            case MANUAL_CONFIRMATION, BURNOUT_TRIGGER -> ConditionCategory.SPECIAL;
        };
    }

    /**
     * Formatea el tipo de condición para mostrar en UI.
     *
     * @return String formateado con icono y nombre
     */
    public String toDisplayString() {
        return icon + " " + displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Categorías de condiciones para organización en UI.
     */
    public enum ConditionCategory {
        BASIC("Básicas"),          // Nivel, gate anterior
        TEMPORAL("Temporales"),     // Rachas, límites de tiempo
        RESOURCE("Recursos"),       // HP, Gold
        ACTIVITY("Actividad"),      // Horas, quests, páginas
        RESTRICTION("Restricciones"), // Abstinencias, no burnout
        SPECIAL("Especiales");      // Confirmaciones manuales, triggers

        private final String displayName;

        ConditionCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}