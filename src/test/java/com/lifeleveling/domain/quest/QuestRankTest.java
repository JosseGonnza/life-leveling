package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.quest.shared.QuestRank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuestRank - Sistema de Rangos de Misiones")
class QuestRankTest {

    @Nested
    class RangeProperties {
        @Test
        @DisplayName("Existen exactamente 8 rangos")
        void thereAreExactly8Ranks() {
            assertEquals(8, QuestRank.values().length);
        }

        @ParameterizedTest(name = "{0}: baseXP={1}, baseGold={2}, moralDamage={3}")
        @CsvSource({
                "E, 10, 15, 0",
                "D, 50, 50, 5",
                "C, 150, 150, 15",
                "B, 500, 400, 20",
                "A, 1500, 2000, 30",
                "S, 5000, 10000, 50",
                "S_PLUS, 20000, 50000, 50",
                "S_PLUS_PLUS, 100000, 500000, 50"
        })
        @DisplayName("Rangos tienen valores base correctos")
        void ranksHaveCorrectBaseValues(QuestRank rank, int expectedXP, int expectedGold, int expectedDamage) {
            assertEquals(expectedXP, rank.getBaseXP());
            assertEquals(expectedGold, rank.getBaseGold());
            assertEquals(expectedDamage, rank.getMoralDamage());
        }

        @Test
        @DisplayName("Todos los rangos tienen icono emoji")
        void allRanksHaveEmoji() {
            for (QuestRank rank : QuestRank.values()) {
                assertNotNull(rank.getIcon());
                assertFalse(rank.getIcon().isBlank());
            }
        }

        @Test
        @DisplayName("Todos los rangos tienen tiempo estimado")
        void allRanksHaveEstimatedTime() {
            for (QuestRank rank : QuestRank.values()) {
                assertNotNull(rank.getEstimatedTime());
                assertFalse(rank.getEstimatedTime().isBlank());
            }
        }
    }

    @Nested
    class Search {
        @ParameterizedTest(name = "Buscar por nombre enum: {0}")
        @ValueSource(strings = {"E", "D", "C", "B", "A", "S", "S_PLUS", "S_PLUS_PLUS"})
        @DisplayName("fromString() encuentra rangos por nombre enum")
        void whenSearchingByEnumName_thenRankIsFound(String name) {
            Optional<QuestRank> result = QuestRank.fromString(name);

            assertTrue(result.isPresent());
            assertEquals(name, result.get().name());
        }

        @ParameterizedTest(name = "Buscar por nombre dificultad: {0}")
        @ValueSource(strings = {"Rutinaria", "Común", "Rara", "Élite", "Heroica", "Legendaria", "Mítica", "Divina"})
        @DisplayName("fromString() encuentra rangos por nombre de dificultad")
        void whenSearchingByDifficultyName_thenRankIsFound(String difficultyName) {
            Optional<QuestRank> result = QuestRank.fromString(difficultyName);

            assertTrue(result.isPresent());
            assertEquals(difficultyName, result.get().getDifficultyName());
        }

        @ParameterizedTest(name = "Case-insensitive: {0}")
        @ValueSource(strings = {"b", "B", "élite", "ÉLITE", "Élite"})
        @DisplayName("fromString() es case-insensitive")
        void whenSearchingWithDifferentCasing_thenRankIsFound(String name) {
            Optional<QuestRank> result = QuestRank.fromString(name);

            assertTrue(result.isPresent());
            assertEquals(QuestRank.B, result.get());
        }

        @Test
        @DisplayName("fromString() con nombre inválido retorna Optional vacío")
        void whenNameIsInvalid_thenReturnsEmpty() {
            Optional<QuestRank> result = QuestRank.fromString("INVALID_RANK");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("fromString() con null lanza IllegalArgumentException")
        void whenNameIsNull_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QuestRank.fromString(null));
        }

        @Test
        @DisplayName("fromString() con string vacío lanza IllegalArgumentException")
        void whenNameIsBlank_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> QuestRank.fromString("   "));
        }
    }

    @Nested
    class HPRestrictions {
        @ParameterizedTest(name = "{0} puede iniciarse con {1}")
        @CsvSource({
                "E, CRITICAL",
                "E, TIRED",
                "E, HEALTHY",
                "D, CRITICAL",
                "D, TIRED",
                "D, HEALTHY",
                "C, TIRED",
                "C, HEALTHY",
                "B, HEALTHY",
                "A, HEALTHY",
                "S, HEALTHY",
                "S_PLUS, HEALTHY",
                "S_PLUS_PLUS, HEALTHY"
        })
        @DisplayName("canStartWith() permite iniciar quest según HP state")
        void whenHPStateIsValid_thenCanStart(QuestRank rank, HPState hpState) {
            assertTrue(rank.canStartWith(hpState));
        }

        @ParameterizedTest(name = "{0} NO puede iniciarse con {1}")
        @CsvSource({
                "C, CRITICAL",
                "B, CRITICAL",
                "B, TIRED",
                "A, CRITICAL",
                "A, TIRED",
                "S, CRITICAL",
                "S, TIRED",
                "S_PLUS, CRITICAL",
                "S_PLUS, TIRED",
                "S_PLUS_PLUS, CRITICAL",
                "S_PLUS_PLUS, TIRED"
        })
        @DisplayName("canStartWith() bloquea quest según HP state")
        void whenHPStateIsInvalid_thenCannotStart(QuestRank rank, HPState hpState) {
            assertFalse(rank.canStartWith(hpState));
        }

        @Test
        @DisplayName("canStartWith() con null lanza IllegalArgumentException")
        void whenHPStateIsNull_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestRank.B.canStartWith(null));
        }

        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"E", "D"})
        @DisplayName("Rangos E y D NO están bloqueados en BURNOUT")
        void lowRanksNotBlockedDuringBurnout(QuestRank rank) {
            assertFalse(rank.isBlockedDuringBurnout());
        }

        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"C", "B", "A", "S", "S_PLUS", "S_PLUS_PLUS"})
        @DisplayName("Rangos C+ están bloqueados en BURNOUT")
        void highRanksBlockedDuringBurnout(QuestRank rank) {
            assertTrue(rank.isBlockedDuringBurnout());
        }

        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"B", "A", "S", "S_PLUS", "S_PLUS_PLUS"})
        @DisplayName("Rangos B+ requieren estado HEALTHY")
        void highRanksRequireHealthyState(QuestRank rank) {
            assertTrue(rank.requiresHealthyState());
        }
    }

    @Nested
    class RewardCalculations {
        @ParameterizedTest(name = "{0} con HEALTHY: XP={1}, Gold={2}")
        @CsvSource({
                "E,    10,     15",
                "D,    50,     50",
                "C,    150,    150",
                "B,    500,    400",
                "A,    1500,   2000",
                "S,    5000,   10000"
        })
        @DisplayName("calculateFinalXP/Gold con estado HEALTHY (sin modificadores)")
        void whenHealthy_thenNoModifiers(QuestRank rank, int expectedXP, int expectedGold) {
            assertEquals(expectedXP, rank.calculateFinalXP(HPState.HEALTHY));
            assertEquals(expectedGold, rank.calculateFinalGold(HPState.HEALTHY));
        }

        @ParameterizedTest(name = "{0} con TIRED: XP reducida a 50%")
        @CsvSource({
                "E,    10,    5",
                "D,    50,    25",
                "C,    150,   75"
        })
        @DisplayName("calculateFinalXP con estado TIRED (50% XP)")
        void whenTired_thenXPReducedBy50Percent(QuestRank rank, int baseXP, int expectedXP) {
            assertEquals(expectedXP, rank.calculateFinalXP(HPState.TIRED));
            assertEquals(rank.getBaseGold(), rank.calculateFinalGold(HPState.TIRED));
        }

        @ParameterizedTest
        @EnumSource(QuestRank.class)
        @DisplayName("calculateFinalXP/Gold con CRITICAL retorna 0")
        void whenCritical_thenNoRewards(QuestRank rank) {
            assertEquals(0, rank.calculateFinalXP(HPState.CRITICAL));
            assertEquals(0, rank.calculateFinalGold(HPState.CRITICAL));
        }

        @Test
        @DisplayName("calculateFinalXP con null lanza IllegalArgumentException")
        void whenCalculatingXPWithNullState_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestRank.B.calculateFinalXP(null));
        }

        @Test
        @DisplayName("calculateFinalGold con null lanza IllegalArgumentException")
        void whenCalculatingGoldWithNullState_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestRank.B.calculateFinalGold(null));
        }
    }

    @Nested
    class Penalties {
        @ParameterizedTest(name = "{0}: moralDamage={1} HP → goldDamage={2} G")
        @CsvSource({
                "E, 0,   0",
                "D, 5,   50",
                "C, 15,  150",
                "B, 20,  200",
                "A, 30,  300",
                "S, 50,  500"
        })
        @DisplayName("getGoldDamageOnBurnout() convierte HP damage a Gold (ratio 1:10)")
        void whenInBurnout_thenDamageConvertsToGold(QuestRank rank, int moralDamage, int expectedGoldLoss) {
            assertEquals(moralDamage, rank.getMoralDamage());
            assertEquals(expectedGoldLoss, rank.getGoldDamageOnBurnout());
        }

        @Test
        @DisplayName("Rango E no penaliza por fallo")
        void rankE_hasZeroPenalty() {
            assertEquals(0, QuestRank.E.getMoralDamage());
            assertEquals(0, QuestRank.E.getGoldDamageOnBurnout());
        }

        @Test
        @DisplayName("Rangos S/S+/S++ tienen la misma penalización máxima (50 HP)")
        void legendaryRanks_haveSamePenalty() {
            assertEquals(50, QuestRank.S.getMoralDamage());
            assertEquals(50, QuestRank.S_PLUS.getMoralDamage());
            assertEquals(50, QuestRank.S_PLUS_PLUS.getMoralDamage());
        }
    }

    @Nested
    class Comparisons {
        @ParameterizedTest(name = "{0} es al menos {1}")
        @CsvSource({
                "E, E",
                "D, E",
                "D, D",
                "C, D",
                "B, B",
                "A, C",
                "S, A",
                "S_PLUS, S",
                "S_PLUS_PLUS, S_PLUS"
        })
        @DisplayName("isAtLeast() compara rangos correctamente (>=)")
        void whenComparingRanks_thenIsAtLeast(QuestRank rank, QuestRank other) {
            assertTrue(rank.isAtLeast(other));
        }

        @ParameterizedTest(name = "{0} NO es al menos {1}")
        @CsvSource({
                "E, D",
                "D, C",
                "C, B",
                "A, S",
                "S, S_PLUS"
        })
        @DisplayName("isAtLeast() detecta rangos menores")
        void whenComparingLowerRanks_thenNotAtLeast(QuestRank rank, QuestRank other) {
            assertFalse(rank.isAtLeast(other));
        }

        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"B", "A", "S", "S_PLUS", "S_PLUS_PLUS"})
        @DisplayName("isHighRank() detecta rangos B+")
        void whenRankIsB_orHigher_thenIsHighRank(QuestRank rank) {
            assertTrue(rank.isHighRank());
        }

        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"E", "D", "C"})
        @DisplayName("isHighRank() detecta rangos bajos (E, D, C)")
        void whenRankIsLow_thenNotHighRank(QuestRank rank) {
            assertFalse(rank.isHighRank());
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea correctamente Rango B")
        void whenFormattingRankB_thenCorrectFormat() {
            String display = QuestRank.B.toDisplayString();

            assertTrue(display.contains("🟠"));
            assertTrue(display.contains("Élite"));
            assertTrue(display.contains("Rango B"));
        }

        @Test
        @DisplayName("toDisplayString() formatea correctamente Rango S+")
        void whenFormattingRankSPlus_thenCorrectFormat() {
            String display = QuestRank.S_PLUS.toDisplayString();

            assertTrue(display.contains("⭐"));
            assertTrue(display.contains("Mítica"));
            assertTrue(display.contains("S+"));
            assertFalse(display.contains("_PLUS"));
        }

        @Test
        @DisplayName("toDisplayString() formatea correctamente Rango S++")
        void whenFormattingRankSPlusPlus_thenCorrectFormat() {
            String display = QuestRank.S_PLUS_PLUS.toDisplayString();

            assertTrue(display.contains("🌌"));
            assertTrue(display.contains("Divina"));
            assertTrue(display.contains("S++"));
            assertFalse(display.contains("_PLUS_PLUS"));
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Todos los rangos tienen estado mínimo de HP válido o null")
        void allRanksHaveValidMinHPState() {
            for (QuestRank rank : QuestRank.values()) {
                Optional<HPState> minState = rank.getMinHPState();
                minState.ifPresent(state -> assertTrue(
                        state == HPState.TIRED || state == HPState.HEALTHY
                ));
            }
        }

        @Test
        @DisplayName("La escala de recompensas es progresiva (XP aumenta)")
        void rewardsScaleProgressively() {
            QuestRank[] ranks = QuestRank.values();
            for (int i = 1; i < ranks.length; i++) {
                assertTrue(ranks[i].getBaseXP() > ranks[i - 1].getBaseXP(),
                        String.format("%s debe dar más XP que %s", ranks[i], ranks[i - 1]));
            }
        }
    }
}