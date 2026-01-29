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

    // Componentes del Jugador (Value Objects inmutables)
    private HPState hpState;
    private int currentHP;
    private Stats stats; // Stats Base (Real XP)
    private Wallet wallet;

    // Componentes mutables (Entidades internas)
    private final Inventory inventory;
    private final TitleInventory titleInventory;
    private final DebuffTracker debuffTracker;
    private final GateTracker gateTracker;
    private final CareerEngine careerEngine;  // [CareerEngine] Gestiona sesiones de código

    // Estado de Burnout (Bloqueo temporal)
    private BurnoutLock activeBurnoutLock;

    // Constructor privado para forzar uso de métodos factoría
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
                100, // HP Inicial
                Stats.initial(),
                Wallet.empty(),
                new Inventory(),
                new TitleInventory(),
                new DebuffTracker(),
                new GateTracker(),
                new CareerEngine(),  // [CareerEngine] Inicializar
                null
        );
    }

    // Para reconstruir desde persistencia (JSON/DB)
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
        if (!wallet.canAfford(amount)) {
            throw new IllegalStateException("No tienes suficiente oro");
        }
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

    public void tryClearBurnout() {
        if (activeBurnoutLock != null && activeBurnoutLock.hasExpired(Instant.now()) && currentHP > 0) {
            this.activeBurnoutLock = null;
        }
    }

    // ========================================================================================
    // GESTIÓN DE DEBUFFS (Con Integración GateTracker - Fase 9)
    // ========================================================================================

    public void applyDebuff(DebuffType type, String source, Instant now) {
        // 1. Verificar Inmunidad (Títulos)
        if (titleInventory.hasImmunityTo(type.name())) {
            return;
        }

        // 2. Aplicar Debuff
        Debuff debuff = Debuff.create(type, source, now);
        debuffTracker.applyDebuff(debuff);

        // 3. [Fase 9] Notificar al Tracker de Progreso (Romper Racha)
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

    /**
     * Método para el ciclo diario (Clean Up).
     * Se llama al iniciar la app o cambiar de día.
     */
    public void updateState(Instant now) {
        // Limpiar debuffs caducados
        debuffTracker.cleanExpiredDebuffs(now);
        // Intentar salir de Burnout
        tryClearBurnout();

        // [Fase 9] Verificar si merecemos sumar racha limpia
        if (debuffTracker.getActiveDebuffs().isEmpty()) {
            gateTracker.incrementDebuffFreeStreak();
        }
    }

    // ========================================================================================
    // CONSUMO DE ITEMS (Fase 8)
    // ========================================================================================

    public void consumeItem(Item item) {
        // 1. Verificar existencia
        if (!inventory.hasItem(item.id())) {
            throw new IllegalStateException("No tienes este item en el inventario: " + item.name());
        }

        System.out.println("🥣 Consumiendo: " + item.name());

        // 2. Efectos base (HP)
        if (item.hpRecovery() > 0) {
            this.heal(item.hpRecovery());
        }
        if (item.hpDamage() > 0) {
            this.takeDamage(item.hpDamage());
        }

        // 3. Efectos secundarios (Debuffs/Curas)
        applyItemSideEffects(item);

        // 4. Eliminar del inventario
        inventory.removeItem(item.id());
    }

    private void applyItemSideEffects(Item item) {
        Instant now = Instant.now();

        // A. Causa Debuff
        if (item.causesDebuff().isPresent()) {
            DebuffType debuffToApply = item.causesDebuff().get();
            this.applyDebuff(debuffToApply, "Consumo de " + item.name(), now);
        }

        // B. Cura Debuff
        if (item.curesDebuff().isPresent()) {
            DebuffType debuffToCure = item.curesDebuff().get();

            // Lógica Especial: Cafeína
            if (item.isCaffeineSource()) {
                handleCaffeineConsumption(debuffToCure, item.id());
            } else {
                // Cura estándar
                this.cureDebuff(debuffToCure);
            }
        }
    }

    private void handleCaffeineConsumption(DebuffType debuffToCure, String itemId) {
        // Usamos el helper del tracker
        if (!debuffTracker.canCureWithCaffeine()) {
            System.out.println("💓 Tu corazón va a mil. La cafeína no surte efecto.");
            return;
        }

        this.cureDebuff(debuffToCure);

        // Riesgo de sobredosis (Monster)
        if (itemId.equalsIgnoreCase("monster_energy")) {
            debuffTracker.recordMonsterConsumed();
            Optional<Debuff> overdose = debuffTracker.checkItemConsumptionTrigger(itemId, Instant.now());
            overdose.ifPresent(d -> this.applyDebuff(d.getType(), d.getSource(), d.getAppliedAt()));
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

    public GateTracker getGateTracker() {
        return gateTracker;
    }

    // ========================================================================================
    // CAREER ENGINE (Sesiones de Código)
    // ========================================================================================

    /**
     * Registra una sesión de código (múltiples entradas permitidas por día).
     *
     * Este método:
     * 1. Registra la sesión en CareerEngine (calcula XP y Flow)
     * 2. Aplica XP (INT, DIS, y WIS si Flow) con todos los multiplicadores
     * 3. Aplica daño HP (3/hora) con mitigación de equipo
     * 4. Actualiza horas totales en GateTracker (para títulos)
     *
     * @param hours Horas trabajadas en esta sesión (mínimo 0.5, máximo 12)
     * @return La sesión creada con sus recompensas
     * @throws IllegalArgumentException si las horas son inválidas o exceden el límite diario
     */
    public CodeSession registerCodeSession(double hours) {
        // 1. Registrar en CareerEngine (calcula XP, HP, Flow)
        CodeSession session = careerEngine.registerSession(hours);
        CareerReward reward = session.getReward();

        // 2. Aplicar XP (con multiplicadores de HP state, debuffs, títulos)
        addXP(StatType.INTELLECT, reward.intellectXP());
        addXP(StatType.DISCIPLINE, reward.disciplineXP());
        if (session.isFlowAchieved()) {
            addXP(StatType.WISDOM, reward.wisdomXP());
        }

        // 3. Aplicar daño HP (con mitigación de equipo: silla, periféricos, etc.)
        takeWorkDamage(reward.hpCost());

        // 4. Actualizar horas totales en GateTracker (para títulos como Code Monkey, 10x Dev)
        gateTracker.setTotalCareerHours(careerEngine.getTotalCareerHours());

        return session;
    }

    /**
     * ¿Se ha registrado al menos una sesión de código hoy?
     * Útil para marcar la DailyQuest CODE como completada.
     */
    public boolean hasCodeActivityToday() {
        return careerEngine.hasActivityToday();
    }

    /**
     * Obtiene el CareerEngine para consultar sesiones y estadísticas.
     */
    public CareerEngine getCareerEngine() {
        return careerEngine;
    }
}