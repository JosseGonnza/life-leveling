package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatType - Tipos de Atributos del Jugador")
class StatTypeTest {

    @Nested
    class Properties {
        @Test
        @DisplayName("STRENGTH tiene propiedades correctas")
        void strengthHasCorrectProperties() {
            assertEquals("💪", StatType.STRENGTH.getIcon());
            assertEquals("Fuerza", StatType.STRENGTH.getDisplayName());
            assertTrue(StatType.STRENGTH.getDescription().contains("física"));
            assertEquals("Titán", StatType.STRENGTH.getMasteryTitle());
        }

        @Test
        @DisplayName("INTELLECT tiene propiedades correctas")
        void intellectHasCorrectProperties() {
            assertEquals("🧠", StatType.INTELLECT.getIcon());
            assertEquals("Inteligencia", StatType.INTELLECT.getDisplayName());
            assertTrue(StatType.INTELLECT.getDescription().contains("Programación"));
            assertEquals("Cyborg", StatType.INTELLECT.getMasteryTitle());
        }

        @Test
        @DisplayName("WISDOM tiene propiedades correctas")
        void wisdomHasCorrectProperties() {
            assertEquals("🦉", StatType.WISDOM.getIcon());
            assertEquals("Sabiduría", StatType.WISDOM.getDisplayName());
            assertTrue(StatType.WISDOM.getDescription().contains("finanzas"));
            assertEquals("Oráculo", StatType.WISDOM.getMasteryTitle());
        }

        @Test
        @DisplayName("DISCIPLINE tiene propiedades correctas")
        void disciplineHasCorrectProperties() {
            assertEquals("🛡️", StatType.DISCIPLINE.getIcon());
            assertEquals("Disciplina", StatType.DISCIPLINE.getDisplayName());
            assertTrue(StatType.DISCIPLINE.getDescription().contains("voluntad"));
            assertEquals("General", StatType.DISCIPLINE.getMasteryTitle());
        }

        @Test
        @DisplayName("CHARISMA tiene propiedades correctas")
        void charismaHasCorrectProperties() {
            assertEquals("🗣️", StatType.CHARISMA.getIcon());
            assertEquals("Carisma", StatType.CHARISMA.getDisplayName());
            assertTrue(StatType.CHARISMA.getDescription().contains("sociales"));
            assertEquals("Estrella", StatType.CHARISMA.getMasteryTitle());
        }

        @Test
        @DisplayName("Existen exactamente 5 stats")
        void thereAreExactly5Stats() {
            assertEquals(5, StatType.values().length);
        }
    }

    @Nested
    class SearchByName {
        @ParameterizedTest(name = "Buscar por nombre enum: {0}")
        @ValueSource(strings = {"STRENGTH", "INTELLECT", "WISDOM", "DISCIPLINE", "CHARISMA"})
        @DisplayName("fromString() encuentra stats por nombre enum (mayúsculas)")
        void whenSearchingByEnumName_thenStatIsFound(String name) {
            Optional<StatType> result = StatType.fromString(name);

            assertTrue(result.isPresent());
            assertEquals(name, result.get().name());
        }

        @ParameterizedTest(name = "Buscar por nombre display: {0}")
        @ValueSource(strings = {"Fuerza", "Inteligencia", "Sabiduría", "Disciplina", "Carisma"})
        @DisplayName("fromString() encuentra stats por nombre display")
        void whenSearchingByDisplayName_thenStatIsFound(String displayName) {
            Optional<StatType> result = StatType.fromString(displayName);

            assertTrue(result.isPresent());
            assertEquals(displayName, result.get().getDisplayName());
        }

        @ParameterizedTest(name = "Case-insensitive: {0}")
        @ValueSource(strings = {"strength", "StReNgTh", "FUERZA", "fuerza", "FuErZa"})
        @DisplayName("fromString() es case-insensitive")
        void whenSearchingWithDifferentCasing_thenStatIsFound(String name) {
            Optional<StatType> result = StatType.fromString(name);

            assertTrue(result.isPresent());
            assertEquals(StatType.STRENGTH, result.get());
        }

        @Test
        @DisplayName("fromString() con espacios alrededor funciona correctamente")
        void whenNameHasWhitespace_thenStatIsFound() {
            Optional<StatType> result = StatType.fromString("  INTELLECT  ");

            assertTrue(result.isPresent());
            assertEquals(StatType.INTELLECT, result.get());
        }

        @Test
        @DisplayName("fromString() con nombre inválido retorna Optional vacío")
        void whenNameIsInvalid_thenReturnsEmpty() {
            Optional<StatType> result = StatType.fromString("INVALID_STAT");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("fromString() con null lanza IllegalArgumentException")
        void whenNameIsNull_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> StatType.fromString(null));
        }

        @Test
        @DisplayName("fromString() con string vacío lanza IllegalArgumentException")
        void whenNameIsBlank_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> StatType.fromString("   "));
        }
    }

    @Nested
    class ExperienceCalculation {
        @ParameterizedTest(name = "Nivel {0} → {1} requiere {2} XP")
        @CsvSource({
                "1,  2,    100",
                "5,  6,    500",
                "10, 11,  1000",
                "25, 26,  2500",
                "49, 50,  4900",
                "50, 51,  5000",
                "99, 100, 9900"
        })
        @DisplayName("getXPRequiredForNextLevel() calcula correctamente")
        void whenCalculatingXPForNextLevel_thenCorrectValue(int currentLevel, int nextLevel, int expectedXP) {
            int requiredXP = StatType.getXPRequiredForNextLevel(currentLevel);

            assertEquals(expectedXP, requiredXP);
        }

        @Test
        @DisplayName("getXPRequiredForNextLevel() con nivel 0 lanza excepción")
        void whenCurrentLevelIsZero_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> StatType.getXPRequiredForNextLevel(0));
        }

        @Test
        @DisplayName("getXPRequiredForNextLevel() con nivel 100 lanza excepción")
        void whenCurrentLevelIs100_thenThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> StatType.getXPRequiredForNextLevel(100)
            );
            assertTrue(exception.getMessage().contains("100"));
        }

        @Test
        @DisplayName("getXPRequiredForNextLevel() con nivel negativo lanza excepción")
        void whenCurrentLevelIsNegative_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> StatType.getXPRequiredForNextLevel(-5));
        }

        @ParameterizedTest(name = "Nivel {0} requiere {1} XP total acumulada")
        @CsvSource({
                "1,     0",
                "2,   100",
                "3,   300",
                "10,  4500",
                "50,  122500",
                "100, 495000"
        })
        @DisplayName("getTotalXPForLevel() calcula XP acumulada correctamente")
        void whenCalculatingTotalXP_thenCorrectValue(int level, int expectedTotalXP) {
            int totalXP = StatType.getTotalXPForLevel(level);

            assertEquals(expectedTotalXP, totalXP);
        }

        @Test
        @DisplayName("getTotalXPForLevel() validación: Total para 5 stats = 2,475,000 XP")
        void totalXPForAll5StatsIs2475000() {
            int totalForOneStat = StatType.getTotalXPForLevel(100);

            int totalForAll5Stats = totalForOneStat * 5;

            assertEquals(495_000, totalForOneStat);
            assertEquals(2_475_000, totalForAll5Stats);
        }

        @Test
        @DisplayName("getTotalXPForLevel() con nivel 0 lanza excepción")
        void whenTargetLevelIsZero_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> StatType.getTotalXPForLevel(0));
        }

        @Test
        @DisplayName("getTotalXPForLevel() con nivel 101 lanza excepción")
        void whenTargetLevelIsOver100_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> StatType.getTotalXPForLevel(101));
        }
    }

    @Nested
    class MasteryLevel {
        @ParameterizedTest(name = "Nivel {0} {1} nivel de maestría")
        @CsvSource({
                "1,  false",
                "25, false",
                "49, false",
                "50, true",
                "51, true",
                "75, true",
                "100, true"
        })
        @DisplayName("hasMasteryLevel() detecta nivel 50+ correctamente")
        void whenCheckingMasteryLevel_thenCorrectResult(int level, boolean expectedHasMastery) {
            boolean hasMastery = StatType.STRENGTH.hasMasteryLevel(level);

            assertEquals(expectedHasMastery, hasMastery);
        }

        @Test
        @DisplayName("Todos los stats tienen título de maestría único")
        void allStatsHaveUniqueMasteryTitles() {
            String[] titles = {
                    StatType.STRENGTH.getMasteryTitle(),
                    StatType.INTELLECT.getMasteryTitle(),
                    StatType.WISDOM.getMasteryTitle(),
                    StatType.DISCIPLINE.getMasteryTitle(),
                    StatType.CHARISMA.getMasteryTitle()
            };

            assertEquals(5, java.util.Set.of(titles).size(),
                    "Todos los títulos de maestría deben ser únicos");
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea correctamente con icono")
        void whenFormattingForDisplay_thenIncludesIcon() {
            String display = StatType.STRENGTH.toDisplayString();

            assertEquals("💪 Fuerza", display);
            assertTrue(display.contains(StatType.STRENGTH.getIcon()));
            assertTrue(display.contains(StatType.STRENGTH.getDisplayName()));
        }

        @Test
        @DisplayName("Todos los stats tienen icono emoji")
        void allStatsHaveEmoji() {
            for (StatType stat : StatType.values()) {
                assertNotNull(stat.getIcon());
                assertFalse(stat.getIcon().isBlank());
            }
        }

        @Test
        @DisplayName("Todos los stats tienen descripción")
        void allStatsHaveDescription() {
            for (StatType stat : StatType.values()) {
                assertNotNull(stat.getDescription());
                assertFalse(stat.getDescription().isBlank());
            }
        }
    }
}