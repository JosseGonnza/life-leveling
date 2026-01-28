package com.lifeleveling.domain.quest.shared;

import java.util.UUID;

public record QuestId(UUID value) {

    public QuestId {
        if (value == null) {
            throw new IllegalArgumentException("El UUID de QuestId no puede ser null");
        }
    }

    public static QuestId generate() {
        return new QuestId(UUID.randomUUID());
    }

    public static QuestId from(UUID uuid) {
        return new QuestId(uuid);
    }

    public static QuestId from(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException("El UUID string no puede ser null o vacío");
        }

        try {
            return new QuestId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("UUID string inválido: %s", uuidString), e
            );
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}