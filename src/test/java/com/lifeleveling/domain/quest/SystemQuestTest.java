package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.quest.shared.QuestId;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.shared.QuestStatus;
import com.lifeleveling.domain.quest.system.SystemQuest;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemQuest - Gates Épicas de Ascenso")
class SystemQuestTest {

    @Nested
    class Creation {
        @Test
        @DisplayName("create() crea SystemQuest válida")
        void whenCreating_thenValidQuest() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            assertNotNull(gate.id());
            assertEquals(SystemQuestType.GATE_E_TO_D, gate.getType());
            assertEquals(QuestStatus.PENDING, gate.status());
            assertNotNull(gate.createdAt());
            assertNull(gate.getCompletedAt());
        }

        @Test
        @DisplayName("create() con tipo null lanza IllegalArgumentException")
        void whenCreatingWithNullType_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> SystemQuest.create(null));
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("create() funciona con todos los tipos de gate")
        void whenCreatingWithAnyType_thenValid(SystemQuestType type) {
            assertDoesNotThrow(() -> SystemQuest.create(type));
        }
    }

    @Nested
    class QuestInterface {
        @Test
        @DisplayName("name() retorna el nombre del tipo")
        void name_returnsTypeName() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_A_TO_S);
            assertEquals("Cambio de Clase: Junior Developer", gate.name());
        }

        @Test
        @DisplayName("rank() retorna el rango que desbloquea")
        void rank_returnsUnlockedRank() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_A_TO_S);
            assertEquals(QuestRank.S, gate.rank());
        }

        @Test
        @DisplayName("reward() está vacía si no está completada")
        void whenPending_thenNoReward() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);
            assertTrue(gate.reward().isEmpty());
        }

        @Test
        @DisplayName("reward() retorna recompensa si está completada")
        void whenCompleted_thenHasReward() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);
            gate = gate.complete(Instant.now());

            QuestReward reward = gate.reward();
            assertEquals(1_500, reward.generalXP());
            assertEquals(2_000, reward.gold());
        }
    }

    @Nested
    class StateTransitions {
        @Test
        @DisplayName("complete() marca gate como COMPLETED")
        void whenCompleting_thenStatusIsCompleted() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            Instant completedAt = Instant.now();

            SystemQuest completed = gate.complete(completedAt);

            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertEquals(completedAt, completed.getCompletedAt());
        }

        @Test
        @DisplayName("complete() no modifica la gate original")
        void whenCompleting_thenOriginalUnchanged() {
            SystemQuest original = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            SystemQuest completed = original.complete(Instant.now());

            assertEquals(QuestStatus.PENDING, original.status());
            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertNotSame(original, completed);
        }

        @Test
        @DisplayName("complete() con null lanza IllegalArgumentException")
        void whenCompletingWithNull_thenThrowsException() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            assertThrows(IllegalArgumentException.class,
                    () -> gate.complete(null));
        }

        @Test
        @DisplayName("complete() desde estado COMPLETED lanza IllegalStateException")
        void whenCompletingAlreadyCompleted_thenThrowsException() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            SystemQuest completed = gate.complete(Instant.now());

            assertThrows(IllegalStateException.class,
                    () -> completed.complete(Instant.now()));
        }

        @Test
        @DisplayName("fail() lanza UnsupportedOperationException")
        void whenFailing_thenThrowsUnsupportedException() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                    () -> gate.fail(Instant.now())
            );

            assertTrue(exception.getMessage().contains("no pueden fallar"));
        }

        @Test
        @DisplayName("start() cambia estado a IN_PROGRESS")
        void whenStarting_thenStatusIsInProgress() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            SystemQuest started = gate.start();

            assertEquals(QuestStatus.IN_PROGRESS, started.status());
        }

        @Test
        @DisplayName("start() desde IN_PROGRESS lanza IllegalStateException")
        void whenStartingAlreadyStarted_thenThrowsException() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            SystemQuest started = gate.start();

            assertThrows(IllegalStateException.class,
                    () -> started.start());
        }
    }

    @Nested
    class Requirements {
        @Test
        @DisplayName("getLevelRequirement() retorna nivel requerido")
        void getLevelRequirement_returnsCorrectLevel() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);
            assertEquals(25, gate.getLevelRequirement());
        }

        @Test
        @DisplayName("getPreviousGate() retorna gate anterior requerida")
        void getPreviousGate_returnsRequiredGate() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);
            assertEquals(SystemQuestType.GATE_E_TO_D, gate.getPreviousGate());
        }

        @Test
        @DisplayName("isFirstGate() detecta la primera gate")
        void isFirstGate_detectsFirstGate() {
            SystemQuest firstGate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            SystemQuest secondGate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);

            assertTrue(firstGate.isFirstGate());
            assertFalse(secondGate.isFirstGate());
        }

        @Test
        @DisplayName("requiresPreviousGate() detecta requisito de gate anterior")
        void requiresPreviousGate_detectsRequirement() {
            SystemQuest firstGate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            SystemQuest secondGate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);

            assertFalse(firstGate.requiresPreviousGate());
            assertTrue(secondGate.requiresPreviousGate());
        }

        @Test
        @DisplayName("meetsLevelRequirement() verifica nivel del jugador")
        void meetsLevelRequirement_checksPlayerLevel() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);  // Req: 25

            assertFalse(gate.meetsLevelRequirement(24));
            assertTrue(gate.meetsLevelRequirement(25));
            assertTrue(gate.meetsLevelRequirement(30));
        }
    }

    @Nested
    class Availability {
        @Test
        @DisplayName("isAvailableFor() retorna true si cumple todos los requisitos")
        void whenMeetsAllRequirements_thenIsAvailable() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);

            // Nivel 25+, gate anterior completada
            assertTrue(gate.isAvailableFor(25, true));
            assertTrue(gate.isAvailableFor(30, true));
        }

        @Test
        @DisplayName("isAvailableFor() retorna false si nivel insuficiente")
        void whenLevelInsufficient_thenNotAvailable() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);  // Req: 25

            assertFalse(gate.isAvailableFor(24, true));
        }

        @Test
        @DisplayName("isAvailableFor() retorna false si gate anterior no completada")
        void whenPreviousGateNotCompleted_thenNotAvailable() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);

            assertFalse(gate.isAvailableFor(30, false));
        }

        @Test
        @DisplayName("isAvailableFor() retorna false si ya está completada")
        void whenAlreadyCompleted_thenNotAvailable() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_D_TO_C);
            gate = gate.complete(Instant.now());

            assertFalse(gate.isAvailableFor(30, true));
        }

        @Test
        @DisplayName("Primera gate no requiere gate anterior")
        void firstGate_doesNotRequirePrevious() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            // Disponible con nivel suficiente, sin necesidad de gate anterior
            assertTrue(gate.isAvailableFor(10, false));
        }
    }

    @Nested
    class SpecialGates {
        @Test
        @DisplayName("isSpecialGate() detecta GATE_REDEMPTION")
        void redemptionGate_isSpecial() {
            SystemQuest redemption = SystemQuest.create(SystemQuestType.GATE_REDEMPTION);
            SystemQuest normal = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            assertTrue(redemption.isSpecialGate());
            assertFalse(normal.isSpecialGate());
        }

        @Test
        @DisplayName("isEndgame() detecta GATE_ENDGAME")
        void endgameGate_isEndgame() {
            SystemQuest endgame = SystemQuest.create(SystemQuestType.GATE_ENDGAME);
            SystemQuest normal = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            assertTrue(endgame.isEndgame());
            assertFalse(normal.isEndgame());
        }

        @Test
        @DisplayName("GATE_REDEMPTION está disponible sin requisito de nivel")
        void redemptionGate_hasNoLevelRequirement() {
            SystemQuest redemption = SystemQuest.create(SystemQuestType.GATE_REDEMPTION);

            assertTrue(redemption.meetsLevelRequirement(1));
            assertTrue(redemption.isAvailableFor(1, false));
        }
    }

    @Nested
    class RankUnlocks {
        @Test
        @DisplayName("getRankUnlocked() retorna el rango correcto")
        void getRankUnlocked_returnsCorrectRank() {
            SystemQuest gateToS = SystemQuest.create(SystemQuestType.GATE_A_TO_S);
            assertEquals(QuestRank.S, gateToS.getRankUnlocked());
        }

        @ParameterizedTest
        @EnumSource(SystemQuestType.class)
        @DisplayName("Todas las gates desbloquean un rango")
        void allGates_unlockRank(SystemQuestType type) {
            SystemQuest gate = SystemQuest.create(type);
            assertNotNull(gate.getRankUnlocked());
        }
    }

    @Nested
    class Reconstitution {
        @Test
        @DisplayName("reconstitute() crea SystemQuest desde datos externos")
        void whenReconstituting_thenValidQuest() {
            QuestId id = QuestId.generate();
            Instant now = Instant.now();

            SystemQuest gate = SystemQuest.reconstitute(
                    id,
                    SystemQuestType.GATE_A_TO_S,
                    QuestStatus.COMPLETED,
                    now,
                    now
            );

            assertEquals(id, gate.id());
            assertEquals(SystemQuestType.GATE_A_TO_S, gate.getType());
            assertEquals(QuestStatus.COMPLETED, gate.status());
        }

        @Test
        @DisplayName("reconstitute() no permite estados FAILED")
        void whenReconstitutingWithFailed_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> SystemQuest.reconstitute(
                            QuestId.generate(),
                            SystemQuestType.GATE_E_TO_D,
                            QuestStatus.FAILED,
                            Instant.now(),
                            null
                    ));
        }

        @Test
        @DisplayName("reconstitute() no permite estados EXPIRED")
        void whenReconstitutingWithExpired_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> SystemQuest.reconstitute(
                            QuestId.generate(),
                            SystemQuestType.GATE_E_TO_D,
                            QuestStatus.EXPIRED,
                            Instant.now(),
                            null
                    ));
        }

        @Test
        @DisplayName("reconstitute() valida coherencia: COMPLETED requiere timestamp")
        void whenReconstitutingCompletedWithoutTimestamp_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> SystemQuest.reconstitute(
                            QuestId.generate(),
                            SystemQuestType.GATE_E_TO_D,
                            QuestStatus.COMPLETED,
                            Instant.now(),
                            null  // ← COMPLETED pero sin completedAt
                    ));
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea gate con todos los elementos")
        void whenFormattingDisplay_thenIncludesAllInfo() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_A_TO_S);

            String display = gate.toDisplayString();

            assertTrue(display.contains("🟡"));
            assertTrue(display.contains("[S]"));
            assertTrue(display.contains("Cambio de Clase: Junior Developer"));
            assertTrue(display.contains("Lvl 60"));
            assertTrue(display.contains("Pendiente"));
        }

        @Test
        @DisplayName("formatReward() formatea recompensas correctamente")
        void whenFormattingReward_thenCorrectFormat() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_A_TO_S);

            String reward = gate.formatReward();

            assertTrue(reward.contains("⭐"));
            assertTrue(reward.contains("20"));
            assertTrue(reward.contains("000 XP"));
            assertTrue(reward.contains("💰"));
            assertTrue(reward.contains("50"));
            assertTrue(reward.contains("000 G"));
            assertTrue(reward.contains("🔓 Rango S"));
        }

        @Test
        @DisplayName("formatReward() maneja gates sin Gold")
        void whenFormattingRewardWithoutGold_thenCorrectFormat() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_C_TO_B_PHASE_1);

            String reward = gate.formatReward();

            assertTrue(reward.contains("XP"));
            assertFalse(reward.contains("G"));
            assertTrue(reward.contains("🔓 Rango"));
        }

        @Test
        @DisplayName("toString() incluye campos clave")
        void whenCallingToString_thenIncludesKeyFields() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            String str = gate.toString();

            assertTrue(str.contains("SystemQuest"));
            assertTrue(str.contains("GATE_E_TO_D"));
            assertTrue(str.contains("PENDING"));
        }
    }

    @Nested
    class Equality {
        @Test
        @DisplayName("SystemQuests con mismo ID son equals()")
        void questsWithSameId_areEqual() {
            QuestId id = QuestId.generate();

            SystemQuest gate1 = SystemQuest.reconstitute(
                    id, SystemQuestType.GATE_E_TO_D,
                    QuestStatus.PENDING, Instant.now(), null
            );

            SystemQuest gate2 = SystemQuest.reconstitute(
                    id, SystemQuestType.GATE_D_TO_C,
                    QuestStatus.COMPLETED, Instant.now(), Instant.now()
            );

            assertEquals(gate1, gate2);
            assertEquals(gate1.hashCode(), gate2.hashCode());
        }

        @Test
        @DisplayName("SystemQuests con diferente ID NO son equals()")
        void questsWithDifferentId_areNotEqual() {
            SystemQuest gate1 = SystemQuest.create(SystemQuestType.GATE_E_TO_D);
            SystemQuest gate2 = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            assertNotEquals(gate1, gate2);
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Flujo completo: PENDING → IN_PROGRESS → COMPLETED")
        void completeGateFlow_works() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            // Start
            gate = gate.start();
            assertEquals(QuestStatus.IN_PROGRESS, gate.status());

            // Complete
            gate = gate.complete(Instant.now());
            assertEquals(QuestStatus.COMPLETED, gate.status());
        }

        @Test
        @DisplayName("Gate puede completarse directamente sin start()")
        void gateCanCompleteDirectly_withoutStart() {
            SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_E_TO_D);

            assertDoesNotThrow(() -> gate.complete(Instant.now()));
        }

        @Test
        @DisplayName("Gates masivas (A→S, ENDGAME) tienen recompensas épicas")
        void massiveGates_haveEpicRewards() {
            SystemQuest bossGate = SystemQuest.create(SystemQuestType.GATE_A_TO_S);
            bossGate = bossGate.complete(Instant.now());

            QuestReward reward = bossGate.reward();
            assertTrue(reward.generalXP() >= 20_000);
            assertTrue(reward.gold() >= 50_000);
        }
    }
}