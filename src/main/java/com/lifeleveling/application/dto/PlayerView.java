package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.Player;

/**
 * Read model del estado del jugador para la UI. Inmutable, sin tipos de dominio crudos:
 * la cara (CLI/JavaFX/web) pinta directamente desde aquí.
 */
public record PlayerView(
        String name,
        int level,
        int currentHP,
        String hpState,
        int gold,
        String rank,
        double salaryMultiplier
) {
    public static PlayerView from(Player p) {
        return new PlayerView(
                p.getName(),
                p.getLevel(),
                p.getCurrentHP(),
                p.getHpState().name(),
                p.getCurrentGold(),
                p.getCurrentRank().getDisplayName(),
                p.getCurrentRank().getGoldMultiplier()
        );
    }
}
