package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stats - Contenedor de los 5 Atributos")
class StatsTest {

    @Test
    @DisplayName("initial() crea todos los stats en nivel 1")
    void whenCreatingInitial_thenAllStatsAreLevel1() {
        Stats stats = Stats.initial();

        assertEquals(1, stats.getLevel(StatType.STRENGTH));
        assertEquals(1, stats.getLevel(StatType.INTELLECT));
        assertEquals(1, stats.getLevel(StatType.WISDOM));
        assertEquals(1, stats.getLevel(StatType.DISCIPLINE));
        assertEquals(1, stats.getLevel(StatType.CHARISMA));

        assertEquals(5, stats.getTotalLevel());
    }

    @Test
    @DisplayName("withLevels() crea stats con niveles específicos")
    void whenCreatingWithLevels_thenCorrectLevels() {
        Stats stats = Stats.withLevels(10, 20, 30, 40, 50);

        assertEquals(10, stats.getLevel(StatType.STRENGTH));
        assertEquals(20, stats.getLevel(StatType.INTELLECT));
        assertEquals(30, stats.getLevel(StatType.WISDOM));
        assertEquals(40, stats.getLevel(StatType.DISCIPLINE));
        assertEquals(50, stats.getLevel(StatType.CHARISMA));

        assertEquals(150, stats.getTotalLevel());
    }

    @Test
    @DisplayName("maxed() crea todos los stats en nivel 100")
    void whenCreatingMaxed_thenAllStatsAre100() {
        Stats stats = Stats.maxed();

        assertTrue(stats.isMaxed(StatType.STRENGTH));
        assertTrue(stats.isMaxed(StatType.INTELLECT));
        assertTrue(stats.isMaxed(StatType.WISDOM));
        assertTrue(stats.isMaxed(StatType.DISCIPLINE));
        assertTrue(stats.isMaxed(StatType.CHARISMA));

        assertTrue(stats.areAllMaxed());
        assertEquals(500, stats.getTotalLevel());
    }

    @Test
    @DisplayName("Constructor con stat null lanza IllegalArgumentException")
    void whenCreatingWithNullStat_thenThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Stats(null,
                        Stat.initial(StatType.INTELLECT),
                        Stat.initial(StatType.WISDOM),
                        Stat.initial(StatType.DISCIPLINE),
                        Stat.initial(StatType.CHARISMA))
        );
    }

    @Test
    @DisplayName("Constructor con stat de tipo incorrecto lanza IllegalArgumentException")
    void whenCreatingWithWrongStatType_thenThrowsException() {
        //Intentamos poner INTELLECT en el slot de STRENGTH
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Stats(
                        Stat.initial(StatType.INTELLECT),  //Tipo incorrecto
                        Stat.initial(StatType.INTELLECT),
                        Stat.initial(StatType.WISDOM),
                        Stat.initial(StatType.DISCIPLINE),
                        Stat.initial(StatType.CHARISMA))
        );

        assertTrue(exception.getMessage().contains("tipo incorrecto"));
    }

    @Nested
    class IndividualAccess {
        @ParameterizedTest
        @EnumSource(StatType.class)
        @DisplayName("getStat() retorna el stat correcto por tipo")
        void whenGettingStat_thenCorrectStatReturned(StatType type) {
            Stats stats = Stats.initial();
            Stat stat = stats.getStat(type);

            assertNotNull(stat);
            assertEquals(type, stat.type());
        }

        @Test
        @DisplayName("getStat() con null lanza IllegalArgumentException")
        void whenGettingStatWithNull_thenThrowsException() {
            Stats stats = Stats.initial();
            assertThrows(IllegalArgumentException.class, () -> stats.getStat(null));
        }

        @ParameterizedTest
        @EnumSource(StatType.class)
        @DisplayName("getLevel() retorna el nivel del stat")
        void whenGettingLevel_thenCorrectLevel(StatType type) {
            Stats stats = Stats.withLevels(5, 10, 15, 20, 25);

            int expectedLevel = switch (type) {
                case STRENGTH -> 5;
                case INTELLECT -> 10;
                case WISDOM -> 15;
                case DISCIPLINE -> 20;
                case CHARISMA -> 25;
            };

            assertEquals(expectedLevel, stats.getLevel(type));
        }

        @ParameterizedTest
        @EnumSource(StatType.class)
        @DisplayName("getCurrentXP() retorna la XP actual del stat")
        void whenGettingCurrentXP_thenCorrectXP(StatType type) {
            Stats stats = Stats.initial();
            stats = stats.addXP(type, 50);

            assertEquals(50, stats.getCurrentXP(type));
        }
    }

    @Nested
    class Queries {
        @Test
        @DisplayName("hasMastery() detecta stats con nivel 50+")
        void whenCheckingMastery_thenCorrectResult() {
            Stats stats = Stats.withLevels(25, 50, 75, 100, 1);

            assertFalse(stats.hasMastery(StatType.STRENGTH));
            assertTrue(stats.hasMastery(StatType.INTELLECT));
            assertTrue(stats.hasMastery(StatType.WISDOM));
            assertTrue(stats.hasMastery(StatType.DISCIPLINE));
            assertFalse(stats.hasMastery(StatType.CHARISMA));
        }

        @Test
        @DisplayName("isMaxed() detecta stats en nivel 100")
        void whenCheckingMaxed_thenCorrectResult() {
            Stats stats = Stats.withLevels(99, 100, 100, 50, 1);

            assertFalse(stats.isMaxed(StatType.STRENGTH));
            assertTrue(stats.isMaxed(StatType.INTELLECT));
            assertTrue(stats.isMaxed(StatType.WISDOM));
            assertFalse(stats.isMaxed(StatType.DISCIPLINE));
            assertFalse(stats.isMaxed(StatType.CHARISMA));
        }

        @Test
        @DisplayName("getMasteryCount() cuenta stats con maestría")
        void whenCountingMastery_thenCorrectCount() {
            Stats stats = Stats.withLevels(25, 50, 51, 75, 100);

            assertEquals(4, stats.getMasteryCount()); // INT, WIS, DIS, CHA
        }

        @Test
        @DisplayName("getMaxedCount() cuenta stats maxeados")
        void whenCountingMaxed_thenCorrectCount() {
            Stats stats = Stats.withLevels(100, 100, 50, 99, 100);

            assertEquals(3, stats.getMaxedCount()); // STR, INT, CHA
        }

        @Test
        @DisplayName("areAllMaxed() detecta cuando todos están en 100")
        void whenAllStatsMaxed_thenAreAllMaxedReturnsTrue() {
            Stats stats = Stats.maxed();
            assertTrue(stats.areAllMaxed());
        }

        @Test
        @DisplayName("areAllMaxed() retorna false si falta alguno")
        void whenNotAllMaxed_thenAreAllMaxedReturnsFalse() {
            Stats stats = Stats.withLevels(100, 100, 100, 100, 99);
            assertFalse(stats.areAllMaxed());
        }

        @ParameterizedTest(name = "Niveles [{0}, {1}, {2}, {3}, {4}] → Total = {5}")
        @CsvSource({
                "1,  1,  1,  1,  1,   5",
                "10, 10, 10, 10, 10,  50",
                "5,  10, 15, 20, 25,  75",
                "100, 100, 100, 100, 100, 500"
        })
        @DisplayName("getTotalLevel() suma correctamente todos los niveles")
        void whenGettingTotalLevel_thenCorrectSum(int str, int intel, int wis, int dis, int cha, int expected) {
            Stats stats = Stats.withLevels(str, intel, wis, dis, cha);
            assertEquals(expected, stats.getTotalLevel());
        }

        @Test
        @DisplayName("getAverageLevel() calcula el promedio correctamente")
        void whenGettingAverageLevel_thenCorrectAverage() {
            Stats stats = Stats.withLevels(10, 20, 30, 40, 50);

            double average = stats.getAverageLevel();
            assertEquals(30.0, average, 0.01);
        }

        @Test
        @DisplayName("getTotalXPAccumulated() suma XP de todos los stats")
        void whenGettingTotalXP_thenSumsAllStats() {
            Stats stats = Stats.withLevels(2, 3, 4, 5, 6);
            int expectedTotal = 100 + 300 + 600 + 1000 + 1500; // 3500 XP

            assertEquals(expectedTotal, stats.getTotalXPAccumulated());
        }

        @Test
        @DisplayName("getTotalXPAccumulated() para stats maxeados es 2,475,000")
        void whenAllStatsMaxed_thenTotalXPIs2475000() {
            Stats stats = Stats.maxed();

            // 495,000 XP por stat × 5 stats = 2,475,000 XP
            assertEquals(2_475_000, stats.getTotalXPAccumulated());
        }

        @Test
        @DisplayName("getDominantStat() retorna el stat con nivel más alto")
        void whenGettingDominantStat_thenReturnsHighest() {
            Stats stats = Stats.withLevels(10, 50, 25, 15, 30);

            assertEquals(StatType.INTELLECT, stats.getDominantStat());
        }

        @Test
        @DisplayName("getDominantStat() en caso de empate retorna el primero")
        void whenTied_thenReturnsFirstInOrder() {
            Stats stats = Stats.withLevels(50, 50, 25, 15, 30);

            assertEquals(StatType.STRENGTH, stats.getDominantStat());
        }
    }

    @Nested
    class ImmutableModification {
        @ParameterizedTest
        @EnumSource(StatType.class)
        @DisplayName("addXP() aumenta solo el stat especificado")
        void whenAddingXP_thenOnlyTargetStatIncreases(StatType type) {
            Stats stats = Stats.initial();
            Stats updated = stats.addXP(type, 150);

            // Solo el stat objetivo debe cambiar
            assertEquals(2, updated.getLevel(type), "Stat objetivo debe subir");

            // Los demás deben permanecer en nivel 1
            for (StatType other : StatType.values()) {
                if (other != type) {
                    assertEquals(1, updated.getLevel(other),
                            "Otros stats no deben cambiar");
                }
            }
        }

        @Test
        @DisplayName("addXP() es inmutable: no modifica el original")
        void whenAddingXP_thenOriginalUnchanged() {
            Stats original = Stats.initial();
            Stats updated = original.addXP(StatType.STRENGTH, 600);

            assertEquals(1, original.getLevel(StatType.STRENGTH), "Original no debe cambiar");
            assertEquals(4, updated.getLevel(StatType.STRENGTH), "Nuevo debe tener cambios");
            assertNotSame(original, updated);
        }

        @Test
        @DisplayName("addXP() procesa level-ups automáticamente")
        void whenAddingXP_thenLevelUpsAutomatically() {
            Stats stats = Stats.initial();

            // Añadir 650 XP a Fuerza (sube hasta Lvl 4)
            stats = stats.addXP(StatType.STRENGTH, 650);

            assertEquals(4, stats.getLevel(StatType.STRENGTH));
            assertEquals(50, stats.getCurrentXP(StatType.STRENGTH));
        }

        @Test
        @DisplayName("addXP() con tipo null lanza IllegalArgumentException")
        void whenAddingXPWithNullType_thenThrowsException() {
            Stats stats = Stats.initial();
            assertThrows(IllegalArgumentException.class, () -> stats.addXP(null, 100));
        }

        @Test
        @DisplayName("addXP() con XP negativa lanza IllegalArgumentException")
        void whenAddingNegativeXP_thenThrowsException() {
            Stats stats = Stats.initial();
            assertThrows(IllegalArgumentException.class,
                    () -> stats.addXP(StatType.STRENGTH, -50));
        }
    }

    @Nested
    class ManuelLevelUp {
        @Test
        @DisplayName("forceLevel() sube el stat sin verificar XP")
        void whenForcingLevel_thenStatIncreasesWithoutXPCheck() {
            Stats stats = Stats.initial();
            Stats updated = stats.forceLevel(StatType.INTELLECT, 10);

            assertEquals(11, updated.getLevel(StatType.INTELLECT));
        }

        @Test
        @DisplayName("forceLevel() solo afecta al stat especificado")
        void whenForcingLevel_thenOnlyTargetStatChanges() {
            Stats stats = Stats.initial();
            Stats updated = stats.forceLevel(StatType.WISDOM, 25);

            assertEquals(26, updated.getLevel(StatType.WISDOM));
            assertEquals(1, updated.getLevel(StatType.STRENGTH));
        }

        @Test
        @DisplayName("forceLevel() con tipo null lanza IllegalArgumentException")
        void whenForcingLevelWithNull_thenThrowsException() {
            Stats stats = Stats.initial();
            assertThrows(IllegalArgumentException.class, () -> stats.forceLevel(null, 10));
        }
    }

    @Nested
    class Reset {
        @Test
        @DisplayName("reset() devuelve todos los stats a nivel 1")
        void whenResetting_thenAllStatsBackToLevel1() {
            Stats stats = Stats.withLevels(50, 60, 70, 80, 90);
            Stats reset = stats.reset();

            assertEquals(1, reset.getLevel(StatType.STRENGTH));
            assertEquals(1, reset.getLevel(StatType.INTELLECT));
            assertEquals(1, reset.getLevel(StatType.WISDOM));
            assertEquals(1, reset.getLevel(StatType.DISCIPLINE));
            assertEquals(1, reset.getLevel(StatType.CHARISMA));

            assertEquals(5, reset.getTotalLevel());
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayMap() retorna mapa con todos los stats formateados")
        void whenGettingDisplayMap_thenAllStatsPresent() {
            Stats stats = Stats.withLevels(10, 20, 30, 40, 50);
            Map<StatType, String> displayMap = stats.toDisplayMap();

            assertEquals(5, displayMap.size());

            for (StatType type : StatType.values()) {
                assertTrue(displayMap.containsKey(type));
                assertNotNull(displayMap.get(type));
                assertTrue(displayMap.get(type).contains("Lvl"));
            }
        }

        @Test
        @DisplayName("toCompactString() formatea todos los stats separados por |")
        void whenGettingCompactString_thenPipeSeparated() {
            Stats stats = Stats.withLevels(1, 2, 3, 4, 5);
            String compact = stats.toCompactString();

            assertTrue(compact.contains("💪 Lvl 1"));
            assertTrue(compact.contains("🧠 Lvl 2"));
            assertTrue(compact.contains("🦉 Lvl 3"));
            assertTrue(compact.contains("🛡️ Lvl 4"));
            assertTrue(compact.contains("🗣️ Lvl 5"));
            assertTrue(compact.contains("|"));
        }

        @Test
        @DisplayName("getSummary() incluye totales, promedio y conteos")
        void whenGettingSummary_thenIncludesKeyMetrics() {
            Stats stats = Stats.withLevels(50, 60, 70, 80, 100);
            String summary = stats.getSummary();

            assertTrue(summary.contains("Total Lvl=360"));
            assertTrue(summary.contains("Avg=72"));
            assertTrue(summary.contains("Mastery=5/5"));
            assertTrue(summary.contains("Maxed=1/5"));
        }

        @Test
        @DisplayName("toString() muestra todos los stats de forma compacta")
        void whenCallingToString_thenShowsAllStats() {
            Stats stats = Stats.withLevels(10, 20, 30, 40, 50);
            String str = stats.toString();

            assertTrue(str.startsWith("Stats["));
            assertTrue(str.contains("STR="));
            assertTrue(str.contains("INT="));
            assertTrue(str.contains("WIS="));
            assertTrue(str.contains("DIS="));
            assertTrue(str.contains("CHA="));
        }
    }

    @Nested
    class Immutability {
        @Test
        @DisplayName("Stats es inmutable: operaciones no modifican el original")
        void stats_isImmutable() {
            Stats original = Stats.initial();

            original.addXP(StatType.STRENGTH, 500);
            original.addXP(StatType.INTELLECT, 300);
            original.forceLevel(StatType.WISDOM, 10);
            original.reset();

            assertEquals(5, original.getTotalLevel(),
                    "Stats original debe permanecer sin cambios después de operaciones");
        }

        @Test
        @DisplayName("Stats con mismos valores son equals()")
        void statsWithSameValues_areEqual() {
            Stats stats1 = Stats.withLevels(10, 20, 30, 40, 50);
            Stats stats2 = Stats.withLevels(10, 20, 30, 40, 50);

            assertEquals(stats1, stats2);
            assertEquals(stats1.hashCode(), stats2.hashCode());
        }

        @Test
        @DisplayName("Stats con diferentes valores NO son equals()")
        void statsWithDifferentValues_areNotEqual() {
            Stats stats1 = Stats.withLevels(10, 20, 30, 40, 50);
            Stats stats2 = Stats.withLevels(10, 20, 30, 40, 51);

            assertNotEquals(stats1, stats2);
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Operaciones encadenadas funcionan correctamente")
        void chainedOperations_workCorrectly() {
            Stats result = Stats.initial()
                    .addXP(StatType.STRENGTH, 250)
                    .addXP(StatType.INTELLECT, 600)
                    .addXP(StatType.WISDOM, 150)
                    .forceLevel(StatType.DISCIPLINE, 10);

            assertEquals(2, result.getLevel(StatType.STRENGTH));
            assertEquals(4, result.getLevel(StatType.INTELLECT));
            assertEquals(2, result.getLevel(StatType.WISDOM));
            assertEquals(11, result.getLevel(StatType.DISCIPLINE));
            assertEquals(1, result.getLevel(StatType.CHARISMA));
        }

        @Test
        @DisplayName("Stats inicial tiene 0 XP total acumulada")
        void initialStats_hasZeroTotalXP() {
            Stats stats = Stats.initial();
            assertEquals(0, stats.getTotalXPAccumulated());
        }
    }
}