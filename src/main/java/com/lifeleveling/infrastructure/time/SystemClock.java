package com.lifeleveling.infrastructure.time;

import com.lifeleveling.application.port.Clock;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Reloj real del sistema. Implementación de producción del puerto Clock.
 */
public final class SystemClock implements Clock {

    private final ZoneId zone;

    public SystemClock() {
        this(ZoneId.systemDefault());
    }

    public SystemClock(ZoneId zone) {
        this.zone = zone;
    }

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(zone);
    }
}
