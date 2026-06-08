package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;

import java.util.Objects;

/**
 * TitleBuff: Representa el efecto/buff que otorga un título cuando está equipado.
 *
 * Tipos de buffs soportados:
 *   - XP_MULTIPLIER: Multiplica XP ganada (por stat específico o general)
 *   - GOLD_MULTIPLIER: Multiplica oro ganado
 *   - HP_RECOVERY_BONUS: Bonus porcentual a la curación de HP
 *   - DEBUFF_IMMUNITY: Inmunidad a un debuff específico
 *   - ALL_STATS_XP: Multiplica XP de TODOS los stats
 *
 * Regla de Oro: Solo aplican los buffs de títulos EQUIPADOS.
 */
public record TitleBuff(
        BuffType type,
        StatType targetStat,   // null si aplica a general/todos
        double value,          // Porcentaje (0.05 = 5%, 0.10 = 10%)
        String debuffImmunity  // Nombre del debuff si type es DEBUFF_IMMUNITY
) {

    /**
     * Tipos de buff disponibles.
     */
    public enum BuffType {
        /** Multiplica XP de un stat específico */
        STAT_XP_MULTIPLIER,
        /** Multiplica XP general */
        GENERAL_XP_MULTIPLIER,
        /** Multiplica XP de TODOS los stats */
        ALL_STATS_XP_MULTIPLIER,
        /** Multiplica oro ganado */
        GOLD_MULTIPLIER,
        /** Bonus porcentual a la curación de HP */
        HP_RECOVERY_BONUS,
        /** Inmunidad a un debuff específico */
        DEBUFF_IMMUNITY,
        /** Multiplica XP de READ específicamente (para Speed Reader) */
        READ_XP_MULTIPLIER
    }

    public TitleBuff {
        Objects.requireNonNull(type, "BuffType no puede ser null");
        if (value < 0) {
            throw new IllegalArgumentException("El valor del buff no puede ser negativo: " + value);
        }
        if (type == BuffType.STAT_XP_MULTIPLIER && targetStat == null) {
            throw new IllegalArgumentException("STAT_XP_MULTIPLIER requiere un targetStat");
        }
        if (type == BuffType.DEBUFF_IMMUNITY && (debuffImmunity == null || debuffImmunity.isBlank())) {
            throw new IllegalArgumentException("DEBUFF_IMMUNITY requiere especificar el debuff");
        }
    }

    // ========================================================================================
    // FACTORY METHODS
    // ========================================================================================

    /**
     * Crea un buff de XP para un stat específico.
     * @param stat El stat afectado
     * @param percentage El porcentaje (0.05 = +5%)
     */
    public static TitleBuff statXP(StatType stat, double percentage) {
        return new TitleBuff(BuffType.STAT_XP_MULTIPLIER, stat, percentage, null);
    }

    /**
     * Crea un buff de XP general.
     * @param percentage El porcentaje (0.05 = +5%)
     */
    public static TitleBuff generalXP(double percentage) {
        return new TitleBuff(BuffType.GENERAL_XP_MULTIPLIER, null, percentage, null);
    }

    /**
     * Crea un buff de XP para TODOS los stats.
     * @param percentage El porcentaje (0.20 = +20%)
     */
    public static TitleBuff allStatsXP(double percentage) {
        return new TitleBuff(BuffType.ALL_STATS_XP_MULTIPLIER, null, percentage, null);
    }

    /**
     * Crea un buff de multiplicador de oro.
     * @param percentage El porcentaje (0.05 = +5%)
     */
    public static TitleBuff goldMultiplier(double percentage) {
        return new TitleBuff(BuffType.GOLD_MULTIPLIER, null, percentage, null);
    }

    /**
     * Crea un buff de bonus a la recuperación de HP.
     * @param percentage El porcentaje (0.10 = +10%)
     */
    public static TitleBuff hpRecovery(double percentage) {
        return new TitleBuff(BuffType.HP_RECOVERY_BONUS, null, percentage, null);
    }

    /**
     * Crea una inmunidad a un debuff específico.
     * @param debuffName Nombre del debuff (ej: "CHAOS", "FATIGUE")
     */
    public static TitleBuff debuffImmunity(String debuffName) {
        return new TitleBuff(BuffType.DEBUFF_IMMUNITY, null, 1.0, debuffName);
    }

    /**
     * Crea un multiplicador de XP específico para READ (Speed Reader).
     * @param multiplier El multiplicador (1.5 = x1.5)
     */
    public static TitleBuff readXPMultiplier(double multiplier) {
        return new TitleBuff(BuffType.READ_XP_MULTIPLIER, null, multiplier, null);
    }

    /**
     * Crea un buff combinado de múltiples stats.
     * Nota: Para combinar varios buffs, usar TitleType con List<TitleBuff>
     */
    public static TitleBuff[] multiStatXP(double percentage, StatType... stats) {
        TitleBuff[] buffs = new TitleBuff[stats.length];
        for (int i = 0; i < stats.length; i++) {
            buffs[i] = statXP(stats[i], percentage);
        }
        return buffs;
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    /**
     * Verifica si este buff afecta a un stat específico.
     */
    public boolean affectsStat(StatType stat) {
        return switch (type) {
            case STAT_XP_MULTIPLIER -> targetStat == stat;
            case ALL_STATS_XP_MULTIPLIER -> true;
            default -> false;
        };
    }

    /**
     * Verifica si este buff afecta al oro.
     */
    public boolean affectsGold() {
        return type == BuffType.GOLD_MULTIPLIER;
    }

    /**
     * Verifica si este buff afecta a la recuperación de HP.
     */
    public boolean affectsHPRecovery() {
        return type == BuffType.HP_RECOVERY_BONUS;
    }

    /**
     * Verifica si este buff da inmunidad a un debuff.
     */
    public boolean givesDebuffImmunity(String debuffName) {
        return type == BuffType.DEBUFF_IMMUNITY &&
                debuffImmunity != null &&
                debuffImmunity.equalsIgnoreCase(debuffName);
    }

    /**
     * Obtiene el multiplicador como valor aplicable (1.0 + percentage).
     * Ejemplo: 0.05 -> 1.05 (para multiplicar por 1.05)
     */
    public double getMultiplier() {
        if (type == BuffType.READ_XP_MULTIPLIER) {
            return value; // Ya es un multiplicador directo (1.5)
        }
        return 1.0 + value;
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    /**
     * Formatea el buff para mostrar en UI.
     */
    public String toDisplayString() {
        String valueStr = String.format("+%.0f%%", value * 100);

        return switch (type) {
            case STAT_XP_MULTIPLIER -> valueStr + " XP de " + targetStat.getDisplayName();
            case GENERAL_XP_MULTIPLIER -> valueStr + " XP General";
            case ALL_STATS_XP_MULTIPLIER -> valueStr + " XP de todas las stats";
            case GOLD_MULTIPLIER -> valueStr + " Oro ganado";
            case HP_RECOVERY_BONUS -> valueStr + " Recuperación de HP";
            case DEBUFF_IMMUNITY -> "Inmunidad a " + debuffDisplayName();
            case READ_XP_MULTIPLIER -> "Lectura x" + value + " XP";
        };
    }

    private String debuffDisplayName() {
        try {
            return com.lifeleveling.domain.debuff.DebuffType.valueOf(debuffImmunity).getDisplayName();
        } catch (IllegalArgumentException | NullPointerException e) {
            return debuffImmunity;
        }
    }
}
