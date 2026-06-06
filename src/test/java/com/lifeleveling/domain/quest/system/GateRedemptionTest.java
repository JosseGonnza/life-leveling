package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.condition.GateTracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Redemption exige haber caído en desgracia (fix bug 6-jun)")
class GateRedemptionTest {

    private final GateService gates = new GateService();

    @Test
    @DisplayName("Un jugador nuevo y sano NO puede reclamar Redemption")
    void healthyPlayerCannotClaimRedemption() {
        Player player = Player.create("Jose");

        GateVerificationResult r = gates.verifyGate(player, SystemQuestType.GATE_REDEMPTION);

        assertFalse(r.success(), "sin 3 burnouts no debe estar disponible");
    }

    @Test
    @DisplayName("Tras 3 burnouts (caída) y HP recuperado 14 días, sí está disponible")
    void fallenAndRecoveredCanClaimRedemption() {
        Player player = Player.create("Jose");
        GateTracker tracker = player.getGateTracker();
        LocalDate today = LocalDate.now();
        // 3 burnouts hace 20-22 días: dentro de los 30 (caída) pero fuera de los 14 (ya recuperado).
        for (int i = 20; i <= 22; i++) {
            tracker.addDailyHistory(burnoutDay(today.minusDays(i)));
        }

        GateVerificationResult r = gates.verifyGate(player, SystemQuestType.GATE_REDEMPTION);

        assertTrue(r.success(), "caído (3 burnouts) + HP>80 14 días → disponible");
    }

    private GateTracker.DailyHistory burnoutDay(LocalDate date) {
        return new GateTracker.DailyHistory(
                date, false, 0, 0, 0.0, true, false,
                List.of(), Set.of(), Map.of());
    }
}
