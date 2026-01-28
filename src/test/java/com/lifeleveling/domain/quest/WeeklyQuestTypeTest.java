package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestType;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestType.WeeklyConditionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeeklyQuestType - Los 6 Desafíos Semanales Rotativos")
class WeeklyQuestTypeTest {

    @Nested
    class Properties {
        @Test
        @DisplayName("Existen exactamente 6 tipos de Weekly Quest")
        void thereAreExactly6Types() {
            assertEquals(6, WeeklyQuestType.values().length);
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen icono")
        void allTypesHaveIcon(WeeklyQuestType type) {
            assertNotNull(type.getIcon());
            assertFalse(type.getIcon().isBlank());
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen nombre")
        void allTypesHaveName(WeeklyQuestType type) {
            assertNotNull(type.getName());
            assertFalse(type.getName().isBlank());
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen descripción")
        void allTypesHaveDescription(WeeklyQuestType type) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isBlank());
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen reward no null")
        void allTypesHaveReward(WeeklyQuestType type) {
            assertNotNull(type.getReward());
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen conditionType")
        void allTypesHaveConditionType(WeeklyQuestType type) {
            assertNotNull(type.getConditionType());
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen target > 0")
        void allTypesHavePositiveTarget(WeeklyQuestType type) {
            assertTrue(type.getTarget() > 0, "Target debe ser positivo para " + type);
        }
    }

    @Nested
    class Ranks {
        @Test
        @DisplayName("THE_STREAK es Rango B (el más difícil)")
        void theStreak_isRankB() {
            assertEquals(QuestRank.B, WeeklyQuestType.THE_STREAK.getRank());
        }

        @ParameterizedTest
        @EnumSource(value = WeeklyQuestType.class, names = {"HIBERNATOR", "PURE_TEMPLE", "IRON_WEEK", "CODE_MARATHON", "THE_SCHOLAR"})
        @DisplayName("Las demás Weekly Quests son Rango C")
        void otherQuests_areRankC(WeeklyQuestType type) {
            assertEquals(QuestRank.C, type.getRank());
        }
    }

    @Nested
    class ConditionTypes {
        @Test
        @DisplayName("HIBERNATOR usa DAILY_HABIT_COUNT")
        void hibernator_usesDailyHabitCount() {
            assertEquals(WeeklyConditionType.DAILY_HABIT_COUNT, WeeklyQuestType.HIBERNATOR.getConditionType());
            assertEquals(DailyQuestType.SLEEP, WeeklyQuestType.HIBERNATOR.getRelatedHabit());
            assertEquals(6, WeeklyQuestType.HIBERNATOR.getDaysRequired());
        }

        @Test
        @DisplayName("PURE_TEMPLE usa DUAL_DAILY_HABIT_COUNT")
        void pureTemple_usesDualDailyHabitCount() {
            assertEquals(WeeklyConditionType.DUAL_DAILY_HABIT_COUNT, WeeklyQuestType.PURE_TEMPLE.getConditionType());
            List<DailyQuestType> habits = WeeklyQuestType.PURE_TEMPLE.getRequiredHabits();
            assertEquals(2, habits.size());
            assertTrue(habits.contains(DailyQuestType.DIET));
            assertTrue(habits.contains(DailyQuestType.SKINCARE));
            assertEquals(6, WeeklyQuestType.PURE_TEMPLE.getDaysRequired());
        }

        @Test
        @DisplayName("IRON_WEEK usa DAILY_HABIT_COUNT para GYM")
        void ironWeek_usesDailyHabitCount() {
            assertEquals(WeeklyConditionType.DAILY_HABIT_COUNT, WeeklyQuestType.IRON_WEEK.getConditionType());
            assertEquals(DailyQuestType.GYM, WeeklyQuestType.IRON_WEEK.getRelatedHabit());
            assertEquals(5, WeeklyQuestType.IRON_WEEK.getDaysRequired());
        }

        @Test
        @DisplayName("CODE_MARATHON usa ACCUMULATED_HOURS")
        void codeMarathon_usesAccumulatedHours() {
            assertEquals(WeeklyConditionType.ACCUMULATED_HOURS, WeeklyQuestType.CODE_MARATHON.getConditionType());
            assertEquals(DailyQuestType.CODE, WeeklyQuestType.CODE_MARATHON.getRelatedHabit());
            assertEquals(12, WeeklyQuestType.CODE_MARATHON.getAccumulatedTarget());
        }

        @Test
        @DisplayName("THE_SCHOLAR usa ACCUMULATED_PAGES")
        void theScholar_usesAccumulatedPages() {
            assertEquals(WeeklyConditionType.ACCUMULATED_PAGES, WeeklyQuestType.THE_SCHOLAR.getConditionType());
            assertEquals(DailyQuestType.READ, WeeklyQuestType.THE_SCHOLAR.getRelatedHabit());
            assertEquals(150, WeeklyQuestType.THE_SCHOLAR.getAccumulatedTarget());
        }

        @Test
        @DisplayName("THE_STREAK usa PERFECT_DAYS_COUNT")
        void theStreak_usesPerfectDaysCount() {
            assertEquals(WeeklyConditionType.PERFECT_DAYS_COUNT, WeeklyQuestType.THE_STREAK.getConditionType());
            assertNull(WeeklyQuestType.THE_STREAK.getRelatedHabit());
            assertEquals(5, WeeklyQuestType.THE_STREAK.getDaysRequired());
        }
    }

    @Nested
    class Targets {
        @Test
        @DisplayName("HIBERNATOR: target = 6 días")
        void hibernator_target6Days() {
            assertEquals(6, WeeklyQuestType.HIBERNATOR.getTarget());
            assertEquals("días", WeeklyQuestType.HIBERNATOR.getTargetUnit());
        }

        @Test
        @DisplayName("PURE_TEMPLE: target = 6 días")
        void pureTemple_target6Days() {
            assertEquals(6, WeeklyQuestType.PURE_TEMPLE.getTarget());
            assertEquals("días", WeeklyQuestType.PURE_TEMPLE.getTargetUnit());
        }

        @Test
        @DisplayName("IRON_WEEK: target = 5 días")
        void ironWeek_target5Days() {
            assertEquals(5, WeeklyQuestType.IRON_WEEK.getTarget());
            assertEquals("días", WeeklyQuestType.IRON_WEEK.getTargetUnit());
        }

        @Test
        @DisplayName("CODE_MARATHON: target = 12 horas")
        void codeMarathon_target12Hours() {
            assertEquals(12, WeeklyQuestType.CODE_MARATHON.getTarget());
            assertEquals("horas", WeeklyQuestType.CODE_MARATHON.getTargetUnit());
        }

        @Test
        @DisplayName("THE_SCHOLAR: target = 150 páginas")
        void theScholar_target150Pages() {
            assertEquals(150, WeeklyQuestType.THE_SCHOLAR.getTarget());
            assertEquals("páginas", WeeklyQuestType.THE_SCHOLAR.getTargetUnit());
        }

        @Test
        @DisplayName("THE_STREAK: target = 5 Perfect Days")
        void theStreak_target5PerfectDays() {
            assertEquals(5, WeeklyQuestType.THE_STREAK.getTarget());
            assertEquals("Perfect Days", WeeklyQuestType.THE_STREAK.getTargetUnit());
        }
    }

    @Nested
    class Conditions {
        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("isConditionMet() = true cuando progreso >= target")
        void isConditionMet_whenProgressMeetsTarget(WeeklyQuestType type) {
            int target = type.getTarget();
            assertTrue(type.isConditionMet(target));
            assertTrue(type.isConditionMet(target + 1));
            assertTrue(type.isConditionMet(target + 100));
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("isConditionMet() = false cuando progreso < target")
        void isConditionMet_falseWhenProgressBelowTarget(WeeklyQuestType type) {
            int target = type.getTarget();
            assertFalse(type.isConditionMet(target - 1));
            assertFalse(type.isConditionMet(0));
        }

        @Test
        @DisplayName("isConditionMet() con valores límite")
        void isConditionMet_boundaryValues() {
            // HIBERNATOR: need 6 days
            assertFalse(WeeklyQuestType.HIBERNATOR.isConditionMet(5));
            assertTrue(WeeklyQuestType.HIBERNATOR.isConditionMet(6));
            assertTrue(WeeklyQuestType.HIBERNATOR.isConditionMet(7));

            // THE_SCHOLAR: need 150 pages
            assertFalse(WeeklyQuestType.THE_SCHOLAR.isConditionMet(149));
            assertTrue(WeeklyQuestType.THE_SCHOLAR.isConditionMet(150));
            assertTrue(WeeklyQuestType.THE_SCHOLAR.isConditionMet(200));
        }
    }

    @Nested
    class ProgressPercentage {
        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("getProgressPercentage() = 0.0 cuando progreso = 0")
        void progressPercentage_zeroWhenNoProgress(WeeklyQuestType type) {
            assertEquals(0.0, type.getProgressPercentage(0));
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("getProgressPercentage() = 1.0 cuando progreso >= target")
        void progressPercentage_oneWhenMeetsTarget(WeeklyQuestType type) {
            assertEquals(1.0, type.getProgressPercentage(type.getTarget()));
            assertEquals(1.0, type.getProgressPercentage(type.getTarget() + 50));
        }

        @Test
        @DisplayName("getProgressPercentage() calcula correctamente valores intermedios")
        void progressPercentage_calculatesIntermediateValues() {
            // HIBERNATOR: 6 days target
            assertEquals(0.5, WeeklyQuestType.HIBERNATOR.getProgressPercentage(3), 0.001);

            // THE_SCHOLAR: 150 pages target
            assertEquals(0.5, WeeklyQuestType.THE_SCHOLAR.getProgressPercentage(75), 0.001);

            // CODE_MARATHON: 12 hours target
            assertEquals(0.25, WeeklyQuestType.CODE_MARATHON.getProgressPercentage(3), 0.001);
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("getProgressPercentage() no supera 1.0")
        void progressPercentage_neverExceedsOne(WeeklyQuestType type) {
            assertTrue(type.getProgressPercentage(type.getTarget() * 10) <= 1.0);
        }
    }

    @Nested
    class Rewards {
        @Test
        @DisplayName("HIBERNATOR: 1,000 G")
        void hibernator_gives1000Gold() {
            QuestReward reward = WeeklyQuestType.HIBERNATOR.getReward();
            assertEquals(1_000, reward.gold());
            assertEquals(0, reward.generalXP());
            assertTrue(reward.statXP().isEmpty());
        }

        @Test
        @DisplayName("PURE_TEMPLE: +500 CHA XP + 1,000 G")
        void pureTemple_gives500CharismaAnd1000Gold() {
            QuestReward reward = WeeklyQuestType.PURE_TEMPLE.getReward();
            assertEquals(1_000, reward.gold());
            assertEquals(500, reward.getStatXP(StatType.CHARISMA));
            assertEquals(0, reward.generalXP());
        }

        @Test
        @DisplayName("IRON_WEEK: +600 STR XP + 1,000 G")
        void ironWeek_gives600StrengthAnd1000Gold() {
            QuestReward reward = WeeklyQuestType.IRON_WEEK.getReward();
            assertEquals(1_000, reward.gold());
            assertEquals(600, reward.getStatXP(StatType.STRENGTH));
            assertEquals(0, reward.generalXP());
        }

        @Test
        @DisplayName("CODE_MARATHON: +800 INT XP + 1,500 G")
        void codeMarathon_gives800IntellectAnd1500Gold() {
            QuestReward reward = WeeklyQuestType.CODE_MARATHON.getReward();
            assertEquals(1_500, reward.gold());
            assertEquals(800, reward.getStatXP(StatType.INTELLECT));
            assertEquals(0, reward.generalXP());
        }

        @Test
        @DisplayName("THE_SCHOLAR: +600 WIS XP + 1,000 G")
        void theScholar_gives600WisdomAnd1000Gold() {
            QuestReward reward = WeeklyQuestType.THE_SCHOLAR.getReward();
            assertEquals(1_000, reward.gold());
            assertEquals(600, reward.getStatXP(StatType.WISDOM));
            assertEquals(0, reward.generalXP());
        }

        @Test
        @DisplayName("THE_STREAK: +2,000 XP General + 3,000 G")
        void theStreak_gives2000GeneralXpAnd3000Gold() {
            QuestReward reward = WeeklyQuestType.THE_STREAK.getReward();
            assertEquals(3_000, reward.gold());
            assertEquals(2_000, reward.generalXP());
            assertTrue(reward.statXP().isEmpty());
        }
    }

    @Nested
    class RequiredHabits {
        @Test
        @DisplayName("HIBERNATOR retorna SLEEP como hábito requerido")
        void hibernator_returnsRelatedHabit() {
            List<DailyQuestType> habits = WeeklyQuestType.HIBERNATOR.getRequiredHabits();
            assertEquals(1, habits.size());
            assertEquals(DailyQuestType.SLEEP, habits.get(0));
        }

        @Test
        @DisplayName("PURE_TEMPLE retorna DIET y SKINCARE")
        void pureTemple_returnsDietAndSkincare() {
            List<DailyQuestType> habits = WeeklyQuestType.PURE_TEMPLE.getRequiredHabits();
            assertEquals(2, habits.size());
            assertTrue(habits.contains(DailyQuestType.DIET));
            assertTrue(habits.contains(DailyQuestType.SKINCARE));
        }

        @Test
        @DisplayName("THE_STREAK retorna lista vacía (no depende de hábito específico)")
        void theStreak_returnsEmptyList() {
            List<DailyQuestType> habits = WeeklyQuestType.THE_STREAK.getRequiredHabits();
            assertTrue(habits.isEmpty());
        }
    }

    @Nested
    class Search {
        @ParameterizedTest(name = "Buscar por enum name: {0}")
        @ValueSource(strings = {"HIBERNATOR", "PURE_TEMPLE", "IRON_WEEK", "CODE_MARATHON", "THE_SCHOLAR", "THE_STREAK"})
        @DisplayName("fromString() encuentra tipos por nombre enum")
        void whenSearchingByEnumName_thenFound(String name) {
            assertTrue(WeeklyQuestType.fromString(name).isPresent());
        }

        @ParameterizedTest(name = "Case-insensitive: {0}")
        @ValueSource(strings = {"hibernator", "HIBERNATOR", "Hibernator", "hIbErNaToR"})
        @DisplayName("fromString() es case-insensitive")
        void whenSearchingWithDifferentCasing_thenFound(String name) {
            assertTrue(WeeklyQuestType.fromString(name).isPresent());
            assertEquals(WeeklyQuestType.HIBERNATOR, WeeklyQuestType.fromString(name).get());
        }

        @Test
        @DisplayName("fromString() encuentra por display name")
        void whenSearchingByDisplayName_thenFound() {
            assertTrue(WeeklyQuestType.fromString("El Oso Hibernador").isPresent());
            assertEquals(WeeklyQuestType.HIBERNATOR, WeeklyQuestType.fromString("El Oso Hibernador").get());
        }

        @Test
        @DisplayName("fromString() con nombre inválido retorna Optional vacío")
        void whenNameIsInvalid_thenReturnsEmpty() {
            assertTrue(WeeklyQuestType.fromString("INVALID").isEmpty());
        }

        @Test
        @DisplayName("fromString() con null retorna Optional vacío")
        void whenNameIsNull_thenReturnsEmpty() {
            assertTrue(WeeklyQuestType.fromString(null).isEmpty());
        }

        @Test
        @DisplayName("fromString() con string vacío retorna Optional vacío")
        void whenNameIsBlank_thenReturnsEmpty() {
            assertTrue(WeeklyQuestType.fromString("   ").isEmpty());
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea con icono y nombre")
        void whenFormattingDisplay_thenIncludesIconAndName() {
            String display = WeeklyQuestType.HIBERNATOR.toDisplayString();

            assertTrue(display.contains("🐻"));
            assertTrue(display.contains("El Oso Hibernador"));
        }

        @ParameterizedTest
        @EnumSource(WeeklyQuestType.class)
        @DisplayName("Todos los tipos tienen formato display válido")
        void allTypesHaveValidDisplayFormat(WeeklyQuestType type) {
            String display = type.toDisplayString();

            assertNotNull(display);
            assertFalse(display.isBlank());
            assertTrue(display.contains(type.getIcon()));
            assertTrue(display.contains(type.getName()));
        }

        @Test
        @DisplayName("formatProgress() formatea correctamente")
        void formatProgress_formatsCorrectly() {
            assertEquals("3/6 días", WeeklyQuestType.HIBERNATOR.formatProgress(3));
            assertEquals("75/150 páginas", WeeklyQuestType.THE_SCHOLAR.formatProgress(75));
            assertEquals("6/12 horas", WeeklyQuestType.CODE_MARATHON.formatProgress(6));
            assertEquals("2/5 Perfect Days", WeeklyQuestType.THE_STREAK.formatProgress(2));
        }
    }

    @Nested
    class SpecificQuests {
        @Test
        @DisplayName("HIBERNATOR tiene todas las propiedades correctas")
        void hibernator_hasCorrectProperties() {
            assertEquals("🐻", WeeklyQuestType.HIBERNATOR.getIcon());
            assertEquals("El Oso Hibernador", WeeklyQuestType.HIBERNATOR.getName());
            assertEquals(DailyQuestType.SLEEP, WeeklyQuestType.HIBERNATOR.getRelatedHabit());
            assertEquals(6, WeeklyQuestType.HIBERNATOR.getDaysRequired());
            assertEquals(0, WeeklyQuestType.HIBERNATOR.getAccumulatedTarget());
            assertEquals(QuestRank.C, WeeklyQuestType.HIBERNATOR.getRank());
        }

        @Test
        @DisplayName("THE_STREAK tiene todas las propiedades correctas")
        void theStreak_hasCorrectProperties() {
            assertEquals("🔥", WeeklyQuestType.THE_STREAK.getIcon());
            assertEquals("The Streak", WeeklyQuestType.THE_STREAK.getName());
            assertNull(WeeklyQuestType.THE_STREAK.getRelatedHabit());
            assertEquals(5, WeeklyQuestType.THE_STREAK.getDaysRequired());
            assertEquals(0, WeeklyQuestType.THE_STREAK.getAccumulatedTarget());
            assertEquals(QuestRank.B, WeeklyQuestType.THE_STREAK.getRank());
        }
    }
}
