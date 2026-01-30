package com.lifeleveling.domain.debuff;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DebuffTracker: Gestiona los estados negativos del jugador.
 * * Responsabilidades:
 * 1. Almacenar debuffs activos (Fatiga, Caos, etc.).
 * 2. Trackear contadores acumulativos para triggers (ej: Monsters semanales).
 * 3. Evaluar condiciones diarias para aplicar nuevos castigos.
 */
public class DebuffTracker {

    private final List<Debuff> activeDebuffs;

    // --- CONTADORES SEMANALES (FASE 5) ---
    private int monstersConsumedWeekly;
    private LocalDate currentWeekStart; // Controla cuándo empezó la semana actual (Lunes)

    public DebuffTracker() {
        this.activeDebuffs = new ArrayList<>();
        this.monstersConsumedWeekly = 0;
        // Inicializamos la semana al Lunes actual o anterior
        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // ========================================================================================
    // LÓGICA DE MONSTER / TAQUICARDIA (FASE 5.1)
    // ========================================================================================

    /**
     * Registra el consumo de un Monster.
     * Detecta automáticamente si es una nueva semana para resetear contadores.
     */
    public void recordMonsterConsumption(LocalDate today) {
        // 1. Calcular el Lunes de la fecha dada
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 2. Detectar cambio de semana (Si ha pasado un lunes desde la última vez)
        if (!thisMonday.equals(currentWeekStart)) {
            System.out.println("📅 Nueva semana detectada en DebuffTracker. Reseteando cafeína...");
            resetWeeklyCounters(thisMonday);
        }

        // 3. Si YA tienes Taquicardia, beber otro no hace nada extra (el castigo ya está activo)
        if (hasDebuff(DebuffType.TACHYCARDIA)) {
            return;
        }

        // 4. Incrementar contador
        monstersConsumedWeekly++;
        // System.out.println("🥤 Monster consumido. Total semanal: " + monstersConsumedWeekly);

        // 5. Verificar Trigger (3+)
        if (monstersConsumedWeekly >= 3) {
            applyTachycardia(today);
        }
    }

    private void applyTachycardia(LocalDate today) {
        // La Taquicardia dura hasta el próximo Lunes
        LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        java.time.Instant expiryTimestamp = nextMonday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        java.time.Instant now = java.time.Instant.now();

        Debuff tachycardia = Debuff.createWithExpiration(
                DebuffType.TACHYCARDIA,
                "Exceso de cafeína (" + monstersConsumedWeekly + "º Monster)",
                now,
                expiryTimestamp
        );

        applyDebuff(tachycardia);
        System.out.println("💓 TAQUICARDIA ACTIVADA: Has bebido 3 Monsters esta semana.");
    }

    /**
     * Resetea contadores semanales. Se llama auto o manualmente.
     */
    public void resetWeeklyCounters(LocalDate newWeekStart) {
        this.monstersConsumedWeekly = 0;
        this.currentWeekStart = newWeekStart;

        // Al cambiar de semana, la Taquicardia se cura sola (si seguía activa)
        if (hasDebuff(DebuffType.TACHYCARDIA)) {
            removeDebuff(DebuffType.TACHYCARDIA);
            System.out.println("💓 Taquicardia curada (Nueva Semana).");
        }
    }

    public void resetWeeklyCounters() {
        resetWeeklyCounters(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
    }

    public int getMonstersConsumedWeekly() { return monstersConsumedWeekly; }

    // ========================================================================================
    // TRIGGERS DIARIOS & DE CONSUMO
    // ========================================================================================

    /**
     * Evalúa triggers automáticos al cerrar el día (Daily Reset).
     * @param daysWithoutTidy Días consecutivos sin hacer la cama (Quest TIDY).
     * @param consecutiveWorkDays Días consecutivos trabajando (Quest CODE).
     */
    public List<Debuff> checkDailyResetTriggers(int daysWithoutTidy, int consecutiveWorkDays, java.time.Instant now) {
        List<Debuff> newDebuffs = new ArrayList<>();

        // 1. Lógica CHAOS (Desorden)
        if (daysWithoutTidy == 2) {
            System.out.println("⚠️ WARNING: Tu habitación está sucia. Mañana recibirás el debuff CAOS.");
        } else if (daysWithoutTidy >= 3 && !hasDebuff(DebuffType.CHAOS)) {
            newDebuffs.add(Debuff.create(DebuffType.CHAOS, "Entorno desordenado (3+ días)", now));
        }

        // 2. Lógica BOREDOM (Rutina laboral excesiva)
        if (consecutiveWorkDays >= 7 && !hasDebuff(DebuffType.BOREDOM)) {
            newDebuffs.add(Debuff.create(DebuffType.BOREDOM, "Rutina laboral excesiva (7 días)", now));
        }

        return newDebuffs;
    }

    /**
     * Evalúa triggers inmediatos por consumo de items (excepto Monster, que va por su propio método).
     */
    public Optional<Debuff> checkItemConsumptionTrigger(String itemId, java.time.Instant now) {
        // El Monster ya se gestiona en recordMonsterConsumption(), así que lo ignoramos aquí
        // para evitar duplicidades, o lo dejamos solo si queremos aplicar efectos extra inmediatos.

        if ("consumable_burger".equals(itemId)) {
            return Optional.of(Debuff.create(DebuffType.HEAVINESS, "Comida basura", now));
        }

        return Optional.empty();
    }

    /**
     * Verifica si el sueño fue insuficiente (< 6h) para aplicar FATIGA.
     */
    public Optional<Debuff> checkSleepTrigger(double hoursSlept, java.time.Instant now) {
        if (hoursSlept < 6.0) {
            // Duración indefinida (hasta que duermas bien)
            return Optional.of(Debuff.create(DebuffType.FATIGUE, "Sueño insuficiente (" + hoursSlept + "h)", now));
        }
        // Si duerme bien, curamos la fatiga existente
        if (hoursSlept >= 7.0 && hasDebuff(DebuffType.FATIGUE)) {
            removeDebuff(DebuffType.FATIGUE);
            System.out.println("🛌 Has descansado bien. Fatiga eliminada.");
        }
        return Optional.empty();
    }

    // ========================================================================================
    // GESTIÓN DE DEBUFFS ACTIVOS
    // ========================================================================================

    public void applyDebuff(Debuff debuff) {
        // Regla: No duplicar el mismo tipo de debuff. Reemplazamos el viejo por el nuevo.
        removeDebuff(debuff.getType());
        activeDebuffs.add(debuff);
    }

    public void removeDebuff(DebuffType type) {
        activeDebuffs.removeIf(d -> d.getType() == type);
    }

    public void cleanExpiredDebuffs(java.time.Instant now) {
        activeDebuffs.removeIf(d -> d.isExpired(now));
    }

    public boolean hasDebuff(DebuffType type) {
        return activeDebuffs.stream().anyMatch(d -> d.getType() == type);
    }

    public boolean canCureWithCaffeine() {
        // Si tienes Taquicardia, la cafeína no te quita la Fatiga (corazón a mil pero mente cansada)
        return !hasDebuff(DebuffType.TACHYCARDIA);
    }

    public void applyPerfectDayCure() {
        // Un día perfecto cura el aburrimiento y el estrés
        if (hasDebuff(DebuffType.BOREDOM)) removeDebuff(DebuffType.BOREDOM);
        if (hasDebuff(DebuffType.FATIGUE)) removeDebuff(DebuffType.FATIGUE);
        // Nota: CAOS no se cura con Perfect Day, requiere TIDY explícito.
        // Nota: TAQUICARDIA no se cura con Perfect Day, requiere tiempo.
    }

    // ========================================================================================
    // GETTERS & DISPLAY
    // ========================================================================================

    public List<Debuff> getActiveDebuffs() {
        return Collections.unmodifiableList(activeDebuffs);
    }

    public double getGlobalXPMultiplier() {
        return activeDebuffs.stream()
                .flatMap(d -> d.getType().getEffects().stream())
                .filter(e -> e.type() == DebuffEffect.EffectType.GLOBAL_XP_MULTIPLIER)
                .mapToDouble(DebuffEffect::value)
                .min().orElse(1.0); // Usamos el peor multiplicador si hay varios
    }

    public double getStatXPMultiplier(com.lifeleveling.domain.player.StatType stat) {

        double penalty = activeDebuffs.stream()
                .flatMap(d -> d.getType().getEffects().stream())
                // CORRECCIÓN AQUÍ: Usamos getTargetStat() que devuelve Optional
                .filter(e -> e.type() == DebuffEffect.EffectType.STAT_XP_PENALTY
                        && e.getTargetStat().map(s -> s == stat).orElse(false))
                .mapToDouble(DebuffEffect::value)
                .sum();

        return Math.max(0.0, 1.0 - penalty);
    }

    public String getActiveDebuffsDisplay() {
        if (activeDebuffs.isEmpty()) return "Ninguno ✨";
        java.time.Instant now = java.time.Instant.now();
        return activeDebuffs.stream()
                .map(d -> d.toDisplayString(now))
                .collect(Collectors.joining(", "));
    }
}