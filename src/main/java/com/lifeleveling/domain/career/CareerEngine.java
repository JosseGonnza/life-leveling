package com.lifeleveling.domain.career;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    // Tracker para el Agente IA
    private double aiAgentHoursUsedToday;

    // Histórico global (Persistencia)
    private double totalCareerHours;
    private int totalFlowSessions;
    private int totalSessions; // [NUEVO] Contador total de sesiones para Títulos

    public CareerEngine() {
        this.todaySessions = new ArrayList<>();
        this.currentDate = LocalDate.now();
        resetDailyCounters();
        this.totalCareerHours = 0.0;
        this.totalFlowSessions = 0;
        this.totalSessions = 0;
    }

    /**
     * Constructor para restaurar estado (Persistencia).
     */
    public CareerEngine(double totalCareerHours, int totalFlowSessions, int totalSessions) {
        this(); // Inicializa las listas y fechas
        this.totalCareerHours = totalCareerHours;
        this.totalFlowSessions = totalFlowSessions;
        this.totalSessions = totalSessions;
    }

    // ========================================================================================
    // CORE: REGISTRO DE SESIONES
    // ========================================================================================

    public CodeSession registerSession(double hours, boolean hasAiAgent) {
        return registerSession(hours, Instant.now(), hasAiAgent);
    }

    private CodeSession registerSession(double hours, Instant registeredAt, boolean hasAiAgent) {
        checkAndResetIfNewDay();
        validateDailyLimit(hours);

        // 1. Crear sesión base
        CodeSession session = CodeSession.register(hours, registeredAt);

        // 2. Aplicar Bonus de Agente IA (Tu lógica existente)
        if (hasAiAgent) {
            double quotaRemaining = Math.max(0, 2.0 - aiAgentHoursUsedToday);
            double bonusHours = Math.min(hours, quotaRemaining);

            if (bonusHours > 0) {
                int bonusXP = (int) Math.round(bonusHours * CodeSessionConstants.XP_INT_PER_HOUR * 0.20);
                session = session.withReward(session.getReward().plusIntellect(bonusXP));
                System.out.println("🤖 Agente IA activo: +" + bonusXP + " INT (" + String.format("%.1f", bonusHours) + "h bonificadas)");
            }
            aiAgentHoursUsedToday += hours;
        }

        // 3. Registrar en el día actual
        todaySessions.add(session);
        updateDailyTotals(session);

        // 4. Actualizar histórico GLOBAL (Aquí es donde sumamos para los Títulos)
        totalCareerHours += hours;
        totalSessions++; // [NUEVO] Incrementamos el contador de sesiones totales

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
        aiAgentHoursUsedToday = 0.0;
    }

    // ========================================================================================
    // GETTERS PARA TITLE UNLOCK CHECKER
    // ========================================================================================

    public double getTotalCareerHours() {
        return totalCareerHours;
    }

    public int getTotalFlowSessions() {
        return totalFlowSessions;
    }

    /**
     * [NUEVO] Necesario para el título 'Hello World' y 'Code Monkey'.
     */
    public int getTotalSessions() {
        return totalSessions;
    }

    public boolean hasActivityToday() {
        checkAndResetIfNewDay();
        return !todaySessions.isEmpty();
    }

    // Getters de acumulados diarios (Opcional, si los usas en la UI)
    public double getTodayTotalHours() { checkAndResetIfNewDay(); return todayTotalHours; }
}