package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Inventory {

    private final List<Item> ownedItems;
    private final Map<ItemSlot, Item> equippedItems;

    // Historial de compras (IDs de items y fecha). Vital para Elder Quest "Voto de Pobreza".
    private final List<PurchaseRecord> purchaseHistory;

    // Cooldowns para items especiales (Café Infinito, Llave Maestra)
    // Key: ItemID, Value: Cuándo estará disponible de nuevo
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

        // Reemplazar: Si ya hay algo, vuelve a la mochila (conceptualmente)
        // En este modelo simple, 'ownedItems' tiene TODO, y 'equippedItems' tiene la referencia activa.
        equippedItems.put(item.slot(), item);
    }

    public void unequip(ItemSlot slot) {
        equippedItems.remove(slot);
    }

    public Optional<Item> getEquippedItem(ItemSlot slot) {
        return Optional.ofNullable(equippedItems.get(slot));
    }

    // ========================================================================================
    // CÁLCULO DE STATS (La magia RPG)
    // ========================================================================================

    /**
     * Calcula el total de bonificadores de todos los items equipados.
     * El Player usará esto para sumar a sus Stats base.
     */
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
     * Calcula la mitigación de daño total (ej: Silla -1 HP, Ratón -1 HP).
     * Se usará en el método takeWorkDamage del Player.
     */
    public int getTotalDamageMitigation() {
        // En el futuro, podríamos poner esto como un atributo en Item.java (mitigation)
        // Por ahora, asumimos que ciertos items reducen daño si están equipados.
        // Implementación placeholder o basada en lógica específica
        return 0;
    }

    // ========================================================================================
    // SISTEMA DE COOLDOWNS (Items Mágicos / Gratuitos)
    // ========================================================================================

    public boolean isCooldownActive(String itemId) {
        if (!itemCooldowns.containsKey(itemId)) return false;
        return Instant.now().isBefore(itemCooldowns.get(itemId));
    }

    public void triggerCooldown(String itemId, long duration, ChronoUnit unit) {
        itemCooldowns.put(itemId, Instant.now().plus(duration, unit));
    }

    // Método para la UI: "¿Puedo pedir el café gratis?"
    public boolean canUseSpecialItem(String itemId) {
        // 1. Debo tenerlo desbloqueado (estar en ownedItems) O ser un servicio de tienda
        // 2. No debe estar en cooldown
        return !isCooldownActive(itemId);
    }

    // ========================================================================================
    // CONSULTAS PARA ELDER QUESTS
    // ========================================================================================

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

    // Record interno para el historial
    private record PurchaseRecord(String itemId, ItemCategory category, Instant timestamp) {}
}