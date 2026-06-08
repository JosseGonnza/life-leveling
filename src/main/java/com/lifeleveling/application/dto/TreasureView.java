package com.lifeleveling.application.dto;

import com.lifeleveling.domain.item.Item;

/**
 * Read model de un Tesoro (money sink de estatus) para la pantalla Tesoros.
 * No otorga stats; `owned` marca el trofeo conseguido, `affordable` si el oro alcanza.
 */
public record TreasureView(
        String id,
        String name,
        int price,
        String lore,
        boolean owned,
        boolean affordable
) {
    public static TreasureView from(Item item, int gold, boolean owned) {
        return new TreasureView(item.id(), item.name(), item.price(),
                item.description(), owned, !owned && item.price() <= gold);
    }
}
