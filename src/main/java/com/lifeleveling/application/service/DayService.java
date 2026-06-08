package com.lifeleveling.application.service;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.event.GameEvent;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.system.GateService;
import com.lifeleveling.domain.quest.system.GateVerificationResult;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import com.lifeleveling.domain.quest.user.UserQuest;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
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

    private final Clock clock;
    private final GateService gateService = new GateService();
    private final Set<SystemQuestType> announced = EnumSet.noneOf(SystemQuestType.class);

    public DayService(Clock clock) {
        this.clock = clock;
    }

    public void endDay(Player player) {
        player.updateState(clock.now());
        failExpiredQuests(player);
        evaluateGateTriggers(player);
    }

    /** Cierre archivando el día bajo {@code closingDate} (cierre automático Opción A: el día real trabajado). */
    public void endDay(Player player, LocalDate closingDate) {
        player.updateState(clock.now(), closingDate);
        failExpiredQuests(player);
        evaluateGateTriggers(player);
    }

    /**
     * Falla las User Quests cuyo plazo ya venció (deadline anterior a HOY): aplica su penalización
     * de HP, las pasa al historial y avisa. Se compara contra el día real ({@code clock.today()}),
     * no contra la fecha de archivado, para que el cierre automático perdone huecos pero sí caduque
     * lo realmente vencido.
     */
    private void failExpiredQuests(Player player) {
        LocalDate today = clock.today();
        for (UserQuest quest : player.getActiveUserQuests()) {
            if (quest.getDeadline() != null && quest.getDeadline().isBefore(today)) {
                int hpDamage = quest.rank().getMoralDamage();
                player.applyUserQuestFailure(quest, clock.now());
                player.getEventPublisher().publish(GameEvent.questFailed(quest.name(), hpDamage));
            }
        }
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

        // Gates especiales: avisan cuando se manifiestan (Vault@40, Redemption tras caer).
        for (SystemQuestType special : List.of(SystemQuestType.GATE_VAULT, SystemQuestType.GATE_REDEMPTION)) {
            if (gateService.hasAppeared(player, special) && !tracker.isGateCompleted(special)) {
                announce(player, special);
            }
        }
    }

    private void announce(Player player, SystemQuestType gate) {
        if (!announced.add(gate)) return;
        player.getEventPublisher().publish(GameEvent.gateAvailable(gate.getName(), gate.getDescription()));
    }
}
