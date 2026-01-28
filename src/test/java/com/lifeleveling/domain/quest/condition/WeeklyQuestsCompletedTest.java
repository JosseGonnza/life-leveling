package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("WeeklyQuestsCompleted - Condición de semanas con Weekly Quests completas")
class WeeklyQuestsCompletedTest {

    @Nested
    class Construction {
        @Test
        @DisplayName("Constructor con semanas válidas crea condición")
        void whenValidWeeks_thenCreatesCondition() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);
            assertEquals(4, condition.getRequiredWeeks());
        }

        @Test
        @DisplayName("Constructor con semanas <= 0 lanza excepción")
        void whenInvalidWeeks_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new WeeklyQuestsCompleted(0));
            assertThrows(IllegalArgumentException.class, () -> new WeeklyQuestsCompleted(-2));
        }
    }

    @Nested
    class Evaluation {
        @Test
        @DisplayName("isMet() retorna true cuando semanas >= semanas requeridas")
        void whenWeeksMeetsRequirement_thenReturnTrue() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getConsecutiveWeeksWithFullWeeklies()).thenReturn(4);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertTrue(condition.isMet(context));
        }

        @Test
        @DisplayName("isMet() retorna true cuando semanas > semanas requeridas")
        void whenWeeksExceedsRequirement_thenReturnTrue() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getConsecutiveWeeksWithFullWeeklies()).thenReturn(6);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertTrue(condition.isMet(context));
        }

        @Test
        @DisplayName("isMet() retorna false cuando semanas < semanas requeridas")
        void whenWeeksBelowRequirement_thenReturnFalse() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getConsecutiveWeeksWithFullWeeklies()).thenReturn(3);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertFalse(condition.isMet(context));
        }
    }

    @Nested
    class Progress {
        @Test
        @DisplayName("getProgress() calcula porcentaje correctamente")
        void whenGettingProgress_thenCalculatesPercentage() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getConsecutiveWeeksWithFullWeeklies()).thenReturn(2);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertEquals(0.5, condition.getProgress(context), 0.01);
        }

        @Test
        @DisplayName("getProgress() no excede 1.0")
        void whenWeeksExceeds_thenProgressCapsAt1() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getConsecutiveWeeksWithFullWeeklies()).thenReturn(8);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertEquals(1.0, condition.getProgress(context), 0.01);
        }

        @Test
        @DisplayName("getProgressText() formatea correctamente")
        void whenGettingProgressText_thenFormatsCorrectly() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getConsecutiveWeeksWithFullWeeklies()).thenReturn(2);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            String progressText = condition.getProgressText(context);
            assertTrue(progressText.contains("2"));
            assertTrue(progressText.contains("4"));
        }
    }

    @Nested
    class Metadata {
        @Test
        @DisplayName("getType() retorna WEEKLY_QUESTS_COMPLETED")
        void whenGettingType_thenReturnsCorrectType() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);
            assertEquals(ConditionType.WEEKLY_QUESTS_COMPLETED, condition.getType());
        }

        @Test
        @DisplayName("canReset() retorna true")
        void whenCheckingCanReset_thenReturnsTrue() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);
            assertTrue(condition.canReset());
        }

        @Test
        @DisplayName("getDescription() incluye número de semanas")
        void whenGettingDescription_thenIncludesWeeks() {
            WeeklyQuestsCompleted condition = new WeeklyQuestsCompleted(4);
            String description = condition.getDescription();

            assertTrue(description.contains("4"));
            assertTrue(description.contains("semana") || description.contains("Weekly"));
        }
    }
}
