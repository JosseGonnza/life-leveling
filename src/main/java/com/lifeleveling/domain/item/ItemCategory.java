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
    ARTIFACT        // Items únicos con mecánicas especiales (Anillo Titán)
}