package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.quest.condition.CareerHoursInPeriod;
import com.lifeleveling.domain.quest.condition.ConditionContext;
import com.lifeleveling.domain.quest.condition.PagesRead;
import com.lifeleveling.domain.quest.condition.UserQuestsCompleted;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.GateService;
import com.lifeleveling.domain.quest.system.GateVerificationResult;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import com.lifeleveling.domain.quest.user.UserQuest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cierre de día alimenta las condiciones de gate (resurrección de GATE 2/3/4)")
class GateFeedingTest {

    private ConditionContext ctx(Player p) {
        return ConditionContext.create(p, null, p.getGateTracker());
    }

    @Test
    @DisplayName("Las páginas no cuentan hasta que se cierra el día")
    void pages_onlyCountAfterDayClose() {
        Player player = Player.create("Lector");
        player.registerReadSession(500);

        assertFalse(new PagesRead(500, 7).isMet(ctx(player)),
                "Sin cerrar el día, el histórico está vacío");

        player.updateState(Instant.now()); // cierra el día

        assertTrue(new PagesRead(500, 7).isMet(ctx(player)),
                "Tras cerrar el día, las 500 páginas cuentan");
    }

    @Test
    @DisplayName("Completar User Quests alimenta la condición por rango")
    void userQuests_feedConditionByRank() {
        Player player = Player.create("Curranta");
        for (int i = 0; i < 3; i++) {
            UserQuest q = UserQuest.createWithoutDeadline("Q" + i, "desc", QuestRank.E);
            player.completeUserQuest(q, Instant.now());
        }
        player.updateState(Instant.now());

        assertTrue(new UserQuestsCompleted(3, 7, QuestRank.E).isMet(ctx(player)));
        assertFalse(new UserQuestsCompleted(3, 7, QuestRank.B).isMet(ctx(player)),
                "Las quests de rango E no cuentan para el umbral de rango B");
    }

    @Test
    @DisplayName("Las horas de CODE alimentan la ventana temporal")
    void careerHours_feedWindow() {
        Player player = Player.create("Dev");
        player.registerCodeSession(10);
        player.updateState(Instant.now());

        assertTrue(new CareerHoursInPeriod(10, 7).isMet(ctx(player)));
    }

    @Test
    @DisplayName("GATE 3 (C→C+) ahora es superable: nivel 35 + rango C + 500 páginas + día cerrado")
    void gate3_isCompletableAfterReadingAndClosingDay() {
        Player player = Player.create("Aspirante");
        player.addGeneralXP(62_000);          // ~nivel 35
        player.promoteToRank(PlayerRank.C);
        assertTrue(player.getLevel() >= 35, "Setup: debe estar a nivel 35+");

        GateService service = new GateService();

        // Antes de leer: la gate existe pero la condición de páginas falla.
        assertFalse(service.verifyGateProgress(player).success());

        player.registerReadSession(500);
        player.updateState(Instant.now());

        GateVerificationResult result = service.verifyGateProgress(player);
        assertTrue(result.success(), "GATE 3 debe ser superable tras leer 500 páginas");
        assertEquals(SystemQuestType.GATE_C_TO_C_PLUS, result.gate());
    }
}
