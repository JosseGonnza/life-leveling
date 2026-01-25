package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wallet - Sistema Económico del Jugador")
class WalletTest {

    @Test
    @DisplayName("Wallet.empty() crea wallet con 0 Gold")
    void whenCreatingEmpty_thenGoldIsZero() {
        Wallet wallet = Wallet.empty();

        assertEquals(0, wallet.currentGold());
        assertTrue(wallet.isEmpty());
    }

    @Test
    @DisplayName("Wallet.of() crea wallet con cantidad específica")
    void whenCreatingWithAmount_thenGoldIsSet() {
        Wallet wallet = Wallet.of(500);

        assertEquals(500, wallet.currentGold());
        assertFalse(wallet.isEmpty());
    }

    @Test
    @DisplayName("Wallet.EMPTY es constante inmutable")
    void emptyConstant_isImmutable() {
        Wallet empty1 = Wallet.EMPTY;
        Wallet empty2 = Wallet.empty();

        assertEquals(0, empty1.currentGold());
        assertEquals(empty1.currentGold(), empty2.currentGold());
    }

    @Test
    @DisplayName("Constructor con Gold negativo lanza IllegalArgumentException")
    void whenCreatingWithNegativeGold_thenThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Wallet(-100)
        );

        assertTrue(exception.getMessage().contains("negativo"));
        assertTrue(exception.getMessage().contains("-100"));
    }

    @Test
    @DisplayName("Wallet.of() con Gold negativo lanza IllegalArgumentException")
    void whenCreatingWithNegativeAmount_thenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Wallet.of(-50));
    }

    @Test
    @DisplayName("Wallet con 0 Gold es válida")
    void walletWithZeroGold_isValid() {
        assertDoesNotThrow(() -> new Wallet(0));
        assertDoesNotThrow(() -> Wallet.of(0));
    }

    @Nested
    class Queries {
        @ParameterizedTest(name = "Wallet con {0} G: isEmpty = {1}")
        @CsvSource({
                "0,    true",
                "1,    false",
                "100,  false",
                "10000, false"
        })
        @DisplayName("isEmpty() detecta correctamente wallets vacías")
        void whenCheckingIfEmpty_thenCorrectResult(int gold, boolean expectedEmpty) {
            Wallet wallet = Wallet.of(gold);
            assertEquals(expectedEmpty, wallet.isEmpty());
        }

        @ParameterizedTest(name = "Wallet {0} G: canAfford({1}) = {2}")
        @CsvSource({
                "100, 50,  true",
                "100, 100, true",
                "100, 101, false",
                "0,   1,   false",
                "500, 0,   true"
        })
        @DisplayName("canAfford() verifica si hay suficiente Gold")
        void whenCheckingCanAfford_thenCorrectResult(int gold, int cost, boolean canAfford) {
            Wallet wallet = Wallet.of(gold);
            assertEquals(canAfford, wallet.canAfford(cost));
        }

        @Test
        @DisplayName("canAfford() con coste negativo lanza IllegalArgumentException")
        void whenCheckingAffordWithNegativeCost_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.canAfford(-10));
        }

        @ParameterizedTest(name = "Wallet {0} G: hasAtLeast({1}) = {2}")
        @CsvSource({
                "10000, 10000, true",
                "15000, 10000, true",
                "9999,  10000, false",
                "0,     1,     false"
        })
        @DisplayName("hasAtLeast() verifica umbrales de riqueza")
        void whenCheckingHasAtLeast_thenCorrectResult(int gold, int threshold, boolean hasIt) {
            Wallet wallet = Wallet.of(gold);
            assertEquals(hasIt, wallet.hasAtLeast(threshold));
        }

        @Test
        @DisplayName("hasAtLeast() con umbral negativo lanza IllegalArgumentException")
        void whenCheckingAtLeastWithNegativeThreshold_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.hasAtLeast(-10));
        }
    }

    @Nested
    class ImmutableOperations {
        @Test
        @DisplayName("add() crea nueva Wallet sin modificar la original")
        void whenAddingGold_thenCreatesNewWallet() {
            Wallet original = Wallet.of(100);
            Wallet result = original.add(50);

            assertEquals(100, original.currentGold(), "Original no debe cambiar");
            assertEquals(150, result.currentGold(), "Nueva wallet debe tener la suma");
            assertNotSame(original, result, "Deben ser instancias diferentes");
        }

        @ParameterizedTest(name = "{0} G + {1} G = {2} G")
        @CsvSource({
                "0,    100,  100",
                "100,  50,   150",
                "500,  500,  1000",
                "9999, 1,    10000"
        })
        @DisplayName("add() suma correctamente Gold")
        void whenAddingGold_thenCorrectSum(int initial, int toAdd, int expected) {
            Wallet wallet = Wallet.of(initial);
            Wallet result = wallet.add(toAdd);

            assertEquals(expected, result.currentGold());
        }

        @Test
        @DisplayName("add() con cantidad negativa lanza IllegalArgumentException")
        void whenAddingNegativeAmount_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.add(-50));
        }

        @Test
        @DisplayName("subtract() crea nueva Wallet sin modificar la original")
        void whenSubtractingGold_thenCreatesNewWallet() {
            Wallet original = Wallet.of(100);
            Wallet result = original.subtract(30);

            assertEquals(100, original.currentGold(), "Original no debe cambiar");
            assertEquals(70, result.currentGold(), "Nueva wallet debe tener la resta");
            assertNotSame(original, result);
        }

        @ParameterizedTest(name = "{0} G - {1} G = {2} G")
        @CsvSource({
                "100,  50,  50",
                "100,  100, 0",
                "100,  150, 0",
                "50,   1000, 0",
                "0,    10,  0"
        })
        @DisplayName("subtract() resta correctamente con clamping a 0")
        void whenSubtractingGold_thenCorrectResult(int initial, int toSubtract, int expected) {
            Wallet wallet = Wallet.of(initial);
            Wallet result = wallet.subtract(toSubtract);

            assertEquals(expected, result.currentGold());
        }

        @Test
        @DisplayName("subtract() con cantidad negativa lanza IllegalArgumentException")
        void whenSubtractingNegativeAmount_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.subtract(-50));
        }
    }

    @Nested
    class Penalties {
        @ParameterizedTest(name = "Wallet {0} G: Burnout tax = {1} G → Queda {2} G")
        @CsvSource({
                "1000,   100,  900",
                "5000,   500,  4500",
                "10000,  1000, 9000",
                "100,    10,   90",
                "0,      0,    0"
        })
        @DisplayName("applyBurnoutTax() aplica penalización del 10%")
        void whenApplyingBurnoutTax_thenLoses10Percent(int initial, int expectedTax, int expectedRemaining) {
            Wallet wallet = Wallet.of(initial);
            Wallet result = wallet.applyBurnoutTax();

            int actualTax = initial - result.currentGold();
            assertEquals(expectedTax, actualTax);
            assertEquals(expectedRemaining, result.currentGold());
        }

        @Test
        @DisplayName("calculateBurnoutTax() calcula 10% correctamente (método estático)")
        void whenCalculatingBurnoutTax_thenCorrect10Percent() {
            assertEquals(100, Wallet.calculateBurnoutTax(1000));
            assertEquals(500, Wallet.calculateBurnoutTax(5000));
            assertEquals(0, Wallet.calculateBurnoutTax(0));
        }

        @Test
        @DisplayName("calculateBurnoutTax() con Gold negativo lanza IllegalArgumentException")
        void whenCalculatingTaxWithNegativeGold_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> Wallet.calculateBurnoutTax(-100));
        }

        @ParameterizedTest(name = "{0} HP damage → {1} G loss")
        @CsvSource({
                "5,   50",
                "15,  150",
                "20,  200",
                "30,  300",
                "50,  500",
                "0,   0"
        })
        @DisplayName("applyMoralDamageAsGold() convierte HP damage a Gold (ratio 1:10)")
        void whenApplyingMoralDamage_thenConvertsToGold(int hpDamage, int expectedGoldLoss) {
            Wallet wallet = Wallet.of(1000);
            Wallet result = wallet.applyMoralDamageAsGold(hpDamage);

            int actualLoss = wallet.currentGold() - result.currentGold();
            assertEquals(expectedGoldLoss, actualLoss);
        }

        @Test
        @DisplayName("applyMoralDamageAsGold() con Wallet insuficiente clampea a 0")
        void whenMoralDamageExceedsGold_thenClampsToZero() {
            Wallet wallet = Wallet.of(100);
            Wallet result = wallet.applyMoralDamageAsGold(50);

            assertEquals(0, result.currentGold(), "No puede quedar negativo");
        }

        @Test
        @DisplayName("applyMoralDamageAsGold() con HP negativo lanza IllegalArgumentException")
        void whenApplyingNegativeMoralDamage_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.applyMoralDamageAsGold(-10));
        }

        @Test
        @DisplayName("convertHPDamageToGold() convierte correctamente (método estático)")
        void whenConvertingHPToGold_thenCorrectRatio() {
            assertEquals(50, Wallet.convertHPDamageToGold(5));
            assertEquals(200, Wallet.convertHPDamageToGold(20));
            assertEquals(500, Wallet.convertHPDamageToGold(50));
            assertEquals(0, Wallet.convertHPDamageToGold(0));
        }

        @Test
        @DisplayName("convertHPDamageToGold() con HP negativo lanza IllegalArgumentException")
        void whenConvertingNegativeHP_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> Wallet.convertHPDamageToGold(-10));
        }
    }

    @Nested
    class UIFormat {
        @ParameterizedTest(name = "{0} G → \"{1}\"")
        @CsvSource(value = {
                "0       | 0 G",
                "100     | 100 G",
                "1000    | 1.000 G",
                "10000   | 10.000 G",
                "100000  | 100.000 G",
                "1000000 | 1.000.000 G"
        }, delimiter = '|')
        @DisplayName("toDisplayString() formatea Gold con separadores de miles")
        void whenFormattingForDisplay_thenCorrectFormat(int gold, String expectedFormat) {
            Wallet wallet = Wallet.of(gold);
            assertEquals(expectedFormat.trim(), wallet.toDisplayString());
        }

        @ParameterizedTest(name = "{0}/{1} G = {2}% progreso")
        @CsvSource({
                "0,     10000,  0.0",
                "5000,  10000,  50.0",
                "9999,  10000,  99.99",
                "10000, 10000,  100.0",
                "15000, 10000,  100.0"     //Clampea a 100%
        })
        @DisplayName("getProgressTowards() calcula porcentaje hacia meta")
        void whenCalculatingProgress_thenCorrectPercentage(int current, int goal, double expectedProgress) {
            Wallet wallet = Wallet.of(current);
            double progress = wallet.getProgressTowards(goal);

            assertEquals(expectedProgress, progress, 0.01);
        }

        @Test
        @DisplayName("getProgressTowards() con meta 0 lanza IllegalArgumentException")
        void whenCalculatingProgressWithZeroGoal_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.getProgressTowards(0));
        }

        @Test
        @DisplayName("getProgressTowards() con meta negativa lanza IllegalArgumentException")
        void whenCalculatingProgressWithNegativeGoal_thenThrowsException() {
            Wallet wallet = Wallet.of(100);
            assertThrows(IllegalArgumentException.class, () -> wallet.getProgressTowards(-100));
        }
    }

    @Nested
    class Immutability {
        @Test
        @DisplayName("Wallet es inmutable: operaciones no modifican el original")
        void wallet_isImmutable() {
            Wallet original = Wallet.of(100);

            original.add(50);
            original.subtract(20);
            original.applyBurnoutTax();
            original.applyMoralDamageAsGold(10);

            assertEquals(100, original.currentGold(),
                    "Wallet original debe permanecer sin cambios después de operaciones");
        }

        @Test
        @DisplayName("Wallet con mismo Gold son equals()")
        void walletsWithSameGold_areEqual() {
            Wallet wallet1 = Wallet.of(500);
            Wallet wallet2 = Wallet.of(500);

            assertEquals(wallet1, wallet2);
            assertEquals(wallet1.hashCode(), wallet2.hashCode());
        }

        @Test
        @DisplayName("Wallet con diferente Gold NO son equals()")
        void walletsWithDifferentGold_areNotEqual() {
            Wallet wallet1 = Wallet.of(500);
            Wallet wallet2 = Wallet.of(600);

            assertNotEquals(wallet1, wallet2);
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Operaciones encadenadas funcionan correctamente")
        void chainedOperations_workCorrectly() {
            Wallet result = Wallet.of(1000)
                        .add(500)           // 1500
                    .subtract(200)  // 1300
                    .applyBurnoutTax()      // 1300 - 130 = 1170
                    .add(30);               // 1200

            assertEquals(1200, result.currentGold());
        }

        @Test
        @DisplayName("Wallet con Gold máximo (Integer.MAX_VALUE) es válida")
        void walletWithMaxInteger_isValid() {
            assertDoesNotThrow(() -> Wallet.of(Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("Sumar Gold cerca del límite no causa overflow")
        void addingNearMaxValue_doesNotOverflow() {
            Wallet wallet = Wallet.of(Integer.MAX_VALUE - 100);
            assertDoesNotThrow(() -> wallet.add(50));
        }
    }
}