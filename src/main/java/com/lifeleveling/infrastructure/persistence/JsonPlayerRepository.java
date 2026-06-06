package com.lifeleveling.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lifeleveling.application.port.PlayerRepository;
import com.lifeleveling.domain.player.Player;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Persistencia de la partida en un fichero JSON (Jackson). Guarda/lee un {@link PlayerSnapshot}
 * y lo mapea a/desde el dominio. Una sola partida por fichero.
 */
public final class JsonPlayerRepository implements PlayerRepository {

    private final Path file;
    private final ObjectMapper mapper;

    public JsonPlayerRepository(Path file) {
        this.file = file;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(Player player) {
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            mapper.writeValue(file.toFile(), PlayerSnapshotMapper.toSnapshot(player));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la partida en " + file, e);
        }
    }

    @Override
    public Optional<Player> load() {
        if (!Files.exists(file)) return Optional.empty();
        try {
            PlayerSnapshot snapshot = mapper.readValue(file.toFile(), PlayerSnapshot.class);
            return Optional.of(PlayerSnapshotMapper.toDomain(snapshot));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la partida de " + file, e);
        }
    }
}
