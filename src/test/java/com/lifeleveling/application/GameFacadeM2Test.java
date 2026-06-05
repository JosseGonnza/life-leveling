package com.lifeleveling.application;

import com.lifeleveling.application.dto.PlayerView;
import com.lifeleveling.application.port.Notifier;
import com.lifeleveling.domain.event.GameEvent;
import com.lifeleveling.domain.event.GameEventType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.user.UserQuest;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M2 - GameFacade orquesta un día entero de juego")
class GameFacadeM2Test {

    static final class CollectingNotifier implements Notifier {
        final List<GameEvent> events = new ArrayList<>();
        @Override public void onEvent(GameEvent event) { events.add(event); }
    }

    @Test
    @DisplayName("Simular un día: trabajar, estudiar, comprar, completar quest, cerrar día")
    void simulateADay() {
        InMemoryPlayerRepository repo = new InMemoryPlayerRepository();
        CollectingNotifier notifier = new CollectingNotifier();
        GameFacade facade = new GameFacade(repo, new SystemClock(), notifier);

        PlayerView start = facade.newGame("Jose");
        assertEquals(1, start.level());
        assertEquals(0, start.gold());
        assertEquals(100, start.currentHP());
        assertEquals("Novato", start.rank());

        // JOB: 8h a rango E = 250 G
        assertEquals(250, facade.workJob(8).gold());

        // CODE: estudia (gana XP de stats, gasta HP, 0 oro)
        facade.workCode(5);

        // Tienda: comprar y consumir un espresso (80 G)
        assertEquals(170, facade.buy("consumable_espresso").gold());
        facade.consume("consumable_espresso");

        // User Quest rango S (+5000 XP, sin oro por C1) → cruza nivel 10 y dispara el milestone L10 (+1000 G)
        UserQuest project = facade.createQuest("Proyecto Personal", "El grande", QuestRank.S);
        PlayerView afterQuest = facade.completeQuest(project);
        assertTrue(afterQuest.level() >= 10, "5000 XP debe subir bien de nivel");
        assertEquals(1170, afterQuest.gold(), "170 + 1000 del milestone L10 (la quest en sí no da oro)");

        // Cerrar el día no rompe nada
        assertDoesNotThrow(facade::endDay);

        // Los eventos del dominio llegaron al Notifier (level-up y milestone)
        assertTrue(notifier.events.stream().anyMatch(e -> e.type() == GameEventType.LEVEL_UP),
                "El subidón de nivel debe haber emitido un evento LEVEL_UP");

        // Persistencia: otra fachada sobre el mismo repo recupera la partida
        GameFacade reopened = new GameFacade(repo, new SystemClock(), new CollectingNotifier());
        assertTrue(reopened.loadGame());
        assertEquals(1170, reopened.state().gold());
        assertEquals("Jose", reopened.state().name());
    }

    @Test
    @DisplayName("Operar sin partida activa falla con mensaje claro")
    void noActiveGame_throws() {
        GameFacade facade = new GameFacade(new InMemoryPlayerRepository(), new SystemClock(), new CollectingNotifier());
        assertThrows(IllegalStateException.class, facade::state);
        assertThrows(IllegalStateException.class, () -> facade.workJob(8));
    }
}
