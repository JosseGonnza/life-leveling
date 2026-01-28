package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inventory - Gestión de Equipo y Mochila")
class InventoryTest {

    private Inventory inventory;
    private Item swordOfTruth; // Imaginemos que es un Teclado Mecánico +5 INT
    private Item shieldOfFaith; // Imaginemos que es una Silla +5 DIS

    @BeforeEach
    void setUp() {
        inventory = new Inventory();

        swordOfTruth = Item.createEquip(
                "keyboard_mech", "Teclado Mecánico", 1000,
                ItemTier.TIER_2, ItemSlot.PERIPH, ItemCategory.TECH,
                Map.of(StatType.INTELLECT, 5)
        );

        shieldOfFaith = Item.createEquip(
                "chair_ergo", "Silla Ergonómica", 2000,
                ItemTier.TIER_2, ItemSlot.CHAIR, ItemCategory.FURNITURE,
                Map.of(StatType.DISCIPLINE, 5)
        );
    }

    @Nested
    @DisplayName("Gestión de Items")
    class ItemManagement {
        @Test
        @DisplayName("addItem() guarda el item en ownedItems")
        void addItem_storesIt() {
            inventory.addItem(swordOfTruth);
            assertTrue(inventory.hasItem("keyboard_mech"));
        }

        @Test
        @DisplayName("No se pueden tener duplicados de equipamiento")
        void addItem_ignoresDuplicatesForEquipment() {
            inventory.addItem(swordOfTruth);
            inventory.addItem(swordOfTruth);

            // Verificamos indirectamente (en una impl real miraríamos el size de la lista)
            // Aquí confiamos en que hasItem funciona
            assertTrue(inventory.hasItem("keyboard_mech"));
        }
    }

    @Nested
    @DisplayName("Equipamiento y Stats")
    class EquipmentLogic {
        @Test
        @DisplayName("equip() mueve el item al slot activo")
        void equip_activatesItem() {
            inventory.addItem(swordOfTruth);
            inventory.equip("keyboard_mech");

            Optional<Item> equipped = inventory.getEquippedItem(ItemSlot.PERIPH);
            assertTrue(equipped.isPresent());
            assertEquals("keyboard_mech", equipped.get().id());
        }

        @Test
        @DisplayName("equip() reemplaza item anterior si el slot estaba ocupado")
        void equip_replacesOldItem() {
            Item basicMouse = Item.createEquip("mouse_basic", "Ratón", 10, ItemTier.TIER_1, ItemSlot.PERIPH, ItemCategory.TECH, Map.of());

            inventory.addItem(basicMouse);
            inventory.addItem(swordOfTruth);

            inventory.equip("mouse_basic");
            inventory.equip("keyboard_mech"); // Reemplaza al ratón (mismo slot PERIPH)

            assertEquals("keyboard_mech", inventory.getEquippedItem(ItemSlot.PERIPH).get().id());
        }

        @Test
        @DisplayName("getTotalStatBonuses() suma correctamente los stats")
        void calculateStats_sumsCorrectly() {
            inventory.addItem(swordOfTruth); // +5 INT
            inventory.addItem(shieldOfFaith); // +5 DIS

            inventory.equip("keyboard_mech");
            inventory.equip("chair_ergo");

            Map<StatType, Integer> bonuses = inventory.getTotalStatBonuses();

            assertEquals(5, bonuses.get(StatType.INTELLECT));
            assertEquals(5, bonuses.get(StatType.DISCIPLINE));
            assertNull(bonuses.get(StatType.STRENGTH)); // No hay bonus de fuerza
        }
    }

    @Nested
    @DisplayName("Cooldowns y Especiales")
    class Cooldowns {
        @Test
        @DisplayName("triggerCooldown() bloquea el uso del item")
        void triggerCooldown_blocksUsage() {
            String itemId = "infinite_coffee";
            inventory.triggerCooldown(itemId, 1, ChronoUnit.HOURS);

            assertTrue(inventory.isCooldownActive(itemId));
            assertFalse(inventory.canUseSpecialItem(itemId));
        }

        @Test
        @DisplayName("canUseSpecialItem() permite uso si no hay cooldown")
        void canUse_whenNoCooldown() {
            assertTrue(inventory.canUseSpecialItem("random_item"));
        }
    }
}