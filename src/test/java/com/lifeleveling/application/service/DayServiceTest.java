package com.lifeleveling.application.service;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.event.GameEvent;
import com.lifeleveling.domain.event.GameEventType;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.condition.GateTracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DayService - triggers de aparición de Gates en el rollover diario")
class DayServiceTest {

    static final class FixedClock implements Clock {
        @Override public Instant now() { return Instant.now(); }
        @Override public LocalDate today() { return LocalDate.now(); }
    }

    private List<GameEvent> wireCollector(Player player) {
        List<GameEvent> events = new ArrayList<>();
        player.getEventPublisher().addListener(events::add);
        return events;
    }

    private long gatesAvailableMentioning(List<GameEvent> events, String needle) {
        return events.stream()
                .filter(e -> e.type() == GameEventType.GATE_AVAILABLE)
                .filter(e -> e.message().contains(needle))
                .count();
    }

    @Test
    @DisplayName("The Vault aparece al alcanzar nivel 40, y solo se avisa una vez")
    void vaultAppearsAtLevel40_announcedOnce() {
        Player player = Player.create("Jose");
        player.addGeneralXP(85_000); // sqrt(85000/50) ≈ nivel 41
        assertTrue(player.getLevel() >= 40, "85k XP debe pasar de nivel 40");

        List<GameEvent> events = wireCollector(player);
        DayService day = new DayService(new FixedClock());

        day.endDay(player);
        assertEquals(1, gatesAvailableMentioning(events, "Vault"), "Vault debe anunciarse al cerrar el día");

        day.endDay(player);
        assertEquals(1, gatesAvailableMentioning(events, "Vault"), "No debe re-anunciarse al día siguiente");
    }

    @Test
    @DisplayName("Redemption aparece tras 3 burnouts en el último mes")
    void redemptionAppearsAfterThreeBurnouts() {
        Player player = Player.create("Jose");
        GateTracker tracker = player.getGateTracker();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 3; i++) {
            tracker.addDailyHistory(burnoutDay(today.minusDays(i)));
        }
        assertEquals(3, tracker.getBurnoutsInLastMonth());

        List<GameEvent> events = wireCollector(player);
        new DayService(new FixedClock()).endDay(player);

        assertEquals(1, gatesAvailableMentioning(events, "Abismo"), "Redemption debe anunciarse con 3 burnouts/mes");
    }

    @Test
    @DisplayName("Sin nivel ni burnouts no aparecen gates especiales")
    void noSpecialGatesWhenNotEligible() {
        Player player = Player.create("Jose");
        List<GameEvent> events = wireCollector(player);

        new DayService(new FixedClock()).endDay(player);

        assertEquals(0, gatesAvailableMentioning(events, "Vault"));
        assertEquals(0, gatesAvailableMentioning(events, "Abismo"));
    }

    private GateTracker.DailyHistory burnoutDay(LocalDate date) {
        return new GateTracker.DailyHistory(
                date, false, 0, 0, 0.0, true, false,
                List.of(), Set.of(), Map.of());
    }
}
