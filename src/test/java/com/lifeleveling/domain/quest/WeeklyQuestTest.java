package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.quest.shared.QuestId;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestStatus;
import com.lifeleveling.domain.quest.weekly.WeeklyQuest;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeeklyQuest - Desafíos Semanales Rotativos")
class WeeklyQuestTest {

    @Nested
    class Creation {
        @Test
        @DisplayName("create() crea WeeklyQuest válida para semana actual")
        void whenCreating_thenValidQuest() {
            LocalDate today = LocalDate.now();
            LocalDate expectedMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, today);

            assertNotNull(quest.id());
            assertEquals(WeeklyQuestType.HIBERNATOR, quest.getType());
            assertEquals(expectedMonday, quest.getWeekStartDate());
            assertEquals(QuestStatus.IN_PROGRESS, quest.status());
            assertEquals(0, quest.getCurrentProgress());
            assertNotNull(quest.createdAt());
            assertNull(quest.getCompletedAt());
        }

        @Test
        @DisplayName("create() con tipo null lanza NullPointerException")
        void whenCreatingWithNullType_thenThrowsException() {
            assertThrows(NullPointerException.class,
                    () -> WeeklyQuest.create(null, LocalDate.now()));
        }

        @Test
        @DisplayName("create() con fecha null lanza NullPointerException")
        void whenCreatingWithNullDate_thenThrowsException() {
            assertThrows(NullPointerException.class,
                    () -> WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, null));
        }

        @Test
        @DisplayName("create() ajusta cualquier día de la semana al Lunes")
        void whenCreatingWithAnyDay_thenAdjustsToMonday() {
            // Jueves
            LocalDate thursday = LocalDate.of(2026, 1, 29);
            LocalDate expectedMonday = LocalDate.of(2026, 1, 26);

            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.IRON_WEEK, thursday);

            assertEquals(expectedMonday, quest.getWeekStartDate());
        }

        @Test
        @DisplayName("create() desde Lunes mantiene el mismo Lunes")
        void whenCreatingOnMonday_thenSameMonday() {
            LocalDate monday = LocalDate.of(2026, 1, 26);

            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.IRON_WEEK, monday);

            assertEquals(monday, quest.getWeekStartDate());
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("create() funciona para todos los tipos")
        void whenCreatingWithAnyType_thenValidQuest(WeeklyQuestType type) {
            WeeklyQuest quest = WeeklyQuest.create(type, LocalDate.now());

            assertNotNull(quest);
            assertEquals(type, quest.getType());
            assertEquals(QuestStatus.IN_PROGRESS, quest.status());
        }
    }

    @Nested
    class ProgressTracking {
        @Test
        @DisplayName("addProgress() incrementa el progreso")
        void whenAddingProgress_thenProgressIncreases() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest updated = quest.addProgress(2);

            assertEquals(2, updated.getCurrentProgress());
            assertEquals(0, quest.getCurrentProgress(), "Original no debe modificarse");
        }

        @Test
        @DisplayName("addProgress() acumula progreso múltiple")
        void whenAddingProgressMultipleTimes_thenProgressAccumulates() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest updated = quest.addProgress(2).addProgress(3);

            assertEquals(5, updated.getCurrentProgress());
        }

        @Test
        @DisplayName("addProgress() con valor negativo lanza excepción")
        void whenAddingNegativeProgress_thenThrowsException() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.addProgress(-1));
        }

        @Test
        @DisplayName("addProgress(0) no cambia el progreso")
        void whenAddingZeroProgress_thenNoChange() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest updated = quest.addProgress(0);

            assertEquals(0, updated.getCurrentProgress());
        }

        @Test
        @DisplayName("setProgress() establece el progreso directamente")
        void whenSettingProgress_thenProgressIsSet() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.THE_SCHOLAR, LocalDate.now());

            WeeklyQuest updated = quest.setProgress(75);

            assertEquals(75, updated.getCurrentProgress());
        }

        @Test
        @DisplayName("setProgress() con valor negativo lanza excepción")
        void whenSettingNegativeProgress_thenThrowsException() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertThrows(IllegalArgumentException.class,
                    () -> quest.setProgress(-1));
        }
    }

    @Nested
    class AutoComplete {
        @Test
        @DisplayName("addProgress() auto-completa al alcanzar target")
        void whenProgressReachesTarget_thenAutoCompletes() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest updated = quest.addProgress(6); // Target es 6 días

            assertEquals(QuestStatus.COMPLETED, updated.status());
            assertNotNull(updated.getCompletedAt());
            assertTrue(updated.isCompleted());
        }

        @Test
        @DisplayName("addProgress() auto-completa al superar target")
        void whenProgressExceedsTarget_thenAutoCompletes() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.IRON_WEEK, LocalDate.now());

            WeeklyQuest updated = quest.addProgress(7); // Target es 5 días

            assertEquals(QuestStatus.COMPLETED, updated.status());
            assertEquals(7, updated.getCurrentProgress());
        }

        @Test
        @DisplayName("setProgress() auto-completa al alcanzar target")
        void whenSetProgressReachesTarget_thenAutoCompletes() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.THE_SCHOLAR, LocalDate.now());

            WeeklyQuest updated = quest.setProgress(150); // Target es 150 páginas

            assertEquals(QuestStatus.COMPLETED, updated.status());
            assertNotNull(updated.getCompletedAt());
        }

        @Test
        @DisplayName("No auto-completa si progreso < target")
        void whenProgressBelowTarget_thenStaysInProgress() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest updated = quest.addProgress(5); // Target es 6

            assertEquals(QuestStatus.IN_PROGRESS, updated.status());
            assertNull(updated.getCompletedAt());
        }
    }

    @Nested
    class ProgressIgnoredWhenNotActive {
        @Test
        @DisplayName("addProgress() no modifica quest completada")
        void whenAddingProgressToCompleted_thenNoChange() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest completed = quest.addProgress(6);

            WeeklyQuest updated = completed.addProgress(1);

            assertSame(completed, updated);
            assertEquals(6, updated.getCurrentProgress());
        }

        @Test
        @DisplayName("setProgress() no modifica quest completada")
        void whenSettingProgressOnCompleted_thenNoChange() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest completed = quest.addProgress(6);

            WeeklyQuest updated = completed.setProgress(10);

            assertSame(completed, updated);
            assertEquals(6, updated.getCurrentProgress());
        }
    }

    @Nested
    class WeekManagement {
        @Test
        @DisplayName("isCurrentWeek() retorna true para semana actual")
        void whenInCurrentWeek_thenReturnsTrue() {
            LocalDate today = LocalDate.now();
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, today);

            assertTrue(quest.isCurrentWeek());
            assertTrue(quest.isCurrentWeek(today));
        }

        @Test
        @DisplayName("isCurrentWeek() retorna false para semana pasada")
        void whenInPastWeek_thenReturnsFalse() {
            LocalDate lastWeek = LocalDate.now().minusWeeks(1);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, lastWeek);

            assertFalse(quest.isCurrentWeek());
        }

        @Test
        @DisplayName("getWeekEndDate() retorna el Domingo de la semana")
        void weekEndDate_isSunday() {
            LocalDate monday = LocalDate.of(2026, 1, 26);
            LocalDate expectedSunday = LocalDate.of(2026, 2, 1);

            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, monday);

            assertEquals(expectedSunday, quest.getWeekEndDate());
        }

        @Test
        @DisplayName("getDaysRemaining() calcula días restantes correctamente")
        void daysRemaining_calculatesCorrectly() {
            LocalDate monday = LocalDate.of(2026, 1, 26);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, monday);

            // Desde el lunes, quedan 7 días (lun a dom incluidos)
            assertEquals(7, quest.getDaysRemaining(monday));

            // Desde el miércoles, quedan 5 días
            assertEquals(5, quest.getDaysRemaining(LocalDate.of(2026, 1, 28)));

            // Desde el domingo, queda 1 día
            assertEquals(1, quest.getDaysRemaining(LocalDate.of(2026, 2, 1)));
        }

        @Test
        @DisplayName("getDaysRemaining() retorna 0 si no es semana actual")
        void daysRemaining_zeroIfNotCurrentWeek() {
            LocalDate lastWeek = LocalDate.now().minusWeeks(1);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, lastWeek);

            assertEquals(0, quest.getDaysRemaining());
        }
    }

    @Nested
    class Expiration {
        @Test
        @DisplayName("isExpired() = false si está en semana actual")
        void whenInCurrentWeek_thenNotExpired() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertFalse(quest.isExpired());
        }

        @Test
        @DisplayName("isExpired() = true si pasó la semana sin completar")
        void whenPastWeekAndNotCompleted_thenExpired() {
            LocalDate lastWeek = LocalDate.now().minusWeeks(1);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, lastWeek);

            assertTrue(quest.isExpired());
        }

        @Test
        @DisplayName("isExpired() = false si está completada aunque pasó la semana")
        void whenCompletedAndPastWeek_thenNotExpired() {
            LocalDate lastWeek = LocalDate.now().minusWeeks(1);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, lastWeek);
            WeeklyQuest completed = quest.addProgress(6);

            assertFalse(completed.isExpired());
        }

        @Test
        @DisplayName("checkExpiration() marca como EXPIRED si corresponde")
        void whenCheckingExpiration_thenMarksExpired() {
            LocalDate lastWeek = LocalDate.now().minusWeeks(1);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, lastWeek);

            WeeklyQuest checked = quest.checkExpiration(LocalDate.now());

            assertEquals(QuestStatus.EXPIRED, checked.status());
        }

        @Test
        @DisplayName("checkExpiration() no cambia quest completada")
        void whenCheckingExpirationOnCompleted_thenNoChange() {
            LocalDate lastWeek = LocalDate.now().minusWeeks(1);
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, lastWeek);
            WeeklyQuest completed = quest.addProgress(6);

            WeeklyQuest checked = completed.checkExpiration(LocalDate.now());

            assertEquals(QuestStatus.COMPLETED, checked.status());
        }

        @Test
        @DisplayName("checkExpiration() no cambia quest de semana actual")
        void whenCheckingExpirationOnCurrentWeek_thenNoChange() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest checked = quest.checkExpiration(LocalDate.now());

            assertEquals(QuestStatus.IN_PROGRESS, checked.status());
        }
    }

    @Nested
    class QuestInterface {
        @Test
        @DisplayName("name() retorna el nombre del tipo")
        void name_returnsTypeName() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            assertEquals("El Oso Hibernador", quest.name());
        }

        @Test
        @DisplayName("description() retorna la descripción del tipo")
        void description_returnsTypeDescription() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            assertTrue(quest.description().contains("7 horas"));
            assertTrue(quest.description().contains("6 días"));
        }

        @Test
        @DisplayName("rank() retorna el rango del tipo")
        void rank_returnsTypeRank() {
            WeeklyQuest hibernator = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest streak = WeeklyQuest.create(WeeklyQuestType.THE_STREAK, LocalDate.now());

            assertEquals(QuestRank.C, hibernator.rank());
            assertEquals(QuestRank.B, streak.rank());
        }

        @Test
        @DisplayName("reward() retorna la recompensa del tipo")
        void reward_returnsTypeReward() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertEquals(1_000, quest.reward().gold());
        }

        @Test
        @DisplayName("complete(Instant) completa la quest manualmente")
        void complete_completesQuest() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            Instant now = Instant.now();

            WeeklyQuest completed = (WeeklyQuest) quest.complete(now);

            assertEquals(QuestStatus.COMPLETED, completed.status());
        }

        @Test
        @DisplayName("complete() no afecta quest ya completada")
        void complete_doesNotAffectAlreadyCompleted() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest completed = quest.addProgress(6);

            WeeklyQuest result = (WeeklyQuest) completed.complete(Instant.now());

            assertSame(completed, result);
        }

        @Test
        @DisplayName("fail(Instant) marca como EXPIRED (no como FAILED)")
        void fail_marksAsExpired() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            WeeklyQuest failed = (WeeklyQuest) quest.fail(Instant.now());

            assertEquals(QuestStatus.EXPIRED, failed.status());
        }
    }

    @Nested
    class HPStateRestrictions {
        @Test
        @DisplayName("canStartWith() = true para HEALTHY")
        void canStart_whenHealthy() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            assertTrue(quest.canStartWith(HPState.HEALTHY));
        }

        @Test
        @DisplayName("canStartWith() = true para TIRED")
        void canStart_whenTired() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            assertTrue(quest.canStartWith(HPState.TIRED));
        }

        @Test
        @DisplayName("canStartWith() = false para CRITICAL")
        void cannotStart_whenCritical() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            assertFalse(quest.canStartWith(HPState.CRITICAL));
        }
    }

    @Nested
    class ProgressQueries {
        @Test
        @DisplayName("getTarget() retorna el target del tipo")
        void getTarget_returnsTypeTarget() {
            WeeklyQuest hibernator = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest scholar = WeeklyQuest.create(WeeklyQuestType.THE_SCHOLAR, LocalDate.now());

            assertEquals(6, hibernator.getTarget());
            assertEquals(150, scholar.getTarget());
        }

        @Test
        @DisplayName("getProgressPercentage() calcula correctamente")
        void progressPercentage_calculatesCorrectly() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertEquals(0.0, quest.getProgressPercentage());

            WeeklyQuest halfDone = quest.addProgress(3);
            assertEquals(0.5, halfDone.getProgressPercentage(), 0.001);

            WeeklyQuest completed = quest.addProgress(6);
            assertEquals(1.0, completed.getProgressPercentage());
        }

        @Test
        @DisplayName("isConditionMet() verifica si se alcanzó el target")
        void isConditionMet_checksTarget() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertFalse(quest.isConditionMet());

            WeeklyQuest almostDone = quest.addProgress(5);
            assertFalse(almostDone.isConditionMet());

            WeeklyQuest done = quest.addProgress(6);
            assertTrue(done.isConditionMet());
        }
    }

    @Nested
    class Reconstitution {
        @Test
        @DisplayName("reconstitute() crea WeeklyQuest desde datos externos")
        void whenReconstituting_thenValidQuest() {
            QuestId id = QuestId.generate();
            LocalDate weekStart = LocalDate.of(2026, 1, 26);
            Instant created = Instant.now();
            Instant completed = Instant.now();

            WeeklyQuest quest = WeeklyQuest.reconstitute(
                    id,
                    WeeklyQuestType.IRON_WEEK,
                    QuestStatus.COMPLETED,
                    weekStart,
                    5,
                    created,
                    completed
            );

            assertEquals(id, quest.id());
            assertEquals(WeeklyQuestType.IRON_WEEK, quest.getType());
            assertEquals(QuestStatus.COMPLETED, quest.status());
            assertEquals(weekStart, quest.getWeekStartDate());
            assertEquals(5, quest.getCurrentProgress());
            assertEquals(created, quest.createdAt());
            assertEquals(completed, quest.getCompletedAt());
        }

        @Test
        @DisplayName("reconstitute() con progreso negativo lanza excepción")
        void whenReconstitutingWithNegativeProgress_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> WeeklyQuest.reconstitute(
                            QuestId.generate(),
                            WeeklyQuestType.HIBERNATOR,
                            QuestStatus.IN_PROGRESS,
                            LocalDate.now(),
                            -1,
                            Instant.now(),
                            null
                    ));
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea quest con progreso")
        void whenFormatting_thenIncludesProgress() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest withProgress = quest.addProgress(3);

            String display = withProgress.toDisplayString();

            assertTrue(display.contains("🐻"));
            assertTrue(display.contains("El Oso Hibernador"));
            assertTrue(display.contains("3/6 días"));
        }

        @Test
        @DisplayName("toDisplayString() muestra checkmark si completada")
        void whenCompleted_thenShowsCheckmark() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest completed = quest.addProgress(6);

            String display = completed.toDisplayString();

            assertTrue(display.contains("✅"));
        }

        @Test
        @DisplayName("getProgressText() formatea el progreso")
        void progressText_formatsCorrectly() {
            WeeklyQuest hibernator = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now())
                    .addProgress(4);
            WeeklyQuest scholar = WeeklyQuest.create(WeeklyQuestType.THE_SCHOLAR, LocalDate.now())
                    .addProgress(100);

            assertEquals("4/6 días", hibernator.getProgressText());
            assertEquals("100/150 páginas", scholar.getProgressText());
        }
    }

    @Nested
    class Equality {
        @Test
        @DisplayName("WeeklyQuests con mismo ID son equals()")
        void questsWithSameId_areEqual() {
            QuestId id = QuestId.generate();

            WeeklyQuest quest1 = WeeklyQuest.reconstitute(
                    id, WeeklyQuestType.HIBERNATOR, QuestStatus.IN_PROGRESS,
                    LocalDate.now(), 0, Instant.now(), null
            );

            WeeklyQuest quest2 = WeeklyQuest.reconstitute(
                    id, WeeklyQuestType.IRON_WEEK, QuestStatus.COMPLETED,
                    LocalDate.now().minusWeeks(1), 5, Instant.now(), Instant.now()
            );

            assertEquals(quest1, quest2);
            assertEquals(quest1.hashCode(), quest2.hashCode());
        }

        @Test
        @DisplayName("WeeklyQuests con diferente ID NO son equals()")
        void questsWithDifferentId_areNotEqual() {
            WeeklyQuest quest1 = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());
            WeeklyQuest quest2 = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now());

            assertNotEquals(quest1, quest2);
        }
    }

    @Nested
    class StaticHelpers {
        @Test
        @DisplayName("getWeekStart() retorna el Lunes de la semana")
        void getWeekStart_returnsMonday() {
            // Jueves 29 de enero 2026
            LocalDate thursday = LocalDate.of(2026, 1, 29);
            LocalDate expectedMonday = LocalDate.of(2026, 1, 26);

            assertEquals(expectedMonday, WeeklyQuest.getWeekStart(thursday));
        }

        @Test
        @DisplayName("getWeekStart() desde Lunes retorna mismo día")
        void getWeekStart_fromMondayReturnsSame() {
            LocalDate monday = LocalDate.of(2026, 1, 26);

            assertEquals(monday, WeeklyQuest.getWeekStart(monday));
        }

        @Test
        @DisplayName("getWeekEnd() retorna el Domingo de la semana")
        void getWeekEnd_returnsSunday() {
            LocalDate thursday = LocalDate.of(2026, 1, 29);
            LocalDate expectedSunday = LocalDate.of(2026, 2, 1);

            assertEquals(expectedSunday, WeeklyQuest.getWeekEnd(thursday));
        }

        @Test
        @DisplayName("getWeekEnd() desde Domingo retorna mismo día")
        void getWeekEnd_fromSundayReturnsSame() {
            LocalDate sunday = LocalDate.of(2026, 2, 1);

            assertEquals(sunday, WeeklyQuest.getWeekEnd(sunday));
        }
    }

    @Nested
    class ToString {
        @Test
        @DisplayName("toString() incluye información relevante")
        void toString_includesRelevantInfo() {
            WeeklyQuest quest = WeeklyQuest.create(WeeklyQuestType.HIBERNATOR, LocalDate.now())
                    .addProgress(3);

            String str = quest.toString();

            assertTrue(str.contains("HIBERNATOR"));
            assertTrue(str.contains("IN_PROGRESS"));
            assertTrue(str.contains("3/6"));
        }
    }
}
