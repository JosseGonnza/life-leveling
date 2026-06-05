package com.lifeleveling.application.service;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.system.GateService;
import com.lifeleveling.domain.quest.system.GateVerificationResult;

/**
 * Casos de uso de las Gates: consultar la siguiente, desafiarla y registrar las
 * confirmaciones manuales (GATE 5/6: empleo conseguido, año ahorrado).
 */
public final class GateChallengeService {

    private final GateService gateService = new GateService();

    public GateVerificationResult status(Player player) {
        return gateService.verifyGateProgress(player);
    }

    public GateVerificationResult challenge(Player player) {
        return gateService.attemptGateCompletion(player);
    }

    public void confirm(Player player, String confirmationId) {
        player.getGateTracker().setManualConfirmation(confirmationId, true);
    }
}
