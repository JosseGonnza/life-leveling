package com.lifeleveling.application.dto;

import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model del modo INVENTORY de The Armory: lo que posees + el loadout por slot de equipo.
 */
public record InventoryView(
        List<OwnedItemView> owned,
        List<SlotView> loadout
) {
    public record OwnedItemView(String id, String name, String category, boolean consumable, boolean equippable, boolean onCooldown) {}

    public record SlotView(String slot, String icon, String itemName, boolean filled) {}

    public static InventoryView from(Inventory inv) {
        List<OwnedItemView> owned = inv.getOwnedItems().stream()
                .map(i -> new OwnedItemView(i.id(), i.name(), i.category().name(),
                        i.isConsumable(), i.slot() != ItemSlot.NONE, inv.isCooldownActive(i.id())))
                .toList();

        List<SlotView> loadout = new ArrayList<>();
        for (ItemSlot slot : ItemSlot.values()) {
            if (slot == ItemSlot.NONE) continue;
            Item equipped = inv.getEquippedItem(slot).orElse(null);
            loadout.add(new SlotView(slot.getDisplayName(), slot.getIcon(),
                    equipped == null ? null : equipped.name(), equipped != null));
        }
        return new InventoryView(owned, loadout);
    }
}
