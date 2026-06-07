package com.lifeleveling.application;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cierre automático de día al reabrir (Opción A)")
class GameFacadeCatchUpTest {

    /** Reloj controlable: la fecha define now()/today(). */
    static final class FakeClock implements Clock {
        LocalDate date;
        FakeClock(LocalDate date) { this.date = date; }
        @Override public Instant now() { return date.atStartOfDay(ZoneId.systemDefault()).toInstant(); }
        @Override public LocalDate today() { return date; }
    }

    @Test
    @DisplayName("Reabrir en otro día cierra el día anterior una vez")
    void reopeningOnNewDay_autoClosesPreviousDay() {
        InMemoryPlayerRepository repo = new InMemoryPlayerRepository();

        GameFacade day1 = new GameFacade(repo, new FakeClock(LocalDate.of(2026, 6, 6)), ev -> {});
        day1.newGame("Jose");
        assertEquals(0, day1.journal().daysLogged());

        GameFacade day2 = new GameFacade(repo, new FakeClock(LocalDate.of(2026, 6, 7)), ev -> {});
        assertTrue(day2.loadGame());

        assertEquals(1, day2.journal().daysLogged(), "el día previo debe cerrarse al reabrir en nueva fecha");
    }

    @Test
    @DisplayName("Reabrir el mismo día no cierra nada")
    void reopeningSameDay_doesNotClose() {
        InMemoryPlayerRepository repo = new InMemoryPlayerRepository();
        FakeClock clock = new FakeClock(LocalDate.of(2026, 6, 7));

        GameFacade facade = new GameFacade(repo, clock, ev -> {});
        facade.newGame("Jose");

        GameFacade reopened = new GameFacade(repo, clock, ev -> {});
        assertTrue(reopened.loadGame());

        assertEquals(0, reopened.journal().daysLogged(), "mismo día: no se cierra ningún día");
    }
}
