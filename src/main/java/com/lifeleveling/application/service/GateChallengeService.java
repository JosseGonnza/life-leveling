package com.lifeleveling.application.service;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.system.GateService;
import com.lifeleveling.domain.quest.system.GateVerificationResult;
import com.lifeleveling.domain.quest.system.SystemQuestType;

/**
 * Casos de uso de las Gates: consultar/desafiar la siguiente de la cadena, retar las
 * especiales (Vault/Redemption, fuera de la cadena) y registrar las confirmaciones
 * manuales (GATE 5/6: empleo conseguido, año ahorrado).
 */
public final class GateChallengeService {

    private final GateService gateService = new GateService();

    public GateVerificationResult status(Player player) {
        return gateService.verifyGateProgress(player);
    }

    public GateVerificationResult challenge(Player player) {
        return gateService.attemptGateCompletion(player);
    }

    public GateVerificationResult statusOf(Player player, SystemQuestType gate) {
        return gateService.verifyGate(player, gate);
    }

    public GateVerificationResult challengeOf(Player player, SystemQuestType gate) {
        return gateService.attemptGate(player, gate);
    }

    public void confirm(Player player, String confirmationId) {
        player.getGateTracker().setManualConfirmation(confirmationId, true);
    }
}
