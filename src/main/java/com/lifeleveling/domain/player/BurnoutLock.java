package com.lifeleveling.domain.player;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/*
 * Reglas de Negocio:
 * - Trigger: Inmediato al tocar HP=0
 * - Duración: 24 horas desde el momento del trigger
 * - Bloqueo: Quests Rango C, B, A, S bloqueadas
 * - Permitido: Daily Quests (Rango E/D) para recuperar HP
 * - Penalización Económica: -10% del Gold total (aplicada externamente al inicio)
 * - Hospitalización Prolongada: Si expira y HP=0 -> -5% Gold y renovación por 24h.
 *
 * Filosofía:
 * El Burnout no es un "Game Over", es un "Modo Supervivencia".
 */
public record BurnoutLock(
        UUID id,
        Instant triggeredAt,
        Instant expiresAt
) {

    public static final Duration LOCKOUT_DURATION = Duration.ofHours(24);

    public BurnoutLock {
        if (id == null) throw new IllegalArgumentException("El ID del BurnoutLock no puede ser null");
        if (triggeredAt == null) throw new IllegalArgumentException("El timestamp de trigger no puede ser null");
        if (expiresAt == null) throw new IllegalArgumentException("El timestamp de expiración no puede ser null");
        if (expiresAt.isBefore(triggeredAt)) {
            throw new IllegalArgumentException(String.format("La fecha de expiración (%s) no puede ser anterior al trigger (%s)", expiresAt, triggeredAt));
        }

        Duration actualDuration = Duration.between(triggeredAt, expiresAt);
        if (actualDuration.compareTo(LOCKOUT_DURATION) < 0) {
            throw new IllegalArgumentException(String.format("La duración del lock (%s) debe ser al menos %s", actualDuration, LOCKOUT_DURATION));
        }
    }

    public static BurnoutLock createNow() {
        return trigger(Instant.now());
    }

    public static BurnoutLock trigger(Instant now) {
        if (now == null) throw new IllegalArgumentException("El timestamp actual no puede ser null");
        return new BurnoutLock(UUID.randomUUID(), now, now.plus(LOCKOUT_DURATION));
    }

    public static BurnoutLock reconstitute(UUID id, Instant triggeredAt, Instant expiresAt) {
        return new BurnoutLock(id, triggeredAt, expiresAt);
    }

    public boolean isActive(Instant currentTime) {
        if (currentTime == null) throw new IllegalArgumentException("El timestamp actual no puede ser null");
        return !currentTime.isBefore(triggeredAt) && currentTime.isBefore(expiresAt);
    }

    public boolean hasExpired(Instant currentTime) {
        if (currentTime == null) throw new IllegalArgumentException("El timestamp actual no puede ser null");
        return !currentTime.isBefore(expiresAt);
    }

    // [FIX] Calcula días extra de hospitalización (útil para logs o UI)
    public long getExtendedHospitalizationDays(Instant now) {
        if (!hasExpired(now)) return 0;
        return Duration.between(expiresAt, now).toDays();
    }

    public Duration getTimeRemaining(Instant currentTime) {
        if (currentTime == null) throw new IllegalArgumentException("El timestamp actual no puede ser null");
        if (hasExpired(currentTime)) return Duration.ZERO;
        return Duration.between(currentTime, expiresAt);
    }

    public Duration getTimeElapsed(Instant currentTime) {
        if (currentTime == null) throw new IllegalArgumentException("El timestamp actual no puede ser null");
        if (currentTime.isBefore(triggeredAt)) return Duration.ZERO;
        return Duration.between(triggeredAt, currentTime);
    }

    public double getProgressPercentage(Instant currentTime) {
        if (currentTime == null) throw new IllegalArgumentException("El timestamp actual no puede ser null");
        if (hasExpired(currentTime)) return 100.0;
        if (currentTime.isBefore(triggeredAt)) return 0.0;

        long totalSeconds = LOCKOUT_DURATION.getSeconds();
        long elapsedSeconds = getTimeElapsed(currentTime).getSeconds();

        return (elapsedSeconds * 100.0) / totalSeconds;
    }

    public String formatTimeRemaining(Instant currentTime) {
        if (hasExpired(currentTime)) return "Expirado";

        Duration remaining = getTimeRemaining(currentTime);
        long hours = remaining.toHours();
        long minutes = remaining.toMinutesPart();

        if (hours > 0) return String.format("%dh %dm restantes", hours, minutes);
        else return String.format("%dm restantes", minutes);
    }

    public String toDisplayString(Instant currentTime) {
        if (hasExpired(currentTime)) return "✅ BURNOUT SUPERADO";

        return String.format("🔒 BURNOUT ACTIVO | %s | [%.0f%%]",
                formatTimeRemaining(currentTime),
                getProgressPercentage(currentTime)
        );
    }

    @Override
    public String toString() {
        return String.format("BurnoutLock[id=%s, triggered=%s, expires=%s, duration=%s]",
                id, triggeredAt, expiresAt, Duration.between(triggeredAt, expiresAt));
    }
}