package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuestReward - Sistema de Recompensas")
class QuestRewardTest {

    @Nested
    class Construction {
        @Test
        @DisplayName("Constructor básico crea reward válida")
        void whenConstructing_thenValidReward() {
            QuestReward reward = new QuestReward(
                    Map.of(StatType.STRENGTH, 50),
                    100,
                    200
            );

            assertEquals(50, reward.getStatXP(StatType.STRENGTH));
            assertEquals(100, reward.generalXP());
            assertEquals(200, reward.gold());
        }

        @Test
        @DisplayName("Constructor con statXP null usa Map vacío")
        void whenStatXPIsNull_thenUsesEmptyMap() {
            QuestReward reward = new QuestReward(null, 100, 200);

            assertTrue(reward.statXP().isEmpty());
            assertEquals(100, reward.generalXP());
            assertEquals(200, reward.gold());
        }

        @Test
        @DisplayName("Constructor con XP negativa lanza IllegalArgumentException")
        void whenStatXPIsNegative_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestReward(Map.of(StatType.STRENGTH, -10), 0, 0));
        }

        @Test
        @DisplayName("Constructor con General XP negativa lanza IllegalArgumentException")
        void whenGeneralXPIsNegative_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestReward(Map.of(), -50, 0));
        }

        @Test
        @DisplayName("Constructor con Gold negativo lanza IllegalArgumentException")
        void whenGoldIsNegative_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestReward(Map.of(), 0, -100));
        }

        @Test
        @DisplayName("Constructor con StatType null lanza IllegalArgumentException")
        void whenStatTypeIsNull_thenThrowsException() {
            Map<StatType, Integer> mapWithNullKey = new java.util.HashMap<>();
            mapWithNullKey.put(null, 50);

            assertThrows(IllegalArgumentException.class,
                    () -> new QuestReward(mapWithNullKey, 0, 0));
        }
    }

    @Nested
    class FactoryMethods {
        @Test
        @DisplayName("empty() crea reward sin recompensas")
        void whenCreatingEmpty_thenNoRewards() {
            QuestReward reward = QuestReward.empty();

            assertTrue(reward.isEmpty());
            assertEquals(0, reward.generalXP());
            assertEquals(0, reward.gold());
            assertTrue(reward.statXP().isEmpty());
        }

        @Test
        @DisplayName("ofGeneralXP() crea reward solo con XP general")
        void whenCreatingWithGeneralXP_thenOnlyGeneralXP() {
            QuestReward reward = QuestReward.ofGeneralXP(100);

            assertEquals(100, reward.generalXP());
            assertEquals(0, reward.gold());
            assertTrue(reward.statXP().isEmpty());
        }

        @Test
        @DisplayName("ofGold() crea reward solo con Gold")
        void whenCreatingWithGold_thenOnlyGold() {
            QuestReward reward = QuestReward.ofGold(500);

            assertEquals(500, reward.gold());
            assertEquals(0, reward.generalXP());
            assertTrue(reward.statXP().isEmpty());
        }

        @Test
        @DisplayName("ofSingleStat() crea reward con XP de un stat")
        void whenCreatingWithSingleStat_thenOnlyStatXP() {
            QuestReward reward = QuestReward.ofSingleStat(StatType.INTELLECT, 75);

            assertEquals(75, reward.getStatXP(StatType.INTELLECT));
            assertEquals(0, reward.generalXP());
            assertEquals(0, reward.gold());
        }

        @Test
        @DisplayName("ofSingleStat() con tipo null lanza IllegalArgumentException")
        void whenCreatingSingleStatWithNull_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestReward.ofSingleStat(null, 50));
        }

        @Test
        @DisplayName("fromRank() crea reward desde QuestRank")
        void whenCreatingFromRank_thenUsesRankValues() {
            QuestReward reward = QuestReward.fromRank(QuestRank.C);

            assertEquals(QuestRank.C.getBaseXP(), reward.generalXP());
            assertEquals(QuestRank.C.getBaseGold(), reward.gold());
        }

        @Test
        @DisplayName("fromRank() con null lanza IllegalArgumentException")
        void whenCreatingFromNullRank_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestReward.fromRank(null));
        }
    }

    @Nested
    class Builder {
        @Test
        @DisplayName("Builder construye reward compleja correctamente")
        void whenUsingBuilder_thenCorrectReward() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 50)
                    .addStatXP(StatType.INTELLECT, 75)
                    .setGeneralXP(100)
                    .setGold(200)
                    .build();

            assertEquals(50, reward.getStatXP(StatType.STRENGTH));
            assertEquals(75, reward.getStatXP(StatType.INTELLECT));
            assertEquals(100, reward.generalXP());
            assertEquals(200, reward.gold());
        }

        @Test
        @DisplayName("Builder acumula múltiples adds del mismo stat")
        void whenAddingMultipleTimesSameStat_thenAccumulates() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 50)
                    .addStatXP(StatType.STRENGTH, 30)
                    .build();

            assertEquals(80, reward.getStatXP(StatType.STRENGTH));
        }

        @Test
        @DisplayName("Builder acumula General XP con addGeneralXP()")
        void whenAddingGeneralXP_thenAccumulates() {
            QuestReward reward = QuestReward.builder()
                    .setGeneralXP(100)
                    .addGeneralXP(50)
                    .build();

            assertEquals(150, reward.generalXP());
        }

        @Test
        @DisplayName("Builder acumula Gold con addGold()")
        void whenAddingGold_thenAccumulates() {
            QuestReward reward = QuestReward.builder()
                    .setGold(100)
                    .addGold(50)
                    .build();

            assertEquals(150, reward.gold());
        }

        @Test
        @DisplayName("Builder con valores negativos lanza IllegalArgumentException")
        void whenBuildingWithNegativeValues_thenThrowsException() {
            QuestReward.Builder builder = QuestReward.builder();

            assertThrows(IllegalArgumentException.class,
                    () -> builder.addStatXP(StatType.STRENGTH, -10));
            assertThrows(IllegalArgumentException.class,
                    () -> builder.setGeneralXP(-50));
            assertThrows(IllegalArgumentException.class,
                    () -> builder.addGeneralXP(-50));
            assertThrows(IllegalArgumentException.class,
                    () -> builder.setGold(-100));
            assertThrows(IllegalArgumentException.class,
                    () -> builder.addGold(-100));
        }

        @Test
        @DisplayName("Builder con StatType null lanza IllegalArgumentException")
        void whenBuildingWithNullStatType_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestReward.builder().addStatXP(null, 50));
        }
    }

    @Nested
    class Queries {
        @Test
        @DisplayName("isEmpty() retorna true para reward vacía")
        void whenRewardIsEmpty_thenIsEmptyReturnsTrue() {
            QuestReward reward = QuestReward.empty();
            assertTrue(reward.isEmpty());
        }

        @Test
        @DisplayName("isEmpty() retorna false si tiene cualquier recompensa")
        void whenRewardHasAnything_thenIsEmptyReturnsFalse() {
            assertFalse(QuestReward.ofGeneralXP(10).isEmpty());
            assertFalse(QuestReward.ofGold(10).isEmpty());
            assertFalse(QuestReward.ofSingleStat(StatType.STRENGTH, 10).isEmpty());
        }

        @Test
        @DisplayName("getStatXP() retorna 0 para stats no presentes")
        void whenStatNotPresent_thenReturnsZero() {
            QuestReward reward = QuestReward.ofSingleStat(StatType.STRENGTH, 50);

            assertEquals(0, reward.getStatXP(StatType.INTELLECT));
        }

        @Test
        @DisplayName("getStatXP() con null lanza IllegalArgumentException")
        void whenGettingStatXPWithNull_thenThrowsException() {
            QuestReward reward = QuestReward.empty();
            assertThrows(IllegalArgumentException.class,
                    () -> reward.getStatXP(null));
        }

        @ParameterizedTest(name = "Stats: {0}, General: {1}, Total: {2}")
        @CsvSource({
                "0,   0,   0",
                "50,  0,   50",
                "0,   100, 100",
                "50,  100, 150",
                "200, 300, 500"
        })
        @DisplayName("getTotalXP() suma stat XP y general XP")
        void whenCalculatingTotalXP_thenSumsAll(int statXP, int generalXP, int expectedTotal) {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, statXP)
                    .setGeneralXP(generalXP)
                    .build();

            assertEquals(expectedTotal, reward.getTotalXP());
        }

        @Test
        @DisplayName("getTotalXP() con múltiples stats suma todas")
        void whenMultipleStats_thenTotalXPSumsAll() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 50)
                    .addStatXP(StatType.INTELLECT, 75)
                    .addStatXP(StatType.WISDOM, 25)
                    .setGeneralXP(100)
                    .build();

            // 50 + 75 + 25 + 100 = 250
            assertEquals(250, reward.getTotalXP());
        }

        @Test
        @DisplayName("hasXP() detecta presencia de cualquier XP")
        void whenCheckingHasXP_thenCorrectResult() {
            assertTrue(QuestReward.ofGeneralXP(10).hasXP());
            assertTrue(QuestReward.ofSingleStat(StatType.STRENGTH, 10).hasXP());
            assertFalse(QuestReward.ofGold(100).hasXP());
            assertFalse(QuestReward.empty().hasXP());
        }

        @Test
        @DisplayName("hasGold() detecta presencia de Gold")
        void whenCheckingHasGold_thenCorrectResult() {
            assertTrue(QuestReward.ofGold(100).hasGold());
            assertFalse(QuestReward.ofGeneralXP(100).hasGold());
            assertFalse(QuestReward.empty().hasGold());
        }
    }

    @Nested
    class Immutability {
        @Test
        @DisplayName("statXP Map es inmutable")
        void statXPMap_isImmutable() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 50)
                    .build();

            assertThrows(UnsupportedOperationException.class,
                    () -> reward.statXP().put(StatType.INTELLECT, 100));
        }

        @Test
        @DisplayName("Modificar Map original no afecta a reward")
        void modifyingOriginalMap_doesNotAffectReward() {
            Map<StatType, Integer> originalMap = new java.util.HashMap<>();
            originalMap.put(StatType.STRENGTH, 50);

            QuestReward reward = new QuestReward(originalMap, 0, 0);
            originalMap.put(StatType.STRENGTH, 999);

            assertEquals(50, reward.getStatXP(StatType.STRENGTH),
                    "Reward no debe verse afectada por cambios en el map original");
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea reward con stats")
        void whenFormattingWithStats_thenCorrectFormat() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 50)
                    .setGeneralXP(100)
                    .setGold(200)
                    .build();

            String display = reward.toDisplayString();

            assertTrue(display.contains("📊"));
            assertTrue(display.contains("💪"));
            assertTrue(display.contains("+50"));
            assertTrue(display.contains("⭐"));
            assertTrue(display.contains("+100"));
            assertTrue(display.contains("💰"));
            assertTrue(display.contains("+200"));
        }

        @Test
        @DisplayName("toDisplayString() para reward solo con Gold")
        void whenFormattingOnlyGold_thenCorrectFormat() {
            QuestReward reward = QuestReward.ofGold(500);
            String display = reward.toDisplayString();

            assertTrue(display.contains("💰"));
            assertTrue(display.contains("+500"));
            assertFalse(display.contains("📊"));
            assertFalse(display.contains("⭐"));
        }

        @Test
        @DisplayName("toString() incluye todos los campos")
        void whenCallingToString_thenIncludesAllFields() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 50)
                    .setGeneralXP(100)
                    .setGold(200)
                    .build();

            String str = reward.toString();

            assertTrue(str.contains("QuestReward"));
            assertTrue(str.contains("statXP"));
            assertTrue(str.contains("generalXP"));
            assertTrue(str.contains("gold"));
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Reward con valores máximos es válida")
        void rewardWithMaxValues_isValid() {
            assertDoesNotThrow(() -> new QuestReward(
                    Map.of(StatType.STRENGTH, Integer.MAX_VALUE),
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE
            ));
        }

        @Test
        @DisplayName("Reward con todos los stats es válida")
        void rewardWithAllStats_isValid() {
            QuestReward reward = QuestReward.builder()
                    .addStatXP(StatType.STRENGTH, 10)
                    .addStatXP(StatType.INTELLECT, 20)
                    .addStatXP(StatType.WISDOM, 30)
                    .addStatXP(StatType.DISCIPLINE, 40)
                    .addStatXP(StatType.CHARISMA, 50)
                    .build();

            assertEquals(150, reward.getTotalXP());
        }
    }
}