package com.lifeleveling.application.service;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.user.UserQuest;

import java.time.LocalDate;

/**
 * Casos de uso de User Quests (las que el jugador se autoimpone). Las quests en curso
 * viven en el agregado Player (se añaden al crearse, se borran al completarse/fallar).
 */
public final class QuestService {

    private final Clock clock;

    public QuestService(Clock clock) {
        this.clock = clock;
    }

    /** Crea una quest (deadline null = sin fecha límite) y la registra como activa en el jugador. */
    public UserQuest create(Player player, String name, String description, QuestRank rank, LocalDate deadline) {
        UserQuest quest = deadline == null
                ? UserQuest.createWithoutDeadline(name, description, rank)
                : UserQuest.create(name, description, rank, deadline);
        player.addUserQuest(quest);
        return quest;
    }

    public UserQuest complete(Player player, String questId) {
        return player.completeUserQuest(questId, clock.now());
    }

    public void fail(Player player, String questId) {
        player.applyUserQuestFailure(questId);
    }
}
