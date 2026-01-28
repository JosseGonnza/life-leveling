package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Inventory {

    private final List<Item> ownedItems;
    private final Map<ItemSlot, Item> equippedItems;

    // Historial de compras (IDs de items y fecha).
    private final List<PurchaseRecord> purchaseHistory;

    // Cooldowns para items especiales
    private final Map<String, Instant> itemCooldowns;

    public Inventory() {
        this.ownedItems = new ArrayList<>();
        this.equippedItems = new EnumMap<>(ItemSlot.class);
        this.purchaseHistory = new ArrayList<>();
        this.itemCooldowns = new HashMap<>();
    }

    // ========================================================================================
    // GESTIÓN DE ITEMS (ADD / REMOVE)
    // ========================================================================================

    public void addItem(Item item) {
        Objects.requireNonNull(item);
        // Si ya lo tenemos y es equipable (no consumible), no lo duplicamos
        if (!item.isConsumable() && hasItem(item.id())) {
            return;
        }
        ownedItems.add(item);
    }

    /**
     * [NUEVO] Método necesario para consumir items.
     * Elimina una instancia del item del inventario.
     */
    public void removeItem(String itemId) {
        // Buscamos el primer item que coincida con el ID
        Optional<Item> itemToRemove = ownedItems.stream()
                .filter(i -> i.id().equals(itemId))
                .findFirst();

        // Si existe, lo borramos de la lista (solo uno, por si tienes 10 pociones)
        itemToRemove.ifPresent(ownedItems::remove);
    }

    public boolean hasItem(String itemId) {
        return ownedItems.stream().anyMatch(i -> i.id().equals(itemId));
    }

    public void recordPurchase(Item item) {
        purchaseHistory.add(new PurchaseRecord(item.id(), item.category(), Instant.now()));
        addItem(item);
    }

    public Optional<Item> getItem(String itemId) {
        return ownedItems.stream()
                .filter(i -> i.id().equals(itemId))
                .findFirst();
    }

    // ========================================================================================
    // EQUIPAMIENTO (LOADOUT)
    // ========================================================================================

    public void equip(String itemId) {
        Item item = getItem(itemId)
                .orElseThrow(() -> new IllegalArgumentException("No posees el item: " + itemId));

        if (item.slot() == ItemSlot.NONE) {
            throw new IllegalArgumentException("Este item no se puede equipar (es de inventario)");
        }

        equippedItems.put(item.slot(), item);
    }

    public void unequip(ItemSlot slot) {
        equippedItems.remove(slot);
    }

    public Optional<Item> getEquippedItem(ItemSlot slot) {
        return Optional.ofNullable(equippedItems.get(slot));
    }

    // ========================================================================================
    // CÁLCULO DE STATS
    // ========================================================================================

    public Map<StatType, Integer> getTotalStatBonuses() {
        Map<StatType, Integer> totalBonuses = new EnumMap<>(StatType.class);

        for (Item item : equippedItems.values()) {
            item.statBonuses().forEach((stat, value) ->
                    totalBonuses.merge(stat, value, Integer::sum)
            );
        }
        return totalBonuses;
    }

    public int getTotalDamageMitigation() {
        return 0; // Implementar lógica futura aquí si es necesario
    }

    // ========================================================================================
    // SISTEMA DE COOLDOWNS Y CONSULTAS
    // ========================================================================================

    public boolean isCooldownActive(String itemId) {
        if (!itemCooldowns.containsKey(itemId)) return false;
        return Instant.now().isBefore(itemCooldowns.get(itemId));
    }

    public void triggerCooldown(String itemId, long duration, ChronoUnit unit) {
        itemCooldowns.put(itemId, Instant.now().plus(duration, unit));
    }

    public boolean canUseSpecialItem(String itemId) {
        return !isCooldownActive(itemId);
    }

    public boolean hasPurchasedCategoryInLastDays(ItemCategory category, int days) {
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        return purchaseHistory.stream()
                .filter(record -> record.timestamp.isAfter(threshold))
                .anyMatch(record -> record.category == category);
    }

    public boolean hasPurchasedItemInLastDays(String itemId, int days) {
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        return purchaseHistory.stream()
                .filter(record -> record.timestamp.isAfter(threshold))
                .anyMatch(record -> record.itemId.equals(itemId));
    }

    private record PurchaseRecord(String itemId, ItemCategory category, Instant timestamp) {}
}