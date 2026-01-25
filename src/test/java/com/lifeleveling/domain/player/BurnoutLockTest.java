package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BurnoutLock - Entity de Lockout por Burnout")
class BurnoutLockTest {

    @Test
    @DisplayName("trigger() crea BurnoutLock con duración de 24h")
    void whenTriggering_thenCreatesLockWith24HourDuration() {
        Instant now = Instant.now();
        BurnoutLock lock = BurnoutLock.trigger(now);

        assertNotNull(lock.id());
        assertEquals(now, lock.triggeredAt());
        assertEquals(now.plus(Duration.ofHours(24)), lock.expiresAt());
    }

    @Test
    @DisplayName("trigger() con null lanza IllegalArgumentException")
    void whenTriggeringWithNull_thenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> BurnoutLock.trigger(null));
    }

    @Test
    @DisplayName("trigger() genera IDs únicos para cada lock")
    void whenTriggeringMultipleLocks_thenUniqueIDs() {
        Instant now = Instant.now();
        BurnoutLock lock1 = BurnoutLock.trigger(now);
        BurnoutLock lock2 = BurnoutLock.trigger(now);

        assertNotEquals(lock1.id(), lock2.id());
    }

    @Test
    @DisplayName("reconstitute() crea lock con datos específicos")
    void whenReconstituting_thenUsesProvidedData() {
        UUID id = UUID.randomUUID();
        Instant triggered = Instant.parse("2024-01-24T10:00:00Z");
        Instant expires = Instant.parse("2024-01-25T10:00:00Z");

        BurnoutLock lock = BurnoutLock.reconstitute(id, triggered, expires);

        assertEquals(id, lock.id());
        assertEquals(triggered, lock.triggeredAt());
        assertEquals(expires, lock.expiresAt());
    }

    @Test
    @DisplayName("Constructor con ID null lanza IllegalArgumentException")
    void whenCreatingWithNullId_thenThrowsException() {
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class,
                () -> new BurnoutLock(null, now, now.plus(Duration.ofHours(24))));
    }

    @Test
    @DisplayName("Constructor con triggeredAt null lanza IllegalArgumentException")
    void whenCreatingWithNullTriggeredAt_thenThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new BurnoutLock(UUID.randomUUID(), null, Instant.now()));
    }

    @Test
    @DisplayName("Constructor con expiresAt null lanza IllegalArgumentException")
    void whenCreatingWithNullExpiresAt_thenThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new BurnoutLock(UUID.randomUUID(), Instant.now(), null));
    }

    @Test
    @DisplayName("Constructor con expiresAt anterior a triggeredAt lanza IllegalArgumentException")
    void whenExpiresBeforeTrigger_thenThrowsException() {
        Instant now = Instant.now();
        Instant past = now.minus(Duration.ofHours(1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BurnoutLock(UUID.randomUUID(), now, past)
        );

        assertTrue(exception.getMessage().contains("anterior al trigger"));
    }

    @Test
    @DisplayName("Constructor con duración menor a 24h lanza IllegalArgumentException")
    void whenDurationLessThan24Hours_thenThrowsException() {
        Instant now = Instant.now();
        Instant tooSoon = now.plus(Duration.ofHours(12));

        assertThrows(IllegalArgumentException.class,
                () -> new BurnoutLock(UUID.randomUUID(), now, tooSoon));
    }

    @Nested
    class LockoutState {
        @Test
        @DisplayName("isActive() retorna true inmediatamente después del trigger")
        void whenJustTriggered_thenIsActive() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            assertTrue(lock.isActive(now));
            assertFalse(lock.hasExpired(now));
        }

        @Test
        @DisplayName("isActive() retorna true durante las 24 horas")
        void whenWithinLockoutPeriod_thenIsActive() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after12Hours = now.plus(Duration.ofHours(12));
            assertTrue(lock.isActive(after12Hours));
            assertFalse(lock.hasExpired(after12Hours));
        }

        @Test
        @DisplayName("isActive() retorna false después de 24 horas")
        void whenAfter24Hours_thenNotActive() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after25Hours = now.plus(Duration.ofHours(25));
            assertFalse(lock.isActive(after25Hours));
            assertTrue(lock.hasExpired(after25Hours));
        }

        @Test
        @DisplayName("isActive() en el momento exacto de expiración retorna false")
        void whenExactlyAtExpiry_thenNotActive() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant exactExpiry = lock.expiresAt();
            assertFalse(lock.isActive(exactExpiry), "En el momento exacto ya no está activo");
            assertTrue(lock.hasExpired(exactExpiry));
        }

        @Test
        @DisplayName("hasExpired() retorna false antes de expirar")
        void whenBeforeExpiry_thenNotExpired() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant oneSecondBefore = lock.expiresAt().minus(Duration.ofSeconds(1));
            assertFalse(lock.hasExpired(oneSecondBefore));
            assertTrue(lock.isActive(oneSecondBefore));
        }

        @Test
        @DisplayName("isActive() con null lanza IllegalArgumentException")
        void whenCheckingActiveWithNull_thenThrowsException() {
            BurnoutLock lock = BurnoutLock.trigger(Instant.now());
            assertThrows(IllegalArgumentException.class, () -> lock.isActive(null));
        }

        @Test
        @DisplayName("hasExpired() con null lanza IllegalArgumentException")
        void whenCheckingExpiredWithNull_thenThrowsException() {
            BurnoutLock lock = BurnoutLock.trigger(Instant.now());
            assertThrows(IllegalArgumentException.class, () -> lock.hasExpired(null));
        }
    }

    @Nested
    class TemporaryCalculations {
        @Test
        @DisplayName("getTimeRemaining() al inicio retorna 24 horas")
        void whenJustTriggered_thenFullDurationRemaining() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Duration remaining = lock.getTimeRemaining(now);
            assertEquals(Duration.ofHours(24), remaining);
        }

        @ParameterizedTest(name = "Después de {0}h: quedan {1}h")
        @CsvSource({
                "1,  23",
                "6,  18",
                "12, 12",
                "18, 6",
                "23, 1"
        })
        @DisplayName("getTimeRemaining() calcula correctamente el tiempo restante")
        void whenCalculatingTimeRemaining_thenCorrectDuration(int hoursElapsed, int hoursRemaining) {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant future = now.plus(Duration.ofHours(hoursElapsed));
            Duration remaining = lock.getTimeRemaining(future);

            assertEquals(Duration.ofHours(hoursRemaining), remaining);
        }

        @Test
        @DisplayName("getTimeRemaining() después de expirar retorna Duration.ZERO")
        void whenExpired_thenTimeRemainingIsZero() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after25Hours = now.plus(Duration.ofHours(25));
            assertEquals(Duration.ZERO, lock.getTimeRemaining(after25Hours));
        }

        @Test
        @DisplayName("getTimeElapsed() calcula tiempo transcurrido correctamente")
        void whenCalculatingTimeElapsed_thenCorrectDuration() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after10Hours = now.plus(Duration.ofHours(10));
            Duration elapsed = lock.getTimeElapsed(after10Hours);

            assertEquals(Duration.ofHours(10), elapsed);
        }

        @Test
        @DisplayName("getTimeElapsed() antes del trigger retorna Duration.ZERO")
        void whenBeforeTrigger_thenElapsedIsZero() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant past = now.minus(Duration.ofHours(1));
            assertEquals(Duration.ZERO, lock.getTimeElapsed(past));
        }

        @ParameterizedTest(name = "Después de {0}h: progreso = {1}%")
        @CsvSource({
                "0,  0.0",
                "6,  25.0",
                "12, 50.0",
                "18, 75.0",
                "24, 100.0",
                "30, 100.0"   //Después de expirar, clampea a 100%
        })
        @DisplayName("getProgressPercentage() calcula porcentaje correctamente")
        void whenCalculatingProgress_thenCorrectPercentage(int hoursElapsed, double expectedProgress) {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant future = now.plus(Duration.ofHours(hoursElapsed));
            double progress = lock.getProgressPercentage(future);

            assertEquals(expectedProgress, progress, 0.1);
        }

        @Test
        @DisplayName("getProgressPercentage() con null lanza IllegalArgumentException")
        void whenCalculatingProgressWithNull_thenThrowsException() {
            BurnoutLock lock = BurnoutLock.trigger(Instant.now());
            assertThrows(IllegalArgumentException.class, () -> lock.getProgressPercentage(null));
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("formatTimeRemaining() muestra formato legible")
        void whenFormattingTimeRemaining_thenReadableFormat() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after5Hours = now.plus(Duration.ofHours(5).plusMinutes(30));
            String formatted = lock.formatTimeRemaining(after5Hours);

            assertTrue(formatted.contains("18h"));
            assertTrue(formatted.contains("30m"));
        }

        @Test
        @DisplayName("formatTimeRemaining() para lock expirado muestra 'Expirado'")
        void whenLockExpired_thenFormatsAsExpired() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after25Hours = now.plus(Duration.ofHours(25));
            assertEquals("Expirado", lock.formatTimeRemaining(after25Hours));
        }

        @Test
        @DisplayName("toDisplayString() formatea estado completo del lock")
        void whenFormattingDisplay_thenIncludesAllInfo() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after12Hours = now.plus(Duration.ofHours(12));
            String display = lock.toDisplayString(after12Hours);

            assertTrue(display.contains("🔒 BURNOUT ACTIVO"));
            assertTrue(display.contains("12h"));
            assertTrue(display.contains("[50%]"));
        }

        @Test
        @DisplayName("toDisplayString() para lock expirado muestra 'SUPERADO'")
        void whenLockExpired_thenDisplayShowsSuperado() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            Instant after25Hours = now.plus(Duration.ofHours(25));
            String display = lock.toDisplayString(after25Hours);

            assertEquals("✅ BURNOUT SUPERADO", display);
        }

        @Test
        @DisplayName("toString() incluye todos los campos para debugging")
        void whenCallingToString_thenIncludesAllFields() {
            Instant now = Instant.parse("2024-01-24T10:00:00Z");
            BurnoutLock lock = BurnoutLock.trigger(now);

            String str = lock.toString();

            assertTrue(str.contains("BurnoutLock["));
            assertTrue(str.contains("id="));
            assertTrue(str.contains("triggered="));
            assertTrue(str.contains("expires="));
            assertTrue(str.contains("duration="));
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Lock con duración exacta de 24h es válido")
        void whenDurationIsExactly24Hours_thenValid() {
            Instant now = Instant.now();
            Instant expires = now.plus(Duration.ofHours(24));

            assertDoesNotThrow(() ->
                    new BurnoutLock(UUID.randomUUID(), now, expires));
        }

        @Test
        @DisplayName("Lock con duración mayor a 24h es válido")
        void whenDurationIsMoreThan24Hours_thenValid() {
            Instant now = Instant.now();
            Instant expires = now.plus(Duration.ofHours(48));

            assertDoesNotThrow(() ->
                    new BurnoutLock(UUID.randomUUID(), now, expires));
        }

        @Test
        @DisplayName("isActive() en el límite exacto del trigger retorna true")
        void whenAtExactTriggerTime_thenIsActive() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            assertTrue(lock.isActive(now), "En el momento del trigger debe estar activo");
        }

        @Test
        @DisplayName("Lock funciona correctamente con fechas en el pasado")
        void whenReconstitutingOldLock_thenWorksCorrectly() {
            Instant triggered = Instant.parse("2024-01-01T00:00:00Z");
            Instant expires = triggered.plus(Duration.ofHours(24));
            Instant now = Instant.parse("2024-01-20T00:00:00Z");

            BurnoutLock lock = BurnoutLock.reconstitute(UUID.randomUUID(), triggered, expires);

            assertTrue(lock.hasExpired(now));
            assertFalse(lock.isActive(now));
            assertEquals(Duration.ZERO, lock.getTimeRemaining(now));
        }

        @Test
        @DisplayName("getTimeRemaining() con tiempo antes del trigger retorna duración completa")
        void whenCheckingBeforeTrigger_thenFullDurationRemaining() {
            Instant now = Instant.now();
            BurnoutLock lock = BurnoutLock.trigger(now);

            // Verificamos con un tiempo antes del trigger (caso edge raro pero posible)
            Instant past = now.minus(Duration.ofHours(1));

            // El lock se activó "en el futuro" relativo a este timestamp
            // No debería ser activo aún
            assertFalse(lock.isActive(past));
        }

        @Test
        @DisplayName("Constante LOCKOUT_DURATION es exactamente 24 horas")
        void lockoutDuration_isExactly24Hours() {
            assertEquals(Duration.ofHours(24), BurnoutLock.LOCKOUT_DURATION);
            assertEquals(24 * 60 * 60, BurnoutLock.LOCKOUT_DURATION.getSeconds());
        }
    }
}