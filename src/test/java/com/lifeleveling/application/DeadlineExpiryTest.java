package com.lifeleveling.application;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auto-fallo de misiones con plazo vencido al cerrar día")
class DeadlineExpiryTest {

    static final class MovableClock implements Clock {
        LocalDate date;
        MovableClock(LocalDate date) { this.date = date; }
        @Override public Instant now() { return date.atStartOfDay(ZoneOffset.UTC).toInstant(); }
        @Override public LocalDate today() { return date; }
    }

    @Test
    @DisplayName("Una quest con plazo vencido se falla, sale de activas y pasa al historial")
    void expiredQuestFailsOnDayClose() {
        MovableClock clock = new MovableClock(LocalDate.of(2026, 6, 8));
        GameFacade f = new GameFacade(new InMemoryPlayerRepository(), clock, e -> {});
        f.newGame("Jose");
        f.createQuest("Entregar el proyecto", "x", QuestRank.B, LocalDate.of(2026, 6, 9));
        assertEquals(1, f.activeQuests().size());
        int hpBefore = f.state().currentHP();

        clock.date = LocalDate.of(2026, 6, 10); // el plazo (9) ya pasó
        f.endDay();

        assertEquals(0, f.activeQuests().size(), "la vencida sale de activas");
        assertEquals(1, f.questHistory().stream().filter(q -> q.status().equals("FAILED")).count(),
                "pasa al historial como fallida");
        assertTrue(f.state().currentHP() < hpBefore, "aplica la penalización de HP");
    }

    @Test
    @DisplayName("Una quest dentro de plazo sobrevive al cierre de día")
    void questWithinDeadlineSurvives() {
        MovableClock clock = new MovableClock(LocalDate.of(2026, 6, 8));
        GameFacade f = new GameFacade(new InMemoryPlayerRepository(), clock, e -> {});
        f.newGame("Jose");
        f.createQuest("Aún a tiempo", "y", QuestRank.C, LocalDate.of(2026, 6, 20));

        f.endDay();

        assertEquals(1, f.activeQuests().size(), "no vence todavía");
    }
}
