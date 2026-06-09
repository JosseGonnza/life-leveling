package com.lifeleveling.application.dto;

import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.item.ItemCategory;
import com.lifeleveling.domain.item.ItemSlot;
import com.lifeleveling.domain.player.StatType;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read model de un ítem del catálogo para el modo SHOP de The Armory.
 * `affordable` ya viene calculado contra el oro actual (la UI tiñe en rojo si false).
 * `unlocked`/`requiredLevel`: candado por nivel del tier (la UI muestra "🔒 Nivel X" si false).
 */
public record ShopItemView(
        String id,
        String name,
        int price,
        String category,
        String slot,
        String effect,
        boolean affordable,
        boolean unlocked,
        int requiredLevel,
        boolean burnoutSafe
) {
    public static ShopItemView from(Item item, int gold, int playerLevel) {
        String slot = item.slot() == ItemSlot.NONE ? "—" : item.slot().getDisplayName();
        return new ShopItemView(item.id(), item.name(), item.price(),
                item.category().getDisplayName(), slot, effect(item), item.price() <= gold,
                item.tier().canEquip(playerLevel), item.tier().getMinLevel(), burnoutSafe(item));
    }

    /** Comprable durante el Burnout: curas (MEDICINE) + los items de salida rápida (Escapada Fin de Semana). */
    private static boolean burnoutSafe(Item item) {
        return item.category() == ItemCategory.MEDICINE
                || item.id().equals(ItemCatalog.WEEKEND_TRIP.id());
    }

    private static String effect(Item item) {
        Map<StatType, Integer> bonuses = item.statBonuses();
        if (!bonuses.isEmpty()) {
            return bonuses.entrySet().stream()
                    .map(e -> "+" + e.getValue() + " " + e.getKey().getAbbreviation())
                    .collect(Collectors.joining("  "));
        }
        if (item.hpRecovery() > 0) return "+" + item.hpRecovery() + " HP";
        if (item.hpDamage() > 0) return "-" + item.hpDamage() + " HP";
        return item.description() == null ? "" : item.description();
    }
}
