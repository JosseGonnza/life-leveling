package com.lifeleveling.domain.player;

/*
 * Reglas de Negocio:
 *   El Gold nunca puede ser negativo (mínimo: 0)
 *   No existe el concepto de "deuda"
 *   En estado BURNOUT (HP=0), se pierde el 10% del Gold total
 *   Las penalizaciones de User Quests fallidas pueden convertir daño HP en Gold loss (1 HP = 10 G)
*/
public record Wallet(int currentGold) {

    public static final Wallet EMPTY = new Wallet(0);
    private static final double BURNOUT_TAX_RATE = 0.10; // 10%
    private static final int HP_TO_GOLD_RATIO = 10;

    public Wallet {
        if (currentGold < 0) {
            throw new IllegalArgumentException(
                    String.format("El Gold no puede ser negativo. Valor inválido: %d", currentGold)
            );
        }
    }

    public static Wallet empty() {
        return EMPTY;
    }

    public static Wallet of(int amount) {
        return new Wallet(amount);
    }

    public boolean isEmpty() {
        return currentGold == 0;
    }

    public boolean canAfford(int cost) {
        if (cost < 0) {
            throw new IllegalArgumentException("El coste no puede ser negativo");
        }
        return currentGold >= cost;
    }

    //Útil para desbloquear títulos como "El Ahorrador" (10,000 G).
    public boolean hasAtLeast(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("El umbral no puede ser negativo");
        }
        return currentGold >= threshold;
    }

    public Wallet add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    String.format("No se puede añadir Gold negativo: %d", amount)
            );
        }
        return new Wallet(currentGold + amount);
    }

    public Wallet subtract(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    String.format("No se puede restar Gold negativo: %d", amount)
            );
        }

        //Clamping: Si restamos más de lo que tenemos, el resultado es 0
        int newAmount = Math.max(0, currentGold - amount);
        return new Wallet(newAmount);
    }

    public Wallet applyBurnoutTax() {
        int taxAmount = (int) (currentGold * BURNOUT_TAX_RATE);
        return subtract(taxAmount);
    }

    public Wallet applyMoralDamageAsGold(int hpDamage) {
        if (hpDamage < 0) {
            throw new IllegalArgumentException("El daño HP no puede ser negativo");
        }

        int goldLoss = hpDamage * HP_TO_GOLD_RATIO;
        return subtract(goldLoss);
    }

    public String toDisplayString() {
        return String.format("%,d G", currentGold);
    }

    public double getProgressTowards(int goal) {
        if (goal <= 0) {
            throw new IllegalArgumentException("La meta debe ser mayor que 0");
        }

        if (currentGold >= goal) {
            return 100.0;
        }

        return (currentGold * 100.0) / goal;
    }

    public static int calculateBurnoutTax(int goldAmount) {
        if (goldAmount < 0) {
            throw new IllegalArgumentException("La cantidad de Gold no puede ser negativa");
        }
        return (int) (goldAmount * BURNOUT_TAX_RATE);
    }

    public static int convertHPDamageToGold(int hpDamage) {
        if (hpDamage < 0) {
            throw new IllegalArgumentException("El daño HP no puede ser negativo");
        }
        return hpDamage * HP_TO_GOLD_RATIO;
    }
}