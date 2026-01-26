package com.lifeleveling.domain.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuestId - Identificador Único de Quest")
class QuestIdTest {

    @Nested
    class Generation {
        @Test
        @DisplayName("generate() crea QuestId con UUID único")
        void whenGenerating_thenUniqueUUID() {
            QuestId id = QuestId.generate();

            assertNotNull(id);
            assertNotNull(id.value());
        }

        @Test
        @DisplayName("generate() crea IDs únicos en cada llamada")
        void whenGeneratingMultiple_thenAllUnique() {
            QuestId id1 = QuestId.generate();
            QuestId id2 = QuestId.generate();
            QuestId id3 = QuestId.generate();

            assertNotEquals(id1, id2);
            assertNotEquals(id2, id3);
            assertNotEquals(id1, id3);
        }
    }

    @Nested
    class Reconstitution {
        @Test
        @DisplayName("from(UUID) crea QuestId desde UUID existente")
        void whenCreatingFromUUID_thenCorrectId() {
            UUID uuid = UUID.randomUUID();
            QuestId id = QuestId.from(uuid);

            assertEquals(uuid, id.value());
        }

        @Test
        @DisplayName("from(String) crea QuestId desde string válido")
        void whenCreatingFromValidString_thenCorrectId() {
            String uuidString = "550e8400-e29b-41d4-a716-446655440000";
            QuestId id = QuestId.from(uuidString);

            assertEquals(uuidString, id.value().toString());
        }

        @Test
        @DisplayName("from(String) con formato inválido lanza IllegalArgumentException")
        void whenCreatingFromInvalidString_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestId.from("not-a-uuid"));
        }

        @Test
        @DisplayName("from(String) con null lanza IllegalArgumentException")
        void whenCreatingFromNullString_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestId.from((String) null));
        }

        @Test
        @DisplayName("from(String) con string vacío lanza IllegalArgumentException")
        void whenCreatingFromBlankString_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestId.from("   "));
        }

        @Test
        @DisplayName("from(UUID) con null lanza IllegalArgumentException")
        void whenCreatingFromNullUUID_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestId.from((UUID) null));
        }
    }

    @Nested
    class Validation {
        @Test
        @DisplayName("Constructor con UUID null lanza IllegalArgumentException")
        void whenConstructingWithNull_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestId(null));
        }
    }

    @Nested
    class Equality {
        @Test
        @DisplayName("QuestIds con mismo UUID son equals()")
        void questIdsWithSameUUID_areEqual() {
            UUID uuid = UUID.randomUUID();
            QuestId id1 = new QuestId(uuid);
            QuestId id2 = new QuestId(uuid);

            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
        }

        @Test
        @DisplayName("QuestIds con diferente UUID NO son equals()")
        void questIdsWithDifferentUUID_areNotEqual() {
            QuestId id1 = QuestId.generate();
            QuestId id2 = QuestId.generate();

            assertNotEquals(id1, id2);
        }
    }

    @Nested
    class StringRepresentation {
        @Test
        @DisplayName("toString() retorna el UUID como string")
        void whenCallingToString_thenReturnsUUIDString() {
            UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            QuestId id = new QuestId(uuid);

            assertEquals("550e8400-e29b-41d4-a716-446655440000", id.toString());
        }
    }

    @Nested
    class Immutability {
        @Test
        @DisplayName("QuestId es inmutable")
        void questId_isImmutable() {
            UUID originalUUID = UUID.randomUUID();
            QuestId id = new QuestId(originalUUID);

            // El valor interno no puede ser modificado desde fuera
            assertEquals(originalUUID, id.value());
        }
    }
}