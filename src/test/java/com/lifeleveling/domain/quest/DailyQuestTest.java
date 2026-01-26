package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DailyQuest - Misiones Recurrentes Diarias")
class DailyQuestTest {

    @Nested
    class Creation {
        @Test
        @DisplayName("create() crea DailyQuest válida")
        void whenCreating_thenValidQuest() {
            LocalDate today = LocalDate.now();

            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, today);

            assertNotNull(quest.id());
            assertEquals(DailyQuestType.DIET, quest.getType());
            assertEquals(today, quest.getQuestDate());
            assertEquals(QuestStatus.PENDING, quest.status());
            assertNotNull(quest.createdAt());
            assertEquals(0, quest.getCurrentStreak());
            assertEquals(0, quest.getBestStreak());
        }

        @Test
        @DisplayName("createWithStreak() preserva rachas")
        void whenCreatingWithStreak_thenStreaksPreserved() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.GYM,
                    LocalDate.now(),
                    5,   // Current streak
                    10   // Best streak
            );

            assertEquals(5, quest.getCurrentStreak());
            assertEquals(10, quest.getBestStreak());
        }

        @Test
        @DisplayName("create() con tipo null lanza IllegalArgumentException")
        void whenCreatingWithNullType_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> DailyQuest.create(null, LocalDate.now()));
        }

        @Test
        @DisplayName("create() con fecha null lanza IllegalArgumentException")
        void whenCreatingWithNullDate_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> DailyQuest.create(DailyQuestType.DIET, null));
        }
    }

    @Nested
    class BooleanCompletion {
        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY", "CODE"})
        @DisplayName("completeWithInput(true) completa quest Boolean")
        void whenCompletingWithTrue_thenQuestCompleted(DailyQuestType type) {
            DailyQuest quest = DailyQuest.create(type, LocalDate.now());

            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertEquals(Boolean.TRUE, completed.getBooleanInput());
            assertNotNull(completed.getCompletedAt());
        }

        @Test
        @DisplayName("completeWithInput(false) en quest Boolean lanza excepción")
        void whenCompletingWithFalse_thenThrowsException() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.completeWithInput(false, Instant.now()));
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"SLEEP", "READ"})
        @DisplayName("completeWithInput(boolean) en quest INTEGER lanza excepción")
        void whenCompletingIntegerQuestWithBoolean_thenThrowsException(DailyQuestType type) {
            DailyQuest quest = DailyQuest.create(type, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.completeWithInput(true, Instant.now()));
        }
    }

    @Nested
    class IntegerCompletion {
        @Test
        @DisplayName("completeWithInput(8) completa SLEEP correctamente")
        void whenCompletingSleepWith8Hours_thenCompleted() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());

            DailyQuest completed = quest.completeWithInput(8, Instant.now());

            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertEquals(8, completed.getIntegerInput());
        }

        @Test
        @DisplayName("completeWithInput(15) completa READ correctamente")
        void whenCompletingReadWith15Pages_thenCompleted() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.READ, LocalDate.now());

            DailyQuest completed = quest.completeWithInput(15, Instant.now());

            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertEquals(15, completed.getIntegerInput());
        }

        @Test
        @DisplayName("SLEEP con <7 horas lanza excepción")
        void whenSleepingLessThan7Hours_thenThrowsException() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.completeWithInput(6, Instant.now()));
        }

        @Test
        @DisplayName("READ con <10 páginas lanza excepción")
        void whenReadingLessThan10Pages_thenThrowsException() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.READ, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.completeWithInput(9, Instant.now()));
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY", "CODE"})
        @DisplayName("completeWithInput(int) en quest BOOLEAN lanza excepción")
        void whenCompletingBooleanQuestWithInteger_thenThrowsException(DailyQuestType type) {
            DailyQuest quest = DailyQuest.create(type, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.completeWithInput(10, Instant.now()));
        }
    }

    @Nested
    class Streaks {
        @Test
        @DisplayName("Completar quest incrementa racha en +1")
        void whenCompleting_thenStreakIncrements() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            assertEquals(1, completed.getCurrentStreak());
            assertEquals(1, completed.getBestStreak());
            assertTrue(completed.hasActiveStreak());
        }

        @Test
        @DisplayName("Completar quest con racha existente incrementa")
        void whenCompletingWithExistingStreak_thenIncrements() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.GYM,
                    LocalDate.now(),
                    5,
                    10
            );

            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            assertEquals(6, completed.getCurrentStreak());
            assertEquals(10, completed.getBestStreak());
        }

        @Test
        @DisplayName("Alcanzar nuevo récord actualiza bestStreak")
        void whenReachingNewRecord_thenBestStreakUpdates() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.GYM,
                    LocalDate.now(),
                    10,
                    10
            );

            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            assertEquals(11, completed.getCurrentStreak());
            assertEquals(11, completed.getBestStreak());
            assertTrue(completed.isNewStreakRecord());
        }

        @Test
        @DisplayName("fail() resetea la racha a 0")
        void whenFailing_thenStreakResets() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.DIET,
                    LocalDate.now(),
                    7,
                    15
            );

            DailyQuest failed = quest.fail(Instant.now());

            assertEquals(0, failed.getCurrentStreak());
            assertEquals(15, failed.getBestStreak(), "Best streak se mantiene");
            assertFalse(failed.hasActiveStreak());
        }

        @Test
        @DisplayName("isNewStreakRecord() detecta nuevo récord correctamente")
        void whenCheckingNewRecord_thenCorrectResult() {
            DailyQuest normalQuest = DailyQuest.createWithStreak(
                    DailyQuestType.GYM,
                    LocalDate.now(),
                    5,
                    10
            );

            assertFalse(normalQuest.isNewStreakRecord());

            DailyQuest recordQuest = normalQuest.completeWithInput(true, Instant.now());
            recordQuest = recordQuest.completeWithInput(true, Instant.now()); // 7
            recordQuest = recordQuest.completeWithInput(true, Instant.now()); // 8
            recordQuest = recordQuest.completeWithInput(true, Instant.now()); // 9
            recordQuest = recordQuest.completeWithInput(true, Instant.now()); // 10
            recordQuest = recordQuest.completeWithInput(true, Instant.now()); // 11 (nuevo récord)

            assertTrue(recordQuest.isNewStreakRecord());
        }
    }

    @Nested
    class Rewards {
        @Test
        @DisplayName("Quest PENDING no da reward")
        void whenPending_thenNoReward() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            assertTrue(quest.reward().isEmpty());
        }

        @Test
        @DisplayName("DIET completada da +50 XP General")
        void whenDietCompleted_then50GeneralXP() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());
            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            QuestReward reward = completed.reward();
            assertEquals(50, reward.generalXP());
        }

        @Test
        @DisplayName("GYM completado da +50 XP Fuerza")
        void whenGymCompleted_then50StrengthXP() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.GYM, LocalDate.now());
            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            QuestReward reward = completed.reward();
            assertEquals(50, reward.getStatXP(StatType.STRENGTH));
        }

        @Test
        @DisplayName("SLEEP con 8 horas da +80 XP General")
        void whenSleep8Hours_then80GeneralXP() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());
            DailyQuest completed = quest.completeWithInput(8, Instant.now());

            QuestReward reward = completed.reward();
            assertEquals(80, reward.generalXP());
        }

        @Test
        @DisplayName("READ con 20 páginas da +100 XP Sabiduría")
        void whenRead20Pages_then100WisdomXP() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.READ, LocalDate.now());
            DailyQuest completed = quest.completeWithInput(20, Instant.now());

            QuestReward reward = completed.reward();
            assertEquals(100, reward.getStatXP(StatType.WISDOM));
        }
    }

    @Nested
    class HPEffects {
        @Test
        @DisplayName("SLEEP tiene efecto +15 HP")
        void sleep_has15HPEffect() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());
            assertEquals(15, quest.getHPEffect());
        }

        @Test
        @DisplayName("GYM tiene efecto -5 HP")
        void gym_hasNegative5HPEffect() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.GYM, LocalDate.now());
            assertEquals(-5, quest.getHPEffect());
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "CODE", "READ", "SKINCARE", "TIDY"})
        @DisplayName("Otras quests tienen efecto 0 HP")
        void otherQuests_haveZeroHPEffect(DailyQuestType type) {
            DailyQuest quest = DailyQuest.create(type, LocalDate.now());
            assertEquals(0, quest.getHPEffect());
        }
    }

    @Nested
    class DateQueries {
        @Test
        @DisplayName("isForToday() retorna true si la fecha es hoy")
        void whenQuestIsForToday_thenReturnsTrue() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());
            assertTrue(quest.isForToday());
        }

        @Test
        @DisplayName("isForToday() retorna false si la fecha no es hoy")
        void whenQuestIsNotForToday_thenReturnsFalse() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now().minusDays(1));
            assertFalse(quest.isForToday());
        }

        @Test
        @DisplayName("isPast() retorna true si la fecha es pasada")
        void whenQuestIsPast_thenReturnsTrue() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now().minusDays(5));
            assertTrue(quest.isPast());
        }

        @Test
        @DisplayName("isPast() retorna false si la fecha es hoy o futura")
        void whenQuestIsNotPast_thenReturnsFalse() {
            DailyQuest today = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());
            DailyQuest future = DailyQuest.create(DailyQuestType.DIET, LocalDate.now().plusDays(1));

            assertFalse(today.isPast());
            assertFalse(future.isPast());
        }
    }

    @Nested
    class QuestInterface {
        @Test
        @DisplayName("name() retorna el nombre del tipo")
        void name_returnsTypeName() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());
            assertEquals("Dieta Limpia", quest.name());
        }

        @Test
        @DisplayName("description() retorna la descripción del tipo")
        void description_returnsTypeDescription() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());
            assertTrue(quest.description().contains("7 horas"));
        }

        @Test
        @DisplayName("rank() siempre retorna E para Daily Quests")
        void rank_alwaysReturnsE() {
            for (DailyQuestType type : DailyQuestType.values()) {
                DailyQuest quest = DailyQuest.create(type, LocalDate.now());
                assertEquals(QuestRank.E, quest.rank());
            }
        }

        @Test
        @DisplayName("complete(Instant) lanza UnsupportedOperationException")
        void complete_throwsUnsupportedException() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            assertThrows(UnsupportedOperationException.class,
                    () -> quest.complete(Instant.now()));
        }
    }

    @Nested
    class StateTransitions {
        @Test
        @DisplayName("fail() marca quest como FAILED")
        void whenFailing_thenStatusIsFailed() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            DailyQuest failed = quest.fail(Instant.now());

            assertEquals(QuestStatus.FAILED, failed.status());
        }

        @Test
        @DisplayName("completeWithInput() no modifica la quest original")
        void whenCompleting_thenOriginalUnchanged() {
            DailyQuest original = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            DailyQuest completed = original.completeWithInput(true, Instant.now());

            assertEquals(QuestStatus.PENDING, original.status());
            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertNotSame(original, completed);
        }

        @Test
        @DisplayName("completeWithInput() con null timestamp lanza excepción")
        void whenCompletingWithNullTimestamp_thenThrowsException() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.completeWithInput(true, null));
        }
    }

    @Nested
    class Reconstitution {
        @Test
        @DisplayName("reconstitute() crea DailyQuest desde datos externos")
        void whenReconstituting_thenValidQuest() {
            QuestId id = QuestId.generate();
            LocalDate date = LocalDate.now();
            Instant now = Instant.now();

            DailyQuest quest = DailyQuest.reconstitute(
                    id,
                    DailyQuestType.DIET,
                    date,
                    QuestStatus.COMPLETED,
                    now,
                    now,
                    true,
                    null,
                    5,
                    10
            );

            assertEquals(id, quest.id());
            assertEquals(DailyQuestType.DIET, quest.getType());
            assertEquals(QuestStatus.COMPLETED, quest.status());
            assertEquals(5, quest.getCurrentStreak());
        }

        @Test
        @DisplayName("reconstitute() valida coherencia: bestStreak >= currentStreak")
        void whenReconstitutingWithInvalidStreaks_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> DailyQuest.reconstitute(
                            QuestId.generate(),
                            DailyQuestType.DIET,
                            LocalDate.now(),
                            QuestStatus.PENDING,
                            Instant.now(),
                            null,
                            null,
                            null,
                            10,  // Current
                            5    // Best (menor que current - INVÁLIDO)
                    ));
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea quest completada con Boolean")
        void whenFormattingCompletedBooleanQuest_thenCorrectFormat() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());
            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            String display = completed.toDisplayString();

            assertTrue(display.contains("🥗"));
            assertTrue(display.contains("Dieta Limpia"));
            assertTrue(display.contains("✓"));
            assertTrue(display.contains("Completada"));
        }

        @Test
        @DisplayName("toDisplayString() formatea quest con Integer")
        void whenFormattingIntegerQuest_thenIncludesValue() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());
            DailyQuest completed = quest.completeWithInput(8, Instant.now());

            String display = completed.toDisplayString();

            assertTrue(display.contains("💤"));
            assertTrue(display.contains("8"));
            assertTrue(display.contains("horas"));
        }

        @Test
        @DisplayName("toDisplayString() muestra racha si está activa")
        void whenFormattingWithStreak_thenShowsStreak() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.GYM,
                    LocalDate.now(),
                    7,
                    10
            );
            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            String display = completed.toDisplayString();

            assertTrue(display.contains("🔥"));
            assertTrue(display.contains("Racha: 8"));
        }

        @Test
        @DisplayName("toDisplayString() muestra RÉCORD si es nuevo best streak")
        void whenFormattingNewRecord_thenShowsRecord() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.GYM,
                    LocalDate.now(),
                    10,
                    10
            );
            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            String display = completed.toDisplayString();

            assertTrue(display.contains("⭐ ¡RÉCORD!"));
        }
    }

    @Nested
    class Equality {
        @Test
        @DisplayName("DailyQuests con mismo ID son equals()")
        void questsWithSameId_areEqual() {
            QuestId id = QuestId.generate();

            DailyQuest quest1 = DailyQuest.reconstitute(
                    id, DailyQuestType.DIET, LocalDate.now(),
                    QuestStatus.PENDING, Instant.now(), null,
                    null, null, 0, 0
            );

            DailyQuest quest2 = DailyQuest.reconstitute(
                    id, DailyQuestType.GYM, LocalDate.now().minusDays(1),
                    QuestStatus.COMPLETED, Instant.now(), Instant.now(),
                    true, null, 5, 10
            );

            assertEquals(quest1, quest2);
            assertEquals(quest1.hashCode(), quest2.hashCode());
        }

        @Test
        @DisplayName("DailyQuests con diferente ID NO son equals()")
        void questsWithDifferentId_areNotEqual() {
            DailyQuest quest1 = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());
            DailyQuest quest2 = DailyQuest.create(DailyQuestType.DIET, LocalDate.now());

            assertNotEquals(quest1, quest2);
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("SLEEP con valor exacto 7 cumple condición")
        void sleep_exactlySevenHours_meetsCondition() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.SLEEP, LocalDate.now());

            assertDoesNotThrow(() -> quest.completeWithInput(7, Instant.now()));
        }

        @Test
        @DisplayName("READ con valor exacto 10 cumple condición")
        void read_exactlyTenPages_meetsCondition() {
            DailyQuest quest = DailyQuest.create(DailyQuestType.READ, LocalDate.now());

            assertDoesNotThrow(() -> quest.completeWithInput(10, Instant.now()));
        }

        @Test
        @DisplayName("Racha puede llegar a valores muy altos")
        void streak_canReachHighValues() {
            DailyQuest quest = DailyQuest.createWithStreak(
                    DailyQuestType.DIET,
                    LocalDate.now(),
                    999,
                    999
            );

            DailyQuest completed = quest.completeWithInput(true, Instant.now());

            assertEquals(1000, completed.getCurrentStreak());
            assertEquals(1000, completed.getBestStreak());
        }
    }
}