package com.lifeleveling.application;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.application.port.PlayerRepository;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M1 - Puertos y adapters base de la capa de aplicación")
class M1ScaffoldingTest {

    @Test
    @DisplayName("InMemoryPlayerRepository hace round-trip save/load")
    void repository_roundTrip() {
        PlayerRepository repo = new InMemoryPlayerRepository();
        assertTrue(repo.load().isEmpty(), "Sin partida guardada, load() vacío");

        Player player = Player.create("Jose");
        repo.save(player);

        Optional<Player> loaded = repo.load();
        assertTrue(loaded.isPresent());
        assertEquals("Jose", loaded.get().getName());
    }

    @Test
    @DisplayName("SystemClock devuelve fecha/instante reales")
    void clock_returnsRealTime() {
        Clock clock = new SystemClock();
        assertNotNull(clock.now());
        assertEquals(LocalDate.now(), clock.today());
    }
}
