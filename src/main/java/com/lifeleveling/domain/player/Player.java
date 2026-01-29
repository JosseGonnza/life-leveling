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
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.weekly.WeeklyManager;
import com.lifeleveling.domain.title.TitleInventory;
import com.lifeleveling.domain.title.TitleType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
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

    // [FASE 4] Tracker de Hitos
    private final MilestoneTracker milestoneTracker;

    // [FASE 5] Gestor de Misiones Semanales
    private final WeeklyManager weeklyManager;

    private BurnoutLock activeBurnoutLock;

    private Player(UUID id, String name, int currentHP, PlayerRank currentRank, Stats stats, Wallet wallet,
                   Inventory inventory, TitleInventory titleInventory,
                   DebuffTracker debuffTracker, GateTracker gateTracker,
                   CareerEngine careerEngine,
                   MilestoneTracker milestoneTracker, // [FASE 4]
                   WeeklyManager weeklyManager,       // [FASE 5]
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
        this.milestoneTracker = milestoneTracker;
        this.weeklyManager = weeklyManager;
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
                new Inventory(),
                new TitleInventory(),
                new DebuffTracker(),
                new GateTracker(),
                new CareerEngine(),
                new MilestoneTracker(), // [FASE 4]
                new WeeklyManager(),    // [FASE 5] Inicializamos el gestor semanal
                null
        );
    }

    public static Player restore(UUID id, String name, int currentHP, PlayerRank rank, Stats stats, Wallet wallet,
                                 Inventory inventory, TitleInventory titleInventory,
                                 DebuffTracker debuffTracker, GateTracker gateTracker,
                                 CareerEngine careerEngine,
                                 MilestoneTracker milestoneTracker, // [FASE 4]
                                 WeeklyManager weeklyManager,       // [FASE 5]
                                 BurnoutLock lock) {
        return new Player(id, name, currentHP, rank, stats, wallet, inventory, titleInventory,
                debuffTracker, gateTracker, careerEngine, milestoneTracker, weeklyManager, lock);
    }

    // ========================================================================================
    // CICLO DIARIO & SEMANAL [FASES 2 y 5]
    // ========================================================================================

    public void updateState(Instant now) {
        // 1. Limpieza estándar diaria
        gateTracker.resetDailyFlags();
        debuffTracker.cleanExpiredDebuffs(now);
        manageBurnoutState(now);

        // 2. [FASE 5] Rotación Semanal (Si es Lunes, WeeklyManager se encarga)
        if (weeklyManager != null) {
            LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());
            weeklyManager.performWeeklyReset(today);
        }

        // 3. Comprobación de Triggers automáticos (Castigos)
        int daysNoTidy = gateTracker.getDaysSinceLastQuestCompletion("TIDY");
        int workStreak = gateTracker.getConsecutiveWorkDays();

        List<Debuff> newDebuffs = debuffTracker.checkDailyResetTriggers(daysNoTidy, workStreak, now);

        for (Debuff db : newDebuffs) {
            applyDebuffDirect(db);
            System.out.println("⚠️ CASTIGO AUTOMÁTICO: " + db.getType().getDisplayName());
        }

        // 4. Racha de pureza
        if (debuffTracker.getActiveDebuffs().isEmpty() && !isBurnoutActive()) {
            gateTracker.incrementDebuffFreeStreak();
        } else {
            gateTracker.notifyDebuffReceived();
        }
    }

    // ========================================================================================
    // INTEGRACIÓN DE EVENTOS (HOOKS) [FASE 6]
    // ========================================================================================

    /**
     * Notifica que una actividad ha sido completada.
     * Sirve de HUB para activar Debuffs, Curas y Misiones Semanales.
     */
    public void notifyQuestCompleted(String questId, double inputValue) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());

        // 1. Triggers de Debuffs (Fatiga inmediata)
        if ("SLEEP".equals(questId)) {
            debuffTracker.checkSleepTrigger(inputValue, now).ifPresent(this::applyDebuffDirect);
        }

        // 2. Curas de Debuffs (Orden cura Caos)
        if ("TIDY".equals(questId) && debuffTracker.hasDebuff(DebuffType.CHAOS)) {
            debuffTracker.removeDebuff(DebuffType.CHAOS);
            System.out.println("✨ El orden ha restaurado tu mente. Adiós Caos.");
        }

        // 3. Notificar al WeeklyManager para actualizar progreso
        if (weeklyManager != null) {
            // recordActivity devuelve las recompensas de quests completadas
            List<QuestReward> weeklyRewards = weeklyManager.recordActivity(questId, inputValue, today);

            // Aplicar cada recompensa ganada
            for (QuestReward reward : weeklyRewards) {
                applyQuestReward(reward);
            }

            // Actualizar progreso de quests DUAL (Templo Puro)
            weeklyManager.refreshDualQuestProgress(today);
        }
    }

    /**
     * Aplica una recompensa de quest al jugador.
     */
    private void applyQuestReward(QuestReward reward) {
        // Aplicar XP de stats
        reward.statXP().forEach((stat, xp) -> {
            if (xp > 0) {
                addXP(stat, xp);
            }
        });

        // Aplicar XP general
        if (reward.generalXP() > 0) {
            addGeneralXP(reward.generalXP());
        }

        // Aplicar oro
        if (reward.gold() > 0) {
            addGold(reward.gold());
        }
    }

    private void applyDebuffDirect(Debuff d) {
        debuffTracker.applyDebuff(d);
        gateTracker.notifyDebuffReceived();
    }

    // ========================================================================================
    // LEVELING & MILESTONES [FASE 4]
    // ========================================================================================

    public int getLevel() {
        long totalXP = stats.getTotalAccumulatedXP();
        if (totalXP == 0) return 1;
        int level = (int) Math.sqrt(totalXP / 45.0);
        return Math.max(1, Math.min(level, 100));
    }

    public void addXP(StatType type, int amount) {
        if (amount <= 0) return;

        int oldLevel = getLevel();

        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double debuffStatMult = debuffTracker.getStatXPMultiplier(type);
        double titleMultiplier = titleInventory.getStatXPMultiplier(type);

        double totalMultiplier = hpMultiplier * debuffGlobalMult * debuffStatMult * titleMultiplier;
        int finalAmount = (int) Math.round(amount * totalMultiplier);

        if (finalAmount > 0) {
            this.stats = stats.addXP(type, finalAmount);
            checkLevelUp(oldLevel);
        }
    }

    public void addGeneralXP(int amount) {
        if (amount <= 0) return;

        int oldLevel = getLevel();

        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double titleMultiplier = titleInventory.getGeneralXPMultiplier();
        int finalAmount = (int) Math.round(amount * hpMultiplier * debuffGlobalMult * titleMultiplier);

        if (finalAmount > 0) {
            int perStat = finalAmount / StatType.values().length;
            for (StatType type : StatType.values()) {
                this.stats = stats.addXP(type, perStat);
            }
            checkLevelUp(oldLevel);
        }
    }

    private void checkLevelUp(int oldLevel) {
        int newLevel = getLevel();
        if (newLevel > oldLevel) {
            System.out.println("🆙 ¡LEVEL UP! " + oldLevel + " -> " + newLevel);
            // [FASE 4] Milestones
            List<MilestoneType> awards = milestoneTracker.checkAndAward(this);
            if (!awards.isEmpty()) {
                System.out.println("✨ Recompensas otorgadas: " + awards.size() + " hitos conseguidos.");
            }
        }
    }

    // ========================================================================================
    // ECONOMÍA & RANGO
    // ========================================================================================

    public PlayerRank getCurrentRank() { return currentRank; }

    public void promoteToRank(PlayerRank newRank) {
        if (newRank == null) return;
        if (newRank.ordinal() > this.currentRank.ordinal()) {
            this.currentRank = newRank;
            System.out.println("🎉 ¡ASCENSO! Nuevo Rango: " + newRank.getDisplayName() +
                    " (Ingresos x" + newRank.getGoldMultiplier() + ")");
        }
    }

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
    // SALUD & BURNOUT
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

    public void takeWorkDamage(int baseDamage, double hours) {
        int mitigationRate = inventory.getHourlyWorkDamageMitigation();
        int totalMitigation = (int) Math.round(hours * mitigationRate);
        int finalDamage = Math.max(0, baseDamage - totalMitigation);
        takeDamage(finalDamage);
    }

    public void takeGymDamage() {
        int baseDamage = 5;
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
        gateTracker.recordBurnoutToday();
        System.out.println("💔 ¡BURNOUT! HP a 0. Bloqueo de 24h y multa aplicada.");
    }

    public boolean isBurnoutActive() {
        return activeBurnoutLock != null && !activeBurnoutLock.hasExpired(Instant.now());
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

    public void triggerPerfectDay() {
        if (gateTracker.isPerfectDayAchievedToday()) return;
        System.out.println("🌟 ¡PERFECT DAY! +100 XP, +100 G, HP MAX 🌟");
        this.heal(100);
        this.addGold(100);
        this.addGeneralXP(100);
        debuffTracker.applyPerfectDayCure();
        gateTracker.setPerfectDayAchievedToday(true);
        gateTracker.incrementPerfectDayStreak();

        // Notificar al WeeklyManager para THE_STREAK quest
        if (weeklyManager != null) {
            Optional<QuestReward> streakReward = weeklyManager.recordPerfectDay();
            streakReward.ifPresent(this::applyQuestReward);
        }
    }

    /**
     * Llamar cuando NO se logra Perfect Day (rompe la racha).
     * Debe llamarse al final del día si no se completaron las 7 daily quests.
     */
    public void breakPerfectDayStreak() {
        gateTracker.resetPerfectDayStreak();
    }

    // ========================================================================================
    // OTROS (ITEMS, DEBUFFS, CAREER, TITLES)
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

    public void consumeItem(Item item) {
        if (!inventory.hasItem(item.id())) throw new IllegalStateException("No tienes este item: " + item.name());
        System.out.println("🥣 Consumiendo: " + item.name());
        if (item.hpRecovery() > 0) heal(item.hpRecovery());
        if (item.hpDamage() > 0) takeDamage(item.hpDamage());
        debuffTracker.checkItemConsumptionTrigger(item.id(), Instant.now()).ifPresent(this::applyDebuffDirect);
        applyItemSideEffects(item);
        inventory.removeItem(item.id());
    }

    private void applyItemSideEffects(Item item) {
        Instant now = Instant.now();
        if (item.causesDebuff().isPresent()) applyDebuff(item.causesDebuff().get(), "Consumo " + item.name(), now);
        if (item.curesDebuff().isPresent()) {
            DebuffType debuffToCure = item.curesDebuff().get();
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

    public boolean unlockTitle(TitleType type) { return titleInventory.unlock(type); }
    public void equipTitle(TitleType type) { titleInventory.equip(type, getLevel()); }
    public boolean unequipTitle(TitleType type) { return titleInventory.unequip(type); }
    public void swapTitle(TitleType old, TitleType n) { titleInventory.swap(old, n, getLevel()); }
    public boolean hasTitle(TitleType type) { return titleInventory.hasTitle(type); }
    public boolean isTitleEquipped(TitleType type) { return titleInventory.isEquipped(type); }

    public CodeSession registerCodeSession(double hours) {
        CodeSession session = careerEngine.registerSession(hours);
        CareerReward reward = session.getReward();
        addXP(StatType.INTELLECT, reward.intellectXP());
        addXP(StatType.DISCIPLINE, reward.disciplineXP());
        if (session.isFlowAchieved()) addXP(StatType.WISDOM, reward.wisdomXP());
        takeWorkDamage(reward.hpCost(), hours);
        gateTracker.setTotalCareerHours(careerEngine.getTotalCareerHours());

        // [FASE 6 HOOK] Notificamos también las horas de código (clave para "Code Marathon")
        notifyQuestCompleted("CODE", hours);

        return session;
    }

    public boolean hasCodeActivityToday() { return careerEngine.hasActivityToday(); }
    public CareerEngine getCareerEngine() { return careerEngine; }
    public Inventory getInventory() { return inventory; }
    public TitleInventory getTitleInventory() { return titleInventory; }
    public DebuffTracker getDebuffTracker() { return debuffTracker; }
    public GateTracker getGateTracker() { return gateTracker; }
    public MilestoneTracker getMilestoneTracker() { return milestoneTracker; }
    public WeeklyManager getWeeklyManager() { return weeklyManager; }

    public int getCurrentHP() { return currentHP; }
    public int getCurrentGold() { return wallet.currentGold(); }
    public Stats getBaseStats() { return stats; }
    public Stats getEffectiveStats() { return stats.applyBonuses(inventory.getTotalStatBonuses()); }
    public HPState getHpState() { return hpState; }
    public UUID getId() { return id; }
    public String getName() { return name; }
}