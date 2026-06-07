package com.lifeleveling.infrastructure.persistence;

import java.util.List;
import java.util.Map;

/**
 * Forma serializable (JSON) del estado del Player. DTO de persistencia: tipos planos, sin lógica
 * de dominio, para que el formato no se acople al modelo. El mapeo va en {@link PlayerSnapshotMapper}.
 *
 * Alcance Fase 1: progreso real. NO persiste (se resetea al cargar): cooldowns de ítems, buffs
 * temporales, debuffs activos, progreso semanal en curso.
 */
public record PlayerSnapshot(
        String id,
        String name,
        int currentHP,
        String rank,
        long generalXP,
        int gold,
        List<StatSnap> stats,
        List<String> inventoryOwned,
        Map<String, String> inventoryEquipped,
        List<String> titlesUnlocked,
        List<String> titlesEquipped,
        List<QuestSnap> activeQuests,
        double careerHours,
        int careerFlowSessions,
        int careerSessions,
        List<String> milestones,
        List<String> gatesCompleted,
        List<DaySnap> dailyHistory,
        int perfectDayStreak,
        int maxPerfectDayStreak,
        BurnoutSnap burnout,
        String lastActiveDate
) {
    public record StatSnap(String type, int level, int xp) {}

    public record QuestSnap(String id, String name, String description, String rank,
                            String deadline, String status,
                            String createdAt, String completedAt, String failedAt) {}

    public record DaySnap(String date, boolean perfectDay, int minHP, int pagesRead,
                          double careerHours, boolean burnout, boolean debuffsActive,
                          List<String> consumablesBought, List<String> completedQuestIds,
                          Map<String, Integer> completedQuestsCount) {}

    public record BurnoutSnap(String id, String triggeredAt, String expiresAt) {}
}
