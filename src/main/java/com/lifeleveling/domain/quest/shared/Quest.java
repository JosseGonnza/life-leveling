package com.lifeleveling.domain.quest.shared;

import java.time.Instant;

/*
 * Tipos de Quest:
 *   - DailyQuest: Las 7 misiones recurrentes diarias
 *   - WeeklyQuest: Desafíos rotativos semanales (3 de 6 activas)
 *   - UserQuest: Tareas creadas manualmente por el usuario
 *   - SystemQuest: Las Gates de ascenso de rango
 *   - ElderQuest: Juicios del Monarca (nivel 75+)
 * Filosofía:
 *   Toda acción productiva en Life Leveling es una Quest.
 *   Completar Quests otorga recompensas (XP, Gold).
 *   Fallar Quests tiene consecuencias (HP loss, Gold loss).
 */

public interface Quest {

    QuestId id();
    String name();
    String description();
    QuestRank rank();
    QuestStatus status();
    Instant createdAt();
    QuestReward reward();

    default boolean canStartWith(com.lifeleveling.domain.player.HPState hpState) {
        if (hpState == null) {
            throw new IllegalArgumentException("El estado de HP no puede ser null");
        }
        return rank().canStartWith(hpState);
    }

    Quest complete(Instant completedAt);

    Quest fail(Instant failedAt);

    default boolean isActive() {
        return status().isActive();
    }

    default boolean isCompleted() {
        return status() == QuestStatus.COMPLETED;
    }

    default boolean hasFailed() {
        return status().isUnsuccessful();
    }

    default boolean isHighRank() {
        return rank().isHighRank();
    }
}