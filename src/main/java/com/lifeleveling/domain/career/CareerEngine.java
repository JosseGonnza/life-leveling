package com.lifeleveling.domain.career;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    // [NUEVO] Tracker para el Agente IA
    private double aiAgentHoursUsedToday;

    // Histórico global
    private double totalCareerHours;
    private int totalFlowSessions;

    public CareerEngine() {
        this.todaySessions = new ArrayList<>();
        this.currentDate = LocalDate.now();
        resetDailyCounters();
        this.totalCareerHours = 0.0;
        this.totalFlowSessions = 0;
    }

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
     * [ACTUALIZADO] Ahora acepta si el usuario tiene el Agente IA activo.
     */
    public CodeSession registerSession(double hours, boolean hasAiAgent) {
        return registerSession(hours, Instant.now(), hasAiAgent);
    }

    private CodeSession registerSession(double hours, Instant registeredAt, boolean hasAiAgent) {
        checkAndResetIfNewDay();
        validateDailyLimit(hours);

        // 1. Crear sesión base (Cálculo estándar)
        CodeSession session = CodeSession.register(hours, registeredAt);

        // 2. [NUEVO] Aplicar Bonus de Agente IA
        if (hasAiAgent) {
            // Límite de 2.0h diarias bonificadas
            double quotaRemaining = Math.max(0, 2.0 - aiAgentHoursUsedToday);
            double bonusHours = Math.min(hours, quotaRemaining);

            if (bonusHours > 0) {
                // Bonus = 20% de la XP base por esas horas
                int bonusXP = (int) Math.round(bonusHours * CodeSessionConstants.XP_INT_PER_HOUR * 0.20);

                // Actualizar la sesión con la nueva recompensa
                session = session.withReward(session.getReward().plusIntellect(bonusXP));
                System.out.println("🤖 Agente IA activo: +" + bonusXP + " INT (" + String.format("%.1f", bonusHours) + "h bonificadas)");
            }
            // Actualizar uso del día (incluso si no hubo bonus, se gasta cuota si pasamos de 2h)
            aiAgentHoursUsedToday += hours;
        }

        // 3. Registrar
        todaySessions.add(session);
        updateDailyTotals(session);

        // 4. Actualizar histórico
        totalCareerHours += hours;
        if (session.isFlowAchieved()) {
            totalFlowSessions++;
        }

        return session;
    }

    private void validateDailyLimit(double newHours) {
        double potentialTotal = todayTotalHours + newHours;
        if (potentialTotal > CodeSessionConstants.MAX_DAILY_HOURS) {
            throw new IllegalArgumentException(String.format("Excedes el límite diario de %.1fh.", CodeSessionConstants.MAX_DAILY_HOURS));
        }
    }

    private void updateDailyTotals(CodeSession session) {
        CareerReward reward = session.getReward();
        todayTotalHours += session.getHours();
        todayIntellectXP += reward.intellectXP();
        todayDisciplineXP += reward.disciplineXP();
        todayWisdomXP += reward.wisdomXP();
        todayHPCost += reward.hpCost();
        if (session.isFlowAchieved()) todayFlowCount++;
    }

    // ========================================================================================
    // RESET DIARIO
    // ========================================================================================

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
        aiAgentHoursUsedToday = 0.0; // [NUEVO] Reset del agente
    }

    // ... (Resto de getters y métodos de persistencia se mantienen igual) ...
    public double getTodayTotalHours() { checkAndResetIfNewDay(); return todayTotalHours; }
    public boolean hasActivityToday() { checkAndResetIfNewDay(); return !todaySessions.isEmpty(); }
    public double getTotalCareerHours() { return totalCareerHours; }
    // ... etc ...
}