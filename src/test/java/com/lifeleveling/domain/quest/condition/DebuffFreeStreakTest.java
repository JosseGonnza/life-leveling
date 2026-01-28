package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DebuffFreeStreak - Condición de racha sin debuffs")
class DebuffFreeStreakTest {

    @Nested
    class Construction {
        @Test
        @DisplayName("Constructor con días válidos crea condición")
        void whenValidDays_thenCreatesCondition() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);
            assertEquals(30, condition.getRequiredDays());
        }

        @Test
        @DisplayName("Constructor con días <= 0 lanza excepción")
        void whenInvalidDays_thenThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new DebuffFreeStreak(0));
            assertThrows(IllegalArgumentException.class, () -> new DebuffFreeStreak(-5));
        }
    }

    @Nested
    class Evaluation {
        @Test
        @DisplayName("isMet() retorna true cuando racha >= días requeridos")
        void whenStreakMeetsRequirement_thenReturnTrue() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getDebuffFreeStreak()).thenReturn(30);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertTrue(condition.isMet(context));
        }

        @Test
        @DisplayName("isMet() retorna true cuando racha > días requeridos")
        void whenStreakExceedsRequirement_thenReturnTrue() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getDebuffFreeStreak()).thenReturn(45);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertTrue(condition.isMet(context));
        }

        @Test
        @DisplayName("isMet() retorna false cuando racha < días requeridos")
        void whenStreakBelowRequirement_thenReturnFalse() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getDebuffFreeStreak()).thenReturn(29);

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
            DebuffFreeStreak condition = new DebuffFreeStreak(30);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getDebuffFreeStreak()).thenReturn(15);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertEquals(0.5, condition.getProgress(context), 0.01);
        }

        @Test
        @DisplayName("getProgress() no excede 1.0")
        void whenStreakExceeds_thenProgressCapsAt1() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getDebuffFreeStreak()).thenReturn(60);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            assertEquals(1.0, condition.getProgress(context), 0.01);
        }

        @Test
        @DisplayName("getProgressText() formatea correctamente")
        void whenGettingProgressText_thenFormatsCorrectly() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);

            GateTracker tracker = mock(GateTracker.class);
            when(tracker.getDebuffFreeStreak()).thenReturn(15);

            Player player = mock(Player.class);
            ConditionContext context = ConditionContext.create(player, null, tracker);

            String progressText = condition.getProgressText(context);
            assertTrue(progressText.contains("15"));
            assertTrue(progressText.contains("30"));
        }
    }

    @Nested
    class Metadata {
        @Test
        @DisplayName("getType() retorna DEBUFF_FREE_STREAK")
        void whenGettingType_thenReturnsCorrectType() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);
            assertEquals(ConditionType.DEBUFF_FREE_STREAK, condition.getType());
        }

        @Test
        @DisplayName("canReset() retorna true")
        void whenCheckingCanReset_thenReturnsTrue() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);
            assertTrue(condition.canReset());
        }

        @Test
        @DisplayName("getDescription() incluye número de días")
        void whenGettingDescription_thenIncludesDays() {
            DebuffFreeStreak condition = new DebuffFreeStreak(30);
            String description = condition.getDescription();

            assertTrue(description.contains("30"));
            assertTrue(description.contains("debuff") || description.contains("Debuff"));
        }
    }
}
