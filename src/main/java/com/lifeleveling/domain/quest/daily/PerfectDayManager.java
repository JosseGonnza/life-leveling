package com.lifeleveling.domain.quest.daily;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.QuestStatus;

import java.util.List;

/**
 * Servicio de Dominio encargado de verificar si se ha cumplido el Perfect Day.
 * Se debe invocar cada vez que se completa una Daily Quest.
 */
public class PerfectDayManager {

    private static final int QUESTS_REQUIRED_FOR_PERFECT = 7;

    /**
     * Verifica el progreso diario y gatilla el premio si corresponde.
     * @param player El jugador actual.
     * @param todaysQuests La lista de las 7 daily quests de hoy.
     */
    public void checkAndApplyPerfectDay(Player player, List<DailyQuest> todaysQuests) {
        if (player == null || todaysQuests == null) return;

        // 1. Contar cuántas están completadas
        long completedCount = todaysQuests.stream()
                .filter(q -> q.status() == QuestStatus.COMPLETED)
                .count();

        // 2. Verificar condición (7/7)
        if (completedCount >= QUESTS_REQUIRED_FOR_PERFECT) {
            // 3. Delegar en el Player (él sabrá si ya recibió el premio o no gracias al GateTracker)
            player.triggerPerfectDay();
        }
    }
}