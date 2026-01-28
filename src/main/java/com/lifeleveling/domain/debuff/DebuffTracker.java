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
    // LÓGICA DE TRIGGERS (EL JUEZ)
    // ========================================================================================

    /**
     * Evalúa el sueño registrado y devuelve un Debuff si corresponde.
     * Regla: Si duermes < 6h -> FATIGUE.
     */
    public Optional<Debuff> checkSleepTrigger(double hoursSlept, Instant now) {
        if (hoursSlept < 6.0) {
            return Optional.of(Debuff.create(
                    DebuffType.FATIGUE,
                    "Sueño insuficiente (" + hoursSlept + "h)",
                    now
            ));
        }
        return Optional.empty();
    }

    /**
     * Evalúa el consumo de items peligrosos.
     * Regla 1: Hamburguesa -> HEAVINESS.
     * Regla 2: 3º Monster de la semana -> TACHYCARDIA.
     */
    public Optional<Debuff> checkItemConsumptionTrigger(String itemId, Instant now) {
        // Trigger Hamburguesa
        if ("hamburguesa_doble".equals(itemId)) {
            return Optional.of(Debuff.create(
                    DebuffType.HEAVINESS,
                    "Comida basura",
                    now
            ));
        }

        // Trigger Monster (Taquicardia)
        if ("monster_energy".equals(itemId)) {
            // El contador ya se incrementó en recordMonsterConsumed() antes de llamar a esto
            if (monstersConsumedWeekly >= 3) {
                // Taquicardia dura hasta el próximo Lunes
                // Calculamos fecha de expiración (Lunes 00:00)
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

    /**
     * Evalúa reglas al cierre del día (Daily Reset).
     * Regla 1: 3 días sin TIDY -> CHAOS.
     * Regla 2: 7 días trabajando seguido -> BOREDOM.
     */
    public List<Debuff> checkDailyResetTriggers(Instant now) {
        List<Debuff> newDebuffs = new ArrayList<>();

        // Trigger CHAOS
        if (daysWithoutTidy >= 3 && !hasDebuff(DebuffType.CHAOS)) {
            newDebuffs.add(Debuff.create(
                    DebuffType.CHAOS,
                    "Entorno desordenado (" + daysWithoutTidy + " días)",
                    now
            ));
        }

        // Trigger BOREDOM
        if (consecutiveWorkDays >= 7 && !hasDebuff(DebuffType.BOREDOM)) {
            newDebuffs.add(Debuff.create(
                    DebuffType.BOREDOM,
                    "Rutina laboral excesiva (" + consecutiveWorkDays + " días)",
                    now
            ));
        }

        return newDebuffs;
    }

    /**
     * Evalúa uso de Redes Sociales.
     * Regla: TRAPPED inmediato (no se guarda, se aplica penalización de oro al momento).
     * Nota: Este método retorna el debuff para notificar, pero TRAPPED suele ser instantáneo.
     */
    public Optional<Debuff> checkSocialMediaTrigger(double hoursWasted, Instant now) {
        if (hoursWasted > 0) {
            // TRAPPED tiene duración ZERO, es solo para aplicar el efecto inmediato de oro
            return Optional.of(Debuff.create(
                    DebuffType.TRAPPED,
                    "Doomscrolling (" + hoursWasted + "h)",
                    now
            ));
        }
        return Optional.empty();
    }

    // Helper simple para calcular el lunes (puedes moverlo a un TimeService más tarde)
    private Instant calculateNextMonday(Instant now) {
        // Implementación simplificada. En un proyecto real usaríamos ZonedDateTime.
        // Aquí asumimos +7 días como fallback o usamos java.time si está disponible.
        return now.plus(java.time.Duration.ofDays(7));
    }

    // ========================================================================================
    // LÓGICA DE CURAS (LA REDENCIÓN)
    // ========================================================================================

    /**
     * Evalúa si el sueño registrado cura la Fatiga.
     * Regla: Dormir >= 7h elimina FATIGUE.
     */
    public boolean checkSleepCure(double hoursSlept) {
        if (hoursSlept >= 7.0 && hasDebuff(DebuffType.FATIGUE)) {
            removeDebuff(DebuffType.FATIGUE);
            return true; // Indica que hubo una cura
        }
        return false;
    }

    /**
     * Evalúa si una tarea completada cura un debuff.
     * Regla: Completar TIDY elimina CHAOS.
     */
    public boolean checkTaskCure(String taskId) {
        if ("TIDY".equalsIgnoreCase(taskId)) {
            // Al hacer Tidy, reseteamos el contador de suciedad
            resetTidyStreak();

            if (hasDebuff(DebuffType.CHAOS)) {
                removeDebuff(DebuffType.CHAOS);
                return true;
            }
        }
        return false;
    }

    /**
     * Evalúa si el consumo de un item cura algo.
     * Gestiona la lógica compleja de Monster vs Taquicardia.
     */
    public Optional<DebuffType> checkItemCure(String itemId) {
        // 1. Monster Energy -> Cura FATIGUE
        if ("monster_energy".equals(itemId)) {
            // CRÍTICO: Si tienes Taquicardia, el Monster NO funciona (y encima te daña más)
            if (hasDebuff(DebuffType.TACHYCARDIA)) {
                return Optional.empty();
            }

            if (hasDebuff(DebuffType.FATIGUE)) {
                removeDebuff(DebuffType.FATIGUE);
                return Optional.of(DebuffType.FATIGUE);
            }
        }

        // 2. Almax -> Cura HEAVINESS
        if ("almax".equals(itemId)) {
            if (hasDebuff(DebuffType.HEAVINESS)) {
                removeDebuff(DebuffType.HEAVINESS);
                return Optional.of(DebuffType.HEAVINESS);
            }
        }

        // 3. Ocio (Juegos/Cine) -> Cura BOREDOM
        if ("videojuego".equals(itemId) || "entrada_cine".equals(itemId)) {
            // Al divertirse, se rompe la racha de trabajo aburrido
            resetWorkStreak();

            if (hasDebuff(DebuffType.BOREDOM)) {
                removeDebuff(DebuffType.BOREDOM);
                return Optional.of(DebuffType.BOREDOM);
            }
        }

        // 4. Inyección de Adrenalina -> Cura BURNOUT (Se gestiona en Player, pero aquí podríamos limpiar efectos secundarios)

        return Optional.empty();
    }

    /**
     * Llamado cuando ocurre un "Perfect Day".
     * Regla: Limpia BOREDOM instantáneamente.
     */
    public void applyPerfectDayCure() {
        if (hasDebuff(DebuffType.BOREDOM)) {
            removeDebuff(DebuffType.BOREDOM);
        }
        // Perfect Day no cura enfermedades físicas (Heaviness/Tachycardia), solo mentales.
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