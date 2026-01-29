package com.lifeleveling.domain.debuff;

import com.lifeleveling.domain.player.StatType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DebuffTracker {

    private final List<Debuff> activeDebuffs;

    // Contadores internos (Solo para lógica intra-semanal como Monsters)
    private int monstersConsumedWeekly;

    public DebuffTracker() {
        this.activeDebuffs = new ArrayList<>();
        this.monstersConsumedWeekly = 0;
    }

    // ========================================================================================
    // GESTIÓN DE DEBUFFS
    // ========================================================================================

    public void applyDebuff(Debuff debuff) {
        removeDebuff(debuff.getType()); // Evitar duplicados del mismo tipo
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

    public boolean canCureWithCaffeine() {
        return !hasDebuff(DebuffType.TACHYCARDIA);
    }

    public List<Debuff> getActiveDebuffs() {
        return Collections.unmodifiableList(activeDebuffs);
    }

    // ========================================================================================
    // CÁLCULO DE EFECTOS (Multiplicadores)
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

    // ========================================================================================
    // LÓGICA DE TRIGGERS (EL JUEZ)
    // ========================================================================================

    /**
     * Verifica triggers automáticos al final del día.
     * Llamado desde Player.updateState() usando datos del GateTracker.
     */
    public List<Debuff> checkDailyResetTriggers(int daysWithoutTidy, int consecutiveWorkDays, Instant now) {
        List<Debuff> newDebuffs = new ArrayList<>();

        // 1. CHAOS: 3 días sin ordenar
        if (daysWithoutTidy >= 3 && !hasDebuff(DebuffType.CHAOS)) {
            newDebuffs.add(Debuff.create(DebuffType.CHAOS, "Entorno desordenado (3+ días)", now));
        }

        // 2. BOREDOM: 7 días trabajando sin descanso
        if (consecutiveWorkDays >= 7 && !hasDebuff(DebuffType.BOREDOM)) {
            newDebuffs.add(Debuff.create(DebuffType.BOREDOM, "Rutina laboral excesiva (7 días)", now));
        }

        return newDebuffs;
    }

    /**
     * Verifica triggers inmediatos (Fatiga por mal sueño).
     * Llamado al completar la quest SLEEP.
     */
    public Optional<Debuff> checkSleepTrigger(double hoursSlept, Instant now) {
        if (hoursSlept < 6.0) {
            return Optional.of(Debuff.create(DebuffType.FATIGUE, "Sueño insuficiente (" + hoursSlept + "h)", now));
        }
        return Optional.empty();
    }

    /**
     * Verifica triggers por consumo de items (Hamburguesa, Monster).
     */
    public Optional<Debuff> checkItemConsumptionTrigger(String itemId, Instant now) {
        if ("hamburguesa_doble".equals(itemId)) {
            return Optional.of(Debuff.create(DebuffType.HEAVINESS, "Comida basura", now));
        }
        if ("monster_energy".equals(itemId)) {
            if (monstersConsumedWeekly >= 3) {
                return Optional.of(Debuff.createWithExpiration(
                        DebuffType.TACHYCARDIA,
                        "Exceso de cafeína (" + monstersConsumedWeekly + " esta semana)",
                        now,
                        now.plus(java.time.Duration.ofDays(7)) // Simplificado, idealmente "Next Monday"
                ));
            }
        }
        return Optional.empty();
    }

    // ========================================================================================
    // CONTADORES INTERNOS (Solo Weekly Monsters)
    // ========================================================================================

    public void recordMonsterConsumed() { this.monstersConsumedWeekly++; }
    public int getMonstersConsumedWeekly() { return monstersConsumedWeekly; }

    public void resetWeeklyCounters() {
        this.monstersConsumedWeekly = 0;
        removeDebuff(DebuffType.TACHYCARDIA);
    }

    // ========================================================================================
    // LÓGICA DE CURAS (ACTIONS)
    // ========================================================================================

    public void applyPerfectDayCure() {
        if (hasDebuff(DebuffType.BOREDOM)) removeDebuff(DebuffType.BOREDOM);
    }

    public String getActiveDebuffsDisplay() {
        if (activeDebuffs.isEmpty()) return "Ninguno ✨";
        Instant now = Instant.now();
        return activeDebuffs.stream()
                .map(d -> d.toDisplayString(now))
                .collect(Collectors.joining(", "));
    }
}