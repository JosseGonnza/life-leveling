package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemQuestType - Las Gates (Biblia cap 2.4)")
class SystemQuestTypeTest {

    @Nested
    class Properties {
        @Test
        @DisplayName("Existen 9 gates (7 de ascenso + Vault + Redemption)")
        void thereAreNineGates() {
            assertEquals(9, SystemQuestType.values().length);
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates tienen icono")
        void allGatesHaveIcon(SystemQuestType type) {
            assertNotNull(type.getIcon());
            assertFalse(type.getIcon().isBlank());
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates tienen nombre")
        void allGatesHaveName(SystemQuestType type) {
            assertNotNull(type.getName());
            assertFalse(type.getName().isBlank());
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates tienen descripción")
        void allGatesHaveDescription(SystemQuestType type) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isBlank());
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates de ascenso (no especiales) desbloquean un rango")
        void ascentGatesUnlockRank(SystemQuestType type) {
            if (!type.isSpecialGate()) {
                assertNotNull(type.getRankUnlocked());
            }
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates tienen al menos una condición de victoria")
        void allGatesHaveConditions(SystemQuestType type) {
            assertFalse(type.getConditions().isEmpty(),
                    type + " debe tener condiciones de victoria");
        }
    }

    @Nested
    class Sequence {
        @Test
        @DisplayName("GATE_E_TO_D es la primera gate (sin anterior)")
        void firstGate_hasNoPrevious() {
            assertTrue(SystemQuestType.GATE_E_TO_D.isFirstGate());
            assertFalse(SystemQuestType.GATE_E_TO_D.hasPreviousGate());
            assertNull(SystemQuestType.GATE_E_TO_D.getPreviousGate());
        }

        @Test
        @DisplayName("La cadena de ascenso E→D→C→C+→B→A→S es correcta")
        void ascentChain_isCorrect() {
            assertEquals(SystemQuestType.GATE_E_TO_D,
                    SystemQuestType.GATE_D_TO_C.getPreviousGate());
            assertEquals(SystemQuestType.GATE_D_TO_C,
                    SystemQuestType.GATE_C_TO_C_PLUS.getPreviousGate());
            assertEquals(SystemQuestType.GATE_C_TO_C_PLUS,
                    SystemQuestType.GATE_C_PLUS_TO_B.getPreviousGate());
            assertEquals(SystemQuestType.GATE_C_PLUS_TO_B,
                    SystemQuestType.GATE_B_TO_A.getPreviousGate());
            assertEquals(SystemQuestType.GATE_B_TO_A,
                    SystemQuestType.GATE_A_TO_S.getPreviousGate());
            assertEquals(SystemQuestType.GATE_A_TO_S,
                    SystemQuestType.GATE_ENDGAME.getPreviousGate());
        }

        @Test
        @DisplayName("Las gates especiales (Vault, Redemption) están fuera de la cadena")
        void specialGates_areOffChain() {
            assertTrue(SystemQuestType.GATE_VAULT.isSpecialGate());
            assertTrue(SystemQuestType.GATE_REDEMPTION.isSpecialGate());
            assertFalse(SystemQuestType.GATE_VAULT.hasPreviousGate());
            assertFalse(SystemQuestType.GATE_REDEMPTION.hasPreviousGate());
            assertFalse(SystemQuestType.GATE_VAULT.isFirstGate());
            assertFalse(SystemQuestType.GATE_REDEMPTION.isFirstGate());
        }
    }

    @Nested
    class LevelRequirements {
        @Test
        @DisplayName("Los niveles de la cadena son los de la Biblia")
        void levelRequirements_matchBiblia() {
            assertEquals(10, SystemQuestType.GATE_E_TO_D.getLevelRequirement());
            assertEquals(25, SystemQuestType.GATE_D_TO_C.getLevelRequirement());
            assertEquals(35, SystemQuestType.GATE_C_TO_C_PLUS.getLevelRequirement());
            assertEquals(35, SystemQuestType.GATE_C_PLUS_TO_B.getLevelRequirement());
            assertEquals(50, SystemQuestType.GATE_B_TO_A.getLevelRequirement());
            assertEquals(75, SystemQuestType.GATE_A_TO_S.getLevelRequirement());
            assertEquals(100, SystemQuestType.GATE_ENDGAME.getLevelRequirement());
        }

        @Test
        @DisplayName("The Vault aparece a nivel 40")
        void vault_appearsAtLevel40() {
            assertEquals(40, SystemQuestType.GATE_VAULT.getLevelRequirement());
        }

        @Test
        @DisplayName("GATE_REDEMPTION no tiene requisito de nivel")
        void redemptionGate_hasNoLevelRequirement() {
            assertEquals(0, SystemQuestType.GATE_REDEMPTION.getLevelRequirement());
            assertTrue(SystemQuestType.GATE_REDEMPTION.meetsLevelRequirement(1));
        }

        @Test
        @DisplayName("meetsLevelRequirement() verifica correctamente")
        void whenCheckingLevelRequirement_thenCorrectResult() {
            SystemQuestType gate = SystemQuestType.GATE_D_TO_C;  // Req: 25
            assertFalse(gate.meetsLevelRequirement(24));
            assertTrue(gate.meetsLevelRequirement(25));
            assertTrue(gate.meetsLevelRequirement(30));
        }
    }

    @Nested
    class RankUnlocks {
        @Test
        @DisplayName("La cadena desbloquea D, C, C+, B, A, S")
        void chain_unlocksExpectedRanks() {
            assertEquals(PlayerRank.D, SystemQuestType.GATE_E_TO_D.getRankUnlocked());
            assertEquals(PlayerRank.C, SystemQuestType.GATE_D_TO_C.getRankUnlocked());
            assertEquals(PlayerRank.C_PLUS, SystemQuestType.GATE_C_TO_C_PLUS.getRankUnlocked());
            assertEquals(PlayerRank.B, SystemQuestType.GATE_C_PLUS_TO_B.getRankUnlocked());
            assertEquals(PlayerRank.A, SystemQuestType.GATE_B_TO_A.getRankUnlocked());
            assertEquals(PlayerRank.S, SystemQuestType.GATE_A_TO_S.getRankUnlocked());
        }

        @Test
        @DisplayName("GATE_ENDGAME no asciende más allá de S (Corona cosmética)")
        void endgame_staysAtS() {
            assertEquals(PlayerRank.S, SystemQuestType.GATE_ENDGAME.getRankUnlocked());
            assertTrue(SystemQuestType.GATE_ENDGAME.isEndgame());
        }

        @Test
        @DisplayName("Las gates especiales no promueven rango")
        void specialGates_doNotPromote() {
            assertNull(SystemQuestType.GATE_VAULT.getRankUnlocked());
            assertNull(SystemQuestType.GATE_REDEMPTION.getRankUnlocked());
        }
    }

    @Nested
    class Rewards {
        @Test
        @DisplayName("GATE_E_TO_D da 500 XP / 500 G")
        void firstGate_hasCorrectReward() {
            QuestReward reward = SystemQuestType.GATE_E_TO_D.getBaseReward();
            assertEquals(500, reward.generalXP());
            assertEquals(500, reward.gold());
        }

        @Test
        @DisplayName("GATE_D_TO_C da 1500 XP / 1000 G")
        void secondGate_hasCorrectReward() {
            QuestReward reward = SystemQuestType.GATE_D_TO_C.getBaseReward();
            assertEquals(1_500, reward.generalXP());
            assertEquals(1_000, reward.gold());
        }

        @Test
        @DisplayName("GATE_C_TO_C_PLUS da 1000 INT XP (no general) y 0 G")
        void theoryGate_givesIntellectXP() {
            QuestReward reward = SystemQuestType.GATE_C_TO_C_PLUS.getBaseReward();
            assertEquals(StatType.INTELLECT, SystemQuestType.GATE_C_TO_C_PLUS.getRewardStat());
            assertEquals(1_000, reward.getStatXP(StatType.INTELLECT));
            assertEquals(0, reward.generalXP());
            assertEquals(0, reward.gold());
        }

        @Test
        @DisplayName("GATE_C_PLUS_TO_B da 3000 XP / 3000 G")
        void practiceGate_hasCorrectReward() {
            QuestReward reward = SystemQuestType.GATE_C_PLUS_TO_B.getBaseReward();
            assertEquals(3_000, reward.generalXP());
            assertEquals(3_000, reward.gold());
        }

        @Test
        @DisplayName("The Vault da +10.000 G")
        void vault_givesGold() {
            QuestReward reward = SystemQuestType.GATE_VAULT.getBaseReward();
            assertEquals(10_000, reward.gold());
        }
    }

    @Nested
    class SpecialGates {
        @Test
        @DisplayName("Vault y Redemption son especiales; el resto no")
        void onlyVaultAndRedemption_areSpecial() {
            assertTrue(SystemQuestType.GATE_VAULT.isSpecialGate());
            assertTrue(SystemQuestType.GATE_REDEMPTION.isSpecialGate());
            for (SystemQuestType type : SystemQuestType.values()) {
                if (type != SystemQuestType.GATE_VAULT && type != SystemQuestType.GATE_REDEMPTION) {
                    assertFalse(type.isSpecialGate(), type + " no debería ser especial");
                }
            }
        }

        @Test
        @DisplayName("Solo GATE_ENDGAME es endgame")
        void onlyEndgame_isEndgame() {
            assertTrue(SystemQuestType.GATE_ENDGAME.isEndgame());
            for (SystemQuestType type : SystemQuestType.values()) {
                if (type != SystemQuestType.GATE_ENDGAME) {
                    assertFalse(type.isEndgame());
                }
            }
        }
    }

    @Nested
    class UIFormat {
        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates tienen formato display válido")
        void allGatesHaveValidDisplayFormat(SystemQuestType type) {
            String display = type.toDisplayString();
            assertNotNull(display);
            assertFalse(display.isBlank());
            assertTrue(display.contains(type.getIcon()));
            assertTrue(display.contains(type.getName()));
        }
    }
}
