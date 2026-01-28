package com.lifeleveling.domain.debuff;

public enum DebuffCure {
    SLEEP_RECOVERY,      // Dormir ≥ 7h
    COMPLETE_TIDY,       // Hacer la cama/ordenar
    ITEM_MONSTER,        // Beber Monster (si no hay taquicardia)
    ITEM_ALMAX,          // Tomar Almax
    ITEM_ENTERTAINMENT,  // Jugar/Cine
    TIME_EXPIRED,        // Esperar X horas
    WEEKLY_RESET,        // Esperar al Lunes
    PERFECT_DAY          // Completar todo
}