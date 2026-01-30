package com.lifeleveling.domain.player;

import com.lifeleveling.domain.career.CareerEngine;
import com.lifeleveling.domain.career.CareerReward;
import com.lifeleveling.domain.career.CodeSession;
import com.lifeleveling.domain.debuff.Debuff;
import com.lifeleveling.domain.debuff.DebuffTracker;
import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemCategory;
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

    // [FASE 2.1] Tracker de Buffs Temporales
    private final TemporaryBuffTracker tempBuffTracker;

    private final GateTracker gateTracker;
    private final CareerEngine careerEngine;

    // [FASE 4] Tracker de Hitos
    private final MilestoneTracker milestoneTracker;

    // [FASE 5] Gestor de Misiones Semanales
    private final WeeklyManager weeklyManager;

    private BurnoutLock activeBurnoutLock;

    private Player(UUID id, String name, int currentHP, PlayerRank currentRank, Stats stats, Wallet wallet,
                   Inventory inventory, TitleInventory titleInventory,
                   DebuffTracker debuffTracker,
                   TemporaryBuffTracker tempBuffTracker,
                   GateTracker gateTracker,
                   CareerEngine careerEngine,
                   MilestoneTracker milestoneTracker,
                   WeeklyManager weeklyManager,
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
        this.tempBuffTracker = tempBuffTracker;
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
                new TemporaryBuffTracker(),
                new GateTracker(),
                new CareerEngine(),
                new MilestoneTracker(),
                new WeeklyManager(),
                null
        );
    }

    public static Player restore(UUID id, String name, int currentHP, PlayerRank rank, Stats stats, Wallet wallet,
                                 Inventory inventory, TitleInventory titleInventory,
                                 DebuffTracker debuffTracker,
                                 TemporaryBuffTracker tempBuffTracker,
                                 GateTracker gateTracker,
                                 CareerEngine careerEngine,
                                 MilestoneTracker milestoneTracker,
                                 WeeklyManager weeklyManager,
                                 BurnoutLock lock) {
        return new Player(id, name, currentHP, rank, stats, wallet, inventory, titleInventory,
                debuffTracker, tempBuffTracker, gateTracker, careerEngine, milestoneTracker, weeklyManager, lock);
    }

    // ========================================================================================
    // CICLO DIARIO & SEMANAL
    // ========================================================================================

    public void updateState(Instant now) {
        // 1. Limpieza estándar diaria
        gateTracker.resetDailyFlags();
        debuffTracker.cleanExpiredDebuffs(now);
        tempBuffTracker.cleanExpiredBuffs(now);
        manageBurnoutState(now);

        // 2. Rotación Semanal
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
    // CONSUMO DE ITEMS
    // ========================================================================================

    public void consumeItem(Item item) {
        if (!inventory.hasItem(item.id())) throw new IllegalStateException("No tienes este item: " + item.name());
        System.out.println("🥣 Consumiendo: " + item.name());
        Instant now = Instant.now();

        // [FASE 5.2] Integración con Monster / Taquicardia
        // Si el item es una fuente de cafeína potente (Monster), registramos el consumo en el tracker.
        // El tracker se encarga de la lógica de "3 por semana" y aplica el debuff si corresponde.
        if (item.isCaffeineSource()) {
            // Convertimos Instant a LocalDate para la lógica semanal
            LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());
            debuffTracker.recordMonsterConsumption(today);
        }

        if (item.hpRecovery() > 0) heal(item.hpRecovery());
        if (item.hpDamage() > 0) takeDamage(item.hpDamage());

        // Esta línea antigua ya no es necesaria para el Monster porque lo gestionamos arriba,
        // pero la dejamos por si hay otros items con triggers específicos.
        debuffTracker.checkItemConsumptionTrigger(item.id(), now).ifPresent(this::applyDebuffDirect);

        applyItemSideEffects(item);

        // Aplicar Buff Temporal si el item lo tiene
        item.temporaryBuff().ifPresent(spec -> {
            tempBuffTracker.addBuff(spec, item.name(), now);
            System.out.println("⚡ Buff Activado: " + item.name());
        });

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

    // ========================================================================================
    // COMPRA DE ITEMS (ECONOMÍA)
    // ========================================================================================

    public void buyItem(Item item) {
        int finalPrice = item.price();

        // [FASE 3.3] Aplicar descuento de Cafetera Espresso para bebidas sociales
        if (item.category() == ItemCategory.DRINK_SOCIAL) {
            // El inventario sabe si tenemos la cafetera equipada
            double multiplier = inventory.getSocialDrinkDiscountMultiplier();
            if (multiplier < 1.0) {
                finalPrice = (int) Math.round(finalPrice * multiplier);
                System.out.println("☕ ¡Cafetera Espresso rentabilizada! Precio reducido a " + finalPrice + " G");
            }
        }

        spendGold(finalPrice);
        inventory.recordPurchase(item);
    }

    // ========================================================================================
    // LEVELING
    // ========================================================================================

    public void addXP(StatType type, int amount) {
        if (amount <= 0) return;

        int oldLevel = getLevel();

        double hpMultiplier = (hpState == HPState.TIRED) ? 0.5 : 1.0;
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double debuffStatMult = debuffTracker.getStatXPMultiplier(type);
        double titleMultiplier = titleInventory.getStatXPMultiplier(type);
        double tempBuffMultiplier = tempBuffTracker.getStatXPMultiplier(type, Instant.now());

        double totalMultiplier = hpMultiplier * debuffGlobalMult * debuffStatMult * titleMultiplier * tempBuffMultiplier;
        int finalAmount = (int) Math.round(amount * totalMultiplier);

        if (finalAmount > 0) {
            this.stats = stats.addXP(type, finalAmount);
            checkLevelUp(oldLevel);
        }
    }

    // ========================================================================================
    // OTROS (Career, Sleep, etc.)
    // ========================================================================================

    public CodeSession registerCodeSession(double hours) {
        // Verificar si tiene el Agente IA ("soft_ai_agent")
        boolean hasAiAgent = inventory.hasItem("soft_ai_agent");

        // Pasamos el flag al motor
        CodeSession session = careerEngine.registerSession(hours, hasAiAgent);

        CareerReward reward = session.getReward();

        // Aplicamos la XP (que ya incluye el bonus si corresponde)
        addXP(StatType.INTELLECT, reward.intellectXP());
        addXP(StatType.DISCIPLINE, reward.disciplineXP());
        if (session.isFlowAchieved()) addXP(StatType.WISDOM, reward.wisdomXP());

        takeWorkDamage(reward.hpCost(), hours);
        gateTracker.setTotalCareerHours(careerEngine.getTotalCareerHours());

        notifyQuestCompleted("CODE", hours);

        return session;
    }

    /**
     * Registra una sesión de sueño y aplica las recompensas.
     * Aplica automáticamente el bonus del Colchón Premium si está equipado.
     */
    public void registerSleepSession(int hours) {
        if (hours <= 0) return;

        // 1. Calcular XP Base (Regla: 50 XP/hora hasta 8h)
        int baseXP = Math.min(hours, 8) * 50;

        // 2. Aplicar Multiplicador de Item (Colchón)
        double itemMultiplier = inventory.getSleepXPMultiplier();
        int finalXP = (int) (baseXP * itemMultiplier);

        // 3. Aplicar al Stat correspondiente (CHARISMA = Beauty Sleep)
        addXP(StatType.CHARISMA, finalXP);

        // 4. Recuperación de HP (Lógica estándar + Bonus Títulos)
        int hpRecovery = hours * 5; // 5 HP/h base
        heal(hpRecovery);

        // 5. Notificar eventos (Weekly Quests, Debuffs, Rachas)
        notifyQuestCompleted("SLEEP", hours);

        if (itemMultiplier > 1.0) {
            System.out.println("🛏️ ¡Descanso Premium! XP x" + itemMultiplier);
        }
    }

    // ========================================================================================
    // RESTO DE MÉTODOS (Getters, Helpers, etc.)
    // ========================================================================================

    public TemporaryBuffTracker getTempBuffTracker() { return tempBuffTracker; }

    public int getLevel() {
        long totalXP = stats.getTotalAccumulatedXP();
        if (totalXP == 0) return 1;
        int level = (int) Math.sqrt(totalXP / 45.0);
        return Math.max(1, Math.min(level, 100));
    }

    private void checkLevelUp(int oldLevel) {
        int newLevel = getLevel();
        if (newLevel > oldLevel) {
            System.out.println("🆙 ¡LEVEL UP! " + oldLevel + " -> " + newLevel);
            List<MilestoneType> awards = milestoneTracker.checkAndAward(this);
            if (!awards.isEmpty()) {
                System.out.println("✨ Recompensas otorgadas: " + awards.size() + " hitos conseguidos.");
            }
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

    public void notifyQuestCompleted(String questId, double inputValue) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());
        if ("SLEEP".equals(questId)) {
            debuffTracker.checkSleepTrigger(inputValue, now).ifPresent(this::applyDebuffDirect);
        }
        if ("TIDY".equals(questId) && debuffTracker.hasDebuff(DebuffType.CHAOS)) {
            debuffTracker.removeDebuff(DebuffType.CHAOS);
            System.out.println("✨ El orden ha restaurado tu mente. Adiós Caos.");
        }
        if (weeklyManager != null) {
            List<QuestReward> weeklyRewards = weeklyManager.recordActivity(questId, inputValue, today);
            for (QuestReward reward : weeklyRewards) applyQuestReward(reward);
            weeklyManager.refreshDualQuestProgress(today);
        }
    }

    private void applyQuestReward(QuestReward reward) {
        reward.statXP().forEach((stat, xp) -> { if (xp > 0) addXP(stat, xp); });
        if (reward.generalXP() > 0) addGeneralXP(reward.generalXP());
        if (reward.gold() > 0) addGold(reward.gold());
    }

    private void applyDebuffDirect(Debuff d) {
        debuffTracker.applyDebuff(d);
        gateTracker.notifyDebuffReceived();
    }

    public PlayerRank getCurrentRank() { return currentRank; }
    public void promoteToRank(PlayerRank newRank) {
        if (newRank == null) return;
        if (newRank.ordinal() > this.currentRank.ordinal()) {
            this.currentRank = newRank;
            System.out.println("🎉 ¡ASCENSO! Nuevo Rango: " + newRank.getDisplayName() + " (Ingresos x" + newRank.getGoldMultiplier() + ")");
        }
    }
    public void addGold(int amount) {
        double rankMultiplier = currentRank.getGoldMultiplier();
        double baseWithRank = amount * rankMultiplier;
        double hpMultiplier = hpState.getGoldMultiplier();
        double titleMultiplier = titleInventory.getGoldMultiplier();
        int finalAmount = (int) Math.round(baseWithRank * hpMultiplier * titleMultiplier);
        if (finalAmount > 0) this.wallet = wallet.add(finalAmount);
    }
    public void spendGold(int amount) {
        if (!wallet.canAfford(amount)) throw new IllegalStateException("No tienes suficiente oro");
        this.wallet = wallet.subtract(amount);
    }
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
    public void applyUserQuestFailure(com.lifeleveling.domain.quest.user.UserQuest quest) {
        int hpDamage = quest.rank().getMoralDamage();
        if (hpDamage <= 0) return;
        System.out.println("❌ Quest Fallida: " + quest.name() + " (-" + hpDamage + " HP)");

        int predictedHP = this.currentHP - hpDamage;
        int overflow = 0;
        if (predictedHP < 0) {
            overflow = Math.abs(predictedHP);
        }
        takeDamage(hpDamage);

        if (overflow > 0) {
            int goldPenalty = overflow * 10;
            System.out.println("💸 DAÑO MORAL CRÍTICO: El exceso de daño (" + overflow + " HP) se convierte en multa de -" + goldPenalty + " G");
            try {
                // Intentamos cobrar la multa completa
                this.wallet = wallet.subtract(goldPenalty);
            } catch (IllegalStateException | IllegalArgumentException e) {
                // Si no tiene suficiente oro (Wallet tira excepción), BANCARROTA TOTAL
                System.out.println("💸 ¡BANCARROTA! No tienes suficiente oro para cubrir la multa moral. Saldo a 0.");
                this.wallet = com.lifeleveling.domain.player.Wallet.empty();
            }
        }
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
        if (mitigation > 0 && finalDamage == 0) System.out.println("👟 ¡Zapatillas Pegasus amortiguan todo el impacto! (-0 HP)");
        takeDamage(finalDamage);
    }
    private void triggerBurnout() {
        this.activeBurnoutLock = BurnoutLock.createNow();
        this.wallet = wallet.applyBurnoutTax();
        gateTracker.recordBurnoutToday();
        System.out.println("💔 ¡BURNOUT! HP a 0. Bloqueo de 24h y multa aplicada.");
    }
    public boolean isBurnoutActive() { return activeBurnoutLock != null && !activeBurnoutLock.hasExpired(Instant.now()); }
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
        if (weeklyManager != null) {
            Optional<QuestReward> streakReward = weeklyManager.recordPerfectDay();
            streakReward.ifPresent(this::applyQuestReward);
        }
    }
    public void breakPerfectDayStreak() { gateTracker.resetPerfectDayStreak(); }
    public void applyDebuff(DebuffType type, String source, Instant now) {
        if (titleInventory.hasImmunityTo(type.name())) return;
        Debuff debuff = Debuff.create(type, source, now);
        debuffTracker.applyDebuff(debuff);
        gateTracker.notifyDebuffReceived();
    }
    public void cureDebuff(DebuffType type) {
        if (debuffTracker.hasDebuff(type)) debuffTracker.removeDebuff(type);
    }
    public void equipItem(String itemId) { inventory.equip(itemId); }
    public void unequipItem(com.lifeleveling.domain.item.ItemSlot slot) { inventory.unequip(slot); }
    public boolean unlockTitle(TitleType type) { return titleInventory.unlock(type); }
    public void equipTitle(TitleType type) { titleInventory.equip(type, getLevel()); }
    public boolean unequipTitle(TitleType type) { return titleInventory.unequip(type); }
    public void swapTitle(TitleType old, TitleType n) { titleInventory.swap(old, n, getLevel()); }
    public boolean hasTitle(TitleType type) { return titleInventory.hasTitle(type); }
    public boolean isTitleEquipped(TitleType type) { return titleInventory.isEquipped(type); }
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