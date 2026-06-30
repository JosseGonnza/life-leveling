package com.lifeleveling.application.dto;

import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.item.ItemCategory;
import com.lifeleveling.domain.item.ItemSlot;
import com.lifeleveling.domain.item.TemporaryBuffSpec;
import com.lifeleveling.domain.player.StatType;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read model de un ítem del catálogo para el modo SHOP de The Armory.
 * `effect`: resumen corto para la tarjeta. `description`: explicación humana para el panel de detalle
 * (qué hace y por qué te interesa), para que el jugador no compre a ciegas.
 * `affordable`/`unlocked`/`requiredLevel`: oro y candado por nivel del tier.
 */
public record ShopItemView(
        String id,
        String name,
        int price,
        String category,
        String slot,
        String effect,
        String description,
        boolean affordable,
        boolean unlocked,
        int requiredLevel,
        boolean burnoutSafe
) {
    public static ShopItemView from(Item item, int gold, int playerLevel) {
        String slot = item.slot() == ItemSlot.NONE ? "—" : item.slot().getDisplayName();
        return new ShopItemView(item.id(), item.name(), item.price(),
                item.category().getDisplayName(), slot, effect(item), details(item),
                item.price() <= gold,
                item.tier().canEquip(playerLevel), item.tier().getMinLevel(), burnoutSafe(item));
    }

    /** Comprable durante el Burnout: curas (MEDICINE) + los items de salida rápida (Escapada Fin de Semana). */
    private static boolean burnoutSafe(Item item) {
        return item.category() == ItemCategory.MEDICINE
                || item.id().equals(ItemCatalog.WEEKEND_TRIP.id());
    }

    /** Resumen corto del efecto (cabe en la tarjeta). */
    private static String effect(Item item) {
        Map<StatType, Integer> bonuses = item.statBonuses();
        if (!bonuses.isEmpty()) {
            return bonuses.entrySet().stream()
                    .map(e -> "+" + e.getValue() + " " + e.getKey().getAbbreviation())
                    .collect(Collectors.joining("  "));
        }
        if (item.temporaryBuff().isPresent()) {
            TemporaryBuffSpec b = item.temporaryBuff().get();
            String buff = "+" + pct(b.multiplier()) + "% " + b.targetStat().getAbbreviation()
                    + " · " + durationText(b.duration());
            return item.hpRecovery() > 0 ? "+" + item.hpRecovery() + " HP · " + buff : buff;
        }
        if (item.curesDebuff().isPresent()) {
            DebuffType d = item.curesDebuff().get();
            String cure = "Cura " + d.getIcon() + " " + d.getDisplayName();
            return item.hpRecovery() > 0 ? "+" + item.hpRecovery() + " HP · " + cure : cure;
        }
        if (item.hpRecovery() > 0) return "+" + item.hpRecovery() + " HP";
        if (item.hpDamage() > 0) return "-" + item.hpDamage() + " HP";
        return item.description() == null ? "" : item.description();
    }

    /** Explicación humana: qué hace y por qué te interesa. Para el panel de detalle. */
    private static String details(Item item) {
        StringBuilder sb = new StringBuilder();
        if (!item.statBonuses().isEmpty()) {
            String bonuses = item.statBonuses().entrySet().stream()
                    .map(e -> "+" + e.getValue() + " " + e.getKey().getDisplayName())
                    .collect(Collectors.joining(", "));
            sb.append("Equipo. Mientras lo lleves equipado en «").append(item.slot().getDisplayName())
                    .append("» te otorga ").append(bonuses).append(".");
        } else if (item.temporaryBuff().isPresent()) {
            TemporaryBuffSpec b = item.temporaryBuff().get();
            sb.append("Potenciador de un solo uso. Al consumirlo, aumenta tu ")
                    .append(b.targetStat().getDisplayName()).append(" un ").append(pct(b.multiplier()))
                    .append("% durante ").append(durationText(b.duration()))
                    .append(". Ideal justo antes de una sesión exigente.");
            if (item.hpRecovery() > 0) sb.append(" Además recupera ").append(item.hpRecovery()).append(" HP.");
        } else if (item.curesDebuff().isPresent()) {
            DebuffType d = item.curesDebuff().get();
            sb.append("Medicina. Elimina el estado ").append(d.getIcon()).append(" ").append(d.getDisplayName())
                    .append(" — ").append(d.getDescription());
            if (item.hpRecovery() > 0) sb.append(" También recupera ").append(item.hpRecovery()).append(" HP.");
        } else if (item.hpRecovery() > 0) {
            sb.append("Recupera ").append(item.hpRecovery()).append(" HP al consumirlo.");
        } else if (item.description() != null && !item.description().isBlank()) {
            sb.append(item.description());
        } else {
            sb.append("Sin efecto especial.");
        }
        if (item.causesDebuff().isPresent()) {
            DebuffType c = item.causesDebuff().get();
            sb.append("\n⚠️ Riesgo: puede provocar ").append(c.getIcon()).append(" ").append(c.getDisplayName()).append(".");
        }
        return sb.toString();
    }

    private static int pct(double multiplier) {
        return (int) Math.round(multiplier * 100);
    }

    private static String durationText(Duration d) {
        if (d == null) return "un rato";
        long hours = d.toHours();
        if (hours >= 1) return hours + (hours == 1 ? " hora" : " horas");
        long minutes = d.toMinutes();
        return minutes + " min";
    }
}
