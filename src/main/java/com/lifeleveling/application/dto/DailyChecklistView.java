package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.daily.DailyQuestType;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado de los 7 hábitos diarios de hoy para el checklist de la Home (n/7 → Perfect Day).
 */
public record DailyChecklistView(
        List<Habit> habits,
        int completedCount,
        int total
) {
    public record Habit(String code, String label, boolean done) {}

    public static DailyChecklistView from(Player player) {
        GateTracker tracker = player.getGateTracker();
        List<Habit> habits = new ArrayList<>();
        for (DailyQuestType type : DailyQuestType.values()) {
            habits.add(new Habit(type.name(), type.getName(), tracker.isHabitCompletedToday(type)));
        }
        int done = (int) habits.stream().filter(Habit::done).count();
        return new DailyChecklistView(List.copyOf(habits), done, habits.size());
    }
}
