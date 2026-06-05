package com.lifeleveling.application.service;

import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.player.Player;

/**
 * Casos de uso de la tienda/inventario. La UI trabaja con ids de item; aquí se resuelven
 * contra el catálogo antes de delegar en el dominio.
 */
public final class ShopService {

    public void buy(Player player, String itemId) {
        player.buyItem(resolve(itemId));
    }

    public void consume(Player player, String itemId) {
        player.consumeItem(resolve(itemId));
    }

    public void equip(Player player, String itemId) {
        player.equipItem(itemId);
    }

    private Item resolve(String itemId) {
        return ItemCatalog.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item desconocido: " + itemId));
    }
}
