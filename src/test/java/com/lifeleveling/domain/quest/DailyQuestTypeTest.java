package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestReward;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DailyQuestType - Los 7 Hábitos Diarios")
class DailyQuestTypeTest {

    @Nested
    class Properties {
        @Test
        @DisplayName("Existen exactamente 7 tipos de Daily Quest")
        void thereAreExactly7Types() {
            assertEquals(7, DailyQuestType.values().length);
        }

        @ParameterizedTest
        @EnumSource(DailyQuestType.class)
        @DisplayName("Todos los tipos tienen icono")
        void allTypesHaveIcon(DailyQuestType type) {
            assertNotNull(type.getIcon());
            assertFalse(type.getIcon().isBlank());
        }

        @ParameterizedTest
        @EnumSource(DailyQuestType.class)
        @DisplayName("Todos los tipos tienen nombre")
        void allTypesHaveName(DailyQuestType type) {
            assertNotNull(type.getName());
            assertFalse(type.getName().isBlank());
        }

        @ParameterizedTest
        @EnumSource(DailyQuestType.class)
        @DisplayName("Todos los tipos tienen descripción")
        void allTypesHaveDescription(DailyQuestType type) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isBlank());
        }

        @ParameterizedTest
        @EnumSource(DailyQuestType.class)
        @DisplayName("Todas las Daily Quests son Rango E")
        void allDailyQuestsAreRankE(DailyQuestType type) {
            assertEquals(QuestRank.E, type.getRank());
        }
    }

    @Nested
    class InputTypes {
        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY"})
        @DisplayName("Quests Boolean-based")
        void booleanQuestshaveBooleanInput(DailyQuestType type) {
            assertTrue(type.requiresBooleanInput());
            assertFalse(type.requiresNumericInput());
            assertFalse(type.isExternallyManaged());
            assertEquals(DailyQuestType.InputType.BOOLEAN, type.getInputType());
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"SLEEP", "READ"})
        @DisplayName("Quests Integer-based")
        void integerQuestsHaveNumericInput(DailyQuestType type) {
            assertTrue(type.requiresNumericInput());
            assertFalse(type.requiresBooleanInput());
            assertFalse(type.isExternallyManaged());
            assertEquals(DailyQuestType.InputType.INTEGER, type.getInputType());
        }

        @Test
        @DisplayName("CODE es gestionado externamente por CareerEngine")
        void code_isExternallyManaged() {
            assertTrue(DailyQuestType.CODE.isExternallyManaged());
            assertFalse(DailyQuestType.CODE.requiresBooleanInput());
            assertFalse(DailyQuestType.CODE.requiresNumericInput());
            assertEquals(DailyQuestType.InputType.EXTERNAL, DailyQuestType.CODE.getInputType());
        }
    }

    @Nested
    class HPEffects {
        @Test
        @DisplayName("SLEEP tiene HP dinámico (0, 15 o 30 según horas)")
        void sleep_hasDynamicHP() {
            // Base es 0, pero se calcula dinámicamente
            assertEquals(0, DailyQuestType.SLEEP.getHpEffect());
            assertTrue(DailyQuestType.SLEEP.grantsHP()); // Devuelve true porque es SLEEP
            assertFalse(DailyQuestType.SLEEP.costsHP());
            assertTrue(DailyQuestType.SLEEP.affectsHP());

            // Verificar cálculo dinámico
            assertEquals(0, DailyQuestType.SLEEP.calculateDynamicHP(5));   // < 6h
            assertEquals(15, DailyQuestType.SLEEP.calculateDynamicHP(6));  // 6h-6.9h
            assertEquals(30, DailyQuestType.SLEEP.calculateDynamicHP(7));  // >= 7h
            assertEquals(30, DailyQuestType.SLEEP.calculateDynamicHP(9));  // >= 7h
        }

        @Test
        @DisplayName("GYM cuesta -5 HP")
        void gym_costsHP() {
            assertEquals(-5, DailyQuestType.GYM.getHpEffect());
            assertTrue(DailyQuestType.GYM.costsHP());
            assertFalse(DailyQuestType.GYM.grantsHP());
            assertTrue(DailyQuestType.GYM.affectsHP());
        }

        @Test
        @DisplayName("DIET otorga +5 HP (según Biblia)")
        void diet_grantsHP() {
            assertEquals(5, DailyQuestType.DIET.getHpEffect());
            assertTrue(DailyQuestType.DIET.grantsHP());
            assertFalse(DailyQuestType.DIET.costsHP());
            assertTrue(DailyQuestType.DIET.affectsHP());
        }

        @Test
        @DisplayName("SKINCARE otorga +10 HP (según Biblia)")
        void skincare_grantsHP() {
            assertEquals(10, DailyQuestType.SKINCARE.getHpEffect());
            assertTrue(DailyQuestType.SKINCARE.grantsHP());
            assertFalse(DailyQuestType.SKINCARE.costsHP());
            assertTrue(DailyQuestType.SKINCARE.affectsHP());
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"CODE", "READ", "TIDY"})
        @DisplayName("CODE, READ y TIDY no afectan HP directamente")
        void neutralQuests_doNotAffectHP(DailyQuestType type) {
            assertEquals(0, type.getHpEffect());
            assertFalse(type.grantsHP());
            assertFalse(type.costsHP());
            assertFalse(type.affectsHP());
        }
    }

    @Nested
    class BooleanRewards {
        @Test
        @DisplayName("DIET completada da +50 XP General")
        void diet_gives50GeneralXP() {
            QuestReward reward = DailyQuestType.DIET.calculateReward(true);

            assertEquals(50, reward.generalXP());
            assertEquals(0, reward.gold());
            assertTrue(reward.statXP().isEmpty());
        }

        @Test
        @DisplayName("GYM completado da +50 XP Fuerza")
        void gym_gives50StrengthXP() {
            QuestReward reward = DailyQuestType.GYM.calculateReward(true);

            assertEquals(50, reward.getStatXP(StatType.STRENGTH));
            assertEquals(0, reward.generalXP());
            assertEquals(0, reward.gold());
        }

        @Test
        @DisplayName("SKINCARE completado da +50 XP Carisma")
        void skincare_gives50CharismaXP() {
            QuestReward reward = DailyQuestType.SKINCARE.calculateReward(true);

            assertEquals(50, reward.getStatXP(StatType.CHARISMA));
        }

        @Test
        @DisplayName("TIDY completado da +50 XP Disciplina (según Biblia: ordenar requiere voluntad)")
        void tidy_gives50DisciplineXP() {
            QuestReward reward = DailyQuestType.TIDY.calculateReward(true);

            assertEquals(50, reward.getStatXP(StatType.DISCIPLINE));
            assertEquals(0, reward.getStatXP(StatType.WISDOM));
        }

        @Test
        @DisplayName("CODE completado no da XP directa (viene del Career Engine)")
        void code_givesNoDirectXP() {
            QuestReward reward = DailyQuestType.CODE.calculateReward(true);

            assertTrue(reward.isEmpty());
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY"})
        @DisplayName("Boolean quests no completadas (false) no dan reward")
        void whenNotCompleted_thenNoReward(DailyQuestType type) {
            QuestReward reward = type.calculateReward(false);

            assertTrue(reward.isEmpty());
        }

        @Test
        @DisplayName("CODE siempre devuelve reward vacío (XP viene de CareerEngine)")
        void code_alwaysReturnsEmptyReward() {
            assertTrue(DailyQuestType.CODE.calculateReward(true).isEmpty());
            assertTrue(DailyQuestType.CODE.calculateReward(false).isEmpty());
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"SLEEP", "READ"})
        @DisplayName("Usar calculateReward(boolean) en quest INTEGER lanza excepción")
        void whenUsingBooleanRewardOnIntegerQuest_thenThrowsException(DailyQuestType type) {
            assertThrows(IllegalStateException.class,
                    () -> type.calculateReward(true));
        }
    }

    @Nested
    class IntegerRewards {
        @Test
        @DisplayName("SLEEP da +15 XP General por hora (según Biblia), máximo 8.5h")
        void sleep_gives15XPPerHour() {
            // 15 XP/h según la Biblia
            assertEquals(105, DailyQuestType.SLEEP.calculateReward(7).generalXP());  // 7 * 15 = 105
            assertEquals(120, DailyQuestType.SLEEP.calculateReward(8).generalXP());  // 8 * 15 = 120

            // Máximo 8.5h computables (127.5 -> 127 XP)
            assertEquals(127, DailyQuestType.SLEEP.calculateReward(9).generalXP());  // min(9, 8.5) * 15 = 127
            assertEquals(127, DailyQuestType.SLEEP.calculateReward(12).generalXP()); // Capped at 8.5h
        }

        @Test
        @DisplayName("READ da +5 XP Sabiduría por página")
        void read_gives5WisdomXPPerPage() {
            QuestReward reward10 = DailyQuestType.READ.calculateReward(10);
            QuestReward reward20 = DailyQuestType.READ.calculateReward(20);

            assertEquals(50, reward10.getStatXP(StatType.WISDOM));
            assertEquals(100, reward20.getStatXP(StatType.WISDOM));
        }

        @Test
        @DisplayName("calculateReward(int) con valor negativo lanza excepción")
        void whenNegativeValue_thenThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> DailyQuestType.SLEEP.calculateReward(-5));
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY"})
        @DisplayName("Usar calculateReward(int) en quest BOOLEAN lanza excepción")
        void whenUsingIntegerRewardOnBooleanQuest_thenThrowsException(DailyQuestType type) {
            assertThrows(IllegalStateException.class,
                    () -> type.calculateReward(10));
        }

        @Test
        @DisplayName("CODE también lanza excepción con calculateReward(int)")
        void code_throwsExceptionWithIntegerReward() {
            assertThrows(IllegalStateException.class,
                    () -> DailyQuestType.CODE.calculateReward(10));
        }
    }

    @Nested
    class Conditions {
        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY"})
        @DisplayName("Boolean quests: meetsCondition(true) = true")
        void booleanQuests_meetConditionWhenTrue(DailyQuestType type) {
            assertTrue(type.meetsCondition(true));
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY"})
        @DisplayName("Boolean quests: meetsCondition(false) = false")
        void booleanQuests_doNotMeetConditionWhenFalse(DailyQuestType type) {
            assertFalse(type.meetsCondition(false));
        }

        @Test
        @DisplayName("CODE: meetsCondition() lanza excepción (gestionado externamente)")
        void code_meetsConditionThrowsException() {
            assertThrows(IllegalStateException.class,
                    () -> DailyQuestType.CODE.meetsCondition(true));
        }

        @Test
        @DisplayName("SLEEP: meetsCondition >= 6 horas (según Biblia, Tier 1 de HP)")
        void sleep_meetsConditionWhen6OrMore() {
            assertFalse(DailyQuestType.SLEEP.meetsCondition(5));
            assertTrue(DailyQuestType.SLEEP.meetsCondition(6));  // Tier 1: +15 HP
            assertTrue(DailyQuestType.SLEEP.meetsCondition(7));  // Tier 2: +30 HP
            assertTrue(DailyQuestType.SLEEP.meetsCondition(8));
            assertTrue(DailyQuestType.SLEEP.meetsCondition(10));
        }

        @Test
        @DisplayName("READ: meetsCondition >= 10 páginas")
        void read_meetsConditionWhen10OrMore() {
            assertFalse(DailyQuestType.READ.meetsCondition(9));
            assertTrue(DailyQuestType.READ.meetsCondition(10));
            assertTrue(DailyQuestType.READ.meetsCondition(15));
            assertTrue(DailyQuestType.READ.meetsCondition(100));
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"SLEEP", "READ"})
        @DisplayName("Usar meetsCondition(boolean) en quest INTEGER lanza excepción")
        void whenUsingBooleanConditionOnIntegerQuest_thenThrowsException(DailyQuestType type) {
            assertThrows(IllegalStateException.class,
                    () -> type.meetsCondition(true));
        }

        @ParameterizedTest
        @EnumSource(value = DailyQuestType.class, names = {"DIET", "GYM", "SKINCARE", "TIDY"})
        @DisplayName("Usar meetsCondition(int) en quest BOOLEAN lanza excepción")
        void whenUsingIntegerConditionOnBooleanQuest_thenThrowsException(DailyQuestType type) {
            assertThrows(IllegalStateException.class,
                    () -> type.meetsCondition(10));
        }
    }

    @Nested
    class Search {
        @ParameterizedTest(name = "Buscar por enum name: {0}")
        @ValueSource(strings = {"SLEEP", "DIET", "GYM", "CODE", "READ", "SKINCARE", "TIDY"})
        @DisplayName("fromString() encuentra tipos por nombre enum")
        void whenSearchingByEnumName_thenFound(String name) {
            assertTrue(DailyQuestType.fromString(name).isPresent());
        }

        @ParameterizedTest(name = "Case-insensitive: {0}")
        @ValueSource(strings = {"sleep", "SLEEP", "Sleep", "sLeEp"})
        @DisplayName("fromString() es case-insensitive")
        void whenSearchingWithDifferentCasing_thenFound(String name) {
            assertTrue(DailyQuestType.fromString(name).isPresent());
            assertEquals(DailyQuestType.SLEEP, DailyQuestType.fromString(name).get());
        }

        @Test
        @DisplayName("fromString() con nombre inválido retorna Optional vacío")
        void whenNameIsInvalid_thenReturnsEmpty() {
            assertTrue(DailyQuestType.fromString("INVALID").isEmpty());
        }

        @Test
        @DisplayName("fromString() con null retorna Optional vacío")
        void whenNameIsNull_thenReturnsEmpty() {
            assertTrue(DailyQuestType.fromString(null).isEmpty());
        }

        @Test
        @DisplayName("fromString() con string vacío retorna Optional vacío")
        void whenNameIsBlank_thenReturnsEmpty() {
            assertTrue(DailyQuestType.fromString("   ").isEmpty());
        }
    }

    @Nested
    class UIFormat {
        @Test
        @DisplayName("toDisplayString() formatea con icono y nombre")
        void whenFormattingDisplay_thenIncludesIconAndName() {
            String display = DailyQuestType.SLEEP.toDisplayString();

            assertTrue(display.contains("💤"));
            assertTrue(display.contains("Descanso"));
        }

        @ParameterizedTest
        @EnumSource(DailyQuestType.class)
        @DisplayName("Todos los tipos tienen formato display válido")
        void allTypesHaveValidDisplayFormat(DailyQuestType type) {
            String display = type.toDisplayString();

            assertNotNull(display);
            assertFalse(display.isBlank());
            assertTrue(display.contains(type.getIcon()));
            assertTrue(display.contains(type.getName()));
        }
    }

    @Nested
    class SpecificQuests {
        @Test
        @DisplayName("SLEEP tiene todas las propiedades correctas")
        void sleep_hasCorrectProperties() {
            assertEquals("💤", DailyQuestType.SLEEP.getIcon());
            assertEquals("Descanso", DailyQuestType.SLEEP.getName());
            assertTrue(DailyQuestType.SLEEP.requiresNumericInput());
            assertEquals(0, DailyQuestType.SLEEP.getHpEffect()); // HP es dinámico
            assertTrue(DailyQuestType.SLEEP.getBaseStatXP().isEmpty());
        }

        @Test
        @DisplayName("GYM tiene todas las propiedades correctas")
        void gym_hasCorrectProperties() {
            assertEquals("🏋️", DailyQuestType.GYM.getIcon());
            assertTrue(DailyQuestType.GYM.requiresBooleanInput());
            assertEquals(-5, DailyQuestType.GYM.getHpEffect());
            assertEquals(50, DailyQuestType.GYM.getBaseStatXP().get(StatType.STRENGTH));
        }

        @Test
        @DisplayName("CODE es gestionado por CareerEngine")
        void code_hasCorrectProperties() {
            assertEquals("💻", DailyQuestType.CODE.getIcon());
            assertTrue(DailyQuestType.CODE.isExternallyManaged());
            assertEquals(0, DailyQuestType.CODE.getHpEffect()); // HP gestionado por CareerEngine
            assertTrue(DailyQuestType.CODE.getBaseStatXP().isEmpty()); // XP gestionada por CareerEngine
        }
    }
}