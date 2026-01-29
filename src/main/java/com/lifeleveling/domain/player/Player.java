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
import java.util.List;
import java.util.UUID;

/**
 * Player: La entidad raíz (Aggregate Root) del dominio.
 */
public class Player {

    private final UUID id;
    private final String name;

    private HPState hpState;
    private int currentHP;

    // Rango Profesional (Multiplicador de Ingresos)
    private PlayerRank currentRank;

    private Stats stats;
    private Wallet wallet;

    private final Inventory inventory;
    private final TitleInventory titleInventory;
    private final DebuffTracker debuffTracker;
    private final GateTracker gateTracker;
    private final CareerEngine careerEngine;

    private BurnoutLock activeBurnoutLock;

    private Player(UUID id, String name, int currentHP, PlayerRank currentRank, Stats stats, Wallet wallet,
                   Inventory inventory, TitleInventory titleInventory,
                   DebuffTracker debuffTracker, GateTracker gateTracker,
                   CareerEngine careerEngine,
                   BurnoutLock activeBurnoutLock) {
        this.id = id;
        this.name = name;
        this.currentHP = currentHP;
        this.hpState = HPState.fromHP(currentHP);
        this.currentRank = currentRank;
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
                PlayerRank.E,
                Stats.initial(),
                Wallet.empty(),
                new Inventory(), new TitleInventory(), new DebuffTracker(), new GateTracker(), new CareerEngine(), null
        );
    }

    public static Player restore(UUID id, String name, int currentHP, PlayerRank rank, Stats stats, Wallet wallet,
                                 Inventory inventory, TitleInventory titleInventory,
                                 DebuffTracker debuffTracker, GateTracker gateTracker,
                                 CareerEngine careerEngine,
                                 BurnoutLock lock) {
        return new Player(id, name, currentHP, rank, stats, wallet, inventory, titleInventory,
                debuffTracker, gateTracker, careerEngine, lock);
    }

    // ========================================================================================
    // CICLO DIARIO & TRIGGERS AUTOMÁTICOS [FASE 2.1]
    // ========================================================================================

    public void updateState(Instant now) {
        // 1. Limpieza estándar
        gateTracker.resetDailyFlags();
        debuffTracker.cleanExpiredDebuffs(now);
        manageBurnoutState(now);

        // 2. [NUEVO] Comprobación de Triggers automáticos (Castigos por dejadez)
        // Consultamos al histórico para ver si hemos fallado en nuestros deberes
        int daysNoTidy = gateTracker.getDaysSinceLastQuestCompletion("TIDY");
        int workStreak = gateTracker.getConsecutiveWorkDays();

        // El DebuffTracker actúa como juez
        List<Debuff> newDebuffs = debuffTracker.checkDailyResetTriggers(daysNoTidy, workStreak, now);

        for (Debuff db : newDebuffs) {
            applyDebuffDirect(db); // Aplicamos y notificamos
            System.out.println("⚠️ CASTIGO AUTOMÁTICO: " + db.getType().getDisplayName());
        }

        // 3. Racha de pureza (Solo aumenta si no tienes debuffs activos ni burnout)
        if (debuffTracker.getActiveDebuffs().isEmpty() && !isBurnoutActive()) {
            gateTracker.incrementDebuffFreeStreak();
        } else {
            gateTracker.notifyDebuffReceived(); // Reset streak
        }
    }

    // ========================================================================================
    // INTEGRACIÓN CON QUESTS (EVENTOS)
    // ========================================================================================

    /**
     * [NUEVO] Método auxiliar para chequear triggers inmediatos tras completar una Quest.
     * Debe ser llamado por el servicio/UI cuando una DailyQuest se completa.
     * Ej: Completar SLEEP con < 6 horas -> Fatiga inmediata.
     */
    public void notifyQuestCompleted(String questId, double inputValue) {
        Instant now = Instant.now();

        // Trigger: FATIGUE (Si duermes poco)
        if ("SLEEP".equals(questId)) {
            debuffTracker.checkSleepTrigger(inputValue, now)
                    .ifPresent(this::applyDebuffDirect);
        }

        // Cura: CHAOS (Si completas TIDY, el caos desaparece)
        if ("TIDY".equals(questId) && debuffTracker.hasDebuff(DebuffType.CHAOS)) {
            debuffTracker.removeDebuff(DebuffType.CHAOS);
            System.out.println("✨ El orden ha restaurado tu mente. Adiós Caos.");
        }
    }

    // Helper privado para aplicar debuff y notificar al tracker de rachas
    private void applyDebuffDirect(Debuff d) {
        debuffTracker.applyDebuff(d);
        gateTracker.notifyDebuffReceived();
    }

    // ========================================================================================
    // GESTIÓN DE RANGO
    // ========================================================================================

    public PlayerRank getCurrentRank() {
        return currentRank;
    }

    public void promoteToRank(PlayerRank newRank) {
        if (newRank == null) return;
        if (newRank.ordinal() > this.currentRank.ordinal()) {
            this.currentRank = newRank;
            System.out.println("🎉 ¡ASCENSO! Nuevo Rango: " + newRank.getDisplayName() +
                    " (Ingresos x" + newRank.getGoldMultiplier() + ")");
        }
    }

    // ========================================================================================
    // ECONOMÍA
    // ========================================================================================

    public void addGold(int amount) {
        double rankMultiplier = currentRank.getGoldMultiplier();
        double baseWithRank = amount * rankMultiplier;
        double hpMultiplier = hpState.getGoldMultiplier();
        double titleMultiplier = titleInventory.getGoldMultiplier();

        int finalAmount = (int) Math.round(baseWithRank * hpMultiplier * titleMultiplier);
        if (finalAmount > 0) {
            this.wallet = wallet.add(finalAmount);
        }
    }

    public void spendGold(int amount) {
        if (!wallet.canAfford(amount)) throw new IllegalStateException("No tienes suficiente oro");
        this.wallet = wallet.subtract(amount);
    }

    // ========================================================================================
    // LEVELING
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

    // ========================================================================================
    // GESTIÓN DE SALUD (HP) Y DAÑO
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

    /**
     * Aplica el daño por trabajo considerando la mitigación por hora del equipo.
     * @param baseDamage El daño total calculado por CareerEngine (Horas * 3).
     * @param hours Las horas trabajadas (necesarias para aplicar la tasa de mitigación).
     */
    public void takeWorkDamage(int baseDamage, double hours) {
        // 1. Obtener tasa de protección (ej: 1 HP/hora con ratón)
        int mitigationRate = inventory.getHourlyWorkDamageMitigation();

        // 2. Calcular protección total (ej: 4 horas * 1 = 4 HP ahorrados)
        int totalMitigation = (int) Math.round(hours * mitigationRate);

        // 3. Aplicar daño neto (Mínimo 0, el trabajo nunca cura)
        int finalDamage = Math.max(0, baseDamage - totalMitigation);

        takeDamage(finalDamage);
    }

    /**
     * Aplica el daño por sesión de Gimnasio.
     * Base: 5 HP (Tarifa plana).
     * Mitigado por Zapatillas Pegasus.
     */
    public void takeGymDamage() {
        int baseDamage = 5; // Constante Biblia
        int mitigation = inventory.getGymDamageMitigation();

        int finalDamage = Math.max(0, baseDamage - mitigation);

        if (mitigation > 0 && finalDamage == 0) {
            System.out.println("👟 ¡Zapatillas Pegasus amortiguan todo el impacto! (-0 HP)");
        }

        takeDamage(finalDamage);
    }

    private void triggerBurnout() {
        this.activeBurnoutLock = BurnoutLock.createNow();
        this.wallet = wallet.applyBurnoutTax();
        gateTracker.recordBurnoutToday(); // Memoria a corto plazo
        System.out.println("💔 ¡BURNOUT! HP a 0. Bloqueo de 24h y multa aplicada.");
    }

    public boolean isBurnoutActive() {
        return activeBurnoutLock != null && !activeBurnoutLock.hasExpired(Instant.now());
    }

    // ========================================================================================
    // PERFECT DAY & BURNOUT STATE
    // ========================================================================================

    public void triggerPerfectDay() {
        if (gateTracker.isPerfectDayAchievedToday()) return;

        System.out.println("🌟 ¡PERFECT DAY! +100 XP, +100 G, HP MAX 🌟");
        this.heal(100);
        this.addGold(100);
        this.addGeneralXP(100);
        debuffTracker.applyPerfectDayCure();
        gateTracker.setPerfectDayAchievedToday(true);
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
    // DEBUFFS & ITEMS & CAREER
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

    public void consumeItem(Item item) {
        if (!inventory.hasItem(item.id())) throw new IllegalStateException("No tienes este item: " + item.name());
        System.out.println("🥣 Consumiendo: " + item.name());

        if (item.hpRecovery() > 0) heal(item.hpRecovery());
        if (item.hpDamage() > 0) takeDamage(item.hpDamage());

        // [NUEVO] Delegamos lógica compleja de triggers al tracker (ej: TACHYCARDIA por 3er Monster)
        debuffTracker.checkItemConsumptionTrigger(item.id(), Instant.now())
                .ifPresent(this::applyDebuffDirect);

        // Lógica estándar del item (campos estáticos causesDebuff/curesDebuff)
        applyItemSideEffects(item);

        inventory.removeItem(item.id());
    }

    private void applyItemSideEffects(Item item) {
        Instant now = Instant.now();
        // Aplica debuff si el item lo tiene configurado estáticamente (ej: Hamburguesa -> Pesadez)
        if (item.causesDebuff().isPresent()) {
            applyDebuff(item.causesDebuff().get(), "Consumo " + item.name(), now);
        }
        // Cura debuff si el item lo tiene configurado (ej: Almax -> Pesadez)
        if (item.curesDebuff().isPresent()) {
            DebuffType debuffToCure = item.curesDebuff().get();
            // Verificación extra para cafeína vs taquicardia
            if (item.isCaffeineSource() && !debuffTracker.canCureWithCaffeine()) {
                System.out.println("💓 Tu corazón va a mil. Cafeína inefectiva.");
            } else {
                cureDebuff(debuffToCure);
            }
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

        // [FIX] Pasamos el coste base Y las horas para calcular la mitigación real
        takeWorkDamage(reward.hpCost(), hours);

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