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
     * Método necesario para consumir items.
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
    // CÁLCULO DE STATS & EFECTOS ESPECIALES [FASE 1.3]
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

    /**
     * Calcula cuántos HP te ahorras POR HORA de trabajo gracias al equipo.
     * IDs según ItemCatalog.
     */
    public int getHourlyWorkDamageMitigation() {
        int mitigation = 0;

        // 1. Periféricos (Ratón Ergonómico reduce 1 HP/h)
        if (isEquipped("gear_mouse")) {
            mitigation += 1;
        }

        // 2. Sillas
        // Herman Miller anula el coste base de 3 HP/h
        if (isEquipped("gear_chair_herman")) {
            mitigation += 3;
        }
        // Sillas de gama media (Markus) reducen 1 HP/h
        else if (isEquipped("gear_chair_markus")) {
            mitigation += 1;
        }

        return mitigation;
    }

    /**
     * Calcula cuántos HP te ahorras por sesión de GYM.
     * Daño Base Gym = 5 HP.
     */
    public int getGymDamageMitigation() {
        // gear_shoes_run (Pegasus): Gym cuesta 0 HP -> Mitiga 5
        if (isEquipped("gear_shoes_run")) {
            return 5;
        }
        return 0;
    }

    /**
     * [NUEVO FASE 3.1] Calcula el multiplicador de XP para la quest SLEEP.
     * Base: 1.0
     * Con Colchón Premium (gear_mattress): 2.0 (+100%)
     */
    public double getSleepXPMultiplier() {
        if (isEquipped("gear_mattress")) {
            return 2.0;
        }
        return 1.0;
    }

    /**
     * Helper privado para verificar si llevamos puesto un item concreto por ID.
     */
    private boolean isEquipped(String itemId) {
        return equippedItems.values().stream()
                .anyMatch(i -> i.id().equals(itemId));
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