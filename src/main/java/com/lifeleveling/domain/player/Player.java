package com.lifeleveling.domain.player;

import java.util.UUID;

/**
 * Player: La entidad raíz (Aggregate Root) del dominio.
 * * Responsabilidades:
 * 1. Mantener la consistencia entre HP, Stats y Wallet.
 * 2. Calcular el Nivel General basado en la XP total acumulada.
 * 3. Orquestar la recepción de daño y curación.
 */
public class Player {

    private final UUID id;
    private final String name;

    // Componentes del Jugador (Value Objects inmutables)
    private HPState hpState;
    private int currentHP;
    private Stats stats;
    private Wallet wallet;

    // Estado de Burnout (Bloqueo temporal)
    private BurnoutLock activeBurnoutLock;

    // Constructor privado para forzar uso de métodos factoría
    private Player(UUID id, String name, int currentHP, Stats stats, Wallet wallet, BurnoutLock activeBurnoutLock) {
        this.id = id;
        this.name = name;
        this.currentHP = currentHP;
        this.hpState = HPState.fromHP(currentHP); // El estado se deriva del HP numérico
        this.stats = stats;
        this.wallet = wallet;
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
                null // Sin burnout al inicio
        );
    }

    // Para reconstruir desde persistencia (JSON/DB)
    public static Player restore(UUID id, String name, int currentHP, Stats stats, Wallet wallet, BurnoutLock lock) {
        return new Player(id, name, currentHP, stats, wallet, lock);
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
        int finalAmount = hpState.applyXPMultiplier(amount);

        // 2. Delegamos la subida al objeto Stats (que es inmutable, nos devuelve uno nuevo)
        this.stats = stats.addXP(type, finalAmount);
    }

    public void addGold(int amount) {
        // 1. Aplicamos multiplicadores (ej: Senior gana x4.0) -> Esto vendría de la Gate/Rango,
        // pero aquí aplicamos el multiplicador de salud si aplica.
        int finalAmount = hpState.applyGoldMultiplier(amount);
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

        this.currentHP = Math.min(100, currentHP + amount);
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

    private void triggerBurnout() {
        // 1. Crear el bloqueo
        this.activeBurnoutLock = BurnoutLock.createNow();

        // 2. Aplicar la penalización económica (Impuesto de Salud)
        this.wallet = wallet.applyBurnoutTax();

        // Aquí podríamos lanzar un evento de dominio "BurnoutOccurred"
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
    // GETTERS (Usados por ConditionContext)
    // ========================================================================================

    public int getCurrentHP() {
        return currentHP;
    }

    public int getCurrentGold() {
        return wallet.currentGold();
    }

    public Stats getStats() {
        return stats;
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