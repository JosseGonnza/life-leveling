package com.lifeleveling.application.dto;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.player.Stats;
import com.lifeleveling.domain.quest.condition.GateTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model del System Log / Journal: timeline de días cerrados (desde `DailyHistory`),
 * desglose de stats actual y totales/rachas. (El timeline de eventos individuales —level up,
 * compras— necesitaría historizar los GameEvent; queda como mejora futura.)
 */
public record JournalView(
        List<DayEntry> timeline,
        List<StatLine> stats,
        int daysLogged,
        int perfectDaysTotal,
        int burnoutsRecovered,
        int perfectDayStreak,
        int maxPerfectDayStreak,
        int totalPagesRead,
        long totalGoldSpent
) {
    public record DayEntry(String date, boolean perfectDay, boolean burnout,
                           double careerHours, int pagesRead, int quests, int minHP) {}

    public record StatLine(String stat, int level, int xp) {}

    public static JournalView from(Player player, int days) {
        GateTracker tracker = player.getGateTracker();

        List<DayEntry> timeline = new ArrayList<>();
        for (GateTracker.DailyHistory d : tracker.getRecentDays(days)) {
            int quests = d.completedQuestsCount().values().stream().mapToInt(Integer::intValue).sum();
            timeline.add(new DayEntry(d.date().toString(), d.perfectDayAchieved(), d.hadBurnout(),
                    d.careerHours(), d.pagesRead(), quests, d.minHP()));
        }

        Stats s = player.getEffectiveStats();
        List<StatLine> stats = new ArrayList<>();
        for (StatType type : StatType.values()) {
            stats.add(new StatLine(type.name().substring(0, 3), s.getLevel(type), s.getCurrentXP(type)));
        }

        return new JournalView(
                timeline,
                stats,
                timeline.size(),
                tracker.getPerfectDaysCountInLastDays(36_500),
                tracker.getBurnoutsRecoveredCount(),
                tracker.getPerfectDayStreak(),
                tracker.getMaxPerfectDayStreak(),
                tracker.getTotalPagesRead(),
                tracker.getTotalGoldSpent());
    }
}
