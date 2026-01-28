package com.lifeleveling.domain.player;

import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.title.TitleInventory;
import com.lifeleveling.domain.title.TitleType;

import java.util.UUID;

/**
 * Player: La entidad raíz (Aggregate Root) del dominio.
 *
 * Responsabilidades:
 * 1. Mantener la consistencia entre HP, Stats y Wallet.
 * 2. Calcular el Nivel General basado en la XP total acumulada.
 * 3. Orquestar la recepción de daño y curación.
 * 4. Gestionar el equipamiento e inventario.
 * 5. Gestionar los títulos desbloqueados y equipados.
 */
public class Player {

    private final UUID id;
    private final String name;

    // Componentes del Jugador (Value Objects inmutables)
    private HPState hpState;
    private int currentHP;
    private Stats stats; // Stats Base (Real XP)
    private Wallet wallet;

    // Componentes mutables (Entidades internas)
    private final Inventory inventory;
    private final TitleInventory titleInventory;

    // Estado de Burnout (Bloqueo temporal)
    private BurnoutLock activeBurnoutLock;

    // Constructor privado para forzar uso de métodos factoría
    private Player(UUID id, String name, int currentHP, Stats stats, Wallet wallet,
                   Inventory inventory, TitleInventory titleInventory, BurnoutLock activeBurnoutLock) {
        this.id = id;
        this.name = name;
        this.currentHP = currentHP;
        this.hpState = HPState.fromHP(currentHP); // El estado se deriva del HP numérico
        this.stats = stats;
        this.wallet = wallet;
        this.inventory = inventory;
        this.titleInventory = titleInventory;
        this.activeBurnoutLock = activeBurnoutLock;
    }

    // ========================================================================================
    // FACTORÍAS
    // ========================================================================================

    public static Player create(String name) {
        return new Player(
                UUID.randomUUID(),
                name,
                100, // HP Inicial
                Stats.initial(),
                Wallet.empty(),
                new Inventory(),       // Inventario vacío
                new TitleInventory(),  // Sin títulos al inicio
                null                   // Sin burnout al inicio
        );
    }

    // Para reconstruir desde persistencia (JSON/DB)
    public static Player restore(UUID id, String name, int currentHP, Stats stats, Wallet wallet,
                                  Inventory inventory, TitleInventory titleInventory, BurnoutLock lock) {
        return new Player(id, name, currentHP, stats, wallet, inventory, titleInventory, lock);
    }

    // ========================================================================================
    // LÓGICA DE NEGOCIO PRINCIPAL
    // ========================================================================================

    /**
     * Calcula el Nivel General del jugador basado en la XP total de todos sus stats.
     * Fórmula inversa de: Total_XP = 45 * Nivel^2
     * Nivel = Raíz_Cuadrada(Total_XP / 45)
     */
    public int getLevel() {
        long totalXP = stats.getTotalAccumulatedXP();
        if (totalXP == 0) return 1;

        // Aplicamos la fórmula inversa para sacar el nivel actual
        int level = (int) Math.sqrt(totalXP / 45.0);

        // Clamp: Mínimo nivel 1, Máximo nivel 100
        return Math.max(1, Math.min(level, 100));
    }

    public void addXP(StatType type, int amount) {
        // 1. Aplicamos multiplicadores según estado de salud (ej: TIRED da 0.5x XP)
        int afterHpMultiplier = hpState.applyXPMultiplier(amount);

        // 2. Aplicamos multiplicadores de títulos equipados
        double titleMultiplier = titleInventory.getStatXPMultiplier(type);
        int finalAmount = (int) Math.round(afterHpMultiplier * titleMultiplier);

        // 3. Delegamos la subida al objeto Stats BASE (que es inmutable, nos devuelve uno nuevo)
        // OJO: La XP siempre se suma a los stats base, no a los dopados por equipo.
        this.stats = stats.addXP(type, finalAmount);
    }

    /**
     * Añade XP general (distribuida entre todos los stats o para nivel general).
     * Aplica multiplicadores de estado de salud y títulos.
     */
    public void addGeneralXP(int amount) {
        // 1. Aplicamos multiplicador de salud
        int afterHpMultiplier = hpState.applyXPMultiplier(amount);

        // 2. Aplicamos multiplicador de títulos para XP general
        double titleMultiplier = titleInventory.getGeneralXPMultiplier();
        int finalAmount = (int) Math.round(afterHpMultiplier * titleMultiplier);

        // 3. Distribuimos equitativamente entre todos los stats
        int perStat = finalAmount / StatType.values().length;
        for (StatType type : StatType.values()) {
            this.stats = stats.addXP(type, perStat);
        }
    }

    public void addGold(int amount) {
        // 1. Aplicamos multiplicadores de estado de salud
        int afterHpMultiplier = hpState.applyGoldMultiplier(amount);

        // 2. Aplicamos multiplicadores de títulos
        double titleMultiplier = titleInventory.getGoldMultiplier();
        int finalAmount = (int) Math.round(afterHpMultiplier * titleMultiplier);

        this.wallet = wallet.add(finalAmount);
    }

    public void spendGold(int amount) {
        if (!wallet.canAfford(amount)) {
            throw new IllegalStateException("No tienes suficiente oro");
        }
        this.wallet = wallet.subtract(amount);
    }

    // ========================================================================================
    // GESTIÓN DE SALUD (HP)
    // ========================================================================================

    public void heal(int amount) {
        if (isBurnoutActive()) {
            // Reglas especiales de curación en Burnout se manejarían aquí o en el servicio
        }

        // Aplicamos bonus de recuperación de HP de títulos equipados
        int finalAmount = titleInventory.applyHPRecoveryBonus(amount);

        this.currentHP = Math.min(100, currentHP + finalAmount);
        this.hpState = HPState.fromHP(this.currentHP);
    }

    public void takeDamage(int amount) {
        // Reducimos HP (mínimo 0)
        this.currentHP = Math.max(0, currentHP - amount);
        this.hpState = HPState.fromHP(this.currentHP);

        // Verificamos si acabamos de entrar en BURNOUT
        if (this.currentHP == 0 && activeBurnoutLock == null) {
            triggerBurnout();
        }
    }

    /**
     * Daño laboral (mitigado por equipamiento).
     * Ejemplo: Si tienes Silla Ergonómica (-1 daño), trabajar duele menos.
     */
    public void takeWorkDamage(int baseDamage) {
        int mitigation = inventory.getTotalDamageMitigation();
        int finalDamage = Math.max(0, baseDamage - mitigation); // Nunca daño negativo (curación)
        takeDamage(finalDamage);
    }

    private void triggerBurnout() {
        // 1. Crear el bloqueo
        this.activeBurnoutLock = BurnoutLock.createNow();

        // 2. Aplicar la penalización económica (Impuesto de Salud)
        this.wallet = wallet.applyBurnoutTax();
    }

    public boolean isBurnoutActive() {
        return activeBurnoutLock != null && !activeBurnoutLock.hasExpired(java.time.Instant.now());
    }

    // Intenta salir del burnout si ya pasó el tiempo y tenemos HP
    public void tryClearBurnout() {
        if (activeBurnoutLock != null && activeBurnoutLock.hasExpired(java.time.Instant.now()) && currentHP > 0) {
            this.activeBurnoutLock = null;
        }
    }

    // ========================================================================================
    // INVENTARIO & EQUIPAMIENTO
    // ========================================================================================

    public void buyItem(Item item) {
        spendGold(item.price());
        inventory.recordPurchase(item);
    }

    public void equipItem(String itemId) {
        inventory.equip(itemId);
    }

    public void unequipItem(com.lifeleveling.domain.item.ItemSlot slot) {
        inventory.unequip(slot);
    }

    public Inventory getInventory() {
        return inventory;
    }

    // ========================================================================================
    // GESTIÓN DE TÍTULOS
    // ========================================================================================

    /**
     * Desbloquea un nuevo título para el jugador.
     * @return true si se desbloqueó (no lo tenía), false si ya lo tenía
     */
    public boolean unlockTitle(TitleType type) {
        return titleInventory.unlock(type);
    }

    /**
     * Equipa un título en un slot disponible.
     * @throws IllegalStateException si no tiene el título o no hay slots disponibles
     */
    public void equipTitle(TitleType type) {
        titleInventory.equip(type, getLevel());
    }

    /**
     * Desequipa un título.
     * @return true si se desequipó, false si no estaba equipado
     */
    public boolean unequipTitle(TitleType type) {
        return titleInventory.unequip(type);
    }

    /**
     * Intercambia un título equipado por otro.
     */
    public void swapTitle(TitleType oldType, TitleType newType) {
        titleInventory.swap(oldType, newType, getLevel());
    }

    /**
     * Verifica si el jugador tiene un título desbloqueado.
     */
    public boolean hasTitle(TitleType type) {
        return titleInventory.hasTitle(type);
    }

    /**
     * Verifica si un título está equipado.
     */
    public boolean isTitleEquipped(TitleType type) {
        return titleInventory.isEquipped(type);
    }

    /**
     * Verifica si el jugador tiene inmunidad a un debuff (por título equipado).
     */
    public boolean hasDebuffImmunity(String debuffName) {
        return titleInventory.hasImmunityTo(debuffName);
    }

    /**
     * Obtiene el número de slots de título disponibles según el nivel.
     */
    public int getTitleSlots() {
        return titleInventory.getAvailableSlots(getLevel());
    }

    public TitleInventory getTitleInventory() {
        return titleInventory;
    }

    // ========================================================================================
    // GETTERS
    // ========================================================================================

    public int getCurrentHP() {
        return currentHP;
    }

    public int getCurrentGold() {
        return wallet.currentGold();
    }

    /**
     * Retorna los Stats BASE (sin bonificadores).
     * Usar esto para persistencia o cálculos de XP.
     */
    public Stats getBaseStats() {
        return stats;
    }

    /**
     * Retorna los Stats EFECTIVOS (Base + Equipo).
     * Usar esto para la UI y checks de requisitos.
     */
    public Stats getEffectiveStats() {
        return stats.applyBonuses(inventory.getTotalStatBonuses());
    }

    public HPState getHpState() {
        return hpState;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}