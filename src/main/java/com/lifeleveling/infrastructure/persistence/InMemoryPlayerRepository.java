package com.lifeleveling.infrastructure.persistence;

import com.lifeleveling.application.port.PlayerRepository;
import com.lifeleveling.domain.player.Player;

import java.util.Optional;

/**
 * Repositorio en memoria. Guarda la referencia viva del Player (el dominio es mutable).
 * Útil para tests de integración y para construir la app antes de tener persistencia JSON.
 */
public final class InMemoryPlayerRepository implements PlayerRepository {

    private Player player;

    public InMemoryPlayerRepository() {}

    public InMemoryPlayerRepository(Player initial) {
        this.player = initial;
    }

    @Override
    public Optional<Player> load() {
        return Optional.ofNullable(player);
    }

    @Override
    public void save(Player player) {
        this.player = player;
    }
}
