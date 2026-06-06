package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.Player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gates especiales: aparición (no existen hasta activarse) y no re-reclamables")
class GateSpecialAvailabilityTest {

    private final GateService gates = new GateService();

    @Test
    @DisplayName("The Vault no se ha manifestado por debajo de nivel 40")
    void vaultHiddenBelow40() {
        Player player = Player.create("Jose");
        assertFalse(gates.hasAppeared(player, SystemQuestType.GATE_VAULT));
        assertFalse(gates.verifyGate(player, SystemQuestType.GATE_VAULT).success(),
                "no se puede ni desafiar antes de aparecer");
    }

    @Test
    @DisplayName("The Vault se manifiesta al alcanzar nivel 40")
    void vaultAppearsAt40() {
        Player player = Player.create("Jose");
        player.addGeneralXP(80_000); // sqrt(80000/50) = nivel 40
        assertTrue(player.getLevel() >= 40);
        assertTrue(gates.hasAppeared(player, SystemQuestType.GATE_VAULT));
    }

    @Test
    @DisplayName("Redemption no se ha manifestado sin haber caído (0 burnouts)")
    void redemptionHiddenWithoutFalling() {
        Player player = Player.create("Jose");
        assertFalse(gates.hasAppeared(player, SystemQuestType.GATE_REDEMPTION));
    }

    @Test
    @DisplayName("Una Gate ya superada no se vuelve a reclamar")
    void completedGateNotReclaimable() {
        Player player = Player.create("Jose");
        player.getGateTracker().markGateAsCompleted(SystemQuestType.GATE_E_TO_D);

        var result = gates.attemptGate(player, SystemQuestType.GATE_E_TO_D);

        assertFalse(result.success());
        assertTrue(result.message().toLowerCase().contains("superada"));
    }
}
