package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.quest.shared.QuestId;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.shared.QuestStatus;
import com.lifeleveling.domain.quest.user.UserQuest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserQuest - Tareas Personalizadas del Usuario")
class UserQuestTest {

    @Nested
    class Creation {
        @Test
        @DisplayName("create() crea UserQuest válida con deadline")
        void whenCreating_thenValidQuest() {
            LocalDate deadline = LocalDate.now().plusDays(7);

            UserQuest quest = UserQuest.create(
                    "Terminar curso Spring Boot",
                    "Completar módulos 1-5 con ejercicios",
                    QuestRank.C,
                    deadline
            );

            assertNotNull(quest.id());
            assertEquals("Terminar curso Spring Boot", quest.name());
            assertEquals("Completar módulos 1-5 con ejercicios", quest.description());
            assertEquals(QuestRank.C, quest.rank());
            assertEquals(deadline, quest.getDeadline());
            assertEquals(QuestStatus.PENDING, quest.status());
            assertNotNull(quest.createdAt());
            assertTrue(quest.hasDeadline());
        }

        @Test
        @DisplayName("createWithoutDeadline() crea quest sin deadline")
        void whenCreatingWithoutDeadline_thenNoDeadline() {
            UserQuest quest = UserQuest.createWithoutDeadline(
                    "Leer libro de Clean Code",
                    "Leer completo y hacer anotaciones",
                    QuestRank.B
            );

            assertNotNull(quest.id());
            assertFalse(quest.hasDeadline());
            assertNull(quest.getDeadline());
            assertEquals(QuestStatus.PENDING, quest.status());
        }

        @Test
        @DisplayName("create() con nombre null lanza IllegalArgumentException")
        void whenCreatingWithNullName_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserQuest.create(null, "Description", QuestRank.D, LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("create() con nombre vacío lanza IllegalArgumentException")
        void whenCreatingWithBlankName_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserQuest.create("   ", "Description", QuestRank.D, LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("create() con descripción null lanza IllegalArgumentException")
        void whenCreatingWithNullDescription_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserQuest.create("Name", null, QuestRank.D, LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("create() con descripción vacía lanza IllegalArgumentException")
        void whenCreatingWithBlankDescription_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserQuest.create("Name", "   ", QuestRank.D, LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("create() con rango null lanza IllegalArgumentException")
        void whenCreatingWithNullRank_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserQuest.create("Name", "Desc", null, LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("create() con deadline en el pasado lanza IllegalArgumentException")
        void whenCreatingWithPastDeadline_thenThrowsException() {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> UserQuest.create("Name", "Desc", QuestRank.C, yesterday)
            );

            assertTrue(exception.getMessage().contains("pasado"));
        }

        @Test
        @DisplayName("create() con deadline hoy es válida")
        void whenCreatingWithTodayDeadline_thenValid() {
            assertDoesNotThrow(() ->
                    UserQuest.create("Name", "Desc", QuestRank.C, LocalDate.now())
            );
        }

        @Test
        @DisplayName("create() trimea nombre y descripción")
        void whenCreatingWithWhitespace_thenTrims() {
            UserQuest quest = UserQuest.create(
                    "  Name with spaces  ",
                    "  Description with spaces  ",
                    QuestRank.D,
                    LocalDate.now().plusDays(1)
            );

            assertEquals("Name with spaces", quest.name());
            assertEquals("Description with spaces", quest.description());
        }
    }

    @Nested
    class Rewards {
        @ParameterizedTest(name = "Rango {0}: XP={1}, Gold={2}")
        @CsvSource({
                "E,    10,    15",
                "D,    50,    50",
                "C,    150,   150",
                "B,    500,   400",
                "A,    1500,  2000",
                "S,    5000,  10000"
        })
        @DisplayName("reward() retorna valores del rango asignado")
        void whenGettingReward_thenMatchesRank(QuestRank rank, int expectedXP, int expectedGold) {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", rank);
            QuestReward reward = quest.reward();

            assertEquals(expectedXP, reward.generalXP());
            assertEquals(expectedGold, reward.gold());
        }

        @Test
        @DisplayName("UserQuests no dan Stat XP, solo General XP")
        void whenGettingReward_thenNoStatXP() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.C);
            QuestReward reward = quest.reward();

            assertTrue(reward.statXP().isEmpty(), "UserQuests no deben dar Stat XP directa");
        }
    }

    @Nested
    class StateTransitions {
        @Test
        @DisplayName("complete() marca quest como COMPLETED")
        void whenCompleting_thenStatusIsCompleted() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));
            Instant completedAt = Instant.now();

            UserQuest completed = quest.complete(completedAt);

            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertEquals(completedAt, completed.getCompletedAt());
            assertNull(completed.getFailedAt());
        }

        @Test
        @DisplayName("complete() no modifica la quest original (inmutabilidad)")
        void whenCompleting_thenOriginalUnchanged() {
            UserQuest original = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));

            UserQuest completed = original.complete(Instant.now());

            assertEquals(QuestStatus.PENDING, original.status(), "Original no debe cambiar");
            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertNotSame(original, completed);
        }

        @Test
        @DisplayName("complete() con null lanza IllegalArgumentException")
        void whenCompletingWithNull_thenThrowsException() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> quest.complete(null));
        }

        @Test
        @DisplayName("complete() desde estado COMPLETED lanza IllegalStateException")
        void whenCompletingAlreadyCompleted_thenThrowsException() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));
            UserQuest completed = quest.complete(Instant.now());

            assertThrows(IllegalStateException.class,
                    () -> completed.complete(Instant.now()));
        }

        @Test
        @DisplayName("fail() marca quest como FAILED")
        void whenFailing_thenStatusIsFailed() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));
            Instant failedAt = Instant.now();

            UserQuest failed = quest.fail(failedAt);

            assertEquals(QuestStatus.FAILED, failed.status());
            assertEquals(failedAt, failed.getFailedAt());
            assertNull(failed.getCompletedAt());
        }

        @Test
        @DisplayName("fail() con null lanza IllegalArgumentException")
        void whenFailingWithNull_thenThrowsException() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> quest.fail(null));
        }

        @Test
        @DisplayName("expire() marca quest como EXPIRED")
        void whenExpiring_thenStatusIsExpired() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));
            Instant expiredAt = Instant.now();

            UserQuest expired = quest.expire(expiredAt);

            assertEquals(QuestStatus.EXPIRED, expired.status());
            assertEquals(expiredAt, expired.getFailedAt());
            assertNull(expired.getCompletedAt());
        }

        @Test
        @DisplayName("expire() sin deadline lanza IllegalStateException")
        void whenExpiringWithoutDeadline_thenThrowsException() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.C);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> quest.expire(Instant.now())
            );

            assertTrue(exception.getMessage().contains("sin deadline"));
        }

        @Test
        @DisplayName("expire() con null lanza IllegalArgumentException")
        void whenExpiringWithNull_thenThrowsException() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> quest.expire(null));
        }

        @Test
        @DisplayName("start() cambia estado a IN_PROGRESS")
        void whenStarting_thenStatusIsInProgress() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));

            UserQuest started = quest.start();

            assertEquals(QuestStatus.IN_PROGRESS, started.status());
        }

        @Test
        @DisplayName("start() desde IN_PROGRESS lanza IllegalStateException")
        void whenStartingAlreadyStarted_thenThrowsException() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.D, LocalDate.now().plusDays(1));
            UserQuest started = quest.start();

            assertThrows(IllegalStateException.class,
                    () -> started.start());
        }
    }

    @Nested
    class DeadlineManagement {
        @Test
        @DisplayName("hasDeadline() retorna true si tiene deadline")
        void whenQuestHasDeadline_thenHasDeadlineIsTrue() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(7));
            assertTrue(quest.hasDeadline());
        }

        @Test
        @DisplayName("hasDeadline() retorna false si no tiene deadline")
        void whenQuestHasNoDeadline_thenHasDeadlineIsFalse() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.C);
            assertFalse(quest.hasDeadline());
        }

        @Test
        @DisplayName("isExpired() retorna false antes de la deadline")
        void whenBeforeDeadline_thenNotExpired() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(7));

            assertFalse(quest.isExpired(LocalDate.now()));
            assertFalse(quest.isExpired(LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("isExpired() retorna true después de la deadline")
        void whenAfterDeadline_thenExpired() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            assertTrue(quest.isExpired(LocalDate.now().plusDays(2)));
        }

        @Test
        @DisplayName("isExpired() retorna false en la deadline exacta")
        void whenExactlyAtDeadline_thenNotExpired() {
            LocalDate deadline = LocalDate.now().plusDays(7);
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, deadline);

            assertFalse(quest.isExpired(deadline), "En la deadline todavía se puede completar");
        }

        @Test
        @DisplayName("isExpired() retorna false si no tiene deadline")
        void whenNoDeadline_thenNeverExpired() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.C);

            assertFalse(quest.isExpired(LocalDate.now()));
            assertFalse(quest.isExpired(LocalDate.now().plusYears(100)));
        }

        @ParameterizedTest(name = "Deadline en +{0} días: {1} días restantes")
        @CsvSource({
                "7,  7",
                "1,  1",
                "0,  0",
        })
        @DisplayName("getDaysRemaining() calcula correctamente los días")
        void whenCalculatingDaysRemaining_thenCorrect(int deadlineDaysFromNow, int expectedDaysRemaining) {
            LocalDate deadline = LocalDate.now().plusDays(deadlineDaysFromNow);
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, deadline);

            Integer daysRemaining = quest.getDaysRemaining(LocalDate.now());

            assertEquals(expectedDaysRemaining, daysRemaining);
        }

        @Test
        @DisplayName("getDaysRemaining() retorna null si no hay deadline")
        void whenNoDeadline_thenDaysRemainingIsNull() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.C);

            assertNull(quest.getDaysRemaining(LocalDate.now()));
        }

        @Test
        @DisplayName("getDaysRemaining() con null lanza IllegalArgumentException")
        void whenCalculatingDaysRemainingWithNull_thenThrowsException() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> quest.getDaysRemaining(null));
        }
    }

    @Nested
    class Penalties {
        @ParameterizedTest(name = "Rango {0}: Daño moral = {1} HP")
        @CsvSource({
                "E,    0",
                "D,    5",
                "C,    15",
                "B,    20",
                "A,    30",
                "S,    50",
                "S_PLUS, 50",
                "S_PLUS_PLUS, 50"
        })
        @DisplayName("calculateMoralDamage() retorna daño HP según rango")
        void whenCalculatingMoralDamage_thenMatchesRank(QuestRank rank, int expectedDamage) {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", rank);

            assertEquals(expectedDamage, quest.calculateMoralDamage());
        }

        @ParameterizedTest(name = "Rango {0}: Daño gold en burnout = {1} G")
        @CsvSource({
                "E,    0",
                "D,    50",
                "C,    150",
                "B,    200",
                "A,    300",
                "S,    500"
        })
        @DisplayName("calculateGoldDamageOnBurnout() convierte HP a Gold (1:10)")
        void whenCalculatingGoldDamage_thenCorrectConversion(QuestRank rank, int expectedGoldDamage) {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", rank);

            assertEquals(expectedGoldDamage, quest.calculateGoldDamageOnBurnout());
        }

        @Test
        @DisplayName("Rango E no tiene penalización")
        void rankE_hasNoPenalty() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.E);

            assertEquals(0, quest.calculateMoralDamage());
            assertEquals(0, quest.calculateGoldDamageOnBurnout());
        }
    }

    @Nested
    class HPStateRestrictions {
        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"E", "D"})
        @DisplayName("Rangos E/D/ no tienen restricciones de HP state")
        void lowRanks_haveNoHPRestrictions(QuestRank rank) {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", rank);

            assertTrue(quest.canStartWith(com.lifeleveling.domain.player.HPState.CRITICAL));
            assertTrue(quest.canStartWith(com.lifeleveling.domain.player.HPState.TIRED));
            assertTrue(quest.canStartWith(com.lifeleveling.domain.player.HPState.HEALTHY));
        }

        @Test
        void rankC_requiresTiredOrHealthy() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.C);

            assertFalse(quest.canStartWith(HPState.CRITICAL));  // ❌ Bloqueado
            assertTrue(quest.canStartWith(HPState.TIRED));      // ✅ OK
            assertTrue(quest.canStartWith(HPState.HEALTHY));    // ✅ OK
        }

        @ParameterizedTest
        @EnumSource(value = QuestRank.class, names = {"B", "A", "S", "S_PLUS", "S_PLUS_PLUS"})
        @DisplayName("Rangos B+ requieren estado HEALTHY")
        void highRanks_requireHealthyState(QuestRank rank) {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", rank);

            assertFalse(quest.canStartWith(com.lifeleveling.domain.player.HPState.CRITICAL));
            assertFalse(quest.canStartWith(com.lifeleveling.domain.player.HPState.TIRED));
            assertTrue(quest.canStartWith(com.lifeleveling.domain.player.HPState.HEALTHY));
        }
    }

    @Nested
    class Reconstitution {
        @Test
        @DisplayName("reconstitute() crea UserQuest desde datos externos")
        void whenReconstituting_thenValidQuest() {
            QuestId id = QuestId.generate();
            Instant now = Instant.now();
            LocalDate deadline = LocalDate.now().plusDays(7);

            UserQuest quest = UserQuest.reconstitute(
                    id,
                    "Reconstituted Quest",
                    "From database",
                    QuestRank.B,
                    deadline,
                    QuestStatus.IN_PROGRESS,
                    now,
                    null,
                    null
            );

            assertEquals(id, quest.id());
            assertEquals("Reconstituted Quest", quest.name());
            assertEquals(QuestStatus.IN_PROGRESS, quest.status());
        }

        @Test
        @DisplayName("reconstitute() con quest completada incluye completedAt")
        void whenReconstitutingCompleted_thenHasCompletedAt() {
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant completedAt = Instant.now();

            UserQuest quest = UserQuest.reconstitute(
                    QuestId.generate(),
                    "Test",
                    "Test",
                    QuestRank.C,
                    LocalDate.now().plusDays(1),
                    QuestStatus.COMPLETED,
                    createdAt,
                    completedAt,
                    null
            );

            assertEquals(QuestStatus.COMPLETED, quest.status());
            assertEquals(completedAt, quest.getCompletedAt());
        }

        @Test
        @DisplayName("reconstitute() valida coherencia de estado COMPLETED")
        void whenReconstitutingCompletedWithoutTimestamp_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> UserQuest.reconstitute(
                            QuestId.generate(),
                            "Test",
                            "Test",
                            QuestRank.C,
                            LocalDate.now().plusDays(1),
                            QuestStatus.COMPLETED,
                            Instant.now(),
                            null,  // ← COMPLETED pero sin completedAt
                            null
                    ));
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea quest con todos los elementos")
        void whenFormattingDisplay_thenIncludesAllInfo() {
            UserQuest quest = UserQuest.create(
                    "Terminar curso Spring",
                    "Módulos 1-5",
                    QuestRank.C,
                    LocalDate.now().plusDays(3)
            );

            String display = quest.toDisplayString();

            assertTrue(display.contains("🟣"));  // Icono rango C
            assertTrue(display.contains("[C]"));
            assertTrue(display.contains("Terminar curso Spring"));
            assertTrue(display.contains("⏰"));
            assertTrue(display.contains("3 días"));
            assertTrue(display.contains("📋"));
            assertTrue(display.contains("Pendiente"));
        }

        @Test
        @DisplayName("toDisplayString() para quest sin deadline")
        void whenFormattingWithoutDeadline_thenShowsNoLimit() {
            UserQuest quest = UserQuest.createWithoutDeadline("Test", "Test", QuestRank.D);
            String display = quest.toDisplayString();

            assertTrue(display.contains("Sin límite"));
        }

        @Test
        @DisplayName("toDisplayString() para quest expirada")
        void whenFormattingExpired_thenShowsExpired() {
            // CAMBIO: Usar reconstitute() en lugar de create()
            LocalDate pastDeadline = LocalDate.now().minusDays(5);
            Instant createdAt = Instant.now().minus(Duration.ofDays(10));
            Instant expiredAt = Instant.now().minus(Duration.ofDays(5));

            // ✅ Usar reconstitute() para crear quest histórica
            UserQuest quest = UserQuest.reconstitute(
                    QuestId.generate(),
                    "Test Quest",
                    "Test Description",
                    QuestRank.C,
                    pastDeadline,           // ✅ Fecha pasada permitida
                    QuestStatus.EXPIRED,    // ✅ Estado correcto
                    createdAt,
                    null,
                    expiredAt               // ✅ Timestamp de expiración
            );

            String display = quest.toDisplayString();

            assertTrue(display.contains("EXPIRADA") || display.contains("Expirada"),
                    "Debe mostrar que la quest está expirada. Display: " + display);
            assertTrue(display.contains("5 días") || display.contains("5"),
                    "Debe mostrar días transcurridos. Display: " + display);
        }

        @Test
        @DisplayName("toDisplayString() para deadline hoy")
        void whenDeadlineIsToday_thenShowsToday() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now());
            String display = quest.toDisplayString();

            assertTrue(display.contains("¡HOY!"));
        }

        @Test
        @DisplayName("toString() incluye campos clave")
        void whenCallingToString_thenIncludesKeyFields() {
            UserQuest quest = UserQuest.create("Test Quest", "Description", QuestRank.B, LocalDate.now().plusDays(1));
            String str = quest.toString();

            assertTrue(str.contains("UserQuest"));
            assertTrue(str.contains("Test Quest"));
            assertTrue(str.contains("B"));
            assertTrue(str.contains("PENDING"));
        }
    }


    @Nested
    class Equality {
        @Test
        @DisplayName("UserQuests con mismo ID son equals()")
        void questsWithSameId_areEqual() {
            QuestId id = QuestId.generate();

            UserQuest quest1 = UserQuest.reconstitute(
                    id, "Quest 1", "Desc", QuestRank.C, null,
                    QuestStatus.PENDING, Instant.now(), null, null
            );

            UserQuest quest2 = UserQuest.reconstitute(
                    id, "Quest 2", "Different", QuestRank.D, null,
                    QuestStatus.COMPLETED, Instant.now(), Instant.now(), null
            );

            assertEquals(quest1, quest2);
            assertEquals(quest1.hashCode(), quest2.hashCode());
        }

        @Test
        @DisplayName("UserQuests con diferente ID NO son equals()")
        void questsWithDifferentId_areNotEqual() {
            UserQuest quest1 = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));
            UserQuest quest2 = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            assertNotEquals(quest1, quest2);
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Quest puede tener nombre muy largo")
        void questWithLongName_isValid() {
            String longName = "A".repeat(500);

            assertDoesNotThrow(() ->
                    UserQuest.create(longName, "Description", QuestRank.C, LocalDate.now().plusDays(1))
            );
        }

        @Test
        @DisplayName("Quest puede tener deadline muy lejana")
        void questWithDistantDeadline_isValid() {
            LocalDate distantFuture = LocalDate.now().plusYears(10);

            assertDoesNotThrow(() ->
                    UserQuest.create("Test", "Test", QuestRank.S, distantFuture)
            );
        }

        @Test
        @DisplayName("Flujo completo: PENDING → IN_PROGRESS → COMPLETED")
        void completeQuestFlow_works() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            // Start
            quest = quest.start();
            assertEquals(QuestStatus.IN_PROGRESS, quest.status());

            // Complete
            quest = quest.complete(Instant.now());
            assertEquals(QuestStatus.COMPLETED, quest.status());
        }

        @Test
        @DisplayName("Flujo de fallo: PENDING → IN_PROGRESS → FAILED")
        void failedQuestFlow_works() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            // Start
            quest = quest.start();

            // Fail
            quest = quest.fail(Instant.now());
            assertEquals(QuestStatus.FAILED, quest.status());
        }

        @Test
        @DisplayName("Quest puede completarse directamente sin start()")
        void questCanCompleteDirectly_withoutStart() {
            UserQuest quest = UserQuest.create("Test", "Test", QuestRank.C, LocalDate.now().plusDays(1));

            assertDoesNotThrow(() -> quest.complete(Instant.now()));
        }
    }
}