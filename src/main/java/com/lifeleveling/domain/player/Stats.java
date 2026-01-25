package com.lifeleveling.domain.player;

import java.util.EnumMap;
import java.util.Map;

/*
 * Value Object inmutable que representa los 5 atributos del jugador.
 *
 * Stats agrupa los cinco atributos fundamentales
 * STRENGTH, INTELLECT, WISDOM, DISCIPLINE y CHARISMA
 * Cada uno tiene su propio nivel y XP
 *
 * Sincronización con Nivel General:
 * La XP ganada en cualquier stat se suma TAMBIÉN al Nivel General del jugador
 */
public record Stats(
        Stat strength,
        Stat intellect,
        Stat wisdom,
        Stat discipline,
        Stat charisma
) {

    public Stats {
        validateStat(strength, StatType.STRENGTH);
        validateStat(intellect, StatType.INTELLECT);
        validateStat(wisdom, StatType.WISDOM);
        validateStat(discipline, StatType.DISCIPLINE);
        validateStat(charisma, StatType.CHARISMA);
    }

    private static void validateStat(Stat stat, StatType expectedType) {
        if (stat == null) {
            throw new IllegalArgumentException(
                    String.format("El stat %s no puede ser null", expectedType.getDisplayName())
            );
        }

        if (stat.type() != expectedType) {
            throw new IllegalArgumentException(
                    String.format("El stat %s tiene tipo incorrecto. Esperado: %s, Actual: %s",
                            expectedType.getDisplayName(), expectedType, stat.type())
            );
        }
    }

    public static Stats initial() {
        return new Stats(
                Stat.initial(StatType.STRENGTH),
                Stat.initial(StatType.INTELLECT),
                Stat.initial(StatType.WISDOM),
                Stat.initial(StatType.DISCIPLINE),
                Stat.initial(StatType.CHARISMA)
        );
    }

    public static Stats withLevels(int strLevel, int intLevel, int wisLevel,
                                   int disLevel, int chaLevel) {
        return new Stats(
                Stat.atLevel(StatType.STRENGTH, strLevel),
                Stat.atLevel(StatType.INTELLECT, intLevel),
                Stat.atLevel(StatType.WISDOM, wisLevel),
                Stat.atLevel(StatType.DISCIPLINE, disLevel),
                Stat.atLevel(StatType.CHARISMA, chaLevel)
        );
    }

    public static Stats maxed() {
        return new Stats(
                Stat.maxed(StatType.STRENGTH),
                Stat.maxed(StatType.INTELLECT),
                Stat.maxed(StatType.WISDOM),
                Stat.maxed(StatType.DISCIPLINE),
                Stat.maxed(StatType.CHARISMA)
        );
    }

    public Stat getStat(StatType type) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }

        return switch (type) {
            case STRENGTH -> strength;
            case INTELLECT -> intellect;
            case WISDOM -> wisdom;
            case DISCIPLINE -> discipline;
            case CHARISMA -> charisma;
        };
    }

    public int getLevel(StatType type) {
        return getStat(type).currentLevel();
    }

    public int getCurrentXP(StatType type) {
        return getStat(type).currentXP();
    }

    public boolean hasMastery(StatType type) {
        return getStat(type).hasMastery();
    }

    public boolean isMaxed(StatType type) {
        return getStat(type).isMaxed();
    }

    public int getTotalLevel() {
        return strength.currentLevel()
                + intellect.currentLevel()
                + wisdom.currentLevel()
                + discipline.currentLevel()
                + charisma.currentLevel();
    }

    public double getAverageLevel() {
        return getTotalLevel() / 5.0;
    }

    public int getMasteryCount() {
        int count = 0;
        for (StatType type : StatType.values()) {
            if (hasMastery(type)) {
                count++;
            }
        }
        return count;
    }

    public int getMaxedCount() {
        int count = 0;
        for (StatType type : StatType.values()) {
            if (isMaxed(type)) {
                count++;
            }
        }
        return count;
    }

    public boolean areAllMaxed() {
        return getMaxedCount() == 5;
    }

    // XP total acumulada (máximo: 2,475,000 si todos están maxeados)
    public int getTotalXPAccumulated() {
        return strength.getTotalXPAccumulated()
                + intellect.getTotalXPAccumulated()
                + wisdom.getTotalXPAccumulated()
                + discipline.getTotalXPAccumulated()
                + charisma.getTotalXPAccumulated();
    }

    public StatType getDominantStat() {
        StatType dominant = StatType.STRENGTH;
        int maxLevel = strength.currentLevel();

        for (StatType type : StatType.values()) {
            int level = getLevel(type);
            if (level > maxLevel) {
                maxLevel = level;
                dominant = type;
            }
        }

        return dominant;
    }

    public Stats addXP(StatType type, int xpGained) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }

        Stat updatedStat = getStat(type).addXP(xpGained);

        return switch (type) {
            case STRENGTH -> new Stats(updatedStat, intellect, wisdom, discipline, charisma);
            case INTELLECT -> new Stats(strength, updatedStat, wisdom, discipline, charisma);
            case WISDOM -> new Stats(strength, intellect, updatedStat, discipline, charisma);
            case DISCIPLINE -> new Stats(strength, intellect, wisdom, updatedStat, charisma);
            case CHARISMA -> new Stats(strength, intellect, wisdom, discipline, updatedStat);
        };
    }

    public Stats forceLevel(StatType type, int levels) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de stat no puede ser null");
        }

        Stat updatedStat = getStat(type).forceLevel(levels);

        return switch (type) {
            case STRENGTH -> new Stats(updatedStat, intellect, wisdom, discipline, charisma);
            case INTELLECT -> new Stats(strength, updatedStat, wisdom, discipline, charisma);
            case WISDOM -> new Stats(strength, intellect, updatedStat, discipline, charisma);
            case DISCIPLINE -> new Stats(strength, intellect, wisdom, updatedStat, charisma);
            case CHARISMA -> new Stats(strength, intellect, wisdom, discipline, updatedStat);
        };
    }

    public Stats reset() {
        return initial();
    }

    public Map<StatType, String> toDisplayMap() {
        Map<StatType, String> displayMap = new EnumMap<>(StatType.class);

        for (StatType type : StatType.values()) {
            displayMap.put(type, getStat(type).toDisplayString());
        }

        return displayMap;
    }


     //Ejemplo: "💪 Lvl 23 | 🧠 Lvl 47 | 🦉 Lvl 29 | 🛡️ Lvl 34 | 🗣️ Lvl 18"
    public String toCompactString() {
        return String.format("%s | %s | %s | %s | %s",
                strength.toCompactString(),
                intellect.toCompactString(),
                wisdom.toCompactString(),
                discipline.toCompactString(),
                charisma.toCompactString()
        );
    }

    public String getSummary() {
        return String.format(
                "📊 Stats Summary: Total Lvl=%d, Avg=%.1f, Mastery=%d/5, Maxed=%d/5",
                getTotalLevel(),
                getAverageLevel(),
                getMasteryCount(),
                getMaxedCount()
        );
    }

    @Override
    public String toString() {
        return String.format(
                "Stats[STR=%s, INT=%s, WIS=%s, DIS=%s, CHA=%s]",
                strength.toCompactString(),
                intellect.toCompactString(),
                wisdom.toCompactString(),
                discipline.toCompactString(),
                charisma.toCompactString()
        );
    }
}