package com.lifeleveling.domain.item;

public enum ItemSlot {
    HEAD("🧢", "Cabeza"),      // Gorras (Estilo/Focus)
    BODY("👕", "Cuerpo"),      // Ropa (Confort/Estilo)
    LEGS("👖", "Piernas"),     // Pantalones
    FEET("👟", "Pies"),        // Zapatillas (Running)
    HAND("⌚", "Mano"),        // Relojes, Anillos, eBooks
    PERIPH("🖱️", "Periférico"),// Teclado, Ratón (Reducen daño CODE)
    DESK("🖥️", "Escritorio"),  // Monitor, Lámpara
    CHAIR("🪑", "Silla"),       // Vital para reducir daño de espalda
    SOFT("💾", "Software"),    // Licencias, IDEs (Boost XP)
    BED("🛏️", "Cama"),         // Colchón (Mejora SLEEP)
    KITCHEN("🍳", "Cocina"),   // Air Fryer (Mejora DIET)
    WALL("🖼️", "Pared"),       // Decoración
    NONE("📦", "Inventario");  // Consumibles guardados en mochila

    private final String icon;
    private final String displayName;

    ItemSlot(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
}