package com.lifeleveling.domain.debuff;

import com.lifeleveling.domain.player.StatType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DebuffTracker {

    private final List<Debuff> activeDebuffs;

    // --- CONTADORES INTERNOS (Para triggers automáticos) ---
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
    // GESTIÓN DE DEBUFFS (APLICAR / QUITAR)
    // ========================================================================================

    public void applyDebuff(Debuff debuff) {
        // Regla 12.1: Si ya tenemos el debuff, lo reemplazamos (reinicia duración)
        // Esto evita tener 3 iconos de "Fatiga" a la vez.
        removeDebuff(debuff.getType());
        activeDebuffs.add(debuff);
    }

    public void removeDebuff(DebuffType type) {
        activeDebuffs.removeIf(d -> d.getType() == type);
    }

    /**
     * Limpia debuffs expirados por tiempo (ej: Heaviness tras 24h).
     * Se debe llamar al iniciar sesión o al cambiar de día.
     */
    public void cleanExpiredDebuffs(Instant now) {
        activeDebuffs.removeIf(d -> d.isExpired(now));
    }

    public boolean hasDebuff(DebuffType type) {
        return activeDebuffs.stream().anyMatch(d -> d.getType() == type);
    }

    public List<Debuff> getActiveDebuffs() {
        return Collections.unmodifiableList(activeDebuffs);
    }

    // ========================================================================================
    // CÁLCULO DE EFECTOS (MATEMÁTICAS)
    // ========================================================================================

    /**
     * Calcula el multiplicador global de XP.
     * Ej: Si tienes FATIGUE (0.5), devuelves 0.5.
     * Si tuvieras dos debuffs multiplicadores, se multiplican entre sí (0.5 * 0.5 = 0.25).
     */
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

    /**
     * Calcula el multiplicador específico para un Stat.
     * Ej: CHAOS quita 20% de WIS. Devuelve 0.8 para WIS, 1.0 para el resto.
     * Fórmula: 1.0 - Suma de penalizaciones.
     */
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

        // Mínimo 0 (no queremos XP negativa, aunque sería divertido)
        return Math.max(0.0, 1.0 - totalPenalty);
    }

    /**
     * Verifica si el efecto de un item está anulado.
     * Ej: TACHYCARDIA anula "monster_energy".
     */
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

    /**
     * Calcula penalización pasiva de oro (ej: TRAPPED).
     * Se llamará al calcular inputs.
     */
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

    /**
     * Se llama cada Lunes a las 00:00.
     * Resetea contadores semanales y limpia Taquicardia.
     */
    public void resetWeeklyCounters() {
        this.monstersConsumedWeekly = 0;
        // Si hay taquicardia, se va el lunes
        removeDebuff(DebuffType.TACHYCARDIA);
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