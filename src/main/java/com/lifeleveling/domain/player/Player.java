package com.lifeleveling.domain.player;

import com.lifeleveling.domain.career.CareerEngine;
import com.lifeleveling.domain.career.CareerReward;
import com.lifeleveling.domain.career.CodeSession;
import com.lifeleveling.domain.debuff.Debuff;
import com.lifeleveling.domain.debuff.DebuffTracker;
import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.title.TitleInventory;
import com.lifeleveling.domain.title.TitleType;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Player: La entidad raíz (Aggregate Root) del dominio.
 * El "Dios" del estado del juego.
 */
public class Player {

    private final UUID id;
    private final String name;

    // Componentes del Jugador
    private HPState hpState;
    private int currentHP;
    private Stats stats;
    private Wallet wallet;

    // Componentes mutables
    private final Inventory inventory;
    private final TitleInventory titleInventory;
    private final DebuffTracker debuffTracker;
    private final GateTracker gateTracker;
    private final CareerEngine careerEngine;

    // Estado de Burnout
    private BurnoutLock activeBurnoutLock;

    private Player(UUID id, String name, int currentHP, Stats stats, Wallet wallet,
                   Inventory inventory, TitleInventory titleInventory,
                   DebuffTracker debuffTracker, GateTracker gateTracker,
                   CareerEngine careerEngine,
                   BurnoutLock activeBurnoutLock) {
        this.id = id;
        this.name = name;
        this.currentHP = currentHP;
        this.hpState = HPState.fromHP(currentHP);
        this.stats = stats;
        this.wallet = wallet;
        this.inventory = inventory;
        this.titleInventory = titleInventory;
        this.debuffTracker = debuffTracker;
        this.gateTracker = gateTracker;
        this.careerEngine = careerEngine;
        this.activeBurnoutLock = activeBurnoutLock;
    }

    // ========================================================================================
    // FACTORÍAS
    // ========================================================================================

    public static Player create(String name) {
        return new Player(
                UUID.randomUUID(),
                name,
                100,
                Stats.initial(),
                Wallet.empty(),
                new Inventory(),
                new TitleInventory(),
                new DebuffTracker(),
                new GateTracker(),
                new CareerEngine(),
                null
        );
    }

    public static Player restore(UUID id, String name, int currentHP, Stats stats, Wallet wallet,
                                 Inventory inventory, TitleInventory titleInventory,
                                 DebuffTracker debuffTracker, GateTracker gateTracker,
                                 CareerEngine careerEngine,
                                 BurnoutLock lock) {
        return new Player(id, name, currentHP, stats, wallet, inventory, titleInventory,
                debuffTracker, gateTracker, careerEngine, lock);
    }

    // ========================================================================================
    // LÓGICA DE NEGOCIO PRINCIPAL (LEVELING)
    // ========================================================================================

    public int getLevel() {
        long totalXP = stats.getTotalAccumulatedXP();
        if (totalXP == 0) return 1;
        int level = (int) Math.sqrt(totalXP / 45.0);
        return Math.max(1, Math.min(level, 100));
    }

    public void addXP(StatType type, int amount) {
        if (amount <= 0) return;

        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double debuffStatMult = debuffTracker.getStatXPMultiplier(type);
        double titleMultiplier = titleInventory.getStatXPMultiplier(type);

        double totalMultiplier = hpMultiplier * debuffGlobalMult * debuffStatMult * titleMultiplier;
        int finalAmount = (int) Math.round(amount * totalMultiplier);

        if (finalAmount > 0) {
            this.stats = stats.addXP(type, finalAmount);
        }
    }

    public void addGeneralXP(int amount) {
        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double titleMultiplier = titleInventory.getGeneralXPMultiplier();

        int finalAmount = (int) Math.round(amount * hpMultiplier * debuffGlobalMult * titleMultiplier);

        if (finalAmount > 0) {
            int perStat = finalAmount / StatType.values().length;
            for (StatType type : StatType.values()) {
                this.stats = stats.addXP(type, perStat);
            }
        }
    }

    public void addGold(int amount) {
        int afterHpMultiplier = hpState.applyGoldMultiplier(amount);
        double titleMultiplier = titleInventory.getGoldMultiplier();
        int finalAmount = (int) Math.round(afterHpMultiplier * titleMultiplier);
        this.wallet = wallet.add(finalAmount);
    }

    public void spendGold(int amount) {
        if (!wallet.canAfford(amount)) throw new IllegalStateException("No tienes suficiente oro");
        this.wallet = wallet.subtract(amount);
    }

    // ========================================================================================
    // GESTIÓN DE SALUD (HP)
    // ========================================================================================

    public void heal(int amount) {
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

    // ========================================================================================
    // CICLO DIARIO & BURNOUT MANAGEMENT
    // ========================================================================================

    public void updateState(Instant now) {
        // 1. Limpiar debuffs caducados
        debuffTracker.cleanExpiredDebuffs(now);

        // 2. [FIX] Gestionar Burnout (Salida o Extensión Hospitalaria)
        manageBurnoutState(now);

        // 3. Verificar Racha Limpia (GateTracker)
        if (debuffTracker.getActiveDebuffs().isEmpty() && !isBurnoutActive()) {
            gateTracker.incrementDebuffFreeStreak();
        }
    }

    /**
     * Gestiona la lógica de salida o renovación del Burnout.
     * Implementa la regla de Hospitalización Prolongada (Tax 5% diario).
     */
    private void manageBurnoutState(Instant now) {
        // Si no hay burnout activo o no ha expirado el tiempo mínimo, no hacemos nada
        if (activeBurnoutLock == null || !activeBurnoutLock.hasExpired(now)) {
            return;
        }

        // El tiempo ha expirado. Verificamos condición de salud.
        if (currentHP > 0) {
            // ✅ PACIENTE RECUPERADO: Salimos del Burnout
            System.out.println("🔥 Burnout superado. ¡Bienvenido de vuelta!");
            this.activeBurnoutLock = null;
        } else {
            // 🏥 PACIENTE CRÍTICO: Hospitalización Extendida
            // Regla: 5% del oro por cada 24h extra.
            int tax = (int) (wallet.currentGold() * 0.05);
            if (tax > 0) {
                this.wallet = wallet.subtract(tax);
                System.out.println("🏥 Hospitalización extendida (HP 0). Se cobra estancia: -" + tax + " G");
            }

            // Renovamos el lock por otras 24 horas (Ciclo diario de hospital)
            this.activeBurnoutLock = BurnoutLock.trigger(now);
        }
    }

    // ========================================================================================
    // GESTIÓN DE DEBUFFS
    // ========================================================================================

    public void applyDebuff(DebuffType type, String source, Instant now) {
        if (titleInventory.hasImmunityTo(type.name())) return;

        Debuff debuff = Debuff.create(type, source, now);
        debuffTracker.applyDebuff(debuff);
        gateTracker.notifyDebuffReceived();
    }

    public void cureDebuff(DebuffType type) {
        if (debuffTracker.hasDebuff(type)) {
            debuffTracker.removeDebuff(type);
        }
    }

    public DebuffTracker getDebuffTracker() {
        return debuffTracker;
    }

    // ========================================================================================
    // CONSUMO DE ITEMS
    // ========================================================================================

    public void consumeItem(Item item) {
        if (!inventory.hasItem(item.id())) {
            throw new IllegalStateException("No tienes este item en el inventario: " + item.name());
        }

        System.out.println("🥣 Consumiendo: " + item.name());

        if (item.hpRecovery() > 0) heal(item.hpRecovery());
        if (item.hpDamage() > 0) takeDamage(item.hpDamage());

        applyItemSideEffects(item);
        inventory.removeItem(item.id());
    }

    private void applyItemSideEffects(Item item) {
        Instant now = Instant.now();
        if (item.causesDebuff().isPresent()) {
            applyDebuff(item.causesDebuff().get(), "Consumo de " + item.name(), now);
        }
        if (item.curesDebuff().isPresent()) {
            DebuffType debuffToCure = item.curesDebuff().get();
            if (item.isCaffeineSource()) {
                handleCaffeineConsumption(debuffToCure, item.id());
            } else {
                this.cureDebuff(debuffToCure);
            }
        }
    }

    private void handleCaffeineConsumption(DebuffType debuffToCure, String itemId) {
        if (!debuffTracker.canCureWithCaffeine()) {
            System.out.println("💓 Tu corazón va a mil. La cafeína no surte efecto.");
            return;
        }

        this.cureDebuff(debuffToCure);

        if (itemId.equalsIgnoreCase("monster_energy")) {
            debuffTracker.recordMonsterConsumed();
            Optional<Debuff> overdose = debuffTracker.checkItemConsumptionTrigger(itemId, Instant.now());
            overdose.ifPresent(d -> this.applyDebuff(d.getType(), d.getSource(), d.getAppliedAt()));
        }
    }

    // ========================================================================================
    // INVENTARIO & TÍTULOS
    // ========================================================================================

    public void buyItem(Item item) {
        spendGold(item.price());
        inventory.recordPurchase(item);
    }

    public void equipItem(String itemId) { inventory.equip(itemId); }
    public void unequipItem(com.lifeleveling.domain.item.ItemSlot slot) { inventory.unequip(slot); }
    public Inventory getInventory() { return inventory; }

    public boolean unlockTitle(TitleType type) { return titleInventory.unlock(type); }
    public void equipTitle(TitleType type) { titleInventory.equip(type, getLevel()); }
    public boolean unequipTitle(TitleType type) { return titleInventory.unequip(type); }
    public void swapTitle(TitleType old, TitleType n) { titleInventory.swap(old, n, getLevel()); }
    public boolean hasTitle(TitleType type) { return titleInventory.hasTitle(type); }
    public boolean isTitleEquipped(TitleType type) { return titleInventory.isEquipped(type); }
    public TitleInventory getTitleInventory() { return titleInventory; }

    // ========================================================================================
    // GETTERS
    // ========================================================================================

    public int getCurrentHP() { return currentHP; }
    public int getCurrentGold() { return wallet.currentGold(); }
    public Stats getBaseStats() { return stats; }
    public Stats getEffectiveStats() { return stats.applyBonuses(inventory.getTotalStatBonuses()); }
    public HPState getHpState() { return hpState; }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public GateTracker getGateTracker() { return gateTracker; }

    // ========================================================================================
    // CAREER ENGINE
    // ========================================================================================

    public CodeSession registerCodeSession(double hours) {
        CodeSession session = careerEngine.registerSession(hours);
        CareerReward reward = session.getReward();

        addXP(StatType.INTELLECT, reward.intellectXP());
        addXP(StatType.DISCIPLINE, reward.disciplineXP());
        if (session.isFlowAchieved()) {
            addXP(StatType.WISDOM, reward.wisdomXP());
        }

        takeWorkDamage(reward.hpCost());
        gateTracker.setTotalCareerHours(careerEngine.getTotalCareerHours());

        return session;
    }

    public boolean hasCodeActivityToday() { return careerEngine.hasActivityToday(); }
    public CareerEngine getCareerEngine() { return careerEngine; }
}