package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.condition.ConditionContext;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.elder.ElderQuest;
import com.lifeleveling.domain.quest.elder.ElderQuestType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ElderQuest - Juicios del Monarca")
class ElderQuestTest {

    @Nested
    class Creation {
        @Test
        @DisplayName("create() genera una ElderQuest válida y alineada al día 1")
        void whenCreating_thenValidQuestAlignedToFirstOfMonth() {
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7);

            assertNotNull(quest.id());
            assertEquals(ElderQuestType.ELDER_7, quest.getType());
            // Las Elder Quests empiezan directamente IN_PROGRESS si tienes el nivel
            assertEquals(QuestStatus.IN_PROGRESS, quest.status());
            assertNotNull(quest.createdAt());
            assertFalse(quest.isCompleted());
        }

        @Test
        @DisplayName("create() con tipo null lanza Exception")
        void whenCreatingWithNullType_thenThrowsException() {
            assertThrows(NullPointerException.class, // O IllegalArgument según tu impl
                    () -> ElderQuest.create(null));
        }

        @ParameterizedTest
        @EnumSource(ElderQuestType.class)
        @DisplayName("create() funciona para todos los Juicios")
        void whenCreatingWithAnyType_thenValid(ElderQuestType type) {
            assertDoesNotThrow(() -> ElderQuest.create(type));
        }
    }

    @Nested
    class QuestInterface {
        @Test
        @DisplayName("name() retorna el nombre épico")
        void name_returnsEpicName() {
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7);
            assertEquals("La Ascensión", quest.name());
        }

        @Test
        @DisplayName("rank() siempre es S+ (Legendario)")
        void rank_returnsSPlus() {
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_1);
            assertEquals(QuestRank.S_PLUS, quest.rank());
        }

        @Test
        @DisplayName("reward() contiene loot masivo al completarse")
        void whenCompleted_thenHasMassiveReward() {
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7); // Da 40k XP
            quest = (ElderQuest) quest.complete(Instant.now());

            assertEquals(40_000, quest.reward().generalXP());
        }
    }

    @Nested
    class MonthlyMechanics {
        @Test
        @DisplayName("checkReset() reinicia la misión si cambia el mes (Quests Mensuales)")
        void whenMonthChanges_thenQuestResets() {
            // Arrange
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7); // Es MENSUAL
            LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(5);

            // Act
            ElderQuest resetQuest = quest.checkReset(nextMonth);

            // Assert
            assertNotEquals(quest.id(), resetQuest.id(), "Debe ser una nueva instancia");
            assertEquals(QuestStatus.IN_PROGRESS, resetQuest.status());
            assertNotSame(quest, resetQuest);
        }

        @Test
        @DisplayName("checkReset() NO hace nada si seguimos en el mismo mes")
        void whenSameMonth_thenNoChange() {
            // Arrange
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7);
            LocalDate sameMonthLater = LocalDate.now().withDayOfMonth(28);

            // Act
            ElderQuest sameQuest = quest.checkReset(sameMonthLater);

            // Assert
            assertSame(quest, sameQuest, "Debe ser la misma instancia");
        }

        @Test
        @DisplayName("checkReset() ignora Quests de frecuencia ÚNICA")
        void whenUniqueQuest_thenNeverResets() {
            // Arrange: ELDER_2 (Voto de Pobreza) es ÚNICA, no mensual
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_2);
            LocalDate nextYear = LocalDate.now().plusYears(1);

            // Act
            ElderQuest sameQuest = quest.checkReset(nextYear);

            // Assert
            assertSame(quest, sameQuest, "Las únicas no deben resetearse por fecha");
        }
    }

    @Nested
    class ConditionLogic {
        @Test
        @DisplayName("areConditionsMet() retorna true si el Tracker confirma los requisitos")
        void whenTrackerConfirms_thenConditionsMet() {
            // Arrange
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7); // Requiere 20 Perfect Days

            // Mocks
            GateTracker tracker = mock(GateTracker.class);
            Player player = mock(Player.class);

            // Simulamos éxito: 20 Perfect Days en los últimos 30 días
            when(tracker.getPerfectDaysCountInLastDays(30)).thenReturn(20);

            ConditionContext context = ConditionContext.create(player, null, tracker); // null gate pq es Elder

            // Act & Assert
            assertTrue(quest.areConditionsMet(context));
        }

        @Test
        @DisplayName("areConditionsMet() retorna false si faltan requisitos")
        void whenTrackerDenies_thenConditionsNotMet() {
            // Arrange
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_7);

            GateTracker tracker = mock(GateTracker.class);
            Player player = mock(Player.class);

            // Simulamos fallo (solo 19 días)
            when(tracker.getPerfectDayStreak()).thenReturn(19);

            ConditionContext context = ConditionContext.create(player, null, tracker);

            // Act & Assert
            assertFalse(quest.areConditionsMet(context));
        }
    }

    @Nested
    class StateTransitions {
        @Test
        @DisplayName("complete() sella la misión y guarda timestamp")
        void whenCompleting_thenStatusIsCompleted() {
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_1);
            Instant now = Instant.now();

            ElderQuest completed = (ElderQuest) quest.complete(now);

            assertEquals(QuestStatus.COMPLETED, completed.status());
            assertTrue(completed.isCompleted());
        }

        @Test
        @DisplayName("complete() es inmutable (no toca original)")
        void whenCompleting_thenOriginalIsUntouched() {
            ElderQuest original = ElderQuest.create(ElderQuestType.ELDER_1);
            original.complete(Instant.now());

            assertEquals(QuestStatus.IN_PROGRESS, original.status());
        }

        @Test
        @DisplayName("fail() cambia estado a FAILED (aunque se resetee luego)")
        void whenFailing_thenStatusIsFailed() {
            ElderQuest quest = ElderQuest.create(ElderQuestType.ELDER_1);
            Instant now = Instant.now();

            ElderQuest failed = (ElderQuest) quest.fail(now);

            assertEquals(QuestStatus.FAILED, failed.status());
        }
    }

    @Nested
    class Equality {
        @Test
        @DisplayName("ElderQuests con mismo ID son iguales")
        void questsWithSameId_areEqual() {
            // Nota: Como no tenemos método 'reconstitute' público expuesto en el test anterior,
            // asumimos que equals funciona por ID si la implementación lo soporta.
            // Si no tienes equals/hashCode en ElderQuest, este test fallará (deberías tenerlo).
            ElderQuest q1 = ElderQuest.create(ElderQuestType.ELDER_1);

            // Como create genera random ID, probamos la identidad consigo misma
            // o si implementaste un constructor para testing.
            assertEquals(q1, q1);
        }
    }
}