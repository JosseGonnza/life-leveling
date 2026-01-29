package com.lifeleveling.domain.buff;

import com.lifeleveling.domain.player.StatType;
import java.time.Instant;

/**
 * Instancia activa de un buff temporal en el jugador.
 */
public record TemporaryBuff(
        StatType targetStat,
        double multiplier,
        Instant expiresAt,
        String sourceItemName
) {
    public boolean isActive(Instant now) {
        return now.isBefore(expiresAt);
    }

    public String toDisplayString() {
        long minutesLeft = java.time.Duration.between(Instant.now(), expiresAt).toMinutes();
        return String.format("+%.0f%% %s XP (%d min)",
                multiplier * 100, targetStat.name(), Math.max(1, minutesLeft));
    }
}