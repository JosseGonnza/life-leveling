package com.lifeleveling.application.service;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.event.GameEvent;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.system.GateService;
import com.lifeleveling.domain.quest.system.GateVerificationResult;
import com.lifeleveling.domain.quest.system.SystemQuestType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Caso de uso del rollover diario: cierra el día (vuelca el DailyHistory, rompe rachas,
 * limpia buffs, rotación semanal) vía Player.updateState y, acto seguido, dispara los
 * triggers que el dominio no hace por sí solo: la aparición de Gates.
 *
 * Aparición = avisar una sola vez (GameEvent.gateAvailable) cuando una Gate pasa a estar
 * disponible: la lineal al alcanzar su nivel, The Vault a nivel 40, Redemption a 3 burnouts/mes.
 * El "ya avisado" se guarda aquí (capa app, frontera de activación). Es estado transitorio:
 * al recargar partida se re-avisa una vez de las pendientes (recordatorio deseable). [M5: persistir si molesta]
 */
public final class DayService {

    private static final int REDEMPTION_BURNOUTS_THRESHOLD = 3;

    private final Clock clock;
    private final GateService gateService = new GateService();
    private final Set<SystemQuestType> announced = EnumSet.noneOf(SystemQuestType.class);

    public DayService(Clock clock) {
        this.clock = clock;
    }

    public void endDay(Player player) {
        player.updateState(clock.now());
        evaluateGateTriggers(player);
    }

    private void evaluateGateTriggers(Player player) {
        GateTracker tracker = player.getGateTracker();
        int level = player.getLevel();

        // Gate lineal: aparece al alcanzar su requisito de nivel.
        GateVerificationResult next = gateService.verifyGateProgress(player);
        SystemQuestType nextGate = next.gate();
        if (nextGate != null && !tracker.isGateCompleted(nextGate)
                && nextGate.meetsLevelRequirement(level)) {
            announce(player, nextGate);
        }

        // The Vault: aparece a nivel 40.
        if (level >= SystemQuestType.GATE_VAULT.getLevelRequirement()
                && !tracker.isGateCompleted(SystemQuestType.GATE_VAULT)) {
            announce(player, SystemQuestType.GATE_VAULT);
        }

        // Redemption: aparece tras caer en desgracia (3 burnouts en el último mes).
        if (tracker.getBurnoutsInLastMonth() >= REDEMPTION_BURNOUTS_THRESHOLD
                && !tracker.isGateCompleted(SystemQuestType.GATE_REDEMPTION)) {
            announce(player, SystemQuestType.GATE_REDEMPTION);
        }
    }

    private void announce(Player player, SystemQuestType gate) {
        if (!announced.add(gate)) return;
        player.getEventPublisher().publish(GameEvent.gateAvailable(gate.getName(), gate.getDescription()));
    }
}
