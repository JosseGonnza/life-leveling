package com.lifeleveling.application.service;

import com.lifeleveling.domain.career.CodeSession;
import com.lifeleveling.domain.player.Player;

/**
 * Casos de uso de carrera: vender tiempo (JOB) o invertirlo en estudiar (CODE).
 */
public final class CareerService {

    public CodeSession code(Player player, double hours) {
        return player.registerCodeSession(hours);
    }

    public int job(Player player, int hours) {
        return player.registerJobSession(hours);
    }
}
