package com.lifeleveling.domain.quest;

import com.lifeleveling.domain.player.StatType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/*
 * Mecánicas:
 *   - Stat XP: XP específica de atributos (STR, INT, WIS, DIS, CHA)
 *   - General XP: XP del nivel general del jugador (ratio 1:1 con Stat XP)
 *   - Gold: Moneda del juego
 *
 * IMPORTANTE: Sistema Dual de XP
 *   Cuando se gana Stat XP, se suma AUTOMÁTICAMENTE a General XP
 *   Ejemplo: +50 INT XP → +50 a Inteligencia Y +50 a Nivel General
*/
public record QuestReward(
        Map<StatType, Integer> statXP,
        int generalXP,
        int gold
) {

    public QuestReward {
        if (statXP == null || statXP.isEmpty()) {
            statXP = Collections.emptyMap();
        } else {
            for (Map.Entry<StatType, Integer> entry : statXP.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("El tipo de stat no puede ser null");
                }
                if (entry.getValue() < 0) {
                    throw new IllegalArgumentException(
                            String.format("La XP de %s no puede ser negativa: %d",
                                    entry.getKey(), entry.getValue())
                    );
                }
                Map<StatType, Integer> copy = new EnumMap<>(StatType.class); // Usar .class
                copy.putAll(statXP);
                statXP = Collections.unmodifiableMap(copy);
            }
            statXP = Collections.unmodifiableMap(new EnumMap<>(statXP));
        }

        if (generalXP < 0) {
            throw new IllegalArgumentException(
                    String.format("La XP general no puede ser negativa: %d", generalXP)
            );
        }
        if (gold < 0) {
            throw new IllegalArgumentException(
                    String.format("El oro no puede ser negativo: %d", gold)
            );
        }
    }

    public static QuestReward empty() {
        return new QuestReward(Map.of(), 0, 0);
    }

    public static QuestReward ofGeneralXP(int xp) {
        return new QuestReward(Map.of(), xp, 0);
    }

    public static QuestReward ofGold(int gold) {
        return new QuestReward(Map.of(), 0, gold);
    }

    public static QuestReward ofSingleStat(StatType type, int xp) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }
        return new QuestReward(Map.of(type, xp), 0, 0);
    }

    public static QuestReward fromRank(QuestRank rank) {
        if (rank == null) {
            throw new IllegalArgumentException("El rango no puede ser null");
        }
        return new QuestReward(Map.of(), rank.getBaseXP(), rank.getBaseGold());
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return statXP.isEmpty() && generalXP == 0 && gold == 0;
    }

    public int getStatXP(StatType type) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }
        return statXP.getOrDefault(type, 0);
    }

    public int getTotalXP() {
        int statXPTotal = statXP.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        return statXPTotal + generalXP;
    }

    public boolean hasXP() {
        return !statXP.isEmpty() || generalXP > 0;
    }

    public boolean hasGold() {
        return gold > 0;
    }

    public static class Builder {
        private final Map<StatType, Integer> statXP = new EnumMap<>(StatType.class);
        private int generalXP = 0;
        private int gold = 0;

        public Builder addStatXP(StatType type, int xp) {
            if (type == null) {
                throw new IllegalArgumentException("El tipo de stat no puede ser null");
            }
            if (xp < 0) {
                throw new IllegalArgumentException("La XP no puede ser negativa");
            }
            statXP.merge(type, xp, Integer::sum);
            return this;
        }

        public Builder setGeneralXP(int xp) {
            if (xp < 0) {
                throw new IllegalArgumentException("La XP general no puede ser negativa");
            }
            this.generalXP = xp;
            return this;
        }

        public Builder addGeneralXP(int xp) {
            if (xp < 0) {
                throw new IllegalArgumentException("La XP general no puede ser negativa");
            }
            this.generalXP += xp;
            return this;
        }

        public Builder setGold(int gold) {
            if (gold < 0) {
                throw new IllegalArgumentException("El oro no puede ser negativo");
            }
            this.gold = gold;
            return this;
        }

        public Builder addGold(int gold) {
            if (gold < 0) {
                throw new IllegalArgumentException("El oro no puede ser negativo");
            }
            this.gold += gold;
            return this;
        }

        public QuestReward build() {
            return new QuestReward(statXP, generalXP, gold);
        }
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (!statXP.isEmpty()) {
            sb.append("📊 Stats: ");
            statXP.forEach((type, xp) ->
                    sb.append(String.format("%s +%d ", type.getIcon(), xp))
            );
            sb.append("| ");
        }

        if (generalXP > 0) {
            sb.append(String.format("⭐ +%d XP | ", generalXP));
        }

        if (gold > 0) {
            sb.append(String.format("💰 +%d G", gold));
        }

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return String.format("QuestReward[statXP=%s, generalXP=%d, gold=%d]",
                statXP, generalXP, gold);
    }
}