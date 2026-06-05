package com.lifeleveling.domain.player;

import com.lifeleveling.domain.career.CareerEngine;
import com.lifeleveling.domain.career.CareerReward;
import com.lifeleveling.domain.career.CodeSessionConstants;
import com.lifeleveling.domain.career.CodeSession;
import com.lifeleveling.domain.debuff.Debuff;
import com.lifeleveling.domain.debuff.DebuffTracker;
import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.event.GameEvent;
import com.lifeleveling.domain.event.GameEventPublisher;
import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemCategory;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
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

    private static final double JOB_BASE_RATE = 31.25; // G/hora base del JOB (Biblia 3.1)
    private static final int JOB_MAX_HOURS = 16;        // Jornada máxima registrable (Biblia 1.1)
    private static final String AIR_FRYER_ID = "gear_airfryer"; // DIET da +5 HP extra (Biblia 2.1.2)

    private final UUID id;
    private final String name;

    private HPState hpState;
    private int currentHP;

    // Rango Profesional (Multiplicador de Ingresos)
    private PlayerRank currentRank;

    private Stats stats;
    private Wallet wallet;

    // [NUEVO] XP que contribuye al nivel pero no pertenece a ningún stat específico
    private long accumulatedGeneralXP;

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

    // Sistema de eventos para notificaciones (reemplaza System.out.println)
    private GameEventPublisher eventPublisher;

    private Player(UUID id, String name, int currentHP, PlayerRank currentRank, Stats stats, Wallet wallet,
                   long accumulatedGeneralXP, // [NUEVO] Argumento en constructor
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
        this.accumulatedGeneralXP = accumulatedGeneralXP; // [NUEVO] Asignación
        this.inventory = inventory;
        this.titleInventory = titleInventory;
        this.debuffTracker = debuffTracker;
        this.tempBuffTracker = tempBuffTracker;
        this.gateTracker = gateTracker;
        this.careerEngine = careerEngine;
        this.milestoneTracker = milestoneTracker;
        this.weeklyManager = weeklyManager;
        this.activeBurnoutLock = activeBurnoutLock;
        this.eventPublisher = GameEventPublisher.silent(); // Default: sin output
    }

    /**
     * Configura el publisher de eventos para recibir notificaciones del juego.
     * Útil para conectar CLI, JavaFX, o cualquier UI.
     */
    public void setEventPublisher(GameEventPublisher publisher) {
        this.eventPublisher = publisher != null ? publisher : GameEventPublisher.silent();
    }

    public GameEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    private void emit(GameEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publish(event);
        }
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
                0L, // [NUEVO] XP General inicial a 0
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
                                 long accumulatedGeneralXP, // [NUEVO] Para restaurar estado
                                 Inventory inventory, TitleInventory titleInventory,
                                 DebuffTracker debuffTracker,
                                 TemporaryBuffTracker tempBuffTracker,
                                 GateTracker gateTracker,
                                 CareerEngine careerEngine,
                                 MilestoneTracker milestoneTracker,
                                 WeeklyManager weeklyManager,
                                 BurnoutLock lock) {
        return new Player(id, name, currentHP, rank, stats, wallet, accumulatedGeneralXP, inventory, titleInventory,
                debuffTracker, tempBuffTracker, gateTracker, careerEngine, milestoneTracker, weeklyManager, lock);
    }

    // ========================================================================================
    // CICLO DIARIO & SEMANAL
    // ========================================================================================

    public void updateState(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());

        // 0. Cerrar el día: volcar la actividad acumulada (horas, páginas, user quests, balance)
        //    al histórico ANTES de limpiar los flags, para que las GateConditions tengan datos.
        gateTracker.setDebuffsActiveToday(!debuffTracker.getActiveDebuffs().isEmpty());
        gateTracker.closeDay(today, wallet.currentGold());

        // 1. Si termina el día y no lograste el Perfect Day, la racha vuelve a 0.
        if (!gateTracker.isPerfectDayAchievedToday()) {
            if (gateTracker.getPerfectDayStreak() > 0) {
                System.out.println("💔 Racha de Perfect Days rota. Vuelta a 0.");
                gateTracker.resetPerfectDayStreak();
            }
        }

        // 2. Limpieza estándar diaria
        gateTracker.resetDailyFlags();
        debuffTracker.cleanExpiredDebuffs(now);
        tempBuffTracker.cleanExpiredBuffs(now);
        manageBurnoutState(now);

        // 3. Rotación Semanal
        if (weeklyManager != null) {
            weeklyManager.performWeeklyReset(today);
        }

        // 4. Comprobación de Triggers automáticos (Castigos)
        int daysNoTidy = gateTracker.getDaysSinceLastQuestCompletion("TIDY");
        int workStreak = gateTracker.getConsecutiveWorkDays();

        List<Debuff> newDebuffs = debuffTracker.checkDailyResetTriggers(daysNoTidy, workStreak, now);

        for (Debuff db : newDebuffs) {
            applyDebuffDirect(db);
            System.out.println("⚠️ CASTIGO AUTOMÁTICO: " + db.getType().getDisplayName());
        }

        // 5. Racha de pureza
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
        if (item.isCaffeineSource()) {
            LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());
            debuffTracker.recordMonsterConsumption(today);
        }

        if (item.hpRecovery() > 0) heal(item.hpRecovery());
        if (item.hpDamage() > 0) takeDamage(item.hpDamage());

        // Logic-Lock de DIET (Biblia 2.1.2): comer basura bloquea el check de Dieta hoy.
        if (item.category() == ItemCategory.FOOD_JUNK) gateTracker.recordJunkFoodConsumed();

        debuffTracker.checkItemConsumptionTrigger(item.id(), now).ifPresent(this::applyDebuffDirect);

        applyItemSideEffects(item);

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

        // [FASE 3.3] Aplicar descuento de Cafetera Espresso
        if (item.category() == ItemCategory.DRINK_SOCIAL) {
            double multiplier = inventory.getSocialDrinkDiscountMultiplier();
            if (multiplier < 1.0) {
                finalPrice = (int) Math.round(finalPrice * multiplier);
                System.out.println("☕ ¡Cafetera Espresso rentabilizada! Precio reducido a " + finalPrice + " G");
            }
        }

        spendGold(finalPrice);
        inventory.recordPurchase(item);
        gateTracker.recordConsumableBought(item.id());
        if (item.category() == ItemCategory.LUXURY) gateTracker.recordLuxuryPurchase();
    }

    // ========================================================================================
    // LEVELING
    // ========================================================================================

    /**
     * Añade XP a un Stat específico (Fuerza, Intelecto...).
     * Esto sube el stat Y contribuye al nivel general.
     */
    public void addXP(StatType type, int amount) {
        if (amount <= 0) return;

        int oldLevel = getLevel();

        double hpMultiplier = hpState.getXpMultiplier();
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

    public void addGeneralXP(int amount) {
        if (amount <= 0) return;
        int oldLevel = getLevel();

        double hpMultiplier = hpState.getXpMultiplier();
        double debuffGlobalMult = debuffTracker.getGlobalXPMultiplier();
        double titleMultiplier = titleInventory.getGeneralXPMultiplier();

        int finalAmount = (int) Math.round(amount * hpMultiplier * debuffGlobalMult * titleMultiplier);

        if (finalAmount > 0) {
            // [CAMBIO CLAVE] Ya no repartimos entre stats. Sumamos al acumulado general.
            this.accumulatedGeneralXP += finalAmount;
            emit(GameEvent.xpGained("General", finalAmount, (int) accumulatedGeneralXP));
            checkLevelUp(oldLevel);
        }
    }

    public int getLevel() {
        long statsXP = stats.getTotalAccumulatedXP();
        long totalXP = statsXP + this.accumulatedGeneralXP;

        if (totalXP == 0) return 1;

        int level = (int) Math.sqrt(totalXP / 50.0);

        return Math.max(1, Math.min(level, 100 ));
    }

    /** XP total acumulada (stats + general); misma base que getLevel. Para la barra de XP del HUD. */
    public long getTotalXP() {
        return stats.getTotalAccumulatedXP() + this.accumulatedGeneralXP;
    }

    private void checkLevelUp(int oldLevel) {
        int newLevel = getLevel();
        if (newLevel > oldLevel) {
            emit(GameEvent.levelUp(oldLevel, newLevel));
            List<MilestoneType> awards = milestoneTracker.checkAndAward(this);
            if (!awards.isEmpty()) {
                emit(GameEvent.info("✨ Recompensas otorgadas: " + awards.size() + " hitos conseguidos."));
            }
        }
    }

    // ========================================================================================
    // OTROS (Career, Sleep, etc.)
    // ========================================================================================

    public CodeSession registerCodeSession(double hours) {
        boolean hasAiAgent = inventory.hasItem("soft_ai_agent");
        CodeSession session = careerEngine.registerSession(hours, hasAiAgent);
        CareerReward reward = session.getReward();

        addXP(StatType.INTELLECT, reward.intellectXP());
        addXP(StatType.DISCIPLINE, reward.disciplineXP());
        if (session.isFlowAchieved()) addXP(StatType.WISDOM, reward.wisdomXP());

        takeWorkDamage(reward.hpCost(), hours);
        gateTracker.setTotalCareerHours(careerEngine.getTotalCareerHours());
        gateTracker.recordCareerHoursToday(hours);
        if (hours > 0) gateTracker.recordDailyQuestCompleted(DailyQuestType.CODE.name());
        notifyQuestCompleted("CODE", hours);
        evaluatePerfectDay();

        return session;
    }

    /**
     * Registra una jornada de JOB (empleo alimenticio).
     * Salario = round(horas × 31.25 × multiplicadorRango). Solo el JOB usa el rango.
     * Desgaste 3 HP/h (con mitigación de equipo). XP nula (es tiempo vendido).
     * Floor Rule: solo horas enteras; máximo 16h (Biblia cap 3.1 y 1.1).
     */
    public int registerJobSession(int hours) {
        if (hours <= 0) return 0;
        int workedHours = Math.min(hours, JOB_MAX_HOURS);
        int salary = (int) Math.round(workedHours * JOB_BASE_RATE * currentRank.getGoldMultiplier());

        if (salary > 0) this.wallet = wallet.add(salary);
        takeWorkDamage(workedHours * CodeSessionConstants.HP_COST_PER_HOUR, workedHours);
        notifyQuestCompleted("JOB", workedHours);

        return salary;
    }

    /**
     * Registra lectura (READ): otorga WIS y acumula páginas del día (alimenta GATE 3 / Elder).
     */
    public void registerReadSession(int pages) {
        if (pages <= 0) return;
        applyQuestReward(DailyQuestType.READ.calculateReward(pages));
        gateTracker.recordPagesRead(pages);
        if (DailyQuestType.READ.meetsCondition(pages)) {
            gateTracker.recordDailyQuestCompleted(DailyQuestType.READ.name());
        }
        notifyQuestCompleted("READ", pages);
        evaluatePerfectDay();
    }

    /**
     * Completa una User Quest: aplica su recompensa (solo XP, C1) y la registra en el día
     * para las condiciones de gate por rango/ventana (GATE 2 y 4). Devuelve la quest completada.
     */
    public com.lifeleveling.domain.quest.user.UserQuest completeUserQuest(
            com.lifeleveling.domain.quest.user.UserQuest quest, Instant completedAt) {
        com.lifeleveling.domain.quest.user.UserQuest completed = quest.complete(completedAt);
        applyQuestReward(completed.reward());
        gateTracker.recordUserQuestCompleted(completed.rank(), completed.id().toString());
        return completed;
    }

    public void registerSleepSession(int hours) {
        if (hours <= 0) return;
        int baseXP = DailyQuestType.SLEEP.calculateReward(hours).generalXP();
        double itemMultiplier = inventory.getSleepXPMultiplier();
        int finalXP = (int) (baseXP * itemMultiplier);

        addGeneralXP(finalXP);
        heal(DailyQuestType.SLEEP.calculateDynamicHP(hours));
        if (DailyQuestType.SLEEP.meetsCondition(hours)) {
            gateTracker.recordDailyQuestCompleted(DailyQuestType.SLEEP.name());
        }
        notifyQuestCompleted("SLEEP", hours);
        evaluatePerfectDay();

        if (itemMultiplier > 1.0) {
            System.out.println("🛏️ ¡Descanso Premium! XP x" + itemMultiplier);
        }
    }

    /**
     * Completa un hábito diario booleano: DIET, GYM, SKINCARE o TIDY (Biblia cap 2.1.2).
     * SLEEP/CODE/READ tienen su propia entrada (registerSleep/Code/ReadSession).
     * Reglas: DIET +50 XP y +5 HP (+10 con Air Fryer), con Logic-Lock si hoy hubo comida basura;
     * GYM +50 STR y -5 HP (mitigable con Pegasus); SKINCARE +50 CHA y +10 HP; TIDY +50 DIS y cura Caos.
     * B1: si este check cierra el Perfect Day (7/7), prevalece (HP a 100, sin Burnout) y se ignora el coste de HP.
     */
    public void completeHabit(DailyQuestType type, boolean completed) {
        if (type == null) throw new IllegalArgumentException("Hábito nulo");
        if (!type.requiresBooleanInput()) {
            throw new IllegalStateException(type + " no es un hábito booleano; usa su registro específico");
        }
        if (!completed) return;

        if (type == DailyQuestType.DIET && gateTracker.wasJunkConsumedToday()) {
            emit(GameEvent.warning("No puedes marcar Dieta hoy. Tu cuerpo recuerda esa hamburguesa."));
            return;
        }

        applyQuestReward(type.calculateReward(true));
        gateTracker.recordDailyQuestCompleted(type.name());
        notifyQuestCompleted(type.name(), 1);
        emit(GameEvent.questCompleted(type.getName()));

        if (evaluatePerfectDay()) return; // B1: Perfect Day cura a 100 y anula el coste de HP

        switch (type) {
            case DIET -> heal(inventory.hasItem(AIR_FRYER_ID) ? 10 : 5);
            case SKINCARE -> heal(type.getHpEffect());
            case GYM -> takeGymDamage();
            default -> { } // TIDY: sin efecto de HP
        }
    }

    /** Dispara el Perfect Day si ya hay 7/7 hábitos hoy. Devuelve true si quedó (o ya estaba) logrado. */
    private boolean evaluatePerfectDay() {
        if (gateTracker.isPerfectDayAchievedToday()) return true;
        if (gateTracker.getDailyHabitsCompletedTodayCount() >= 7) {
            triggerPerfectDay();
            return true;
        }
        return false;
    }

    // ========================================================================================
    // RESTO DE MÉTODOS (Getters, Helpers, etc.)
    // ========================================================================================

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

    public void applyQuestReward(QuestReward reward) {
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
        // El multiplicador de RANGO solo afecta al JOB (Biblia 3.1.4.B): misiones, loot
        // y milestones son oro fijo. Aquí solo aplican los modificadores globales (HP/títulos).
        double hpMultiplier = hpState.getGoldMultiplier();
        double titleMultiplier = titleInventory.getGoldMultiplier();
        int finalAmount = (int) Math.round(amount * hpMultiplier * titleMultiplier);
        if (finalAmount > 0) this.wallet = wallet.add(finalAmount);
    }

    public void spendGold(int amount) {
        if (!wallet.canAfford(amount)) throw new IllegalStateException("No tienes suficiente oro");
        this.wallet = wallet.subtract(amount);
        gateTracker.recordGoldSpent(amount);
    }

    public void heal(int amount) {
        int finalAmount = titleInventory.applyHPRecoveryBonus(amount);
        this.currentHP = Math.min(100, currentHP + finalAmount);
        this.hpState = HPState.fromHP(this.currentHP);
    }

    public void takeDamage(int amount) {
        this.currentHP = Math.max(0, currentHP - amount);
        this.hpState = HPState.fromHP(this.currentHP);
        gateTracker.recordHPSnapshot(this.currentHP);
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
            int actualPenalty = Math.min(goldPenalty, wallet.currentGold());
            System.out.println("💸 DAÑO MORAL CRÍTICO: El exceso de daño (" + overflow + " HP) se convierte en multa de -" + goldPenalty + " G");
            if (actualPenalty < goldPenalty) {
                System.out.println("💸 ¡BANCARROTA! No tienes suficiente oro. Saldo a 0.");
            }
            this.wallet = wallet.subtract(goldPenalty); // Wallet.subtract() clamps to 0
            gateTracker.recordGoldSpent(actualPenalty);
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
        int taxAmount = Wallet.calculateBurnoutTax(wallet.currentGold());
        this.wallet = wallet.applyBurnoutTax();
        gateTracker.recordGoldSpent(taxAmount);
        gateTracker.recordBurnoutToday();
        emit(GameEvent.burnout());
    }

    public boolean isBurnoutActive() { return activeBurnoutLock != null && !activeBurnoutLock.hasExpired(Instant.now()); }

    private void manageBurnoutState(Instant now) {
        if (activeBurnoutLock == null || !activeBurnoutLock.hasExpired(now)) return;
        if (currentHP > 0) {
            emit(GameEvent.burnoutRecovered());
            this.activeBurnoutLock = null;
        } else {
            int tax = (int) (wallet.currentGold() * 0.10); // Bucle de Hospital: 10% diario (Biblia 1.5 Fase 4.1)
            if (tax > 0) {
                this.wallet = wallet.subtract(tax);
                gateTracker.recordGoldSpent(tax);
                emit(GameEvent.goldSpent(tax, "Hospitalización extendida"));
            }
            this.activeBurnoutLock = BurnoutLock.trigger(now);
        }
    }

    public void triggerPerfectDay() {
        if (gateTracker.isPerfectDayAchievedToday()) return;
        emit(GameEvent.perfectDay());
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
    public TemporaryBuffTracker getTempBuffTracker() { return tempBuffTracker; }
}