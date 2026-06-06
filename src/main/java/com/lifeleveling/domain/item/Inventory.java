package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Inventory {

    private final List<Item> ownedItems;
    private final Map<ItemSlot, Item> equippedItems;
    private final List<PurchaseRecord> purchaseHistory;
    private final Map<String, Instant> itemCooldowns;

    public Inventory() {
        this.ownedItems = new ArrayList<>();
        this.equippedItems = new EnumMap<>(ItemSlot.class);
        this.purchaseHistory = new ArrayList<>();
        this.itemCooldowns = new HashMap<>();
    }

    // ... (Métodos de gestión de items addItem, removeItem, etc. sin cambios) ...
    public void addItem(Item item) {
        Objects.requireNonNull(item);
        if (!item.isConsumable() && hasItem(item.id())) return;
        ownedItems.add(item);
    }

    public void removeItem(String itemId) {
        Optional<Item> itemToRemove = ownedItems.stream().filter(i -> i.id().equals(itemId)).findFirst();
        itemToRemove.ifPresent(ownedItems::remove);
    }

    public boolean hasItem(String itemId) { return ownedItems.stream().anyMatch(i -> i.id().equals(itemId)); }

    public void recordPurchase(Item item) {
        purchaseHistory.add(new PurchaseRecord(item.id(), item.category(), Instant.now()));
        addItem(item);
    }

    public Optional<Item> getItem(String itemId) { return ownedItems.stream().filter(i -> i.id().equals(itemId)).findFirst(); }

    // ... (Métodos de equipamiento equip, unequip, etc. sin cambios) ...
    public void equip(String itemId) {
        Item item = getItem(itemId).orElseThrow(() -> new IllegalArgumentException("No posees el item: " + itemId));
        if (item.slot() == ItemSlot.NONE) throw new IllegalArgumentException("Este item no se puede equipar");
        equippedItems.put(item.slot(), item);
    }

    public void unequip(ItemSlot slot) { equippedItems.remove(slot); }
    public Optional<Item> getEquippedItem(ItemSlot slot) { return Optional.ofNullable(equippedItems.get(slot)); }

    public List<Item> getOwnedItems() { return List.copyOf(ownedItems); }
    public Map<ItemSlot, Item> getEquippedItems() { return Map.copyOf(equippedItems); }

    // ========================================================================================
    // CÁLCULO DE STATS & EFECTOS ESPECIALES
    // ========================================================================================

    public Map<StatType, Integer> getTotalStatBonuses() {
        Map<StatType, Integer> totalBonuses = new EnumMap<>(StatType.class);
        for (Item item : equippedItems.values()) {
            item.statBonuses().forEach((stat, value) -> totalBonuses.merge(stat, value, Integer::sum));
        }
        return totalBonuses;
    }

    public int getHourlyWorkDamageMitigation() {
        int mitigation = 0;
        if (isEquipped("gear_mouse")) mitigation += 1;
        if (isEquipped("gear_chair_herman")) mitigation += 3;
        else if (isEquipped("gear_chair_markus")) mitigation += 1;
        return mitigation;
    }

    public int getGymDamageMitigation() {
        if (isEquipped("gear_shoes_run")) return 5;
        return 0;
    }

    public double getSleepXPMultiplier() {
        if (isEquipped("gear_mattress")) return 2.0;
        return 1.0;
    }

    /**
     * [NUEVO FASE 3.3] Calcula el multiplicador de precio para bebidas sociales.
     * Si tienes la Cafetera Espresso (gear_espresso_machine) equipada, el café es casi gratis.
     * Retorna 0.25 (75% descuento) o 1.0 (precio normal).
     */
    public double getSocialDrinkDiscountMultiplier() {
        if (isEquipped("gear_espresso_machine")) {
            return 0.25;
        }
        return 1.0;
    }

    private boolean isEquipped(String itemId) {
        return equippedItems.values().stream().anyMatch(i -> i.id().equals(itemId));
    }

    // ... (Resto de métodos cooldowns y queries sin cambios) ...
    public boolean isCooldownActive(String itemId) {
        if (!itemCooldowns.containsKey(itemId)) return false;
        return Instant.now().isBefore(itemCooldowns.get(itemId));
    }
    public void triggerCooldown(String itemId, long duration, ChronoUnit unit) {
        itemCooldowns.put(itemId, Instant.now().plus(duration, unit));
    }
    public boolean canUseSpecialItem(String itemId) { return !isCooldownActive(itemId); }

    public boolean hasPurchasedCategoryInLastDays(ItemCategory category, int days) {
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        return purchaseHistory.stream().filter(record -> record.timestamp.isAfter(threshold))
                .anyMatch(record -> record.category == category);
    }
    public boolean hasPurchasedItemInLastDays(String itemId, int days) {
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        return purchaseHistory.stream().filter(record -> record.timestamp.isAfter(threshold))
                .anyMatch(record -> record.itemId.equals(itemId));
    }
    private record PurchaseRecord(String itemId, ItemCategory category, Instant timestamp) {}
}