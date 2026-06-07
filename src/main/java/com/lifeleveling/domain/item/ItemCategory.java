package com.lifeleveling.domain.item;

public enum ItemCategory {
    // --- EQUIPAMIENTO ---
    CLOTHING,       // Ropa
    TECH,           // Hardware/Software
    FURNITURE,      // Muebles
    DECORATION,     // Decoración

    // --- CONSUMIBLES ---
    FOOD_HEALTHY,   // Poke Bowl
    FOOD_JUNK,      // Hamburguesa
    DRINK_ENERGY,   // Monster
    DRINK_SOCIAL,   // Café con amigos
    MEDICINE,       // Curas (Almax, Ibuprofeno)
    SUPPLEMENT,     // [NUEVO] Potenciadores temporales (Nootrópicos, Proteína)
    LUXURY,         // Caprichos
    ENTERTAINMENT,  // Ocio

    // --- ENDGAME & SPECIALS ---
    TREASURE,       // Metas financieras (Coche, Casa, Libertad)
    ARTIFACT;       // Items únicos con mecánicas especiales (Anillo Titán)

    public String getDisplayName() {
        return switch (this) {
            case CLOTHING -> "Ropa";
            case TECH -> "Tecnología";
            case FURNITURE -> "Mobiliario";
            case DECORATION -> "Decoración";
            case FOOD_HEALTHY -> "Comida sana";
            case FOOD_JUNK -> "Comida basura";
            case DRINK_ENERGY -> "Bebida energética";
            case DRINK_SOCIAL -> "Bebida social";
            case MEDICINE -> "Medicina";
            case SUPPLEMENT -> "Suplemento";
            case LUXURY -> "Capricho";
            case ENTERTAINMENT -> "Ocio";
            case TREASURE -> "Tesoro";
            case ARTIFACT -> "Artefacto";
        };
    }
}