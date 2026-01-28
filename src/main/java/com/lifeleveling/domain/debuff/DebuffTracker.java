package com.lifeleveling.domain.debuff;

import com.lifeleveling.domain.player.StatType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DebuffTracker {

    private final List<Debuff> activeDebuffs;

    // --- CONTADORES INTERNOS ---
    private int monstersConsumedWeekly;
    private int daysWithoutTidy;
    private int consecutiveWorkDays;

    public DebuffTracker() {
        this.activeDebuffs = new ArrayList<>();
        this.monstersConsumedWeekly = 0;
        this.daysWithoutTidy = 0;
        this.consecutiveWorkDays = 0;
    }

    // ========================================================================================
    // GESTIÓN DE DEBUFFS
    // ========================================================================================

    public void applyDebuff(Debuff debuff) {
        removeDebuff(debuff.getType());
        activeDebuffs.add(debuff);
    }

    public void removeDebuff(DebuffType type) {
        activeDebuffs.removeIf(d -> d.getType() == type);
    }

    public void cleanExpiredDebuffs(Instant now) {
        activeDebuffs.removeIf(d -> d.isExpired(now));
    }

    public boolean hasDebuff(DebuffType type) {
        return activeDebuffs.stream().anyMatch(d -> d.getType() == type);
    }

    /**
     * [NUEVO] Helper para saber si la cafeína es efectiva.
     */
    public boolean canCureWithCaffeine() {
        return !hasDebuff(DebuffType.TACHYCARDIA);
    }

    public List<Debuff> getActiveDebuffs() {
        return Collections.unmodifiableList(activeDebuffs);
    }

    // ========================================================================================
    // CÁLCULO DE EFECTOS
    // ========================================================================================

    public double getGlobalXPMultiplier() {
        double multiplier = 1.0;
        for (Debuff debuff : activeDebuffs) {
            for (DebuffEffect effect : debuff.getType().getEffects()) {
                if (effect.type() == DebuffEffect.EffectType.GLOBAL_XP_MULTIPLIER) {
                    multiplier *= effect.value();
                }
            }
        }
        return multiplier;
    }

    public double getStatXPMultiplier(StatType stat) {
        double totalPenalty = 0.0;
        for (Debuff debuff : activeDebuffs) {
            for (DebuffEffect effect : debuff.getType().getEffects()) {
                if (effect.type() == DebuffEffect.EffectType.STAT_XP_PENALTY
                        && effect.getTargetStat().isPresent()
                        && effect.getTargetStat().get() == stat) {
                    totalPenalty += effect.value();
                }
            }
        }
        return Math.max(0.0, 1.0 - totalPenalty);
    }

    public boolean isItemEffectDisabled(String itemId) {
        for (Debuff debuff : activeDebuffs) {
            for (DebuffEffect effect : debuff.getType().getEffects()) {
                if (effect.type() == DebuffEffect.EffectType.DISABLE_ITEM_EFFECT
                        && effect.getTargetItemId().isPresent()
                        && effect.getTargetItemId().get().equals(itemId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getGoldPenalty() {
        int totalPenalty = 0;
        for (Debuff debuff : activeDebuffs) {
            for (DebuffEffect effect : debuff.getType().getEffects()) {
                if (effect.type() == DebuffEffect.EffectType.GOLD_PENALTY_PER_UNIT) {
                    totalPenalty += (int) effect.value();
                }
            }
        }
        return totalPenalty;
    }

    // ========================================================================================
    // GESTIÓN DE CONTADORES (TRIGGERS)
    // ========================================================================================

    public void recordMonsterConsumed() {
        this.monstersConsumedWeekly++;
    }

    public int getMonstersConsumedWeekly() {
        return monstersConsumedWeekly;
    }

    public void incrementDaysWithoutTidy() {
        this.daysWithoutTidy++;
    }

    public void resetTidyStreak() {
        this.daysWithoutTidy = 0;
    }

    public int getDaysWithoutTidy() {
        return daysWithoutTidy;
    }

    public void incrementWorkStreak() {
        this.consecutiveWorkDays++;
    }

    public void resetWorkStreak() {
        this.consecutiveWorkDays = 0;
    }

    public int getConsecutiveWorkDays() {
        return consecutiveWorkDays;
    }

    public void resetWeeklyCounters() {
        this.monstersConsumedWeekly = 0;
        removeDebuff(DebuffType.TACHYCARDIA);
    }

    // ========================================================================================
    // LÓGICA DE TRIGGERS (EL JUEZ)
    // ========================================================================================

    public Optional<Debuff> checkSleepTrigger(double hoursSlept, Instant now) {
        if (hoursSlept < 6.0) {
            return Optional.of(Debuff.create(DebuffType.FATIGUE, "Sueño insuficiente (" + hoursSlept + "h)", now));
        }
        return Optional.empty();
    }

    public Optional<Debuff> checkItemConsumptionTrigger(String itemId, Instant now) {
        if ("hamburguesa_doble".equals(itemId)) {
            return Optional.of(Debuff.create(DebuffType.HEAVINESS, "Comida basura", now));
        }
        if ("monster_energy".equals(itemId)) {
            if (monstersConsumedWeekly >= 3) {
                Instant nextMonday = calculateNextMonday(now);
                return Optional.of(Debuff.createWithExpiration(
                        DebuffType.TACHYCARDIA,
                        "Exceso de cafeína (" + monstersConsumedWeekly + " esta semana)",
                        now,
                        nextMonday
                ));
            }
        }
        return Optional.empty();
    }

    public List<Debuff> checkDailyResetTriggers(Instant now) {
        List<Debuff> newDebuffs = new ArrayList<>();
        if (daysWithoutTidy >= 3 && !hasDebuff(DebuffType.CHAOS)) {
            newDebuffs.add(Debuff.create(DebuffType.CHAOS, "Entorno desordenado", now));
        }
        if (consecutiveWorkDays >= 7 && !hasDebuff(DebuffType.BOREDOM)) {
            newDebuffs.add(Debuff.create(DebuffType.BOREDOM, "Rutina laboral excesiva", now));
        }
        return newDebuffs;
    }

    public Optional<Debuff> checkSocialMediaTrigger(double hoursWasted, Instant now) {
        if (hoursWasted > 0) {
            return Optional.of(Debuff.create(DebuffType.TRAPPED, "Doomscrolling (" + hoursWasted + "h)", now));
        }
        return Optional.empty();
    }

    private Instant calculateNextMonday(Instant now) {
        return now.plus(java.time.Duration.ofDays(7)); // Simplificado
    }

    // ========================================================================================
    // LÓGICA DE CURAS (ACTIONS)
    // ========================================================================================

    public boolean checkSleepCure(double hoursSlept) {
        if (hoursSlept >= 7.0 && hasDebuff(DebuffType.FATIGUE)) {
            removeDebuff(DebuffType.FATIGUE);
            return true;
        }
        return false;
    }

    public boolean checkTaskCure(String taskId) {
        if ("TIDY".equalsIgnoreCase(taskId)) {
            resetTidyStreak();
            if (hasDebuff(DebuffType.CHAOS)) {
                removeDebuff(DebuffType.CHAOS);
                return true;
            }
        }
        return false;
    }

    // *Nota: La lógica de cura por items se ha movido al Player (consumeItem) para mejor orquestación.

    public void applyPerfectDayCure() {
        if (hasDebuff(DebuffType.BOREDOM)) {
            removeDebuff(DebuffType.BOREDOM);
        }
    }

    // ========================================================================================
    // UI HELPER
    // ========================================================================================

    public String getActiveDebuffsDisplay() {
        if (activeDebuffs.isEmpty()) return "Ninguno ✨";
        return activeDebuffs.stream()
                .map(d -> d.getType().toDisplayString())
                .collect(Collectors.joining(", "));
    }
}