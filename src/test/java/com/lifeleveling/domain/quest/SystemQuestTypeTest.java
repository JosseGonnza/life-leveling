package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemQuestType - Las 10 Gates Épicas")
class SystemQuestTypeTest {

    @Nested
    class Properties {
        @Test
        @DisplayName("Existen exactamente 10 gates")
        void thereAreExactly10Gates() {
            assertEquals(10, SystemQuestType.values().length);
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
        @DisplayName("Todas las gates desbloquean un rango")
        void allGatesUnlockRank(SystemQuestType type) {
            assertNotNull(type.getRankUnlocked());
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
        @DisplayName("GATE_D_TO_C requiere GATE_E_TO_D anterior")
        void secondGate_requiresFirst() {
            assertFalse(SystemQuestType.GATE_D_TO_C.isFirstGate());
            assertTrue(SystemQuestType.GATE_D_TO_C.hasPreviousGate());
            assertEquals(SystemQuestType.GATE_E_TO_D, SystemQuestType.GATE_D_TO_C.getPreviousGate());
        }

        @Test
        @DisplayName("La secuencia de gates es correcta")
        void gateSequence_isCorrect() {
            assertEquals(SystemQuestType.GATE_E_TO_D,
                    SystemQuestType.GATE_D_TO_C.getPreviousGate());

            assertEquals(SystemQuestType.GATE_D_TO_C,
                    SystemQuestType.GATE_C_TO_B_PHASE_1.getPreviousGate());

            assertEquals(SystemQuestType.GATE_C_TO_B_PHASE_1,
                    SystemQuestType.GATE_C_TO_B_PHASE_2.getPreviousGate());

            assertEquals(SystemQuestType.GATE_C_TO_B_PHASE_2,
                    SystemQuestType.GATE_VAULT.getPreviousGate());

            assertEquals(SystemQuestType.GATE_VAULT,
                    SystemQuestType.GATE_B_TO_A.getPreviousGate());

            assertEquals(SystemQuestType.GATE_B_TO_A,
                    SystemQuestType.GATE_A_TO_S.getPreviousGate());

            assertEquals(SystemQuestType.GATE_A_TO_S,
                    SystemQuestType.GATE_S_TO_S_PLUS.getPreviousGate());

            assertEquals(SystemQuestType.GATE_S_TO_S_PLUS,
                    SystemQuestType.GATE_ENDGAME.getPreviousGate());
        }

        @Test
        @DisplayName("GATE_REDEMPTION no tiene gate anterior (especial)")
        void redemptionGate_hasNoPrevious() {
            assertTrue(SystemQuestType.GATE_REDEMPTION.isSpecialGate());
            assertFalse(SystemQuestType.GATE_REDEMPTION.hasPreviousGate());
        }
    }

    @Nested
    class LevelRequirements {
        @Test
        @DisplayName("Los niveles requeridos son progresivos")
        void levelRequirements_areProgressive() {
            assertEquals(10, SystemQuestType.GATE_E_TO_D.getLevelRequirement());
            assertEquals(25, SystemQuestType.GATE_D_TO_C.getLevelRequirement());
            assertEquals(35, SystemQuestType.GATE_C_TO_B_PHASE_1.getLevelRequirement());
            assertEquals(35, SystemQuestType.GATE_C_TO_B_PHASE_2.getLevelRequirement());
            assertEquals(40, SystemQuestType.GATE_VAULT.getLevelRequirement());
            assertEquals(50, SystemQuestType.GATE_B_TO_A.getLevelRequirement());
            assertEquals(60, SystemQuestType.GATE_A_TO_S.getLevelRequirement());
            assertEquals(75, SystemQuestType.GATE_S_TO_S_PLUS.getLevelRequirement());
            assertEquals(100, SystemQuestType.GATE_ENDGAME.getLevelRequirement());
        }

        @Test
        @DisplayName("meetsLevelRequirement() verifica correctamente")
        void whenCheckingLevelRequirement_thenCorrectResult() {
            SystemQuestType gate = SystemQuestType.GATE_D_TO_C;  // Req: 25

            assertFalse(gate.meetsLevelRequirement(24));
            assertTrue(gate.meetsLevelRequirement(25));
            assertTrue(gate.meetsLevelRequirement(30));
        }

        @Test
        @DisplayName("GATE_REDEMPTION no tiene requisito de nivel")
        void redemptionGate_hasNoLevelRequirement() {
            assertEquals(0, SystemQuestType.GATE_REDEMPTION.getLevelRequirement());
            assertTrue(SystemQuestType.GATE_REDEMPTION.meetsLevelRequirement(1));
        }
    }

    @Nested
    class RankUnlocks {
        @Test
        @DisplayName("GATE_E_TO_D desbloquea Rango D")
        void firstGate_unlocksRankD() {
            assertEquals(PlayerRank.D, SystemQuestType.GATE_E_TO_D.getRankUnlocked());
        }

        @Test
        @DisplayName("GATE_D_TO_C desbloquea Rango C")
        void secondGate_unlocksRankC() {
            assertEquals(PlayerRank.C, SystemQuestType.GATE_D_TO_C.getRankUnlocked());
        }

        @Test
        @DisplayName("GATE_C_TO_B_PHASE_2 desbloquea Rango B")
        void phase2Gate_unlocksRankB() {
            assertEquals(PlayerRank.B, SystemQuestType.GATE_C_TO_B_PHASE_2.getRankUnlocked());
        }

        @Test
        @DisplayName("GATE_A_TO_S desbloquea Rango S (Legendario)")
        void bossGate_unlocksRankS() {
            assertEquals(PlayerRank.S, SystemQuestType.GATE_A_TO_S.getRankUnlocked());
        }

        @Test
        @DisplayName("GATE_ENDGAME desbloquea Rango S++")
        void endgameGate_unlocksRankSPlusPlus() {
            assertEquals(PlayerRank.S_PLUS_PLUS, SystemQuestType.GATE_ENDGAME.getRankUnlocked());
        }
    }

    @Nested
    class Rewards {
        @Test
        @DisplayName("GATE_E_TO_D da recompensa correcta")
        void firstGate_hasCorrectReward() {
            QuestReward reward = SystemQuestType.GATE_E_TO_D.getBaseReward();

            assertEquals(500, reward.generalXP());
            assertEquals(1_000, reward.gold());
        }

        @Test
        @DisplayName("GATE_A_TO_S (Jefe Final) da recompensa masiva")
        void bossGate_hasMassiveReward() {
            QuestReward reward = SystemQuestType.GATE_A_TO_S.getBaseReward();

            assertEquals(20_000, reward.generalXP());
            assertEquals(50_000, reward.gold());
        }

        @Test
        @DisplayName("GATE_ENDGAME da la mayor recompensa")
        void endgameGate_hasMaxReward() {
            QuestReward reward = SystemQuestType.GATE_ENDGAME.getBaseReward();

            assertEquals(100_000, reward.generalXP());
            assertEquals(500_000, reward.gold());
        }

        @Test
        @DisplayName("GATE_C_TO_B_PHASE_1 no da Gold (solo XP)")
        void phase1Gate_givesNoGold() {
            QuestReward reward = SystemQuestType.GATE_C_TO_B_PHASE_1.getBaseReward();

            assertEquals(1_000, reward.generalXP());
            assertEquals(0, reward.gold());
        }

        @Test
        @DisplayName("Todas las gates dan al menos XP o Gold")
        void allGates_giveRewards() {
            for (SystemQuestType type : SystemQuestType.values()) {
                QuestReward reward = type.getBaseReward();
                assertTrue(reward.generalXP() > 0 || reward.gold() > 0,
                        type + " debe dar al menos XP o Gold");
            }
        }
    }

    @Nested
    class SpecialGates {
        @Test
        @DisplayName("Solo GATE_REDEMPTION es especial")
        void onlyRedemption_isSpecial() {
            assertTrue(SystemQuestType.GATE_REDEMPTION.isSpecialGate());

            for (SystemQuestType type : SystemQuestType.values()) {
                if (type != SystemQuestType.GATE_REDEMPTION) {
                    assertFalse(type.isSpecialGate());
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
        @Test
        @DisplayName("toDisplayString() formatea con icono y nombre")
        void whenFormattingDisplay_thenIncludesIconAndName() {
            String display = SystemQuestType.GATE_A_TO_S.toDisplayString();

            assertTrue(display.contains("🟡"));
            assertTrue(display.contains("Cambio de Clase: Junior Developer"));
        }

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

    @Nested
    class ProgressionLogic {
        @Test
        @DisplayName("No se puede saltarse gates en la secuencia")
        void gatesFormUnbrokenChain() {
            // Verificar que cada gate (excepto especiales) tiene anterior o es la primera
            for (SystemQuestType type : SystemQuestType.values()) {
                if (type.isSpecialGate()) {
                    continue;  // Skip REDEMPTION
                }

                if (type == SystemQuestType.GATE_E_TO_D) {
                    assertTrue(type.isFirstGate());
                } else {
                    assertTrue(type.hasPreviousGate(),
                            type + " debe tener gate anterior");
                }
            }
        }

        @Test
        @DisplayName("La progresión de niveles es razonable")
        void levelProgression_isReasonable() {
            SystemQuestType[] mainGates = {
                    SystemQuestType.GATE_E_TO_D,
                    SystemQuestType.GATE_D_TO_C,
                    SystemQuestType.GATE_C_TO_B_PHASE_2,
                    SystemQuestType.GATE_B_TO_A,
                    SystemQuestType.GATE_A_TO_S,
                    SystemQuestType.GATE_S_TO_S_PLUS,
                    SystemQuestType.GATE_ENDGAME
            };

            // Verificar que cada gate requiere más nivel que la anterior
            for (int i = 1; i < mainGates.length; i++) {
                int prevLevel = mainGates[i-1].getLevelRequirement();
                int currLevel = mainGates[i].getLevelRequirement();

                assertTrue(currLevel >= prevLevel,
                        String.format("%s (Lvl %d) debe requerir >= nivel que %s (Lvl %d)",
                                mainGates[i], currLevel, mainGates[i-1], prevLevel));
            }
        }
    }
}