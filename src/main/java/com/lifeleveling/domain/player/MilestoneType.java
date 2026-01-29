package com.lifeleveling.domain.player;

import com.lifeleveling.domain.item.ItemTier;
import com.lifeleveling.domain.title.TitleType;

/**
 * Hitos de Nivel (Milestones).
 * Recompensas automáticas que se otorgan al alcanzar ciertos niveles clave.
 */
public enum MilestoneType {

    LEVEL_10(10, 1_000, TitleType.SURVIVOR, ItemTier.TIER_1),
    LEVEL_20(20, 2_500, TitleType.VETERAN, ItemTier.TIER_2),

    // Nivel 30: El título EXPERT debería dar +5% XP permanente en sus buffs
    LEVEL_30(30, 5_000, TitleType.EXPERT, null),

    LEVEL_40(40, 10_000, TitleType.MASTER, ItemTier.TIER_3),

    // Nivel 50: Desbloquea 2º slot de título (Gestionado en TitleInventory)
    LEVEL_50(50, 20_000, TitleType.LEGEND, null),

    // Nivel 60: El título DEMIGOD debería dar +10% Gold
    LEVEL_60(60, 30_000, TitleType.DEMIGOD, null),

    // Nivel 70: Anillo del Titán (Item único o Title buff)
    LEVEL_70(70, 50_000, TitleType.TITAN_MILESTONE, null),

    // Nivel 80: +5 HP Base (Gestionado por TitleBuff o BaseStats modifier)
    LEVEL_80(80, 75_000, TitleType.IMMORTAL_MILESTONE, null),

    // Nivel 90: Automatización (Gestionado por sistema de Dailies)
    LEVEL_90(90, 100_000, TitleType.TRANSCENDENT, null),

    // Nivel 100: New Game+
    LEVEL_100(100, 250_000, TitleType.MONARCH, null);

    private final int levelRequired;
    private final int goldReward;
    private final TitleType titleReward;
    private final ItemTier itemReward; // Puede ser null si no hay item

    MilestoneType(int levelRequired, int goldReward, TitleType titleReward, ItemTier itemReward) {
        this.levelRequired = levelRequired;
        this.goldReward = goldReward;
        this.titleReward = titleReward;
        this.itemReward = itemReward;
    }

    public int getLevelRequired() { return levelRequired; }
    public int getGoldReward() { return goldReward; }
    public TitleType getTitleReward() { return titleReward; }
    public ItemTier getItemReward() { return itemReward; }
}