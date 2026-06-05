package com.lifeleveling.application;

import com.lifeleveling.application.dto.QuestView;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.user.UserQuest;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M4 - User Quests activas viven en el agregado y persisten")
class ActiveQuestsTest {

    private GameFacade newFacade(InMemoryPlayerRepository repo) {
        return new GameFacade(repo, new SystemClock(), e -> {});
    }

    @Test
    @DisplayName("Crear deja la quest activa; completar por id la retira y da XP")
    void createThenCompleteById() {
        var repo = new InMemoryPlayerRepository();
        GameFacade facade = newFacade(repo);
        facade.newGame("Jose");

        UserQuest q = facade.createQuest("Proyecto Z", "El grande", QuestRank.B);
        assertEquals(1, facade.activeQuests().size());

        facade.completeQuest(q.id().toString());

        assertTrue(facade.activeQuests().isEmpty(), "completar la retira de activas");
        assertTrue(facade.state().totalXP() > 0, "B da XP");
    }

    @Test
    @DisplayName("Fallar por id retira la quest de activas")
    void failByIdRemoves() {
        GameFacade facade = newFacade(new InMemoryPlayerRepository());
        facade.newGame("Jose");

        UserQuest q = facade.createQuest("Tarea volátil", "x", QuestRank.C);
        assertEquals(1, facade.activeQuests().size());

        facade.failQuest(q.id().toString());
        assertTrue(facade.activeQuests().isEmpty());
    }

    @Test
    @DisplayName("Las quests activas persisten al recargar (mismo repo)")
    void activeQuestsPersistAcrossReload() {
        var repo = new InMemoryPlayerRepository();
        GameFacade facade = newFacade(repo);
        facade.newGame("Jose");
        facade.createQuest("Persistente", "no me pierdas", QuestRank.A);

        GameFacade reopened = newFacade(repo);
        assertTrue(reopened.loadGame());
        assertEquals(1, reopened.activeQuests().size());
        assertEquals("Persistente", reopened.activeQuests().get(0).name());
    }

    @Test
    @DisplayName("Se activa el deadline en la creación con fecha")
    void deadlineIsWired() {
        GameFacade facade = newFacade(new InMemoryPlayerRepository());
        facade.newGame("Jose");

        LocalDate deadline = LocalDate.now().plusDays(7);
        facade.createQuest("Con plazo", "vence pronto", QuestRank.B, deadline);

        QuestView view = facade.activeQuests().get(0);
        assertEquals(deadline.toString(), view.deadline());
        assertEquals("B", view.rank());
        assertTrue(view.rewardXP() > 0);
    }
}
