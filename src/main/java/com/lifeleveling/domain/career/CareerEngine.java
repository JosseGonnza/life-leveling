package com.lifeleveling.domain.career;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Agregado que gestiona todas las sesiones de código del jugador.
 *
 * El CareerEngine es el "cerebro" del sistema de programación:
 *   - Registra sesiones individuales (con cálculo de Flow por sesión)
 *   - Acumula totales diarios para HP y XP
 *   - Trackea horas totales históricas (para títulos como "Code Monkey")
 *   - Controla el límite diario de 12h
 *
 * Según la Biblia (Cap 1.2 §4):
 *   - Multi-Entry: Varias sesiones al día se suman
 *   - Flow Bonus: Se evalúa POR SESIÓN, no al final del día
 */
public class CareerEngine {

    // Sesiones del día actual
    private final List<CodeSession> todaySessions;
    private LocalDate currentDate;

    // Acumuladores del día
    private double todayTotalHours;
    private int todayIntellectXP;
    private int todayDisciplineXP;
    private int todayWisdomXP;
    private int todayHPCost;
    private int todayFlowCount;

    // Histórico global (para títulos y logros)
    private double totalCareerHours;
    private int totalFlowSessions;

    // ========================================================================================
    // CONSTRUCTORES
    // ========================================================================================

    public CareerEngine() {
        this.todaySessions = new ArrayList<>();
        this.currentDate = LocalDate.now();
        resetDailyCounters();
        this.totalCareerHours = 0.0;
        this.totalFlowSessions = 0;
    }

    /**
     * Constructor para restaurar desde persistencia.
     */
    public CareerEngine(double totalCareerHours, int totalFlowSessions) {
        this();
        this.totalCareerHours = totalCareerHours;
        this.totalFlowSessions = totalFlowSessions;
    }

    // ========================================================================================
    // CORE: REGISTRO DE SESIONES
    // ========================================================================================

    /**
     * Registra una nueva sesión de código.
     *
     * Este método es llamado desde Player cuando el usuario introduce horas.
     * Calcula automáticamente XP, HP cost y si alcanzó Flow.
     *
     * @param hours Horas trabajadas en esta sesión
     * @return La sesión creada con sus recompensas
     * @throws IllegalArgumentException si las horas son inválidas o exceden el límite diario
     */
    public CodeSession registerSession(double hours) {
        return registerSession(hours, Instant.now());
    }

    /**
     * Registra una sesión con timestamp específico (para tests/persistencia).
     */
    public CodeSession registerSession(double hours, Instant registeredAt) {
        // 1. Verificar cambio de día (auto-reset)
        checkAndResetIfNewDay();

        // 2. Validar límite diario
        validateDailyLimit(hours);

        // 3. Crear la sesión (calcula XP y Flow internamente)
        CodeSession session = CodeSession.register(hours, registeredAt);

        // 4. Registrar en el día
        todaySessions.add(session);
        updateDailyTotals(session);

        // 5. Actualizar histórico global
        totalCareerHours += hours;
        if (session.isFlowAchieved()) {
            totalFlowSessions++;
        }

        return session;
    }

    private void validateDailyLimit(double newHours) {
        double potentialTotal = todayTotalHours + newHours;

        if (potentialTotal > CodeSessionConstants.MAX_DAILY_HOURS) {
            double available = CodeSessionConstants.MAX_DAILY_HOURS - todayTotalHours;
            throw new IllegalArgumentException(
                    String.format("Excedes el límite diario de %.1fh. Disponible: %.1fh",
                            CodeSessionConstants.MAX_DAILY_HOURS, Math.max(0, available))
            );
        }
    }

    private void updateDailyTotals(CodeSession session) {
        CareerReward reward = session.getReward();

        todayTotalHours += session.getHours();
        todayIntellectXP += reward.intellectXP();
        todayDisciplineXP += reward.disciplineXP();
        todayWisdomXP += reward.wisdomXP();
        todayHPCost += reward.hpCost();

        if (session.isFlowAchieved()) {
            todayFlowCount++;
        }
    }

    // ========================================================================================
    // RESET DIARIO
    // ========================================================================================

    /**
     * Verifica si cambió el día y resetea los contadores si es necesario.
     * Se llama automáticamente al registrar una sesión.
     */
    public void checkAndResetIfNewDay() {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            resetDailyCounters();
            currentDate = today;
        }
    }

    private void resetDailyCounters() {
        todaySessions.clear();
        todayTotalHours = 0.0;
        todayIntellectXP = 0;
        todayDisciplineXP = 0;
        todayWisdomXP = 0;
        todayHPCost = 0;
        todayFlowCount = 0;
    }

    /**
     * Reset manual (útil para tests o cambio forzado de día).
     */
    public void forceReset() {
        resetDailyCounters();
        currentDate = LocalDate.now();
    }

    // ========================================================================================
    // CONSULTAS DEL DÍA
    // ========================================================================================

    /**
     * Obtiene el resumen del día actual.
     */
    public CareerDaySummary getDaySummary() {
        checkAndResetIfNewDay();

        return new CareerDaySummary(
                todayTotalHours,
                todayIntellectXP,
                todayDisciplineXP,
                todayWisdomXP,
                todayHPCost,
                todaySessions.size(),
                todayFlowCount
        );
    }

    /**
     * Obtiene las sesiones del día actual (vista inmutable).
     */
    public List<CodeSession> getTodaySessions() {
        checkAndResetIfNewDay();
        return Collections.unmodifiableList(todaySessions);
    }

    /**
     * ¿Se ha registrado al menos una sesión hoy?
     * Útil para marcar la DailyQuest CODE como completada.
     */
    public boolean hasActivityToday() {
        checkAndResetIfNewDay();
        return !todaySessions.isEmpty();
    }

    /**
     * Horas disponibles hoy antes de alcanzar el límite.
     */
    public double getAvailableHoursToday() {
        checkAndResetIfNewDay();
        return Math.max(0, CodeSessionConstants.MAX_DAILY_HOURS - todayTotalHours);
    }

    /**
     * ¿Se alcanzó el límite diario?
     */
    public boolean isDailyLimitReached() {
        return getAvailableHoursToday() < CodeSessionConstants.MIN_SESSION_HOURS;
    }

    // ========================================================================================
    // CONSULTAS HISTÓRICAS (Para Títulos y Logros)
    // ========================================================================================

    /**
     * Horas totales de código en toda la carrera.
     * Usado para títulos: "Code Monkey" (100h), "10x Developer" (500h), "Arquitecto" (1000h)
     */
    public double getTotalCareerHours() {
        return totalCareerHours;
    }

    /**
     * Total de sesiones que alcanzaron Flow.
     * Usado para título: "Deep Worker" (50 sesiones Flow)
     */
    public int getTotalFlowSessions() {
        return totalFlowSessions;
    }

    /**
     * Setter para restaurar desde persistencia.
     */
    public void setTotalCareerHours(double hours) {
        this.totalCareerHours = hours;
    }

    /**
     * Setter para restaurar desde persistencia.
     */
    public void setTotalFlowSessions(int count) {
        this.totalFlowSessions = count;
    }

    // ========================================================================================
    // GETTERS DIRECTOS (Para UI o Player)
    // ========================================================================================

    public double getTodayTotalHours() {
        checkAndResetIfNewDay();
        return todayTotalHours;
    }

    public int getTodayTotalXP() {
        return todayIntellectXP + todayDisciplineXP + todayWisdomXP;
    }

    public int getTodayHPCost() {
        checkAndResetIfNewDay();
        return todayHPCost;
    }

    public int getTodayFlowCount() {
        checkAndResetIfNewDay();
        return todayFlowCount;
    }

    public int getTodaySessionCount() {
        checkAndResetIfNewDay();
        return todaySessions.size();
    }

    /**
     * Última sesión registrada (para mostrar en UI).
     */
    public Optional<CodeSession> getLastSession() {
        checkAndResetIfNewDay();
        if (todaySessions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(todaySessions.get(todaySessions.size() - 1));
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    @Override
    public String toString() {
        return String.format(
                "CareerEngine[today=%.1fh/%d sessions/%d flows, career=%.1fh total]",
                todayTotalHours, todaySessions.size(), todayFlowCount, totalCareerHours
        );
    }
}
