package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TitleType - Catálogo de Títulos")
class TitleTypeTest {

    @Nested
    class Properties {
        @ParameterizedTest
        @EnumSource(TitleType.class)
        @DisplayName("Todos los títulos tienen displayName")
        void allTitlesHaveDisplayName(TitleType type) {
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isBlank());
        }

        @ParameterizedTest
        @EnumSource(TitleType.class)
        @DisplayName("Todos los títulos tienen categoría")
        void allTitlesHaveCategory(TitleType type) {
            assertNotNull(type.getCategory());
        }

        @ParameterizedTest
        @EnumSource(TitleType.class)
        @DisplayName("Todos los títulos tienen lore")
        void allTitlesHaveLore(TitleType type) {
            assertNotNull(type.getLore());
            assertFalse(type.getLore().isBlank());
        }

        @ParameterizedTest
        @EnumSource(TitleType.class)
        @DisplayName("Todos los títulos tienen requirementType")
        void allTitlesHaveRequirementType(TitleType type) {
            assertNotNull(type.getRequirementType());
        }

        @ParameterizedTest
        @EnumSource(TitleType.class)
        @DisplayName("Todos los títulos tienen al menos un buff")
        void allTitlesHaveBuffs(TitleType type) {
            assertNotNull(type.getBuffs());
            assertFalse(type.getBuffs().isEmpty(), "Título " + type + " no tiene buffs");
        }
    }

    @Nested
    class Categories {
        @Test
        @DisplayName("Hay títulos en todas las categorías")
        void allCategoriesHaveTitles() {
            for (TitleCategory category : TitleCategory.values()) {
                List<TitleType> titles = TitleType.getByCategory(category);
                assertFalse(titles.isEmpty(),
                        "Categoría " + category + " no tiene títulos");
            }
        }

        @Test
        @DisplayName("SURVIVAL tiene al menos 5 títulos")
        void survival_hasMinimumTitles() {
            List<TitleType> survival = TitleType.getByCategory(TitleCategory.SURVIVAL);
            assertTrue(survival.size() >= 5);
        }

        @Test
        @DisplayName("CAREER tiene al menos 5 títulos")
        void career_hasMinimumTitles() {
            List<TitleType> career = TitleType.getByCategory(TitleCategory.CAREER);
            assertTrue(career.size() >= 5);
        }

        @Test
        @DisplayName("MILESTONE tiene títulos para niveles 10-100")
        void milestone_hasLevelTitles() {
            List<TitleType> milestones = TitleType.getByCategory(TitleCategory.MILESTONE);

            // Verificar que hay títulos para niveles clave
            assertTrue(milestones.stream()
                    .anyMatch(t -> t.getRequirementValue() == 50));  // Leyenda
            assertTrue(milestones.stream()
                    .anyMatch(t -> t.getRequirementValue() == 100)); // Monarca
        }
    }

    @Nested
    class SurvivalTitles {
        @Test
        @DisplayName("SURVIVOR requiere nivel 10")
        void survivor_requiresLevel10() {
            assertEquals(TitleType.TitleRequirementType.LEVEL, TitleType.SURVIVOR.getRequirementType());
            assertEquals(10, TitleType.SURVIVOR.getRequirementValue());
        }

        @Test
        @DisplayName("SURVIVOR da +3% HP Recovery")
        void survivor_givesHPRecovery() {
            TitleBuff buff = TitleType.SURVIVOR.getBuffs().get(0);
            assertEquals(TitleBuff.BuffType.HP_RECOVERY_BONUS, buff.type());
            assertEquals(0.03, buff.value());
        }

        @Test
        @DisplayName("SLEEP_GUARDIAN requiere 14 días de racha SLEEP")
        void sleepGuardian_requiresSleepStreak() {
            assertEquals(TitleType.TitleRequirementType.HABIT_STREAK, TitleType.SLEEP_GUARDIAN.getRequirementType());
            assertEquals(14, TitleType.SLEEP_GUARDIAN.getRequirementValue());
            assertEquals(DailyQuestType.SLEEP, TitleType.SLEEP_GUARDIAN.getRelatedHabit());
        }

        @Test
        @DisplayName("IMMORTAL da +20% HP Recovery")
        void immortal_gives20HPRecovery() {
            assertTrue(TitleType.IMMORTAL.getHPRecoveryBonus() >= 0.20);
        }
    }

    @Nested
    class CareerTitles {
        @Test
        @DisplayName("HELLO_WORLD requiere primera CODE")
        void helloWorld_requiresFirstCode() {
            assertEquals(TitleType.TitleRequirementType.FIRST_COMPLETION, TitleType.HELLO_WORLD.getRequirementType());
            assertEquals(DailyQuestType.CODE, TitleType.HELLO_WORLD.getRelatedHabit());
        }

        @Test
        @DisplayName("CODE_MONKEY requiere 100h CODE")
        void codeMonkey_requires100Hours() {
            assertEquals(TitleType.TitleRequirementType.ACCUMULATED_HOURS, TitleType.CODE_MONKEY.getRequirementType());
            assertEquals(100, TitleType.CODE_MONKEY.getRequirementValue());
        }

        @Test
        @DisplayName("TEN_X_DEVELOPER da +10% INT XP")
        void tenXDev_gives10IntXP() {
            assertTrue(TitleType.TEN_X_DEVELOPER.buffsStat(StatType.INTELLECT));
            assertEquals(1.10, TitleType.TEN_X_DEVELOPER.getStatMultiplier(StatType.INTELLECT), 0.001);
        }

        @Test
        @DisplayName("ARCHITECT da buff doble (WIS + INT)")
        void architect_givesDoubleStatBuff() {
            assertTrue(TitleType.ARCHITECT.buffsStat(StatType.WISDOM));
            assertTrue(TitleType.ARCHITECT.buffsStat(StatType.INTELLECT));
            assertEquals(2, TitleType.ARCHITECT.getBuffs().size());
        }

        @Test
        @DisplayName("DEEP_WORKER requiere 50 sesiones Flow")
        void deepWorker_requires50FlowSessions() {
            assertEquals(TitleType.TitleRequirementType.FLOW_SESSIONS, TitleType.DEEP_WORKER.getRequirementType());
            assertEquals(50, TitleType.DEEP_WORKER.getRequirementValue());
        }
    }

    @Nested
    class LifestyleTitles {
        @Test
        @DisplayName("LONE_WOLF requiere 50 sesiones GYM")
        void loneWolf_requires50Gym() {
            assertEquals(TitleType.TitleRequirementType.ACCUMULATED_COUNT, TitleType.LONE_WOLF.getRequirementType());
            assertEquals(50, TitleType.LONE_WOLF.getRequirementValue());
            assertEquals(DailyQuestType.GYM, TitleType.LONE_WOLF.getRelatedHabit());
        }

        @Test
        @DisplayName("SACRED_TEMPLE requiere racha dual DIET + SKINCARE")
        void sacredTemple_requiresDualStreak() {
            assertEquals(TitleType.TitleRequirementType.DUAL_HABIT_STREAK, TitleType.SACRED_TEMPLE.getRequirementType());
            assertEquals(DailyQuestType.DIET, TitleType.SACRED_TEMPLE.getRelatedHabit());
            assertEquals(DailyQuestType.SKINCARE, TitleType.SACRED_TEMPLE.getSecondHabit());
            assertEquals(21, TitleType.SACRED_TEMPLE.getRequirementValue());
        }

        @Test
        @DisplayName("LIBRARIAN requiere 2000 páginas")
        void librarian_requires2000Pages() {
            assertEquals(TitleType.TitleRequirementType.ACCUMULATED_PAGES, TitleType.LIBRARIAN.getRequirementType());
            assertEquals(2000, TitleType.LIBRARIAN.getRequirementValue());
        }
    }

    @Nested
    class WealthTitles {
        @Test
        @DisplayName("SAVER requiere 50,000 G")
        void saver_requires50000Gold() {
            assertEquals(TitleType.TitleRequirementType.GOLD_THRESHOLD, TitleType.SAVER.getRequirementType());
            assertEquals(50000, TitleType.SAVER.getRequirementValue());
        }

        @Test
        @DisplayName("WHALE requiere 500,000 G de gasto")
        void whale_requires500000Spending() {
            assertEquals(TitleType.TitleRequirementType.TOTAL_SPENDING, TitleType.WHALE.getRequirementType());
            assertEquals(500000, TitleType.WHALE.getRequirementValue());
        }

        @Test
        @DisplayName("Títulos de riqueza dan Gold Multiplier")
        void wealthTitles_giveGoldMultiplier() {
            assertTrue(TitleType.SAVER.getGoldMultiplier() > 1.0);
            assertTrue(TitleType.WOLF_OF_WALL_ST.getGoldMultiplier() > 1.0);
            assertTrue(TitleType.WHALE.getGoldMultiplier() > 1.0);
        }
    }

    @Nested
    class StatMasteryTitles {
        @Test
        @DisplayName("Hay 10 títulos de Stat Mastery (5 stats x 2 niveles)")
        void statMastery_has10Titles() {
            List<TitleType> mastery = TitleType.getByCategory(TitleCategory.STAT_MASTERY);
            assertEquals(10, mastery.size());
        }

        @Test
        @DisplayName("TITAN requiere STR nivel 50")
        void titan_requiresStrLevel50() {
            assertEquals(TitleType.TitleRequirementType.STAT_LEVEL, TitleType.TITAN.getRequirementType());
            assertEquals(50, TitleType.TITAN.getRequirementValue());
            assertEquals(StatType.STRENGTH, TitleType.TITAN.getRelatedStat());
        }

        @Test
        @DisplayName("WORLD_DESTROYER requiere STR nivel 100")
        void worldDestroyer_requiresStrLevel100() {
            assertEquals(100, TitleType.WORLD_DESTROYER.getRequirementValue());
            assertEquals(StatType.STRENGTH, TitleType.WORLD_DESTROYER.getRelatedStat());
        }

        @Test
        @DisplayName("Títulos nivel 50 dan +5%, nivel 100 dan +10%")
        void statMastery_correctBonuses() {
            // Nivel 50: +5%
            assertEquals(0.05, TitleType.TITAN.getBuffs().get(0).value());
            assertEquals(0.05, TitleType.CYBORG.getBuffs().get(0).value());

            // Nivel 100: +10%
            assertEquals(0.10, TitleType.WORLD_DESTROYER.getBuffs().get(0).value());
            assertEquals(0.10, TitleType.SINGULARITY.getBuffs().get(0).value());
        }
    }

    @Nested
    class ElderTitles {
        @Test
        @DisplayName("SUPREME_MINIMALIST requiere ELDER_2")
        void supremeMinimalist_requiresElder2() {
            assertEquals(TitleType.TitleRequirementType.ELDER_QUEST, TitleType.SUPREME_MINIMALIST.getRequirementType());
            assertEquals(2, TitleType.SUPREME_MINIMALIST.getRequirementValue());
        }

        @Test
        @DisplayName("SPEED_READER da x1.5 READ XP")
        void speedReader_givesReadMultiplier() {
            TitleBuff buff = TitleType.SPEED_READER.getBuffs().get(0);
            assertEquals(TitleBuff.BuffType.READ_XP_MULTIPLIER, buff.type());
            assertEquals(1.5, buff.value());
        }

        @Test
        @DisplayName("STEEL_MIND da inmunidad a CHAOS")
        void steelMind_givesImmunity() {
            assertTrue(TitleType.STEEL_MIND.givesImmunityTo("CHAOS"));
        }

        @Test
        @DisplayName("HAND_OF_THE_KING da +20% ALL STATS")
        void handOfTheKing_gives20AllStats() {
            TitleBuff buff = TitleType.HAND_OF_THE_KING.getBuffs().get(0);
            assertEquals(TitleBuff.BuffType.ALL_STATS_XP_MULTIPLIER, buff.type());
            assertEquals(0.20, buff.value());
        }
    }

    @Nested
    class MilestoneTitles {
        @Test
        @DisplayName("LEGEND requiere nivel 50")
        void legend_requiresLevel50() {
            assertEquals(TitleType.TitleRequirementType.LEVEL, TitleType.LEGEND.getRequirementType());
            assertEquals(50, TitleType.LEGEND.getRequirementValue());
        }

        @Test
        @DisplayName("MONARCH requiere nivel 100")
        void monarch_requiresLevel100() {
            assertEquals(100, TitleType.MONARCH.getRequirementValue());
        }

        @Test
        @DisplayName("MONARCH da +10% ALL STATS XP")
        void monarch_givesAllStatsBonus() {
            TitleBuff buff = TitleType.MONARCH.getBuffs().get(0);
            assertEquals(TitleBuff.BuffType.ALL_STATS_XP_MULTIPLIER, buff.type());
            assertEquals(0.10, buff.value());
        }
    }

    @Nested
    class Queries {
        @Test
        @DisplayName("buffsStat() detecta buffs de stat")
        void buffsStat_detectsStatBuffs() {
            assertTrue(TitleType.TITAN.buffsStat(StatType.STRENGTH));
            assertFalse(TitleType.TITAN.buffsStat(StatType.INTELLECT));
        }

        @Test
        @DisplayName("getStatMultiplier() calcula multiplicador")
        void getStatMultiplier_calculatesCorrectly() {
            assertEquals(1.05, TitleType.TITAN.getStatMultiplier(StatType.STRENGTH), 0.001);
            assertEquals(1.0, TitleType.TITAN.getStatMultiplier(StatType.INTELLECT), 0.001);
        }

        @Test
        @DisplayName("getGoldMultiplier() calcula multiplicador de oro")
        void getGoldMultiplier_calculatesCorrectly() {
            assertEquals(1.02, TitleType.SAVER.getGoldMultiplier(), 0.001);
            assertEquals(1.05, TitleType.WOLF_OF_WALL_ST.getGoldMultiplier(), 0.001);
        }

        @Test
        @DisplayName("getHPRecoveryBonus() calcula bonus HP")
        void getHPRecoveryBonus_calculatesCorrectly() {
            assertEquals(0.03, TitleType.SURVIVOR.getHPRecoveryBonus(), 0.001);
            assertEquals(0.10, TitleType.SLEEP_GUARDIAN.getHPRecoveryBonus(), 0.001);
        }

        @Test
        @DisplayName("givesImmunityTo() verifica inmunidad")
        void givesImmunityTo_checksImmunity() {
            assertTrue(TitleType.STEEL_MIND.givesImmunityTo("CHAOS"));
            assertFalse(TitleType.STEEL_MIND.givesImmunityTo("FATIGUE"));
            assertFalse(TitleType.SURVIVOR.givesImmunityTo("CHAOS"));
        }
    }

    @Nested
    class Search {
        @Test
        @DisplayName("fromString() encuentra por enum name")
        void fromString_findsByEnumName() {
            assertTrue(TitleType.fromString("SURVIVOR").isPresent());
            assertEquals(TitleType.SURVIVOR, TitleType.fromString("SURVIVOR").get());
        }

        @Test
        @DisplayName("fromString() es case-insensitive")
        void fromString_caseInsensitive() {
            assertTrue(TitleType.fromString("survivor").isPresent());
            assertTrue(TitleType.fromString("Survivor").isPresent());
        }

        @Test
        @DisplayName("fromString() encuentra por display name")
        void fromString_findsByDisplayName() {
            assertTrue(TitleType.fromString("Superviviente").isPresent());
            assertEquals(TitleType.SURVIVOR, TitleType.fromString("Superviviente").get());
        }

        @Test
        @DisplayName("fromString() retorna empty para inválido")
        void fromString_emptyForInvalid() {
            assertTrue(TitleType.fromString("INVALID").isEmpty());
            assertTrue(TitleType.fromString(null).isEmpty());
            assertTrue(TitleType.fromString("   ").isEmpty());
        }

        @Test
        @DisplayName("getByCategory() retorna títulos de categoría")
        void getByCategory_returnsCategoryTitles() {
            List<TitleType> survival = TitleType.getByCategory(TitleCategory.SURVIVAL);

            assertFalse(survival.isEmpty());
            assertTrue(survival.contains(TitleType.SURVIVOR));
            assertTrue(survival.contains(TitleType.IMMORTAL));
        }

        @Test
        @DisplayName("getByLevelRequirement() retorna títulos por nivel")
        void getByLevelRequirement_returnsByLevel() {
            List<TitleType> level50 = TitleType.getByLevelRequirement(50);

            assertTrue(level50.contains(TitleType.LEGEND));
        }
    }

    @Nested
    class Display {
        @Test
        @DisplayName("getBuffsDisplay() formatea buffs")
        void getBuffsDisplay_formatsBuffs() {
            String display = TitleType.SURVIVOR.getBuffsDisplay();
            assertNotNull(display);
            assertTrue(display.contains("HP Recovery") || display.contains("%"));
        }

        @Test
        @DisplayName("getRequirementDisplay() formatea requisitos")
        void getRequirementDisplay_formatsRequirements() {
            assertEquals("Alcanzar Nivel 10", TitleType.SURVIVOR.getRequirementDisplay());

            String sleepGuardian = TitleType.SLEEP_GUARDIAN.getRequirementDisplay();
            assertTrue(sleepGuardian.contains("14"));
            assertTrue(sleepGuardian.contains("días"));
        }
    }
}
