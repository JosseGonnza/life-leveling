package com.lifeleveling.domain.debuff;

import com.lifeleveling.domain.player.StatType;

import java.util.Optional;

/**
 * Representa un efecto mecánico único de un Debuff.
 * Un Debuff puede tener múltiples efectos (ej: Taquicardia baja XP y bloquea Items).
 */
public record DebuffEffect(
        EffectType type,
        double value,          // Ej: 0.5 para multiplicador, 150 para oro
        StatType targetStat,   // Opcional: Solo si afecta a un stat específico
        String targetItemId    // Opcional: Solo si bloquea un item específico (ej: "consumable_monster")
) {

    public enum EffectType {
        GLOBAL_XP_MULTIPLIER,  // Multiplica toda la XP ganada (Fatiga)
        STAT_XP_PENALTY,       // Reduce XP de un stat concreto (Chaos, Heaviness)
        GOLD_PENALTY_PER_UNIT, // Resta oro por unidad de tiempo/input (Trapped)
        DISABLE_ITEM_EFFECT    // Impide que un item funcione (Tachycardia bloquea Monster)
    }

    // Constructores de conveniencia
    public static DebuffEffect globalXpMult(double multiplier) {
        return new DebuffEffect(EffectType.GLOBAL_XP_MULTIPLIER, multiplier, null, null);
    }

    public static DebuffEffect statPenalty(StatType stat, double percentage) {
        return new DebuffEffect(EffectType.STAT_XP_PENALTY, percentage, stat, null);
    }

    public static DebuffEffect goldPenalty(int amount) {
        return new DebuffEffect(EffectType.GOLD_PENALTY_PER_UNIT, amount, null, null);
    }

    public static DebuffEffect disableItem(String itemId) {
        return new DebuffEffect(EffectType.DISABLE_ITEM_EFFECT, 0, null, itemId);
    }

    public Optional<StatType> getTargetStat() {
        return Optional.ofNullable(targetStat);
    }

    public Optional<String> getTargetItemId() {
        return Optional.ofNullable(targetItemId);
    }
}