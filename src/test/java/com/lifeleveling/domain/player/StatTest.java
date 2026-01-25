package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stat - Value Object de Stat Individual")
class StatTest {

    @ParameterizedTest
    @EnumSource(StatType.class)
    @DisplayName("initial() crea stat en nivel 1 con 0 XP")
    void whenCreatingInitial_thenLevel1WithZeroXP(StatType type) {
        Stat stat = Stat.initial(type);

        assertEquals(type, stat.type());
        assertEquals(1, stat.currentLevel());
        assertEquals(0, stat.currentXP());
        assertTrue(stat.isInitial());
        assertFalse(stat.isMaxed());
    }

    @Test
    @DisplayName("initial() con null lanza IllegalArgumentException")
    void whenCreatingInitialWithNull_thenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Stat.initial(null));
    }

    @ParameterizedTest(name = "atLevel({0}, {1})")
    @CsvSource({
            "STRENGTH, 1",
            "STRENGTH, 10",
            "INTELLECT, 50",
            "WISDOM, 99",
            "DISCIPLINE, 100"
    })
    @DisplayName("atLevel() crea stat en nivel específico con 0 XP")
    void whenCreatingAtLevel_thenCorrectLevelAndZeroXP(StatType type, int level) {
        Stat stat = Stat.atLevel(type, level);

        assertEquals(type, stat.type());
        assertEquals(level, stat.currentLevel());
        assertEquals(0, stat.currentXP());
    }

    @Test
    @DisplayName("atLevel() con nivel 0 lanza IllegalArgumentException")
    void whenCreatingWithLevel0_thenThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> Stat.atLevel(StatType.STRENGTH, 0));
    }

    @Test
    @DisplayName("atLevel() con nivel 101 lanza IllegalArgumentException")
    void whenCreatingWithLevel101_thenThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> Stat.atLevel(StatType.STRENGTH, 101));
    }

    @ParameterizedTest
    @EnumSource(StatType.class)
    @DisplayName("maxed() crea stat en nivel 100 con 0 XP")
    void whenCreatingMaxed_thenLevel100WithZeroXP(StatType type) {
        Stat stat = Stat.maxed(type);

        assertEquals(100, stat.currentLevel());
        assertEquals(0, stat.currentXP());
        assertTrue(stat.isMaxed());
        assertFalse(stat.canLevelUp());
    }

    @Test
    @DisplayName("Constructor con XP negativa lanza IllegalArgumentException")
    void whenCreatingWithNegativeXP_thenThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stat(StatType.STRENGTH, 5, -100));
    }

    @Test
    @DisplayName("Constructor con nivel 100 y XP > 0 lanza IllegalArgumentException")
    void whenMaxedStatHasXP_thenThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Stat(StatType.STRENGTH, 100, 50)
        );

        assertTrue(exception.getMessage().contains("nivel máximo"));
    }

    @Nested
    class Queries {
        @ParameterizedTest(name = "Nivel {0}: isInitial = {1}, isMaxed = {2}")
        @CsvSource({
                "1,   true,  false",
                "2,   false, false",
                "50,  false, false",
                "99,  false, false",
                "100, false, true"
        })
        @DisplayName("isInitial() y isMaxed() detectan estados correctamente")
        void whenCheckingInitialAndMaxed_thenCorrectState(int level, boolean initial, boolean maxed) {
            Stat stat = Stat.atLevel(StatType.STRENGTH, level);

            assertEquals(initial, stat.isInitial());
            assertEquals(maxed, stat.isMaxed());
        }

        @ParameterizedTest(name = "Nivel {0}: hasMastery = {1}")
        @CsvSource({
                "1,   false",
                "25,  false",
                "49,  false",
                "50,  true",
                "51,  true",
                "100, true"
        })
        @DisplayName("hasMastery() detecta nivel 50+")
        void whenCheckingMastery_thenCorrectResult(int level, boolean hasMastery) {
            Stat stat = Stat.atLevel(StatType.STRENGTH, level);
            assertEquals(hasMastery, stat.hasMastery());
        }

        @ParameterizedTest(name = "Lvl {0}, XP {1}/{2}: canLevelUp = {3}")
        @CsvSource({
                "1,  50,  100, false",
                "1,  100, 100, true",
                "1,  150, 100, true",
                "10, 1000, 1000, true",
                "50, 4999, 5000, false",
                "100, 0, 0, false"
        })
        @DisplayName("canLevelUp() verifica si puede subir de nivel")
        void whenCheckingCanLevelUp_thenCorrectResult(int level, int currentXP, int xpNeeded, boolean canLevelUp) {
            Stat stat = new Stat(StatType.STRENGTH, level, currentXP);

            assertEquals(xpNeeded, stat.getXPRequiredForNextLevel());
            assertEquals(canLevelUp, stat.canLevelUp());
        }
    }

    @Nested
    class ExperienceCalculation {
        @ParameterizedTest(name = "Nivel {0}: XP requerida = {1}")
        @CsvSource({
                "1,   100",
                "5,   500",
                "10,  1000",
                "50,  5000",
                "99,  9900",
                "100, 0"
        })
        @DisplayName("getXPRequiredForNextLevel() calcula correctamente")
        void whenGettingXPRequired_thenCorrectAmount(int level, int expectedXP) {
            Stat stat = Stat.atLevel(StatType.STRENGTH, level);
            assertEquals(expectedXP, stat.getXPRequiredForNextLevel());
        }

        @ParameterizedTest(name = "Lvl {0}, XP {1}/{2} = {3}%")
        @CsvSource({
                "1,  0,    100,  0.0",
                "1,  50,   100,  50.0",
                "1,  100,  100,  100.0",
                "10, 500,  1000, 50.0",
                "50, 2500, 5000, 50.0",
                "100, 0,   0,    100.0"
        })
        @DisplayName("getProgressPercentage() calcula porcentaje correcto")
        void whenGettingProgress_thenCorrectPercentage(int level, int currentXP, int xpNeeded, double expectedProgress) {
            Stat stat = new Stat(StatType.STRENGTH, level, currentXP);
            assertEquals(expectedProgress, stat.getProgressPercentage(), 0.01);
        }

        @ParameterizedTest(name = "Lvl {0} (XP {1}): Total XP = {2}")
        @CsvSource({
                "1,  0,    0",
                "2,  50,   150",
                "3,  0,    300",
                "10, 500,  5000",
                "100, 0,   495000"
        })
        @DisplayName("getTotalXPAccumulated() suma XP gastada + XP actual")
        void whenGettingTotalXP_thenIncludesSpentAndCurrent(int level, int currentXP, int expectedTotal) {
            Stat stat = new Stat(StatType.STRENGTH, level, currentXP);
            assertEquals(expectedTotal, stat.getTotalXPAccumulated());
        }
    }

    @Nested
    class AutomaticLevelUps {
        @Test
        @DisplayName("addXP() sin suficiente XP no sube de nivel")
        void whenAddingInsufficientXP_thenNoLevelUp() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            Stat result = stat.addXP(50);

            assertEquals(1, result.currentLevel());
            assertEquals(50, result.currentXP());
            assertFalse(result.canLevelUp());
        }

        @Test
        @DisplayName("addXP() con XP exacta sube 1 nivel")
        void whenAddingExactXP_thenLevelUpOnce() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            Stat result = stat.addXP(100);

            assertEquals(2, result.currentLevel());
            assertEquals(0, result.currentXP());
        }

        @Test
        @DisplayName("addXP() con XP de sobra sube 1 nivel y guarda el resto")
        void whenAddingExcessXP_thenLevelUpAndSaveRemainder() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            Stat result = stat.addXP(150);

            assertEquals(2, result.currentLevel());
            assertEquals(50, result.currentXP());
        }

        @Test
        @DisplayName("addXP() sube múltiples niveles si hay suficiente XP")
        void whenAddingMassiveXP_thenMultipleLevelUps() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            // 100 (1→2) + 200 (2→3) + 300 (3→4) = 600 XP para llegar a Lvl 4
            Stat result = stat.addXP(650);  // Sube a Lvl 4 con 50 XP sobrantes

            assertEquals(4, result.currentLevel());
            assertEquals(50, result.currentXP());
        }

        @Test
        @DisplayName("addXP() en stat maxeado ignora la XP")
        void whenAddingXPToMaxedStat_thenIgnored() {
            Stat stat = Stat.maxed(StatType.STRENGTH);
            Stat result = stat.addXP(10000);

            assertEquals(100, result.currentLevel());
            assertEquals(0, result.currentXP());
            assertSame(stat, result, "Si está maxeado, debe retornar el mismo objeto");
        }

        @Test
        @DisplayName("addXP() que alcanza nivel 100 descarta XP sobrante")
        void whenReachingLevel100_thenDiscardExcessXP() {
            Stat stat = Stat.atLevel(StatType.STRENGTH, 99);
            Stat result = stat.addXP(10000);

            assertEquals(100, result.currentLevel());
            assertEquals(0, result.currentXP(), "XP sobrante se descarta al maxear");
        }

        @Test
        @DisplayName("addXP() con XP negativa lanza IllegalArgumentException")
        void whenAddingNegativeXP_thenThrowsException() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            assertThrows(IllegalArgumentException.class, () -> stat.addXP(-50));
        }

        @Test
        @DisplayName("addXP() con 0 XP retorna stat sin cambios")
        void whenAddingZeroXP_thenNoChanges() {
            Stat stat = Stat.atLevel(StatType.STRENGTH, 5);
            Stat result = stat.addXP(0);

            assertEquals(5, result.currentLevel());
            assertEquals(0, result.currentXP());
        }
    }

    @Nested
    class ManualLevelUp {
        @Test
        @DisplayName("forceLevel() sube niveles sin verificar XP")
        void whenForcingLevels_thenLevelsUpWithoutXPCheck() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            Stat result = stat.forceLevel(10);

            assertEquals(11, result.currentLevel());
            assertEquals(0, result.currentXP());
        }

        @Test
        @DisplayName("forceLevel() no puede exceder nivel 100")
        void whenForcingBeyondMax_thenClampsTo100() {
            Stat stat = Stat.atLevel(StatType.STRENGTH, 95);
            Stat result = stat.forceLevel(20);

            assertEquals(100, result.currentLevel());
            assertEquals(0, result.currentXP());
        }

        @Test
        @DisplayName("forceLevel() con niveles negativos lanza IllegalArgumentException")
        void whenForcingNegativeLevels_thenThrowsException() {
            Stat stat = Stat.initial(StatType.STRENGTH);
            assertThrows(IllegalArgumentException.class, () -> stat.forceLevel(-5));
        }
    }

    @Nested
    class Reset {
        @Test
        @DisplayName("reset() devuelve el stat a nivel inicial")
        void whenResetting_thenBackToLevel1() {
            Stat stat = Stat.atLevel(StatType.STRENGTH, 50);
            stat = stat.addXP(2500);

            Stat reset = stat.reset();

            assertEquals(1, reset.currentLevel());
            assertEquals(0, reset.currentXP());
            assertTrue(reset.isInitial());
        }
    }


    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea stat con toda la info")
        void whenFormattingDisplay_thenIncludesAllInfo() {
            Stat stat = new Stat(StatType.STRENGTH, 23, 1450);
            String display = stat.toDisplayString();

            assertTrue(display.contains("💪"));
            assertTrue(display.contains("Fuerza"));
            assertTrue(display.contains("Lvl 23"));
            assertTrue(display.contains("1.450"));
            assertTrue(display.contains("2.300"));
            assertTrue(display.contains("%"));
        }

        @Test
        @DisplayName("toDisplayString() para stat maxeado muestra [MAX]")
        void whenFormattingMaxedStat_thenShowsMax() {
            Stat stat = Stat.maxed(StatType.STRENGTH);
            String display = stat.toDisplayString();

            assertTrue(display.contains("[MAX]"));
            assertFalse(display.contains("XP"));
        }

        @Test
        @DisplayName("toDisplayString() para stat con maestría muestra corona")
        void whenFormattingMasteryStat_thenShowsCrown() {
            Stat stat = Stat.atLevel(StatType.STRENGTH, 50);
            String display = stat.toDisplayString();

            assertTrue(display.contains("👑"), "Debe mostrar corona para maestría");
        }

        @Test
        @DisplayName("toCompactString() formatea de forma reducida")
        void whenFormattingCompact_thenOnlyIconAndLevel() {
            Stat stat = new Stat(StatType.STRENGTH, 23, 1450);
            String compact = stat.toCompactString();

            assertEquals("💪 Lvl 23", compact);
        }

        @Test
        @DisplayName("getMasteryTitle() retorna el título correcto del tipo")
        void whenGettingMasteryTitle_thenCorrectTitle() {
            Stat strength = Stat.initial(StatType.STRENGTH);
            assertEquals("Titán", strength.getMasteryTitle());

            Stat intellect = Stat.initial(StatType.INTELLECT);
            assertEquals("Cyborg", intellect.getMasteryTitle());
        }
    }

    @Nested
    class Immutability {
        @Test
        @DisplayName("Stat es inmutable: addXP no modifica el original")
        void stat_isImmutable() {
            Stat original = Stat.initial(StatType.STRENGTH);

            original.addXP(500);
            original.forceLevel(10);
            original.reset();

            assertEquals(1, original.currentLevel(), "Original no debe cambiar");
            assertEquals(0, original.currentXP());
        }

        @Test
        @DisplayName("Stats con mismos valores son equals()")
        void statsWithSameValues_areEqual() {
            Stat stat1 = new Stat(StatType.STRENGTH, 10, 500);
            Stat stat2 = new Stat(StatType.STRENGTH, 10, 500);

            assertEquals(stat1, stat2);
            assertEquals(stat1.hashCode(), stat2.hashCode());
        }

        @Test
        @DisplayName("Stats con diferentes tipos NO son equals()")
        void statsWithDifferentTypes_areNotEqual() {
            Stat strength = new Stat(StatType.STRENGTH, 10, 500);
            Stat intellect = new Stat(StatType.INTELLECT, 10, 500);

            assertNotEquals(strength, intellect);
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Operaciones encadenadas funcionan correctamente")
        void chainedOperations_workCorrectly() {
            Stat result = Stat.initial(StatType.STRENGTH)
                    .addXP(250) // Lvl 2, 50 XP
                    .addXP(150) // Lvl 3, 0 XP
                    .addXP(300) // Lvl 4, 0 XP
                    .forceLevel(1); // Lvl 5, 0 XP

            assertEquals(5, result.currentLevel());
            assertEquals(0, result.currentXP());
        }

        @Test
        @DisplayName("addXP desde nivel inicial hasta maxeado funciona")
        void addingXPFromInitialToMaxed_works() {
            Stat stat = Stat.initial(StatType.STRENGTH);

            // XP total para maxear: 495,000
            stat = stat.addXP(495_000);

            assertTrue(stat.isMaxed());
            assertEquals(100, stat.currentLevel());
            assertEquals(0, stat.currentXP());
        }
    }
}