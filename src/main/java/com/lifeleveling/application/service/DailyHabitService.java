package com.lifeleveling.application.service;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.daily.DailyQuestType;

/**
 * Casos de uso de los 7 hábitos diarios. SLEEP/READ tienen input (horas/páginas);
 * DIET/GYM/SKINCARE/TIDY son checks booleanos que pasan por Player.completeHabit
 * (que resuelve Air Fryer, Logic-Lock de DIET, mitigación Pegasus y el Perfect Day 7/7).
 * CODE va por CareerService (es carrera, no solo hábito).
 */
public final class DailyHabitService {

    public void sleep(Player player, int hours) {
        player.registerSleepSession(hours);
    }

    public void read(Player player, int pages) {
        player.registerReadSession(pages);
    }

    public void diet(Player player, boolean completed) {
        player.completeHabit(DailyQuestType.DIET, completed);
    }

    public void gym(Player player, boolean completed) {
        player.completeHabit(DailyQuestType.GYM, completed);
    }

    public void skincare(Player player, boolean completed) {
        player.completeHabit(DailyQuestType.SKINCARE, completed);
    }

    public void tidy(Player player, boolean completed) {
        player.completeHabit(DailyQuestType.TIDY, completed);
    }
}
