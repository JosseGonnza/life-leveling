package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record Item(
        String id,
        String name,
        String description,
        int price,
        ItemTier tier,
        ItemSlot slot,
        ItemCategory category,
        Map<StatType, Integer> statBonuses,
        int hpRecovery,
        int hpDamage,
        boolean isConsumable,
        boolean isHidden // <--- NUEVO CAMPO: True si es un item secreto/desbloqueable
) {
    public Item {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(tier);
        Objects.requireNonNull(slot);
        Objects.requireNonNull(category);
        if (price < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        if (statBonuses == null) statBonuses = Collections.emptyMap();
    }

    // --- FACTORÍAS ---

    // Equipamiento normal (Visible)
    public static Item createEquip(String id, String name, int price, ItemTier tier, ItemSlot slot, ItemCategory cat, Map<StatType, Integer> bonuses) {
        return new Item(id, name, "", price, tier, slot, cat, bonuses, 0, 0, false, false);
    }

    // Consumibles (Visibles)
    public static Item createConsumable(String id, String name, int price, ItemCategory cat, int hpRec, int hpDmg) {
        return new Item(id, name, "", price, ItemTier.CONSUMABLE, ItemSlot.NONE, cat, Map.of(), hpRec, hpDmg, true, false);
    }

    // NUEVO: Tesoros (Visibles pero caros)
    public static Item createTreasure(String id, String name, int price, String description) {
        // Los tesoros van al slot NONE (inventario) o WALL (si quieres colgarlos como trofeo)
        // Usamos TIER_3 por ser endgame.
        return new Item(id, name, description, price, ItemTier.TIER_3, ItemSlot.NONE, ItemCategory.TREASURE, Map.of(), 0, 0, false, false);
    }

    // NUEVO: Artefactos Ocultos (Hidden = true)
    public static Item createArtifact(String id, String name, String description, ItemSlot slot) {
        // Los artefactos suelen ser gratis o premios de Quest, precio 0 o simbólico.
        // TIER_3 porque son legendarios.
        return new Item(id, name, description, 0, ItemTier.TIER_3, slot, ItemCategory.ARTIFACT, Map.of(), 0, 0, false, true);
    }

    public String toDisplayString() {
        if (isHidden) return "🔒 ??? (Item Oculto)";
        return String.format("%s %s | %d G", slot.getIcon(), name, price);
    }
}