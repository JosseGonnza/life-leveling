package com.lifeleveling.infrastructure.persistence;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.WeeklyQuestView;
import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.infrastructure.time.SystemClock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M5 - Persistencia JSON: la partida sobrevive a guardar y recargar")
class PersistenceRoundTripTest {

    @Test
    @DisplayName("Round-trip: nivel, oro, rango, quests activas, títulos e historial se conservan")
    void roundTrip(@TempDir Path dir) {
        Path save = dir.resolve("savegame.json");

        // Partida 1: jugar un poco y persistir (cada comando guarda).
        GameFacade f1 = new GameFacade(new JsonPlayerRepository(save), new SystemClock(), e -> {});
        f1.newGame("Jose");
        f1.workJob(16);                 // oro
        f1.workCode(4);                 // stats INT/DIS/WIS + nivel
        f1.read(40);
        f1.gym(true);
        f1.createQuest("Proyecto Z", "el grande", QuestRank.B, null);
        f1.endDay();                    // vuelca un día al historial

        int level = f1.state().level();
        int gold = f1.state().gold();
        String rank = f1.state().rank();
        int quests = f1.activeQuests().size();
        int daysLogged = f1.journal().daysLogged();

        assertTrue(gold > 0 && level >= 1 && quests == 1);

        // Partida 2: repo nuevo sobre el mismo fichero → debe cargar lo guardado.
        GameFacade f2 = new GameFacade(new JsonPlayerRepository(save), new SystemClock(), e -> {});
        assertTrue(f2.loadGame(), "debe encontrar la partida guardada");

        assertEquals(level, f2.state().level());
        assertEquals(gold, f2.state().gold());
        assertEquals(rank, f2.state().rank());
        assertEquals(quests, f2.activeQuests().size());
        assertEquals("Proyecto Z", f2.activeQuests().get(0).name());
        assertEquals(daysLogged, f2.journal().daysLogged());
        assertEquals("Jose", f2.state().name());
    }

    @Test
    @DisplayName("Round-trip intra-día: el progreso del día sobrevive sin cerrar día ni double-dip")
    void intradayRoundTrip(@TempDir Path dir) {
        Path save = dir.resolve("savegame.json");
        Clock fixed = new FixedClock(LocalDate.of(2026, 6, 8)); // mismo día en ambas sesiones → catch-up NO dispara

        GameFacade f1 = new GameFacade(new JsonPlayerRepository(save), fixed, e -> {});
        f1.newGame("Jose");
        f1.gym(true);
        f1.read(40);
        f1.skincare(true);

        int completed = f1.dailyChecklist().completedCount();
        int gold = f1.state().gold();
        int days = f1.journal().daysLogged();
        assertEquals(3, completed, "se registraron 3 hábitos");

        // Reabrir el MISMO día: no debe cerrarse el día ni resetear la checklist.
        GameFacade f2 = new GameFacade(new JsonPlayerRepository(save), fixed, e -> {});
        assertTrue(f2.loadGame());

        assertEquals(completed, f2.dailyChecklist().completedCount(), "la checklist del día se conserva");
        assertEquals(gold, f2.state().gold(), "no se re-aplican recompensas (sin double-dip)");
        assertEquals(days, f2.journal().daysLogged(), "no se ha cerrado ningún día al reabrir");
    }

    @Test
    @DisplayName("Misiones semanales: aparecen en partida nueva y su progreso sobrevive al recargar")
    void weeklyQuestsAppearAndPersist(@TempDir Path dir) {
        Path save = dir.resolve("savegame.json");
        Clock fixed = new FixedClock(LocalDate.of(2026, 6, 8));

        GameFacade f1 = new GameFacade(new JsonPlayerRepository(save), fixed, e -> {});
        f1.newGame("Jose");
        assertEquals(3, f1.weeklyQuests().size(), "la partida nueva ya trae las 3 semanales");

        // Actividad que alimenta varias semanales (horas, páginas, gym, sueño, dúo dieta+skincare).
        f1.workCode(4);
        f1.read(80);
        f1.gym(true);
        f1.diet(true);
        f1.skincare(true);
        f1.sleep(8);

        List<String> names = f1.weeklyQuests().stream().map(WeeklyQuestView::name).toList();
        int progress = f1.weeklyQuests().stream().mapToInt(WeeklyQuestView::currentProgress).sum();
        assertTrue(progress > 0, "la actividad debió alimentar alguna semanal");

        GameFacade f2 = new GameFacade(new JsonPlayerRepository(save), fixed, e -> {});
        assertTrue(f2.loadGame());

        assertEquals(names, f2.weeklyQuests().stream().map(WeeklyQuestView::name).toList(), "mismas semanales");
        assertEquals(progress, f2.weeklyQuests().stream().mapToInt(WeeklyQuestView::currentProgress).sum(),
                "el progreso semanal se conserva al recargar");
    }

    @Test
    @DisplayName("Historial de misiones: completadas y falladas se guardan y sobreviven al recargar")
    void questHistoryPersists(@TempDir Path dir) {
        Path save = dir.resolve("savegame.json");
        Clock fixed = new FixedClock(LocalDate.of(2026, 6, 8));

        GameFacade f1 = new GameFacade(new JsonPlayerRepository(save), fixed, e -> {});
        f1.newGame("Jose");
        var done = f1.createQuest("La hecha", "x", QuestRank.C, null);
        var lost = f1.createQuest("La fallada", "y", QuestRank.B, null);
        f1.completeQuest(done.id().toString());
        f1.failQuest(lost.id().toString());

        assertEquals(0, f1.activeQuests().size(), "ya no quedan activas");
        assertEquals(2, f1.questHistory().size(), "ambas pasan al historial");

        GameFacade f2 = new GameFacade(new JsonPlayerRepository(save), fixed, e -> {});
        assertTrue(f2.loadGame());

        assertEquals(2, f2.questHistory().size(), "el historial sobrevive al recargar");
        assertEquals(1, f2.questHistory().stream().filter(q -> q.status().equals("COMPLETED")).count());
        assertEquals(1, f2.questHistory().stream().filter(q -> q.status().equals("FAILED")).count());
    }

    private record FixedClock(LocalDate date) implements Clock {
        @Override public Instant now() { return date.atStartOfDay(ZoneOffset.UTC).toInstant(); }
        @Override public LocalDate today() { return date; }
    }

    @Test
    @DisplayName("Sin fichero, load() devuelve vacío")
    void noFileNoLoad(@TempDir Path dir) {
        GameFacade f = new GameFacade(new JsonPlayerRepository(dir.resolve("nope.json")), new SystemClock(), e -> {});
        assertFalse(f.loadGame());
    }
}
