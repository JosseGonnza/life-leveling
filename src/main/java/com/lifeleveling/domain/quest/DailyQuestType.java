package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;

import java.util.Map;
import java.util.Optional;

/*
 * Cada DailyQuest tiene:
 *   - Input Type: Cómo se completa (Boolean, Integer, etc.)
 *   - Condición de éxito
 *   - Recompensa base (XP + Stat específico)
 *   - Efecto extra (HP recovery, buffs, etc.)
 *
 * Reseteo: 00:00 Local Time cada día.
 * Streak: Se trackea individualmente por quest.
 */

public enum DailyQuestType {

    /**
     * 💤 DESCANSO: Dormir +7 horas.
     * <p>
     * Input: Integer (horas dormidas)
     * Condición: >= 7
     * Reward: +10 XP General por hora dormida
     * Efecto: +15 HP Recovery
     */
    SLEEP(
            "💤",
            "Descanso (+7h)",
            "Dormir al menos 7 horas para recuperar energía mental",
            InputType.INTEGER,
            null,  // XP se calcula dinámicamente (10 XP/hora)
            15     // HP Recovery
    ),

    /**
     * 🥗 DIETA LIMPIA: Comer sin ultraprocesados.
     * <p>
     * Input: Boolean
     * Condición: true
     * Reward: +50 XP General
     * Efecto: Mantiene HP (evita decay)
     */
    DIET(
            "🥗",
            "Dieta Limpia",
            "Comer saludable sin ultraprocesados durante el día",
            InputType.BOOLEAN,
            Map.of(),  // Solo General XP
            0
    ),

    /**
     * 🏋️ DEPORTE: Gym o actividad física.
     * <p>
     * Input: Boolean
     * Condición: true
     * Reward: +50 XP Fuerza
     * Efecto: -5 HP (ir al gym cansa)
     */
    GYM(
            "🏋️",
            "Deporte",
            "Entrenar en el gym o hacer actividad física significativa",
            InputType.BOOLEAN,
            Map.of(StatType.STRENGTH, 50),
            -5  // Cuesta HP
    ),

    /**
     * 💻 CODE SESSION: Programar/Estudiar desarrollo.
     * <p>
     * Input: Boolean (trigger del Career Engine)
     * Condición: true
     * Reward: Ver Career Engine (Diminishing Returns)
     * Efecto: -2 HP por hora (según Career Engine)
     */
    CODE(
            "💻",
            "Code Session",
            "Programar o estudiar desarrollo (trackear con Wakatime)",
            InputType.BOOLEAN,
            Map.of(),  // XP viene del Career Engine
            0
    ),

    /**
     * 📚 LEER: Leer +10 páginas.
     * <p>
     * Input: Integer (páginas leídas)
     * Condición: >= 10
     * Reward: +5 XP Sabiduría por página
     * Efecto: Ninguno
     */
    READ(
            "📚",
            "Leer (10p)",
            "Leer al menos 10 páginas de libros de no-ficción",
            InputType.INTEGER,
            Map.of(StatType.WISDOM, 5),  // 5 XP/página
            0
    ),

    /**
     * ✨ SKINCARE: Rutina de cuidado personal.
     * <p>
     * Input: Boolean
     * Condición: true
     * Reward: +50 XP Carisma
     * Efecto: Ninguno
     */
    SKINCARE(
            "✨",
            "Skincare",
            "Completar rutina de cuidado personal (skincare o socializar)",
            InputType.BOOLEAN,
            Map.of(StatType.CHARISMA, 50),
            0
    ),

    /**
     * 🧹 ORDEN: Ordenar la casa 10 min.
     * <p>
     * Input: Boolean
     * Condición: true
     * Reward: +50 XP Sabiduría
     * Efecto: Evita DEBUFF "Montaña de Platos"
     */
    TIDY(
            "🧹",
            "Orden (10m)",
            "Ordenar y limpiar el espacio de vida al menos 10 minutos",
            InputType.BOOLEAN,
            Map.of(StatType.WISDOM, 50),
            0
    ),
    ;

    public enum InputType {
        BOOLEAN,   // Checkbox simple (true/false)
        INTEGER    // Valor numérico (horas, páginas, minutos)
    }

    private final String icon;
    private final String name;
    private final String description;
    private final InputType inputType;
    private final Map<StatType, Integer> baseStatXP;  // XP base por stat (null = dinámico)
    private final int hpEffect;  // Positivo = recovery, Negativo = cost

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

    public boolean requiresNumericInput() {
        return inputType == InputType.INTEGER;
    }

    public boolean requiresBooleanInput() {
        return inputType == InputType.BOOLEAN;
    }

    public boolean grantsHP() {
        return hpEffect > 0;
    }

    public boolean costsHP() {
        return hpEffect < 0;
    }

    public boolean affectsHP() {
        return hpEffect != 0;
    }

    public QuestRank getRank() {
        return QuestRank.E;  // Todas las Daily Quests son rutinarias
    }

    public QuestReward calculateReward(boolean completed) {
        if (!requiresBooleanInput()) {
            throw new IllegalStateException(
                    String.format("%s requiere input INTEGER, no BOOLEAN", this.name())
            );
        }
        if (!completed) {
            return QuestReward.empty();
        }
        // DIET es especial: solo da General XP
        if (this == DIET) {
            return QuestReward.ofGeneralXP(50);
        }
        // CODE es especial: no da XP directa (viene del Career Engine)
        if (this == CODE) {
            return QuestReward.empty();
        }

        QuestReward.Builder builder = QuestReward.builder();
        baseStatXP.forEach(builder::addStatXP);
        return builder.build();
    }

    public QuestReward calculateReward(int value) {
        if (!requiresNumericInput()) {
            throw new IllegalStateException(
                    String.format("%s requiere input BOOLEAN, no INTEGER", this.name())
            );
        }
        if (value < 0) {
            throw new IllegalArgumentException(
                    String.format("El valor no puede ser negativo: %d", value)
            );
        }

        return switch (this) {
            case SLEEP -> {
                // 10 XP General por hora dormida
                int xp = value * 10;
                yield QuestReward.ofGeneralXP(xp);
            }
            case READ -> {
                // 5 XP Sabiduría por página
                int xp = value * 5;
                yield QuestReward.ofSingleStat(StatType.WISDOM, xp);
            }
            default -> throw new IllegalStateException(
                    String.format("%s no debería usar calculateReward(int)", this.name())
            );
        };
    }

    public boolean meetsCondition(boolean input) {
        if (!requiresBooleanInput()) {
            throw new IllegalStateException(
                    String.format("%s requiere input INTEGER", this.name())
            );
        }
        return input;
    }

    public boolean meetsCondition(int value) {
        if (!requiresNumericInput()) {
            throw new IllegalStateException(
                    String.format("%s requiere input BOOLEAN", this.name())
            );
        }

        return switch (this) {
            case SLEEP -> value >= 7;   // Al menos 7 horas
            case READ -> value >= 10;   // Al menos 10 páginas
            default -> throw new IllegalStateException(
                    String.format("%s no debería usar meetsCondition(int)", this.name())
            );
        };
    }

    public static Optional<DailyQuestType> fromString(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim().toUpperCase();
        // Primero intentar por enum name
        try {
            return Optional.of(DailyQuestType.valueOf(normalized));
        } catch (IllegalArgumentException e) {
            // Si falla, buscar por display name
            for (DailyQuestType type : values()) {
                if (type.name.equalsIgnoreCase(name.trim())) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }

    public String toDisplayString() {
        return icon + " " + name;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public InputType getInputType() {
        return inputType;
    }

    public Map<StatType, Integer> getBaseStatXP() {
        return baseStatXP;
    }

    public int getHpEffect() {
        return hpEffect;
    }
}