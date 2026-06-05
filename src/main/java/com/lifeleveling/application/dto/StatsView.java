package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.Stats;
import com.lifeleveling.domain.player.StatType;

/**
 * Niveles de las 5 stats para el radar pentagonal de la Home. Valores efectivos
 * (con bonos de equipo aplicados).
 */
public record StatsView(
        int strength,
        int intellect,
        int wisdom,
        int discipline,
        int charisma
) {
    public static StatsView from(Stats stats) {
        return new StatsView(
                stats.getLevel(StatType.STRENGTH),
                stats.getLevel(StatType.INTELLECT),
                stats.getLevel(StatType.WISDOM),
                stats.getLevel(StatType.DISCIPLINE),
                stats.getLevel(StatType.CHARISMA)
        );
    }
}
