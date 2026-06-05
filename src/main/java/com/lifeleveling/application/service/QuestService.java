package com.lifeleveling.application.service;

import com.lifeleveling.application.port.Clock;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.user.UserQuest;

/**
 * Casos de uso de User Quests (las que el jugador se autoimpone).
 * La gestión de la lista de quests activas la lleva la cara; aquí se crean/resuelven.
 */
public final class QuestService {

    private final Clock clock;

    public QuestService(Clock clock) {
        this.clock = clock;
    }

    public UserQuest create(String name, String description, QuestRank rank) {
        return UserQuest.createWithoutDeadline(name, description, rank);
    }

    public UserQuest complete(Player player, UserQuest quest) {
        return player.completeUserQuest(quest, clock.now());
    }

    public void fail(Player player, UserQuest quest) {
        player.applyUserQuestFailure(quest);
    }
}
