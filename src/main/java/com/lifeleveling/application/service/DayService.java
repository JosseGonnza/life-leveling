package com.lifeleveling.application.service;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.player.Player;

/**
 * Caso de uso del rollover diario: cierra el día (vuelca el DailyHistory, rompe rachas,
 * limpia buffs, rotación semanal) vía Player.updateState.
 *
 * Pendiente (paso siguiente): triggers automáticos que el dominio no hace —
 * aparición de The Vault (nivel 40), REDEMPTION (3 burnouts/mes) y aparición de gates.
 */
public final class DayService {

    private final Clock clock;

    public DayService(Clock clock) {
        this.clock = clock;
    }

    public void endDay(Player player) {
        player.updateState(clock.now());
    }
}
