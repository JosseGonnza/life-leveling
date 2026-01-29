package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;
import java.time.Duration;

/**
 * Especificación de un buff temporal asociado a un Item.
 * Define qué pasará si consumes el item.
 */
public record TemporaryBuffSpec(
        StatType targetStat,   // Stat afectado (ej: INTELLECT)
        double multiplier,     // Bonus (ej: 0.20 para +20%)
        Duration duration      // Duración (ej: Duration.ofHours(1))
) {
    public static TemporaryBuffSpec of(StatType stat, double multiplier, Duration duration) {
        return new TemporaryBuffSpec(stat, multiplier, duration);
    }
}