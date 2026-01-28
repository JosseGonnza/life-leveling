package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TitleBuff - Efectos de Títulos")
class TitleBuffTest {

    @Nested
    class FactoryMethods {
        @Test
        @DisplayName("statXP() crea buff de XP para stat específico")
        void statXP_createsStatBuff() {
            TitleBuff buff = TitleBuff.statXP(StatType.STRENGTH, 0.05);

            assertEquals(TitleBuff.BuffType.STAT_XP_MULTIPLIER, buff.type());
            assertEquals(StatType.STRENGTH, buff.targetStat());
            assertEquals(0.05, buff.value());
            assertNull(buff.debuffImmunity());
        }

        @Test
        @DisplayName("generalXP() crea buff de XP general")
        void generalXP_createsGeneralBuff() {
            TitleBuff buff = TitleBuff.generalXP(0.10);

            assertEquals(TitleBuff.BuffType.GENERAL_XP_MULTIPLIER, buff.type());
            assertNull(buff.targetStat());
            assertEquals(0.10, buff.value());
        }

        @Test
        @DisplayName("allStatsXP() crea buff para todos los stats")
        void allStatsXP_createsAllStatsBuff() {
            TitleBuff buff = TitleBuff.allStatsXP(0.20);

            assertEquals(TitleBuff.BuffType.ALL_STATS_XP_MULTIPLIER, buff.type());
            assertEquals(0.20, buff.value());
        }

        @Test
        @DisplayName("goldMultiplier() crea buff de oro")
        void goldMultiplier_createsGoldBuff() {
            TitleBuff buff = TitleBuff.goldMultiplier(0.05);

            assertEquals(TitleBuff.BuffType.GOLD_MULTIPLIER, buff.type());
            assertEquals(0.05, buff.value());
        }

        @Test
        @DisplayName("hpRecovery() crea buff de recuperación HP")
        void hpRecovery_createsHPBuff() {
            TitleBuff buff = TitleBuff.hpRecovery(0.10);

            assertEquals(TitleBuff.BuffType.HP_RECOVERY_BONUS, buff.type());
            assertEquals(0.10, buff.value());
        }

        @Test
        @DisplayName("debuffImmunity() crea inmunidad a debuff")
        void debuffImmunity_createsImmunity() {
            TitleBuff buff = TitleBuff.debuffImmunity("CHAOS");

            assertEquals(TitleBuff.BuffType.DEBUFF_IMMUNITY, buff.type());
            assertEquals("CHAOS", buff.debuffImmunity());
            assertEquals(1.0, buff.value());
        }

        @Test
        @DisplayName("readXPMultiplier() crea multiplicador de READ")
        void readXPMultiplier_createsReadBuff() {
            TitleBuff buff = TitleBuff.readXPMultiplier(1.5);

            assertEquals(TitleBuff.BuffType.READ_XP_MULTIPLIER, buff.type());
            assertEquals(1.5, buff.value());
        }

        @Test
        @DisplayName("multiStatXP() crea array de buffs")
        void multiStatXP_createsMultipleBuffs() {
            TitleBuff[] buffs = TitleBuff.multiStatXP(0.05, StatType.WISDOM, StatType.INTELLECT);

            assertEquals(2, buffs.length);
            assertEquals(StatType.WISDOM, buffs[0].targetStat());
            assertEquals(StatType.INTELLECT, buffs[1].targetStat());
        }
    }

    @Nested
    class Validation {
        @Test
        @DisplayName("Constructor valida type no null")
        void constructor_validatesTypeNotNull() {
            assertThrows(NullPointerException.class,
                    () -> new TitleBuff(null, null, 0.05, null));
        }

        @Test
        @DisplayName("Constructor valida value no negativo")
        void constructor_validatesValueNotNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> TitleBuff.statXP(StatType.STRENGTH, -0.05));
        }

        @Test
        @DisplayName("STAT_XP_MULTIPLIER requiere targetStat")
        void statXPMultiplier_requiresTargetStat() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TitleBuff(TitleBuff.BuffType.STAT_XP_MULTIPLIER, null, 0.05, null));
        }

        @Test
        @DisplayName("DEBUFF_IMMUNITY requiere debuffImmunity")
        void debuffImmunity_requiresDebuffName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TitleBuff(TitleBuff.BuffType.DEBUFF_IMMUNITY, null, 1.0, null));

            assertThrows(IllegalArgumentException.class,
                    () -> new TitleBuff(TitleBuff.BuffType.DEBUFF_IMMUNITY, null, 1.0, "   "));
        }
    }

    @Nested
    class Queries {
        @Test
        @DisplayName("affectsStat() verifica stat específico")
        void affectsStat_checksSpecificStat() {
            TitleBuff strBuff = TitleBuff.statXP(StatType.STRENGTH, 0.05);

            assertTrue(strBuff.affectsStat(StatType.STRENGTH));
            assertFalse(strBuff.affectsStat(StatType.INTELLECT));
        }

        @ParameterizedTest
        @EnumSource(StatType.class)
        @DisplayName("ALL_STATS_XP afecta a todos los stats")
        void allStatsXP_affectsAllStats(StatType stat) {
            TitleBuff buff = TitleBuff.allStatsXP(0.20);
            assertTrue(buff.affectsStat(stat));
        }

        @Test
        @DisplayName("GENERAL_XP no afecta stats individuales")
        void generalXP_doesNotAffectIndividualStats() {
            TitleBuff buff = TitleBuff.generalXP(0.10);

            for (StatType stat : StatType.values()) {
                assertFalse(buff.affectsStat(stat));
            }
        }

        @Test
        @DisplayName("affectsGold() detecta buff de oro")
        void affectsGold_detectsGoldBuff() {
            TitleBuff goldBuff = TitleBuff.goldMultiplier(0.05);
            TitleBuff xpBuff = TitleBuff.statXP(StatType.STRENGTH, 0.05);

            assertTrue(goldBuff.affectsGold());
            assertFalse(xpBuff.affectsGold());
        }

        @Test
        @DisplayName("affectsHPRecovery() detecta buff de HP")
        void affectsHPRecovery_detectsHPBuff() {
            TitleBuff hpBuff = TitleBuff.hpRecovery(0.10);
            TitleBuff xpBuff = TitleBuff.statXP(StatType.STRENGTH, 0.05);

            assertTrue(hpBuff.affectsHPRecovery());
            assertFalse(xpBuff.affectsHPRecovery());
        }

        @Test
        @DisplayName("givesDebuffImmunity() verifica inmunidad específica")
        void givesDebuffImmunity_checksSpecificDebuff() {
            TitleBuff chaos = TitleBuff.debuffImmunity("CHAOS");

            assertTrue(chaos.givesDebuffImmunity("CHAOS"));
            assertTrue(chaos.givesDebuffImmunity("chaos"));  // Case insensitive
            assertFalse(chaos.givesDebuffImmunity("FATIGUE"));
        }
    }

    @Nested
    class Multiplier {
        @Test
        @DisplayName("getMultiplier() calcula 1 + value para buffs normales")
        void getMultiplier_addsOneToValue() {
            TitleBuff buff = TitleBuff.statXP(StatType.STRENGTH, 0.05);
            assertEquals(1.05, buff.getMultiplier(), 0.001);

            TitleBuff buff10 = TitleBuff.goldMultiplier(0.10);
            assertEquals(1.10, buff10.getMultiplier(), 0.001);
        }

        @Test
        @DisplayName("getMultiplier() usa value directo para READ_XP")
        void getMultiplier_usesDirectValueForRead() {
            TitleBuff buff = TitleBuff.readXPMultiplier(1.5);
            assertEquals(1.5, buff.getMultiplier(), 0.001);
        }
    }

    @Nested
    class Display {
        @Test
        @DisplayName("toDisplayString() formatea stat XP")
        void toDisplayString_formatsStatXP() {
            TitleBuff buff = TitleBuff.statXP(StatType.STRENGTH, 0.05);
            String display = buff.toDisplayString();

            assertTrue(display.contains("+5%"));
            assertTrue(display.contains("Fuerza") || display.contains("STR"));
            assertTrue(display.contains("XP"));
        }

        @Test
        @DisplayName("toDisplayString() formatea gold multiplier")
        void toDisplayString_formatsGold() {
            TitleBuff buff = TitleBuff.goldMultiplier(0.10);
            String display = buff.toDisplayString();

            assertTrue(display.contains("+10%"));
            assertTrue(display.contains("Gold"));
        }

        @Test
        @DisplayName("toDisplayString() formatea HP recovery")
        void toDisplayString_formatsHPRecovery() {
            TitleBuff buff = TitleBuff.hpRecovery(0.20);
            String display = buff.toDisplayString();

            assertTrue(display.contains("+20%"));
            assertTrue(display.contains("HP Recovery"));
        }

        @Test
        @DisplayName("toDisplayString() formatea debuff immunity")
        void toDisplayString_formatsDebuffImmunity() {
            TitleBuff buff = TitleBuff.debuffImmunity("CHAOS");
            String display = buff.toDisplayString();

            assertTrue(display.contains("Inmunidad"));
            assertTrue(display.contains("CHAOS"));
        }
    }
}
