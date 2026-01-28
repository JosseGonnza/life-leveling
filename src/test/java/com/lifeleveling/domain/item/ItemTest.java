package com.lifeleveling.domain.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    @DisplayName("createEquip() crea un item visible y no consumible")
    void createEquip_shouldCreateVisibleEquipment() {
        Item item = Item.createEquip(
                "test_shirt", "Camiseta Test", 100,
                ItemTier.TIER_1, ItemSlot.BODY, ItemCategory.CLOTHING, null
        );

        assertFalse(item.isConsumable());
        assertFalse(item.isHidden());
        assertEquals(ItemSlot.BODY, item.slot());
    }

    @Test
    @DisplayName("createArtifact() crea un item OCULTO por defecto")
    void createArtifact_shouldCreateHiddenItem() {
        Item artifact = Item.createArtifact(
                "ring_titan", "Anillo Titán", "OP", ItemSlot.HAND
        );

        assertTrue(artifact.isHidden());
        assertEquals(0, artifact.price()); // Los artefactos suelen ser priceless/gratis
    }

    @Test
    @DisplayName("toDisplayString() oculta el nombre si es un item secreto")
    void toDisplayString_shouldMaskHiddenItems() {
        Item artifact = Item.createArtifact("secret", "Secreto", "Desc", ItemSlot.HAND);

        assertTrue(artifact.toDisplayString().contains("???"));
        assertFalse(artifact.toDisplayString().contains("Secreto"));
    }
}