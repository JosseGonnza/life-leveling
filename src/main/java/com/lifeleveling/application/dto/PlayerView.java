package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.Player;

/**
 * Read model del estado del jugador para la UI. Inmutable, sin tipos de dominio crudos:
 * la cara (CLI/JavaFX/web) pinta directamente desde aquí.
 */
public record PlayerView(
        String name,
        int level,
        long totalXP,
        int xpIntoLevel,
        int xpForLevelSpan,
        int currentHP,
        String hpState,
        int gold,
        String rank,
        String rankLetter,
        double salaryMultiplier,
        StatsView stats,
        boolean burnout,
        boolean highRankLocked
) {
    public static PlayerView from(Player p) {
        int level = p.getLevel();
        long totalXP = p.getTotalXP();
        long base = 50L * level * level;            // XP para SER nivel L (getLevel = floor(sqrt(XP/50)))
        long next = 50L * (level + 1) * (level + 1);
        int xpIntoLevel = level >= 100 ? 0 : (int) Math.max(0, totalXP - base);
        int xpForLevelSpan = level >= 100 ? 0 : (int) (next - base);

        return new PlayerView(
                p.getName(),
                level,
                totalXP,
                xpIntoLevel,
                xpForLevelSpan,
                p.getCurrentHP(),
                p.getHpState().getDisplayName(),
                p.getCurrentGold(),
                p.getCurrentRank().getDisplayName(),
                p.getCurrentRank().getLetter(),
                p.getCurrentRank().getGoldMultiplier(),
                StatsView.from(p.getEffectiveStats()),
                p.getHpState().isBurnout(),
                !p.getHpState().canStartHighRankQuests()
        );
    }
}
