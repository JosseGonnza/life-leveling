package com.lifeleveling.domain.debuff;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class Debuff {

    private final UUID id;
    private final DebuffType type;
    private final Instant appliedAt;
    private final Instant expiresAt; // Puede ser null
    private final String source;

    private Debuff(DebuffType type, Instant appliedAt, Instant expiresAt, String source) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.appliedAt = appliedAt;
        this.expiresAt = expiresAt;
        this.source = source;
    }

    public static Debuff create(DebuffType type, String source, Instant now) {
        Instant expiration = null;
        if (type.getDefaultDuration() != null && !type.getDefaultDuration().isZero()) {
            expiration = now.plus(type.getDefaultDuration());
        }
        return new Debuff(type, now, expiration, source);
    }

    public static Debuff createWithExpiration(DebuffType type, String source, Instant now, Instant expiresAt) {
        return new Debuff(type, now, expiresAt, source);
    }

    public boolean isExpired(Instant now) {
        if (expiresAt == null) return false;
        return now.isAfter(expiresAt);
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }

    // [FASE 10] UI Helper: Formato bonito con tiempo restante
    public String toDisplayString(Instant now) {
        String base = type.getIcon() + " " + type.getDisplayName();

        if (expiresAt != null) {
            long minutesLeft = Duration.between(now, expiresAt).toMinutes();
            if (minutesLeft > 0) {
                long hours = minutesLeft / 60;
                long mins = minutesLeft % 60;
                return String.format("%s (%dh %dm)", base, hours, mins);
            } else {
                return base + " (Expirando...)";
            }
        }
        return base; // Persistente (hasta cura manual)
    }

    public UUID getId() { return id; }
    public DebuffType getType() { return type; }
    public Instant getAppliedAt() { return appliedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getSource() { return source; }
}