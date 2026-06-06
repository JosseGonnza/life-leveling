package com.lifeleveling.application;

import com.lifeleveling.application.dto.JournalView;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M4 - Read model del Journal (timeline desde DailyHistory + analytics)")
class JournalReadModelTest {

    private GameFacade newFacade() {
        GameFacade f = new GameFacade(new InMemoryPlayerRepository(), new SystemClock(), e -> {});
        f.newGame("Jose");
        return f;
    }

    @Test
    @DisplayName("Sin días cerrados el timeline está vacío; siempre hay 5 líneas de stats")
    void emptyTimelineButStats() {
        JournalView j = newFacade().journal();
        assertTrue(j.timeline().isEmpty());
        assertEquals(0, j.daysLogged());
        assertEquals(5, j.stats().size());
    }

    @Test
    @DisplayName("Cerrar el día añade una entrada al timeline con la actividad registrada")
    void closingDayPopulatesTimeline() {
        GameFacade f = newFacade();
        f.read(40);
        f.endDay();

        JournalView j = f.journal();
        assertEquals(1, j.daysLogged());
        assertEquals(40, j.timeline().get(0).pagesRead());
        assertEquals(40, j.totalPagesRead());
    }
}
