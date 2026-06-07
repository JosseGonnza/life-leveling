package com.lifeleveling.domain.quest.daily;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.title.TitleInventory;

import java.util.Map;
import java.util.Optional;

public enum DailyQuestType {

    SLEEP(
            "💤", "Descanso", "Dormir para recuperar energía mental",
            InputType.INTEGER, null, 0
    ),
    DIET(
            "🥗", "Dieta Limpia", "Comer saludable sin ultraprocesados",
            InputType.BOOLEAN, Map.of(), 5
    ),
    GYM(
            "🏋️", "Deporte", "Entrenar en el gym",
            InputType.BOOLEAN, Map.of(StatType.STRENGTH, 50), -5
    ),
    CODE(
            "💻", "Sesión de Código", "Registrar horas de programación",
            InputType.EXTERNAL, Map.of(), 0
    ),
    READ(
            "📚", "Leer (10p)", "Leer al menos 10 páginas",
            InputType.INTEGER, Map.of(StatType.WISDOM, 5), 0
    ),
    SKINCARE(
            "✨", "Cuidado Personal", "Rutina de cuidado personal",
            InputType.BOOLEAN, Map.of(StatType.CHARISMA, 50), 10
    ),
    TIDY(
            "🧹", "Orden (10m)", "Ordenar el espacio de vida",
            InputType.BOOLEAN, Map.of(StatType.DISCIPLINE, 50), 0
    );

    public enum InputType { BOOLEAN, INTEGER, EXTERNAL }

    private final String icon;
    private final String name;
    private final String description;
    private final InputType inputType;
    private final Map<StatType, Integer> baseStatXP;
    private final int hpEffect;

    DailyQuestType(String icon, String name, String description, InputType inputType,
                   Map<StatType, Integer> baseStatXP, int hpEffect) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.inputType = inputType;
        this.baseStatXP = baseStatXP != null ? Map.copyOf(baseStatXP) : Map.of();
        this.hpEffect = hpEffect;
    }

    /**
     * [NUEVO] Calcula el HP real considerando horas y Buffs de Títulos.
     * Ej: Dormir 7h (30 HP) con "Superviviente" (+3%) -> 31 HP.
     */
    public int calculateDynamicHP(Integer input, TitleInventory titles) {
        // 1. Cálculo Base (Reglas de la Biblia)
        int baseHP = hpEffect; // Por defecto (ej: GYM = -5)

        if (this == SLEEP && input != null) {
            if (input < 6) baseHP = 0;       // < 6h: 0 HP
            else if (input < 7) baseHP = 15; // 6.0 - 6.9h: 15 HP
            else baseHP = 30;                // >= 7.0h: 30 HP
        }

        // 2. Aplicar Buffs de Títulos (Solo si es curación > 0)
        // Evitamos bonificar daño (-5) o nulos (0)
        if (baseHP > 0 && titles != null) {
            return titles.applyHPRecoveryBonus(baseHP);
        }

        return baseHP;
    }

    // [COMPATIBILIDAD] Versión sin títulos para llamadas legacy
    public int calculateDynamicHP(Integer input) {
        return calculateDynamicHP(input, null);
    }

    public boolean requiresNumericInput() { return inputType == InputType.INTEGER; }
    public boolean requiresBooleanInput() { return inputType == InputType.BOOLEAN; }
    public boolean isExternallyManaged() { return inputType == InputType.EXTERNAL; }

    public QuestRank getRank() { return QuestRank.E; }

    public QuestReward calculateReward(int value) {
        if (!requiresNumericInput()) throw new IllegalStateException("Requiere input INTEGER");
        return switch (this) {
            case SLEEP -> QuestReward.ofGeneralXP(Math.min(value, 12) * 15);
            case READ -> QuestReward.ofSingleStat(StatType.WISDOM, value * 5);
            default -> QuestReward.empty();
        };
    }

    public QuestReward calculateReward(boolean completed) {
        if (isExternallyManaged()) return QuestReward.empty();
        if (!requiresBooleanInput()) throw new IllegalStateException(this + " no usa input booleano");
        if (!completed) return QuestReward.empty();
        if (this == DIET) return QuestReward.ofGeneralXP(50);
        QuestReward.Builder builder = QuestReward.builder();
        baseStatXP.forEach(builder::addStatXP);
        return builder.build();
    }

    public boolean meetsCondition(int value) {
        if (!requiresNumericInput()) throw new IllegalStateException(this + " no usa input numérico");
        return switch (this) {
            case SLEEP -> value >= 7;
            case READ -> value >= 10;
            default -> false;
        };
    }

    public boolean meetsCondition(boolean completed) {
        if (!requiresBooleanInput()) throw new IllegalStateException(this + " no usa input booleano");
        return completed;
    }

    // ========================================================================================
    // SEARCH HELPER (El código que te falta)
    // ========================================================================================

    public static Optional<DailyQuestType> fromString(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        try {
            // Intenta buscar exacto o normalizando mayúsculas
            return Optional.of(DailyQuestType.valueOf(name.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Búsqueda extra por si el nombre de display difiere del enum
            for (DailyQuestType type : values()) {
                if (type.name.equalsIgnoreCase(name.trim())) return Optional.of(type);
            }
            return Optional.empty();
        }
    }

    // Getters básicos...
    public String getIcon() { return icon; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String toDisplayString() { return icon + " " + name; }
    public int getHpEffect() { return hpEffect; }

    public InputType getInputType() { return inputType; }
    public Map<StatType, Integer> getBaseStatXP() { return baseStatXP; }

    public boolean grantsHP() { return this == SLEEP || hpEffect > 0; }
    public boolean costsHP() { return hpEffect < 0; }
    public boolean affectsHP() { return grantsHP() || costsHP(); }
}