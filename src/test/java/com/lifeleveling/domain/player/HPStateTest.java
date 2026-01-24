package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Estados de energía mental")
class HPStateTest {

    @Nested
    class StateValidation {
        @ParameterizedTest(name = "HP={0} debería ser HEALTHY")
        @ValueSource(ints = {50, 51, 75, 99, 100})
        @DisplayName("HP entre 50-100 → Estado HEALTHY")
        void whenHPBetween50And100_thenStateIsHealthy(int hp) {
            HPState state = HPState.fromHP(hp);

            assertEquals(HPState.HEALTHY, state);
            assertFalse(state.isBurnout());
            assertTrue(state.canStartHighRankQuests());
        }

        @ParameterizedTest(name = "HP={0} debería ser TIRED")
        @ValueSource(ints = {1, 10, 25, 49})
        @DisplayName("HP entre 1-49 → Estado TIRED")
        void whenHPBetween1And49_thenStateIsTired(int hp) {
            HPState state = HPState.fromHP(hp);

            assertEquals(HPState.TIRED, state);
            assertFalse(state.isBurnout());
            assertFalse(state.canStartHighRankQuests());
        }

        @Test
        @DisplayName("HP = 0 → Estado CRITICAL (Burnout)")
        void whenHPIsZero_thenStateIsCritical() {
            HPState state = HPState.fromHP(0);

            assertEquals(HPState.CRITICAL, state);
            assertTrue(state.isBurnout());
            assertFalse(state.canStartHighRankQuests());
        }
    }

    @Nested
    class RangeValidation {
        @Test
        @DisplayName("HP < 0 lanza IllegalArgumentException")
        void whenHPIsNegative_thenThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> HPState.fromHP(-1)
            );

            assertTrue(exception.getMessage().contains("HP inválido"));
            assertTrue(exception.getMessage().contains("-1"));
        }

        @Test
        @DisplayName("HP > 100 lanza IllegalArgumentException")
        void whenHPIsOver100_thenThrowsException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> HPState.fromHP(101)
            );

            assertTrue(exception.getMessage().contains("HP inválido"));
            assertTrue(exception.getMessage().contains("101"));
        }
    }
    @Nested
    class Multiplier {
        @ParameterizedTest(name = "{0}: baseXP={1} → finalXP={2}")
        @CsvSource({
                "HEALTHY, 100, 100",
                "TIRED,   100, 50",
                "CRITICAL, 100, 0"
        })
        @DisplayName("Multiplicadores de XP se aplican correctamente")
        void whenApplyingXPMultiplier_thenCorrectValueReturned(HPState state, int baseXP, int expectedXP) {
            int finalXP = state.applyXPMultiplier(baseXP);

            assertEquals(expectedXP, finalXP);
        }

        @Test
        @DisplayName("applyXPMultiplier() con XP negativa lanza excepción")
        void whenApplyingNegativeXP_thenThrowsException() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> HPState.HEALTHY.applyXPMultiplier(-10)
            );
        }

        @ParameterizedTest(name = "{0}: baseGold={1} → finalGold={2}")
        @CsvSource({
                "HEALTHY,  200, 200",
                "TIRED,    200, 200",
                "CRITICAL, 200, 0"
        })
        @DisplayName("Multiplicadores de Gold se aplican correctamente")
        void whenApplyingGoldMultiplier_thenCorrectValueReturned(HPState state, int baseGold, int expectedGold) {
            int finalGold = state.applyGoldMultiplier(baseGold);

            assertEquals(expectedGold, finalGold);
        }

        @Test
        @DisplayName("applyGoldMultiplier() con Gold negativo lanza excepción")
        void whenApplyingNegativeGold_thenThrowsException() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> HPState.HEALTHY.applyGoldMultiplier(-50)
            );
        }
    }

    @Nested
    class Properties {
        @Test
        @DisplayName("HEALTHY tiene valores correctos")
        void healthyStateHasCorrectProperties() {
            assertEquals(50, HPState.HEALTHY.getMinHP());
            assertEquals(100, HPState.HEALTHY.getMaxHP());
            assertEquals(1.0, HPState.HEALTHY.getXpMultiplier());
            assertEquals(1.0, HPState.HEALTHY.getGoldMultiplier());
        }

        @Test
        @DisplayName("TIRED tiene valores correctos")
        void tiredStateHasCorrectProperties() {
            assertEquals(1, HPState.TIRED.getMinHP());
            assertEquals(49, HPState.TIRED.getMaxHP());
            assertEquals(0.5, HPState.TIRED.getXpMultiplier());
            assertEquals(1.0, HPState.TIRED.getGoldMultiplier());
        }

        @Test
        @DisplayName("CRITICAL tiene valores correctos")
        void criticalStateHasCorrectProperties() {
            assertEquals(0, HPState.CRITICAL.getMinHP());
            assertEquals(0, HPState.CRITICAL.getMaxHP());
            assertEquals(0.0, HPState.CRITICAL.getXpMultiplier());
            assertEquals(0.0, HPState.CRITICAL.getGoldMultiplier());
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Valor límite: HP=50 es exactamente HEALTHY")
        void whenHPIs50_thenIsHealthy() {
            assertEquals(HPState.HEALTHY, HPState.fromHP(50));
        }

        @Test
        @DisplayName("Valor límite: HP=49 es exactamente TIRED")
        void whenHPIs49_thenIsTired() {
            assertEquals(HPState.TIRED, HPState.fromHP(49));
        }

        @Test
        @DisplayName("XP=0 se mantiene en 0 con cualquier multiplicador")
        void whenBaseXPIsZero_thenResultIsZero() {
            assertEquals(0, HPState.HEALTHY.applyXPMultiplier(0));
            assertEquals(0, HPState.TIRED.applyXPMultiplier(0));
            assertEquals(0, HPState.CRITICAL.applyXPMultiplier(0));
        }
    }
}