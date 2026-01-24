package com.lifeleveling.domain.player;

public enum HPState {

    /*
     * Estado SALUDABLE: HP entre 50 y 100
     *   XP Multiplier: 1.0x (100%)
     *   Gold Multiplier: 1.0x (100%)
     *   Restricciones: Ninguna
    */
    HEALTHY(50, 100, 1.0, 1.0),
    /*
     * Estado CANSADO: HP entre 1 y 49
     *   XP Multiplier: 0.5x (50%)
     *   Gold Multiplier: 1.0x (100%)
     *   Restricciones: Prohibido iniciar Quests Rango B+
    */
    TIRED(1, 49, 0.5, 1.0),
    /*
     * Estado CRÍTICO: HP = 0 (BURNOUT)
     *   XP Multiplier: 0.0x (0%) - No se gana XP
     *   Gold Multiplier: 0.0x (0%) - No se gana Gold
     *   Restricciones: LOCKDOWN total. Solo Daily Quests Rango E/D permitidas
     *   Penalización: -10% del Oro total + Bloqueo de 24 horas
    */
    CRITICAL(0, 0, 0.0, 0.0);

    private final int minHP;
    private final int maxHP;
    private final double xpMultiplier;
    private final double goldMultiplier;

    HPState(int minHP, int maxHP, double xpMultiplier, double goldMultiplier) {
        this.minHP = minHP;
        this.maxHP = maxHP;
        this.xpMultiplier = xpMultiplier;
        this.goldMultiplier = goldMultiplier;
    }

    public static HPState fromHP(int currentHP) {
        if (currentHP < 0 || currentHP > 100) {
            throw new IllegalArgumentException(
                    String.format("HP inválido: %d. Debe estar entre 0 y 100.", currentHP)
            );
        }

        if (currentHP == 0) {
            return CRITICAL;
        } else if (currentHP < 50) {
            return TIRED;
        } else {
            return HEALTHY;
        }
    }

    //Verifica si este estado permite iniciar Quests de alto rango (B+)
    public boolean canStartHighRankQuests() {
        return this == HEALTHY;
    }

    //Verifica si el jugador está en estado de BURNOUT
    public boolean isBurnout() {
        return this == CRITICAL;
    }

    public int applyXPMultiplier(int baseXP) {
        if (baseXP < 0) {
            throw new IllegalArgumentException("La XP base no puede ser negativa");
        }
        return (int) (baseXP * xpMultiplier);
    }

    public int applyGoldMultiplier(int baseGold) {
        if (baseGold < 0) {
            throw new IllegalArgumentException("El Gold base no puede ser negativo");
        }
        return (int) (baseGold * goldMultiplier);
    }

    public int getMinHP() {
        return minHP;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public double getGoldMultiplier() {
        return goldMultiplier;
    }
}
