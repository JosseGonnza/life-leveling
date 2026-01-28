package com.lifeleveling.domain.player;

import com.lifeleveling.domain.debuff.Debuff;
import com.lifeleveling.domain.debuff.DebuffTracker;
import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.title.TitleInventory;
import com.lifeleveling.domain.title.TitleType;

import java.time.Instant;
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
 * 6. Gestionar los Debuffs activos y sus penalizaciones. [Nuevo]
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

    // [Fase 4] Nuevo componente: Cerebro de Debuffs
    private final DebuffTracker debuffTracker;

    // Estado de Burnout (Bloqueo temporal)
    private BurnoutLock activeBurnoutLock;

    // Constructor privado para forzar uso de métodos factoría
    private Player(UUID id, String name, int currentHP, Stats stats, Wallet wallet,
                   Inventory inventory, TitleInventory titleInventory,
                   DebuffTracker debuffTracker, BurnoutLock activeBurnoutLock) {
        this.id = id;
        this.name = name;
        this.currentHP = currentHP;
        this.hpState = HPState.fromHP(currentHP); // El estado se deriva del HP numérico
        this.stats = stats;
        this.wallet = wallet;
        this.inventory = inventory;
        this.titleInventory = titleInventory;
        this.debuffTracker = debuffTracker;
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
                new DebuffTracker(),   // [Fase 4] Tracker vacío
                null                   // Sin burnout al inicio
        );
    }

    // Para reconstruir desde persistencia (JSON/DB)
    public static Player restore(UUID id, String name, int currentHP, Stats stats, Wallet wallet,
                                 Inventory inventory, TitleInventory titleInventory,
                                 DebuffTracker debuffTracker, BurnoutLock lock) {
        return new Player(id, name, currentHP, stats, wallet, inventory, titleInventory, debuffTracker, lock);
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

    /**
     * Añade XP a un stat. Aplica penalizaciones de Salud (TIRED) y Debuffs (CHAOS, FATIGUE).
     * Y Bonificadores de Títulos.
     */
    public void addXP(StatType type, int amount) {
        if (amount <= 0) return;

        // --- PASO 1: Penalizaciones por Salud (HPState) ---
        // Ej: TIRED reduce XP a la mitad.
        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        // Nota: Si HPState ya tenía un método applyXPMultiplier, úsalo, pero devuelve int y perdemos precisión para los siguientes pasos.
        // Mejor trabajar con doubles hasta el final.

        // --- PASO 2: Penalizaciones por Debuffs [Fase 4] ---
        // Ej: FATIGUE (0.5 global) y CHAOS (0.8 WIS).
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double debuffStatMult = debuffTracker.getStatXPMultiplier(type);

        // --- PASO 3: Bonificadores de Títulos ---
        double titleMultiplier = titleInventory.getStatXPMultiplier(type);

        // --- CÁLCULO FINAL ---
        // XP = Base * (HP_Factor * Debuff_Global * Debuff_Stat) * Title_Bonus
        double totalMultiplier = hpMultiplier * debuffGlobalMult * debuffStatMult * titleMultiplier;

        int finalAmount = (int) Math.round(amount * totalMultiplier);

        // Logging de depuración si hubo reducción drástica
        if (finalAmount < amount) {
            // System.out.println("🔻 XP Reducida: " + amount + " -> " + finalAmount + " (HP:" + hpMultiplier + ", Debuffs:" + (debuffGlobalMult*debuffStatMult) + ")");
        }

        if (finalAmount > 0) {
            // 4. Delegamos la subida al objeto Stats BASE
            this.stats = stats.addXP(type, finalAmount);

            // 5. [Regla de Oro] La XP Neta ganada se suma también al Nivel General (implícito en Stats.getTotalAccumulatedXP, pero si tienes un contador separado, súmalo aquí).
            // Si tu implementación de Stats ya maneja la XP General internamente, perfecto. Si no:
            // this.stats = stats.addGeneralXP(finalAmount);
        }
    }

    /**
     * Añade XP general pura.
     */
    public void addGeneralXP(int amount) {
        // Misma lógica de multiplicadores globales
        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double titleMultiplier = titleInventory.getGeneralXPMultiplier();

        int finalAmount = (int) Math.round(amount * hpMultiplier * debuffGlobalMult * titleMultiplier);

        // Distribuimos equitativamente entre stats para mantener la coherencia
        if (finalAmount > 0) {
            int perStat = finalAmount / StatType.values().length;
            for (StatType type : StatType.values()) {
                this.stats = stats.addXP(type, perStat);
            }
        }
    }

    public void addGold(int amount) {
        // 1. Salud
        int afterHpMultiplier = hpState.applyGoldMultiplier(amount);

        // 2. Debuffs (Si alguno penaliza ganancia global de oro, iría aquí)
        // Por ahora TRAPPED penaliza por hora, no porcentual.

        // 3. Títulos
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
            // Reglas especiales de curación en Burnout...
        }

        // Bonus de títulos
        int finalAmount = titleInventory.applyHPRecoveryBonus(amount);

        this.currentHP = Math.min(100, currentHP + finalAmount);
        this.hpState = HPState.fromHP(this.currentHP);
    }

    public void takeDamage(int amount) {
        this.currentHP = Math.max(0, currentHP - amount);
        this.hpState = HPState.fromHP(this.currentHP);

        if (this.currentHP == 0 && activeBurnoutLock == null) {
            triggerBurnout();
        }
    }

    public void takeWorkDamage(int baseDamage) {
        int mitigation = inventory.getTotalDamageMitigation();
        int finalDamage = Math.max(0, baseDamage - mitigation);
        takeDamage(finalDamage);
    }

    private void triggerBurnout() {
        this.activeBurnoutLock = BurnoutLock.createNow();
        this.wallet = wallet.applyBurnoutTax();
    }

    public boolean isBurnoutActive() {
        return activeBurnoutLock != null && !activeBurnoutLock.hasExpired(Instant.now());
    }

    public void tryClearBurnout() {
        if (activeBurnoutLock != null && activeBurnoutLock.hasExpired(Instant.now()) && currentHP > 0) {
            this.activeBurnoutLock = null;
        }
    }

    // ========================================================================================
    // GESTIÓN DE DEBUFFS
    // ========================================================================================

    public void applyDebuff(DebuffType type, String source, Instant now) {
        // [Fase 5] Verificar Inmunidad por Títulos
        // Ej: Título "Mente de Acero" da inmunidad a "CHAOS"
        if (titleInventory.hasImmunityTo(type.name())) {
            // System.out.println("🛡️ INMUNE a " + type.getDisplayName() + " gracias a tus títulos.");
            return;
        }

        Debuff debuff = Debuff.create(type, source, now);
        debuffTracker.applyDebuff(debuff);
    }

    public void cureDebuff(DebuffType type) {
        if (debuffTracker.hasDebuff(type)) {
            debuffTracker.removeDebuff(type);
        }
    }

    public DebuffTracker getDebuffTracker() {
        return debuffTracker;
    }

    // Método para el ciclo diario (Clean Up)
    public void updateState(Instant now) {
        // Limpiamos debuffs expirados
        debuffTracker.cleanExpiredDebuffs(now);
        // Intentamos limpiar Burnout si corresponde
        tryClearBurnout();
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

    public boolean unlockTitle(TitleType type) {
        return titleInventory.unlock(type);
    }

    public void equipTitle(TitleType type) {
        titleInventory.equip(type, getLevel());
    }

    public boolean unequipTitle(TitleType type) {
        return titleInventory.unequip(type);
    }

    public void swapTitle(TitleType oldType, TitleType newType) {
        titleInventory.swap(oldType, newType, getLevel());
    }

    public boolean hasTitle(TitleType type) {
        return titleInventory.hasTitle(type);
    }

    public boolean isTitleEquipped(TitleType type) {
        return titleInventory.isEquipped(type);
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

    public Stats getBaseStats() {
        return stats;
    }

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