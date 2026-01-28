package com.lifeleveling.domain.item;

public enum ItemTier {
    TIER_1("Supervivencia", 10), // Disponible desde nivel 10
    TIER_2("Profesional", 20),   // Disponible desde nivel 20
    TIER_3("Legendario", 40),    // Disponible desde nivel 40
    CONSUMABLE("Consumible", 0); // Siempre disponible (Comida, Bebida)

    private final String name;
    private final int minLevel;

    ItemTier(String name, int minLevel) {
        this.name = name;
        this.minLevel = minLevel;
    }

    public boolean canEquip(int playerLevel) {
        return playerLevel >= minLevel;
    }
}