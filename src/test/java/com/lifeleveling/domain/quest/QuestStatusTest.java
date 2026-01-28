package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.quest.shared.QuestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuestStatus - Estados del Ciclo de Vida de Quest")
class QuestStatusTest {

    @Nested
    class Properties {
        @Test
        @DisplayName("Existen exactamente 5 estados")
        void thereAreExactly5States() {
            assertEquals(5, QuestStatus.values().length);
        }

        @ParameterizedTest
        @EnumSource(QuestStatus.class)
        @DisplayName("Todos los estados tienen icono")
        void allStatesHaveIcon(QuestStatus status) {
            assertNotNull(status.getIcon());
            assertFalse(status.getIcon().isBlank());
        }

        @ParameterizedTest
        @EnumSource(QuestStatus.class)
        @DisplayName("Todos los estados tienen nombre display")
        void allStatesHaveDisplayName(QuestStatus status) {
            assertNotNull(status.getDisplayName());
            assertFalse(status.getDisplayName().isBlank());
        }
    }

    @Nested
    class StateQueries {
        @ParameterizedTest(name = "{0}: isActive = {1}")
        @CsvSource({
                "PENDING, true",
                "IN_PROGRESS, true",
                "COMPLETED, false",
                "FAILED, false",
                "EXPIRED, false"
        })
        @DisplayName("isActive() detecta estados activos correctamente")
        void whenCheckingActive_thenCorrectResult(QuestStatus status, boolean expectedActive) {
            assertEquals(expectedActive, status.isActive());
        }

        @ParameterizedTest(name = "{0}: isTerminal = {1}")
        @CsvSource({
                "PENDING, false",
                "IN_PROGRESS, false",
                "COMPLETED, true",
                "FAILED, true",
                "EXPIRED, true"
        })
        @DisplayName("isTerminal() detecta estados terminales correctamente")
        void whenCheckingTerminal_thenCorrectResult(QuestStatus status, boolean expectedTerminal) {
            assertEquals(expectedTerminal, status.isTerminal());
        }

        @Test
        @DisplayName("isSuccessful() solo es true para COMPLETED")
        void onlyCompleted_isSuccessful() {
            assertTrue(QuestStatus.COMPLETED.isSuccessful());

            assertFalse(QuestStatus.PENDING.isSuccessful());
            assertFalse(QuestStatus.IN_PROGRESS.isSuccessful());
            assertFalse(QuestStatus.FAILED.isSuccessful());
            assertFalse(QuestStatus.EXPIRED.isSuccessful());
        }

        @ParameterizedTest(name = "{0}: isUnsuccessful = {1}")
        @CsvSource({
                "PENDING, false",
                "IN_PROGRESS, false",
                "COMPLETED, false",
                "FAILED, true",
                "EXPIRED, true"
        })
        @DisplayName("isUnsuccessful() detecta fallos correctamente")
        void whenCheckingUnsuccessful_thenCorrectResult(QuestStatus status, boolean expectedUnsuccessful) {
            assertEquals(expectedUnsuccessful, status.isUnsuccessful());
        }
    }

    @Nested
    class StateTransitions {
        @ParameterizedTest(name = "PENDING → {0} = {1}")
        @CsvSource({
                "PENDING, false",
                "IN_PROGRESS, true",
                "COMPLETED, true",
                "FAILED, true",
                "EXPIRED, true"
        })
        @DisplayName("PENDING puede transicionar a estados válidos")
        void pendingCanTransitionTo(QuestStatus target, boolean canTransition) {
            assertEquals(canTransition, QuestStatus.PENDING.canTransitionTo(target));
        }

        @ParameterizedTest(name = "IN_PROGRESS → {0} = {1}")
        @CsvSource({
                "PENDING, false",
                "IN_PROGRESS, false",
                "COMPLETED, true",
                "FAILED, true",
                "EXPIRED, true"
        })
        @DisplayName("IN_PROGRESS puede transicionar a estados válidos")
        void inProgressCanTransitionTo(QuestStatus target, boolean canTransition) {
            assertEquals(canTransition, QuestStatus.IN_PROGRESS.canTransitionTo(target));
        }

        @ParameterizedTest
        @EnumSource(QuestStatus.class)
        @DisplayName("COMPLETED NO puede transicionar (estado terminal)")
        void completed_cannotTransition(QuestStatus target) {
            assertFalse(QuestStatus.COMPLETED.canTransitionTo(target));
        }

        @ParameterizedTest
        @EnumSource(QuestStatus.class)
        @DisplayName("FAILED NO puede transicionar (estado terminal)")
        void failed_cannotTransition(QuestStatus target) {
            assertFalse(QuestStatus.FAILED.canTransitionTo(target));
        }

        @ParameterizedTest
        @EnumSource(QuestStatus.class)
        @DisplayName("EXPIRED NO puede transicionar (estado terminal)")
        void expired_cannotTransition(QuestStatus target) {
            assertFalse(QuestStatus.EXPIRED.canTransitionTo(target));
        }

        @Test
        @DisplayName("canTransitionTo() con null lanza IllegalArgumentException")
        void whenTransitioningToNull_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> QuestStatus.PENDING.canTransitionTo(null));
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea con icono y nombre")
        void whenFormattingDisplay_thenIncludesIconAndName() {
            String display = QuestStatus.PENDING.toDisplayString();

            assertTrue(display.contains("📋"));
            assertTrue(display.contains("Pendiente"));
        }

        @ParameterizedTest
        @EnumSource(QuestStatus.class)
        @DisplayName("Todos los estados tienen formato display válido")
        void allStatesHaveValidDisplayFormat(QuestStatus status) {
            String display = status.toDisplayString();

            assertNotNull(display);
            assertFalse(display.isBlank());
            assertTrue(display.contains(status.getIcon()));
            assertTrue(display.contains(status.getDisplayName()));
        }
    }

    @Nested
    class StateTransitionValidation {
        @Test
        @DisplayName("Estados terminales son mutuamente excluyentes con activos")
        void terminalAndActive_areMutuallyExclusive() {
            for (QuestStatus status : QuestStatus.values()) {
                if (status.isTerminal()) {
                    assertFalse(status.isActive(),
                            status + " no puede ser terminal Y activo");
                }
            }
        }

        @Test
        @DisplayName("COMPLETED y estados fallidos son mutuamente excluyentes")
        void successfulAndUnsuccessful_areMutuallyExclusive() {
            for (QuestStatus status : QuestStatus.values()) {
                if (status.isSuccessful()) {
                    assertFalse(status.isUnsuccessful(),
                            status + " no puede ser exitoso Y fallido");
                }
            }
        }
    }
}