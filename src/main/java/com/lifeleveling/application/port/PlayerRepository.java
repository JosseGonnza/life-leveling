package com.lifeleveling.application.port;

import com.lifeleveling.domain.player.Player;

import java.util.Optional;

/**
 * Puerto de persistencia del jugador. App personal de un solo save:
 * load() devuelve la partida guardada (si existe) y save() la sobrescribe.
 * La infraestructura decide el medio (memoria, JSON, BD…).
 */
public interface PlayerRepository {

    Optional<Player> load();

    void save(Player player);
}
