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
import java.util.UUID;

/**
 * Player: La entidad raíz (Aggregate Root) del dominio.
 */
public class Player {

    private final UUID id;
    private final String name;

    private HPState hpState;
    private int currentHP;
    private Stats stats;
    private Wallet wallet;

    private final Inventory inventory;
    private final TitleInventory titleInventory;
    private final DebuffTracker debuffTracker;
    private final GateTracker gateTracker;
    private final CareerEngine careerEngine;

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
        return new Player(UUID.randomUUID(), name, 100, Stats.initial(), Wallet.empty(),
                new Inventory(), new TitleInventory(), new DebuffTracker(), new GateTracker(), new CareerEngine(), null);
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
    // LÓGICA DE NEGOCIO PRINCIPAL
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

        if (finalAmount > 0) this.stats = stats.addXP(type, finalAmount);
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
        if (this.currentHP == 0 && activeBurnoutLock == null) triggerBurnout();
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
    // PERFECT DAY MECHANIC (Fase 11)
    // ========================================================================================

    /**
     * Intenta reclamar el premio por Perfect Day (7/7 Dailies).
     * Solo funciona una vez al día.
     */
    public void triggerPerfectDay() {
        // 1. Verificar CERROJO: Si ya lo logramos hoy, no damos premio doble.
        if (gateTracker.isPerfectDayAchievedToday()) {
            return;
        }

        System.out.println("🌟 ¡PERFECT DAY! +100 XP, +100 G, HP MAX 🌟");

        // 2. Aplicar Recompensas Biblia (Cap 2.1 §4)
        this.heal(100); // Recuperación completa
        this.addGold(100);
        this.addGeneralXP(100);

        // 3. Limpieza Mental (Cura Aburrimiento si existe)
        debuffTracker.applyPerfectDayCure();

        // 4. CERRAR EL CERROJO
        gateTracker.setPerfectDayAchievedToday(true);
    }

    // ========================================================================================
    // CICLO DIARIO
    // ========================================================================================

    public void updateState(Instant now) {
        // 1. Resetear flags diarias
        gateTracker.resetDailyFlags(); // [Fase 11] Permitir Perfect Day mañana

        // 2. Limpiar debuffs caducados
        debuffTracker.cleanExpiredDebuffs(now);

        // 3. Gestionar Burnout
        manageBurnoutState(now);

        // 4. Verificar Racha Limpia
        if (debuffTracker.getActiveDebuffs().isEmpty() && !isBurnoutActive()) {
            gateTracker.incrementDebuffFreeStreak();
        }
    }

    private void manageBurnoutState(Instant now) {
        if (activeBurnoutLock == null || !activeBurnoutLock.hasExpired(now)) return;

        if (currentHP > 0) {
            System.out.println("🔥 Burnout superado. ¡Bienvenido de vuelta!");
            this.activeBurnoutLock = null;
        } else {
            int tax = (int) (wallet.currentGold() * 0.05);
            if (tax > 0) {
                this.wallet = wallet.subtract(tax);
                System.out.println("🏥 Hospitalización extendida. -" + tax + " G");
            }
            this.activeBurnoutLock = BurnoutLock.trigger(now);
        }
    }

    // ========================================================================================
    // DEBUFFS
    // ========================================================================================

    public void applyDebuff(DebuffType type, String source, Instant now) {
        if (titleInventory.hasImmunityTo(type.name())) return;
        Debuff debuff = Debuff.create(type, source, now);
        debuffTracker.applyDebuff(debuff);
        gateTracker.notifyDebuffReceived();
    }

    public void cureDebuff(DebuffType type) {
        if (debuffTracker.hasDebuff(type)) debuffTracker.removeDebuff(type);
    }

    public DebuffTracker getDebuffTracker() { return debuffTracker; }

    // ========================================================================================
    // ITEMS & CAREER (Resto sin cambios estructurales, solo getters/setters)
    // ========================================================================================

    public void consumeItem(Item item) {
        if (!inventory.hasItem(item.id())) throw new IllegalStateException("No tienes este item: " + item.name());
        System.out.println("🥣 Consumiendo: " + item.name());
        if (item.hpRecovery() > 0) heal(item.hpRecovery());
        if (item.hpDamage() > 0) takeDamage(item.hpDamage());
        applyItemSideEffects(item);
        inventory.removeItem(item.id());
    }

    private void applyItemSideEffects(Item item) {
        Instant now = Instant.now();
        if (item.causesDebuff().isPresent()) applyDebuff(item.causesDebuff().get(), "Consumo " + item.name(), now);
        if (item.curesDebuff().isPresent()) {
            DebuffType debuffToCure = item.curesDebuff().get();
            if (item.isCaffeineSource()) handleCaffeineConsumption(debuffToCure, item.id());
            else cureDebuff(debuffToCure);
        }
    }

    private void handleCaffeineConsumption(DebuffType debuffToCure, String itemId) {
        if (!debuffTracker.canCureWithCaffeine()) {
            System.out.println("💓 Tu corazón va a mil. Cafeína inefectiva.");
            return;
        }
        cureDebuff(debuffToCure);
        if (itemId.equalsIgnoreCase("monster_energy")) {
            debuffTracker.recordMonsterConsumed();
            debuffTracker.checkItemConsumptionTrigger(itemId, Instant.now()).ifPresent(d -> applyDebuff(d.getType(), d.getSource(), d.getAppliedAt()));
        }
    }

    public void buyItem(Item item) { spendGold(item.price()); inventory.recordPurchase(item); }
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

    public CodeSession registerCodeSession(double hours) {
        CodeSession session = careerEngine.registerSession(hours);
        CareerReward reward = session.getReward();
        addXP(StatType.INTELLECT, reward.intellectXP());
        addXP(StatType.DISCIPLINE, reward.disciplineXP());
        if (session.isFlowAchieved()) addXP(StatType.WISDOM, reward.wisdomXP());
        takeWorkDamage(reward.hpCost());
        gateTracker.setTotalCareerHours(careerEngine.getTotalCareerHours());
        return session;
    }

    public boolean hasCodeActivityToday() { return careerEngine.hasActivityToday(); }
    public CareerEngine getCareerEngine() { return careerEngine; }

    public int getCurrentHP() { return currentHP; }
    public int getCurrentGold() { return wallet.currentGold(); }
    public Stats getBaseStats() { return stats; }
    public Stats getEffectiveStats() { return stats.applyBonuses(inventory.getTotalStatBonuses()); }
    public HPState getHpState() { return hpState; }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public GateTracker getGateTracker() { return gateTracker; }
}