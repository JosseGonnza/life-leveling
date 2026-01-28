package com.lifeleveling.domain.title;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Title - Título Desbloqueado")
class TitleTest {

    @Nested
    class Creation {
        @Test
        @DisplayName("unlock() crea título con ID y timestamp")
        void unlock_createsWithIdAndTimestamp() {
            Title title = Title.unlock(TitleType.SURVIVOR);

            assertNotNull(title.id());
            assertEquals(TitleType.SURVIVOR, title.type());
            assertNotNull(title.unlockedAt());
        }

        @Test
        @DisplayName("unlock() genera IDs únicos")
        void unlock_generatesUniqueIds() {
            Title title1 = Title.unlock(TitleType.SURVIVOR);
            Title title2 = Title.unlock(TitleType.ZOMBIE);

            assertNotEquals(title1.id(), title2.id());
        }

        @Test
        @DisplayName("reconstitute() reconstruye desde persistencia")
        void reconstitute_recreatesFromData() {
            UUID id = UUID.randomUUID();
            Instant unlocked = Instant.now().minusSeconds(3600);

            Title title = Title.reconstitute(id, TitleType.IMMORTAL, unlocked);

            assertEquals(id, title.id());
            assertEquals(TitleType.IMMORTAL, title.type());
            assertEquals(unlocked, title.unlockedAt());
        }
    }

    @Nested
    class Validation {
        @Test
        @DisplayName("Constructor valida id no null")
        void constructor_validatesIdNotNull() {
            assertThrows(NullPointerException.class,
                    () -> new Title(null, TitleType.SURVIVOR, Instant.now()));
        }

        @Test
        @DisplayName("Constructor valida type no null")
        void constructor_validatesTypeNotNull() {
            assertThrows(NullPointerException.class,
                    () -> new Title(UUID.randomUUID(), null, Instant.now()));
        }

        @Test
        @DisplayName("Constructor valida unlockedAt no null")
        void constructor_validatesUnlockedAtNotNull() {
            assertThrows(NullPointerException.class,
                    () -> new Title(UUID.randomUUID(), TitleType.SURVIVOR, null));
        }
    }

    @Nested
    class DelegateMethods {
        @Test
        @DisplayName("getDisplayName() delega a TitleType")
        void getDisplayName_delegatesToType() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            assertEquals(TitleType.SURVIVOR.getDisplayName(), title.getDisplayName());
        }

        @Test
        @DisplayName("getCategory() delega a TitleType")
        void getCategory_delegatesToType() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            assertEquals(TitleType.SURVIVOR.getCategory(), title.getCategory());
        }

        @Test
        @DisplayName("getLore() delega a TitleType")
        void getLore_delegatesToType() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            assertEquals(TitleType.SURVIVOR.getLore(), title.getLore());
        }

        @Test
        @DisplayName("getBuffs() delega a TitleType")
        void getBuffs_delegatesToType() {
            Title title = Title.unlock(TitleType.TITAN);
            assertEquals(TitleType.TITAN.getBuffs(), title.getBuffs());
        }

        @Test
        @DisplayName("getBuffsDisplay() delega a TitleType")
        void getBuffsDisplay_delegatesToType() {
            Title title = Title.unlock(TitleType.TITAN);
            assertEquals(TitleType.TITAN.getBuffsDisplay(), title.getBuffsDisplay());
        }
    }

    @Nested
    class Display {
        @Test
        @DisplayName("toDisplayString() incluye nombre y categoría")
        void toDisplayString_includesNameAndCategory() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            String display = title.toDisplayString();

            assertTrue(display.contains("Superviviente"));
            assertTrue(display.contains("Supervivencia"));
        }

        @Test
        @DisplayName("toDetailedString() incluye fecha")
        void toDetailedString_includesDate() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            String detailed = title.toDetailedString();

            assertTrue(detailed.contains("Superviviente"));
            assertTrue(detailed.contains("Desbloqueado"));
            // Debe contener formato de fecha
            assertTrue(detailed.matches(".*\\d{2}/\\d{2}/\\d{4}.*"));
        }
    }

    @Nested
    class Equality {
        @Test
        @DisplayName("Títulos del mismo tipo son equals")
        void sameType_areEqual() {
            Title title1 = Title.unlock(TitleType.SURVIVOR);
            Title title2 = Title.unlock(TitleType.SURVIVOR);

            assertEquals(title1, title2);
            assertEquals(title1.hashCode(), title2.hashCode());
        }

        @Test
        @DisplayName("Títulos de diferente tipo NO son equals")
        void differentType_areNotEqual() {
            Title survivor = Title.unlock(TitleType.SURVIVOR);
            Title zombie = Title.unlock(TitleType.ZOMBIE);

            assertNotEquals(survivor, zombie);
        }

        @Test
        @DisplayName("Título es igual a sí mismo")
        void sameInstance_areEqual() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            assertEquals(title, title);
        }

        @Test
        @DisplayName("Título no es igual a null")
        void notEqualToNull() {
            Title title = Title.unlock(TitleType.SURVIVOR);
            assertNotEquals(null, title);
        }
    }
}
