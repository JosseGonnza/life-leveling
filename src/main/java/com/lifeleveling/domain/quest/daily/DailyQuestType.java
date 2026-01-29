package com.lifeleveling.domain.quest.daily;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;

import java.util.Map;
import java.util.Optional;

public enum DailyQuestType {

    /**
     * 💤 DESCANSO: Dormir.
     * <p>
     * Input: Integer (horas dormidas)
     * Condición: >= 6 (Para activar el primer tier de recuperación)
     * Reward: +10 XP General por hora
     * Efecto: Dinámico (0, 15 o 30 HP)
     */
    SLEEP(
            "💤",
            "Descanso",
            "Dormir para recuperar energía mental",
            InputType.INTEGER,
            null,
            0     // [FIX] HP Base es 0, se calcula dinámicamente según horas
    ),

    /**
     * 🥗 DIETA LIMPIA: Comer sin ultraprocesados.
     */
    DIET(
            "🥗",
            "Dieta Limpia",
            "Comer saludable sin ultraprocesados durante el día",
            InputType.BOOLEAN,
            Map.of(),  // Solo General XP
            5   // [FIX] Corrección: +5 HP Base. (+10 si tienes Air Fryer se calcula al completar)
    ),

    /**
     * 🏋️ DEPORTE: Gym o actividad física.
     */
    GYM(
            "🏋️",
            "Deporte",
            "Entrenar en el gym o hacer actividad física significativa",
            InputType.BOOLEAN,
            Map.of(StatType.STRENGTH, 50),
            -5
    ),

    /**
     * 💻 CODE SESSION: Programar/Estudiar desarrollo.
     * <p>
     * Input: EXTERNAL (gestionado por CareerEngine)
     * Condición: Al menos 1 sesión registrada en CareerEngine
     * Reward: Calculado por CareerEngine (INT 60/h, DIS 20/h, WIS +50 si Flow >= 3h)
     * Efecto: -3 HP por hora (gestionado por CareerEngine)
     * <p>
     * NOTA: CODE es especial - permite MÚLTIPLES entradas al día.
     * Cada sesión se evalúa independientemente para el Flow Bonus.
     * El usuario va a la sección CODE, mete horas, y puede repetir.
     */
    CODE(
            "💻",
            "Code Session",
            "Registrar horas de programación (múltiples sesiones permitidas)",
            InputType.EXTERNAL,  // Gestionado por CareerEngine
            Map.of(),            // XP calculada externamente
            0                    // HP calculado externamente por sesión
    ),

    /**
     * 📚 LEER: Leer +10 páginas.
     */
    READ(
            "📚",
            "Leer (10p)",
            "Leer al menos 10 páginas de libros de no-ficción",
            InputType.INTEGER,
            Map.of(StatType.WISDOM, 5),
            0
    ),

    /**
     * ✨ SKINCARE: Rutina de cuidado personal.
     */
    SKINCARE(
            "✨",
            "Skincare",
            "Completar rutina de cuidado personal (skincare o socializar)",
            InputType.BOOLEAN,
            Map.of(StatType.CHARISMA, 50),
            10  // [FIX] Corrección: Autocuidado recupera +10 HP
    ),

    /**
     * 🧹 ORDEN: Ordenar la casa 10 min.
     */
    TIDY(
            "🧹",
            "Orden (10m)",
            "Ordenar y limpiar el espacio de vida al menos 10 minutos",
            InputType.BOOLEAN,
            Map.of(StatType.DISCIPLINE, 50), // [FIX] Corrección: Ordenar requiere Voluntad (DIS), no intelecto.
            0
    );

    public enum InputType {
        BOOLEAN,   // Checkbox simple (true/false)
        INTEGER,   // Valor numérico (horas, páginas)
        EXTERNAL   // Gestionado por sistema externo (CareerEngine)
    }

    private final String icon;
    private final String name;
    private final String description;
    private final InputType inputType;
    private final Map<StatType, Integer> baseStatXP;
    private final int hpEffect;

    DailyQuestType(
            String icon,
            String name,
            String description,
            InputType inputType,
            Map<StatType, Integer> baseStatXP,
            int hpEffect
    ) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.inputType = inputType;
        this.baseStatXP = baseStatXP != null ? Map.copyOf(baseStatXP) : Map.of();
        this.hpEffect = hpEffect;
    }

    // [FIX] Nuevo method para calcular HP dinámico
    public int calculateDynamicHP(Integer input) {
        if (this == SLEEP && input != null) {
            if (input < 6) return 0;       // Menos de 6h: Nada (y triggers Fatigue en otro lado)
            if (input < 7) return 15;      // 6h - 6.9h: Recuperación Parcial
            return 30;                     // >= 7h: Recuperación Completa
        }
        // Para el resto de quests, devolvemos el valor estático (ej: Gym -5)
        return hpEffect;
    }

    public boolean requiresNumericInput() {
        return inputType == InputType.INTEGER;
    }

    public boolean requiresBooleanInput() {
        return inputType == InputType.BOOLEAN;
    }

    /**
     * Indica si esta quest es gestionada por un sistema externo (ej: CareerEngine para CODE).
     * El sistema externo se encarga de calcular XP, HP y marcar como completada.
     */
    public boolean isExternallyManaged() {
        return inputType == InputType.EXTERNAL;
    }

    public boolean affectsHP() {
        return hpEffect != 0 || this == SLEEP; // SLEEP afecta HP aunque su base sea 0
    }

    /**
     * ¿Esta quest otorga HP al completarse?
     */
    public boolean grantsHP() {
        return hpEffect > 0 || this == SLEEP; // SLEEP tiene HP dinámico
    }

    /**
     * ¿Esta quest cuesta HP al completarse?
     */
    public boolean costsHP() {
        return hpEffect < 0;
    }

    public QuestRank getRank() {
        return QuestRank.E;
    }

    public QuestReward calculateReward(boolean completed) {
        if (isExternallyManaged()) {
            // CODE es gestionado por CareerEngine, no calcular reward aquí
            return QuestReward.empty();
        }
        if (!requiresBooleanInput()) {
            throw new IllegalStateException(name() + " requiere input INTEGER");
        }
        if (!completed) return QuestReward.empty();

        if (this == DIET) return QuestReward.ofGeneralXP(50);

        QuestReward.Builder builder = QuestReward.builder();
        baseStatXP.forEach(builder::addStatXP);
        return builder.build();
    }

    public QuestReward calculateReward(int value) {
        if (!requiresNumericInput()) throw new IllegalStateException(name() + " requiere input BOOLEAN");
        if (value < 0) throw new IllegalArgumentException("Valor negativo: " + value);

        return switch (this) {
            case SLEEP -> {
                // [FIX] Biblia: 15 XP/h, máximo computable 8.5h (127.5 -> 127 XP)
                double effectiveHours = Math.min(value, 8.5);
                int xp = (int) (effectiveHours * 15);
                yield QuestReward.ofGeneralXP(xp);
            }
            case READ -> QuestReward.ofSingleStat(StatType.WISDOM, value * 5);
            default -> throw new IllegalStateException(name() + " no usa reward dinámico");
        };
    }

    public boolean meetsCondition(boolean input) {
        if (isExternallyManaged()) {
            throw new IllegalStateException(name() + " es gestionado externamente. Usa CareerEngine.hasActivityToday()");
        }
        if (!requiresBooleanInput()) {
            throw new IllegalStateException(name() + " requiere input INTEGER");
        }
        return input;
    }

    public boolean meetsCondition(int value) {
        if (!requiresNumericInput()) throw new IllegalStateException(name() + " requiere input BOOLEAN");

        return switch (this) {
            case SLEEP -> value >= 6;   // [FIX] Bajamos requisito a 6h para permitir el Tier 1 de HP
            case READ -> value >= 10;
            default -> throw new IllegalStateException(name() + " no usa condición dinámica");
        };
    }

    // Getters y fromString...
    public static Optional<DailyQuestType> fromString(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        try {
            return Optional.of(DailyQuestType.valueOf(name.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            for (DailyQuestType type : values()) {
                if (type.name.equalsIgnoreCase(name.trim())) return Optional.of(type);
            }
            return Optional.empty();
        }
    }

    public String toDisplayString() { return icon + " " + name; }
    public String getIcon() { return icon; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public InputType getInputType() { return inputType; }
    public Map<StatType, Integer> getBaseStatXP() { return baseStatXP; }
    public int getHpEffect() { return hpEffect; }
}