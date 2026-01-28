package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TitleInventory - Gestión de Títulos del Jugador")
class TitleInventoryTest {

    private TitleInventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new TitleInventory();
    }

    @Nested
    class Constants {
        @Test
        @DisplayName("SECOND_SLOT_UNLOCK_LEVEL es 50")
        void secondSlotLevel_is50() {
            assertEquals(50, TitleInventory.SECOND_SLOT_UNLOCK_LEVEL);
        }

        @Test
        @DisplayName("MAX_TITLE_SLOTS es 2")
        void maxSlots_is2() {
            assertEquals(2, TitleInventory.MAX_TITLE_SLOTS);
        }
    }

    @Nested
    class UnlockTitles {
        @Test
        @DisplayName("unlock() añade título a la colección")
        void unlock_addsTitleToCollection() {
            boolean unlocked = inventory.unlock(TitleType.SURVIVOR);

            assertTrue(unlocked);
            assertTrue(inventory.hasTitle(TitleType.SURVIVOR));
            assertEquals(1, inventory.getUnlockedCount());
        }

        @Test
        @DisplayName("unlock() retorna false si ya tiene el título")
        void unlock_returnsFalseIfAlreadyHas() {
            inventory.unlock(TitleType.SURVIVOR);
            boolean secondUnlock = inventory.unlock(TitleType.SURVIVOR);

            assertFalse(secondUnlock);
            assertEquals(1, inventory.getUnlockedCount());
        }

        @Test
        @DisplayName("unlock() acepta múltiples títulos diferentes")
        void unlock_acceptsMultipleDifferentTitles() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.ZOMBIE);
            inventory.unlock(TitleType.PHOENIX);

            assertEquals(3, inventory.getUnlockedCount());
            assertTrue(inventory.hasTitle(TitleType.SURVIVOR));
            assertTrue(inventory.hasTitle(TitleType.ZOMBIE));
            assertTrue(inventory.hasTitle(TitleType.PHOENIX));
        }

        @Test
        @DisplayName("getTitle() retorna el título si existe")
        void getTitle_returnsTitleIfExists() {
            inventory.unlock(TitleType.SURVIVOR);

            assertTrue(inventory.getTitle(TitleType.SURVIVOR).isPresent());
            assertEquals(TitleType.SURVIVOR, inventory.getTitle(TitleType.SURVIVOR).get().type());
        }

        @Test
        @DisplayName("getTitle() retorna empty si no existe")
        void getTitle_returnsEmptyIfNotExists() {
            assertTrue(inventory.getTitle(TitleType.MONARCH).isEmpty());
        }
    }

    @Nested
    class EquipTitles {
        @BeforeEach
        void unlockSomeTitles() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.ZOMBIE);
            inventory.unlock(TitleType.TITAN);
        }

        @Test
        @DisplayName("equip() equipa título desbloqueado")
        void equip_equipsUnlockedTitle() {
            boolean equipped = inventory.equip(TitleType.SURVIVOR, 30);

            assertTrue(equipped);
            assertTrue(inventory.isEquipped(TitleType.SURVIVOR));
            assertEquals(1, inventory.getEquippedCount());
        }

        @Test
        @DisplayName("equip() lanza excepción si no tiene el título")
        void equip_throwsIfNotUnlocked() {
            assertThrows(IllegalStateException.class,
                    () -> inventory.equip(TitleType.MONARCH, 30));
        }

        @Test
        @DisplayName("equip() retorna false si ya está equipado")
        void equip_returnsFalseIfAlreadyEquipped() {
            inventory.equip(TitleType.SURVIVOR, 30);
            boolean second = inventory.equip(TitleType.SURVIVOR, 30);

            assertFalse(second);
            assertEquals(1, inventory.getEquippedCount());
        }

        @Test
        @DisplayName("Nivel < 50 solo permite 1 slot")
        void belowLevel50_onlyOneSlot() {
            inventory.equip(TitleType.SURVIVOR, 30);

            assertThrows(IllegalStateException.class,
                    () -> inventory.equip(TitleType.ZOMBIE, 30));
        }

        @Test
        @DisplayName("Nivel >= 50 permite 2 slots")
        void level50OrAbove_allowsTwoSlots() {
            inventory.equip(TitleType.SURVIVOR, 50);
            inventory.equip(TitleType.ZOMBIE, 50);

            assertEquals(2, inventory.getEquippedCount());
            assertTrue(inventory.isEquipped(TitleType.SURVIVOR));
            assertTrue(inventory.isEquipped(TitleType.ZOMBIE));
        }

        @Test
        @DisplayName("getAvailableSlots() retorna slots correctos")
        void getAvailableSlots_returnsCorrectSlots() {
            assertEquals(1, inventory.getAvailableSlots(1));
            assertEquals(1, inventory.getAvailableSlots(49));
            assertEquals(2, inventory.getAvailableSlots(50));
            assertEquals(2, inventory.getAvailableSlots(100));
        }
    }

    @Nested
    class UnequipTitles {
        @BeforeEach
        void setupEquipped() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.ZOMBIE);
            inventory.equip(TitleType.SURVIVOR, 50);
            inventory.equip(TitleType.ZOMBIE, 50);
        }

        @Test
        @DisplayName("unequip() desequipa título")
        void unequip_removesEquippedTitle() {
            boolean unequipped = inventory.unequip(TitleType.SURVIVOR);

            assertTrue(unequipped);
            assertFalse(inventory.isEquipped(TitleType.SURVIVOR));
            assertEquals(1, inventory.getEquippedCount());
            // Sigue desbloqueado
            assertTrue(inventory.hasTitle(TitleType.SURVIVOR));
        }

        @Test
        @DisplayName("unequip() retorna false si no estaba equipado")
        void unequip_returnsFalseIfNotEquipped() {
            inventory.unlock(TitleType.TITAN);

            boolean result = inventory.unequip(TitleType.TITAN);

            assertFalse(result);
        }

        @Test
        @DisplayName("unequipAll() desequipa todos")
        void unequipAll_removesAllEquipped() {
            inventory.unequipAll();

            assertEquals(0, inventory.getEquippedCount());
            assertFalse(inventory.isEquipped(TitleType.SURVIVOR));
            assertFalse(inventory.isEquipped(TitleType.ZOMBIE));
        }
    }

    @Nested
    class SwapTitles {
        @BeforeEach
        void setup() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.ZOMBIE);
            inventory.unlock(TitleType.TITAN);
            inventory.equip(TitleType.SURVIVOR, 30);
        }

        @Test
        @DisplayName("swap() intercambia títulos")
        void swap_exchangesTitles() {
            inventory.swap(TitleType.SURVIVOR, TitleType.ZOMBIE, 30);

            assertFalse(inventory.isEquipped(TitleType.SURVIVOR));
            assertTrue(inventory.isEquipped(TitleType.ZOMBIE));
            assertEquals(1, inventory.getEquippedCount());
        }

        @Test
        @DisplayName("swap() lanza excepción si oldType no está equipado")
        void swap_throwsIfOldNotEquipped() {
            assertThrows(IllegalStateException.class,
                    () -> inventory.swap(TitleType.ZOMBIE, TitleType.TITAN, 30));
        }

        @Test
        @DisplayName("swap() lanza excepción si newType no está desbloqueado")
        void swap_throwsIfNewNotUnlocked() {
            assertThrows(IllegalStateException.class,
                    () -> inventory.swap(TitleType.SURVIVOR, TitleType.MONARCH, 30));
        }
    }

    @Nested
    class BuffCalculations {
        @Test
        @DisplayName("getStatXPMultiplier() retorna 1.0 sin buffs")
        void statXPMultiplier_returnsOneWithoutBuffs() {
            assertEquals(1.0, inventory.getStatXPMultiplier(StatType.STRENGTH), 0.001);
        }

        @Test
        @DisplayName("getStatXPMultiplier() aplica buff de título equipado")
        void statXPMultiplier_appliesEquippedBuff() {
            inventory.unlock(TitleType.TITAN); // +5% STR XP
            inventory.equip(TitleType.TITAN, 50);

            assertEquals(1.05, inventory.getStatXPMultiplier(StatType.STRENGTH), 0.001);
            assertEquals(1.0, inventory.getStatXPMultiplier(StatType.INTELLECT), 0.001);
        }

        @Test
        @DisplayName("getStatXPMultiplier() multiplica varios buffs")
        void statXPMultiplier_multipliesMultipleBuffs() {
            inventory.unlock(TitleType.TITAN);      // +5% STR
            inventory.unlock(TitleType.LONE_WOLF);  // +5% STR
            inventory.equip(TitleType.TITAN, 50);
            inventory.equip(TitleType.LONE_WOLF, 50);

            // 1.05 * 1.05 = 1.1025
            assertEquals(1.1025, inventory.getStatXPMultiplier(StatType.STRENGTH), 0.001);
        }

        @Test
        @DisplayName("ALL_STATS_XP afecta a todos los stats")
        void allStatsXP_affectsAllStats() {
            inventory.unlock(TitleType.HAND_OF_THE_KING); // +20% ALL STATS
            inventory.equip(TitleType.HAND_OF_THE_KING, 75);

            for (StatType stat : StatType.values()) {
                assertEquals(1.20, inventory.getStatXPMultiplier(stat), 0.001);
            }
        }

        @Test
        @DisplayName("getGoldMultiplier() calcula multiplicador de oro")
        void goldMultiplier_calculatesCorrectly() {
            inventory.unlock(TitleType.SAVER); // +2% Gold
            inventory.equip(TitleType.SAVER, 30);

            assertEquals(1.02, inventory.getGoldMultiplier(), 0.001);
        }

        @Test
        @DisplayName("getHPRecoveryBonus() suma bonuses de HP")
        void hpRecoveryBonus_sumsUp() {
            inventory.unlock(TitleType.SURVIVOR);      // +3% HP Rec
            inventory.unlock(TitleType.SLEEP_GUARDIAN); // +10% HP Rec
            inventory.equip(TitleType.SURVIVOR, 50);
            inventory.equip(TitleType.SLEEP_GUARDIAN, 50);

            assertEquals(0.13, inventory.getHPRecoveryBonus(), 0.001);
        }

        @Test
        @DisplayName("applyHPRecoveryBonus() aplica bonus a curación")
        void applyHPRecoveryBonus_appliesBonus() {
            inventory.unlock(TitleType.SLEEP_GUARDIAN); // +10% HP Rec
            inventory.equip(TitleType.SLEEP_GUARDIAN, 50);

            int baseHealing = 30;
            int withBonus = inventory.applyHPRecoveryBonus(baseHealing);

            assertEquals(33, withBonus); // 30 * 1.10 = 33
        }

        @Test
        @DisplayName("hasImmunityTo() verifica inmunidad a debuff")
        void hasImmunityTo_checksDebuffImmunity() {
            inventory.unlock(TitleType.STEEL_MIND); // Inmunidad a CHAOS
            inventory.equip(TitleType.STEEL_MIND, 75);

            assertTrue(inventory.hasImmunityTo("CHAOS"));
            assertFalse(inventory.hasImmunityTo("FATIGUE"));
        }

        @Test
        @DisplayName("getReadXPMultiplier() obtiene multiplicador de READ")
        void readXPMultiplier_getsCorrectValue() {
            inventory.unlock(TitleType.SPEED_READER); // x1.5 READ XP
            inventory.equip(TitleType.SPEED_READER, 75);

            assertEquals(1.5, inventory.getReadXPMultiplier(), 0.001);
        }

        @Test
        @DisplayName("Buffs no aplican sin equipar")
        void buffs_dontApplyWithoutEquipping() {
            inventory.unlock(TitleType.TITAN);

            assertEquals(1.0, inventory.getStatXPMultiplier(StatType.STRENGTH), 0.001);
        }
    }

    @Nested
    class Queries {
        @BeforeEach
        void setupTitles() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.ZOMBIE);
            inventory.unlock(TitleType.TITAN);
            inventory.equip(TitleType.SURVIVOR, 30);
        }

        @Test
        @DisplayName("getUnlockedTitles() retorna copia inmutable")
        void getUnlockedTitles_returnsImmutableCopy() {
            Set<Title> titles = inventory.getUnlockedTitles();
            assertThrows(UnsupportedOperationException.class,
                    () -> titles.add(Title.unlock(TitleType.MONARCH)));
        }

        @Test
        @DisplayName("getEquippedTitles() retorna copia inmutable")
        void getEquippedTitles_returnsImmutableCopy() {
            List<Title> equipped = inventory.getEquippedTitles();
            assertThrows(UnsupportedOperationException.class,
                    () -> equipped.add(Title.unlock(TitleType.MONARCH)));
        }

        @Test
        @DisplayName("getTitlesByCategory() filtra por categoría")
        void getTitlesByCategory_filtersByCategory() {
            List<Title> survival = inventory.getTitlesByCategory(TitleCategory.SURVIVAL);

            assertEquals(2, survival.size());
            assertTrue(survival.stream().allMatch(
                    t -> t.getCategory() == TitleCategory.SURVIVAL));
        }

        @Test
        @DisplayName("getLockedTitles() retorna títulos no desbloqueados")
        void getLockedTitles_returnsLocked() {
            List<TitleType> locked = inventory.getLockedTitles();

            assertFalse(locked.contains(TitleType.SURVIVOR));
            assertFalse(locked.contains(TitleType.ZOMBIE));
            assertTrue(locked.contains(TitleType.MONARCH));
        }

        @Test
        @DisplayName("getCollectionProgress() calcula porcentaje")
        void collectionProgress_calculatesPercentage() {
            double progress = inventory.getCollectionProgress();

            // 3 de N títulos
            assertTrue(progress > 0);
            assertTrue(progress < 1);
        }
    }

    @Nested
    class Display {
        @Test
        @DisplayName("getEquippedDisplay() muestra títulos equipados")
        void equippedDisplay_showsEquippedTitles() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.TITAN);
            inventory.equip(TitleType.SURVIVOR, 50);
            inventory.equip(TitleType.TITAN, 50);

            String display = inventory.getEquippedDisplay();

            assertTrue(display.contains("Superviviente"));
            assertTrue(display.contains("Titán"));
        }

        @Test
        @DisplayName("getEquippedDisplay() muestra mensaje si vacío")
        void equippedDisplay_showsEmptyMessage() {
            String display = inventory.getEquippedDisplay();
            assertEquals("Ningún título equipado", display);
        }

        @Test
        @DisplayName("getActiveBuffsDisplay() muestra buffs activos")
        void activeBuffsDisplay_showsBuffs() {
            inventory.unlock(TitleType.TITAN);
            inventory.equip(TitleType.TITAN, 50);

            String display = inventory.getActiveBuffsDisplay();

            assertTrue(display.contains("STR") || display.contains("Fuerza"));
            assertTrue(display.contains("XP") || display.contains("%"));
        }

        @Test
        @DisplayName("getCollectionSummary() muestra resumen")
        void collectionSummary_showsSummary() {
            inventory.unlock(TitleType.SURVIVOR);
            inventory.unlock(TitleType.TITAN);

            String summary = inventory.getCollectionSummary();

            assertTrue(summary.contains("Títulos"));
            assertTrue(summary.contains("2/"));
            assertTrue(summary.contains("%"));
        }
    }

    @Nested
    class EdgeCases {
        @Test
        @DisplayName("Inventario vacío no tiene buffs")
        void emptyInventory_hasNoBuffs() {
            assertEquals(1.0, inventory.getGoldMultiplier(), 0.001);
            assertEquals(0.0, inventory.getHPRecoveryBonus(), 0.001);
            assertFalse(inventory.hasImmunityTo("CHAOS"));
        }

        @Test
        @DisplayName("Título desequipado pierde buffs")
        void unequippedTitle_losesBuffs() {
            inventory.unlock(TitleType.TITAN);
            inventory.equip(TitleType.TITAN, 50);

            assertEquals(1.05, inventory.getStatXPMultiplier(StatType.STRENGTH), 0.001);

            inventory.unequip(TitleType.TITAN);

            assertEquals(1.0, inventory.getStatXPMultiplier(StatType.STRENGTH), 0.001);
        }

        @Test
        @DisplayName("Múltiples buffs del mismo tipo se multiplican")
        void multipleBuffsSameType_multiply() {
            // Simular múltiples gold buffs
            inventory.unlock(TitleType.SAVER);          // +2%
            inventory.unlock(TitleType.WOLF_OF_WALL_ST); // +5%
            inventory.equip(TitleType.SAVER, 50);
            inventory.equip(TitleType.WOLF_OF_WALL_ST, 50);

            // 1.02 * 1.05 = 1.071
            assertEquals(1.071, inventory.getGoldMultiplier(), 0.001);
        }
    }
}
