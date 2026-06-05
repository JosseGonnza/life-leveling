package com.lifeleveling.domain.event;

/**
 * Tipos de eventos del juego.
 * Permite filtrar y categorizar eventos para diferentes listeners.
 */
public enum GameEventType {
    // Progresión
    LEVEL_UP,
    XP_GAINED,
    RANK_UP,
    MILESTONE_REACHED,
    TITLE_UNLOCKED,

    // Quests
    QUEST_COMPLETED,
    QUEST_FAILED,
    WEEKLY_QUEST_COMPLETED,
    GATE_COMPLETED,
    GATE_AVAILABLE,

    // HP y Estado
    BURNOUT,
    BURNOUT_RECOVERED,
    PERFECT_DAY,
    PERFECT_DAY_STREAK_BROKEN,
    DAMAGE_TAKEN,
    HEALED,

    // Debuffs y Buffs
    DEBUFF_APPLIED,
    DEBUFF_CURED,
    DEBUFF_WARNING,
    BUFF_ACTIVATED,
    BUFF_EXPIRED,

    // Economía
    GOLD_GAINED,
    GOLD_SPENT,
    ITEM_PURCHASED,
    ITEM_CONSUMED,

    // Career
    CODE_SESSION_COMPLETED,
    FLOW_STATE_ACHIEVED,

    // General
    INFO,
    WARNING,
    NEW_WEEK
}
