package com.lifeleveling.infrastructure.persistence;

import com.lifeleveling.domain.career.CareerEngine;
import com.lifeleveling.domain.debuff.DebuffTracker;
import com.lifeleveling.domain.item.Inventory;
import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.item.ItemSlot;
import com.lifeleveling.domain.player.BurnoutLock;
import com.lifeleveling.domain.player.MilestoneTracker;
import com.lifeleveling.domain.player.MilestoneType;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.player.Stat;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.player.Stats;
import com.lifeleveling.domain.player.TemporaryBuffTracker;
import com.lifeleveling.domain.player.Wallet;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.daily.DailyQuestType;
import com.lifeleveling.domain.quest.shared.QuestId;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.shared.QuestStatus;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import com.lifeleveling.domain.quest.user.UserQuest;
import com.lifeleveling.domain.quest.weekly.WeeklyManager;
import com.lifeleveling.domain.quest.weekly.WeeklyQuest;
import com.lifeleveling.domain.quest.weekly.WeeklyQuestType;
import com.lifeleveling.domain.title.Title;
import com.lifeleveling.domain.title.TitleInventory;
import com.lifeleveling.domain.title.TitleType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Traduce entre el agregado Player y su forma serializable (PlayerSnapshot). */
final class PlayerSnapshotMapper {

    static PlayerSnapshot toSnapshot(Player p) {
        List<PlayerSnapshot.StatSnap> stats = new ArrayList<>();
        for (StatType type : StatType.values()) {
            Stat st = p.getBaseStats().getStat(type);
            stats.add(new PlayerSnapshot.StatSnap(type.name(), st.currentLevel(), st.currentXP()));
        }

        List<String> owned = new ArrayList<>();
        p.getInventory().getOwnedItems().forEach(i -> owned.add(i.id()));
        Map<String, String> equipped = new LinkedHashMap<>();
        p.getInventory().getEquippedItems().forEach((slot, item) -> equipped.put(slot.name(), item.id()));

        List<String> titlesUnlocked = new ArrayList<>();
        p.getTitleInventory().getUnlockedTitles().forEach(t -> titlesUnlocked.add(t.type().name()));
        List<String> titlesEquipped = new ArrayList<>();
        p.getTitleInventory().getEquippedTitles().forEach(t -> titlesEquipped.add(t.type().name()));

        List<PlayerSnapshot.QuestSnap> quests = new ArrayList<>();
        for (UserQuest q : p.getActiveUserQuests()) {
            quests.add(new PlayerSnapshot.QuestSnap(
                    q.id().value().toString(), q.name(), q.description(), q.rank().name(),
                    q.getDeadline() == null ? null : q.getDeadline().toString(),
                    q.status().name(), q.createdAt().toString(), null, null));
        }

        List<PlayerSnapshot.QuestSnap> questHistory = new ArrayList<>();
        for (UserQuest q : p.getQuestHistory()) {
            questHistory.add(new PlayerSnapshot.QuestSnap(
                    q.id().value().toString(), q.name(), q.description(), q.rank().name(),
                    q.getDeadline() == null ? null : q.getDeadline().toString(),
                    q.status().name(), q.createdAt().toString(),
                    q.getCompletedAt() == null ? null : q.getCompletedAt().toString(),
                    q.getFailedAt() == null ? null : q.getFailedAt().toString()));
        }

        List<String> milestones = new ArrayList<>();
        p.getMilestoneTracker().getAchievedMilestones().forEach(m -> milestones.add(m.name()));
        List<String> gates = new ArrayList<>();
        p.getGateTracker().getCompletedGates().forEach(g -> gates.add(g.name()));

        List<PlayerSnapshot.DaySnap> history = new ArrayList<>();
        for (GateTracker.DailyHistory d : p.getGateTracker().getRecentDays(36_500)) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            d.completedQuestsCount().forEach((rank, n) -> counts.put(rank.name(), n));
            history.add(new PlayerSnapshot.DaySnap(d.date().toString(), d.perfectDayAchieved(),
                    d.minHP(), d.pagesRead(), d.careerHours(), d.hadBurnout(), d.hadDebuffsActive(),
                    new ArrayList<>(d.consumablesBought()), new ArrayList<>(d.completedQuestIds()), counts));
        }

        BurnoutLock lock = p.getActiveBurnoutLock();
        PlayerSnapshot.BurnoutSnap burnout = lock == null ? null : new PlayerSnapshot.BurnoutSnap(
                lock.id().toString(), lock.triggeredAt().toString(), lock.expiresAt().toString());

        GateTracker gt = p.getGateTracker();
        Map<String, Integer> todayCounts = new LinkedHashMap<>();
        gt.getTodayCompletedQuestsCount().forEach((rank, n) -> todayCounts.put(rank.name(), n));
        PlayerSnapshot.TodaySnap today = new PlayerSnapshot.TodaySnap(
                gt.getTodayPagesRead(), gt.getTodayCareerHours(), gt.getTodayMinHP(),
                gt.wasDebuffsActiveToday(), gt.wasJunkConsumedToday(),
                gt.isPerfectDayAchievedToday(), gt.didBurnoutOccurToday(),
                new ArrayList<>(gt.getTodayConsumablesBought()),
                new ArrayList<>(gt.getTodayCompletedQuestIds()), todayCounts);

        WeeklyManager wm = p.getWeeklyManager();
        List<PlayerSnapshot.WeeklyQuestSnap> weeklyQuests = new ArrayList<>();
        for (WeeklyQuest q : wm.getActiveQuests()) {
            weeklyQuests.add(new PlayerSnapshot.WeeklyQuestSnap(
                    q.id().value().toString(), q.getType().name(), q.status().name(),
                    q.getWeekStartDate().toString(), q.getCurrentProgress(), q.createdAt().toString(),
                    q.getCompletedAt() == null ? null : q.getCompletedAt().toString()));
        }
        Map<String, List<String>> dailyCompletions = new LinkedHashMap<>();
        wm.getDailyCompletions().forEach((date, set) ->
                dailyCompletions.put(date.toString(), set.stream().map(Enum::name).toList()));
        PlayerSnapshot.WeeklySnap weekly = new PlayerSnapshot.WeeklySnap(
                weeklyQuests, wm.getLastResetDate() == null ? null : wm.getLastResetDate().toString(),
                dailyCompletions, wm.getPerfectDaysThisWeek());

        CareerEngine ce = p.getCareerEngine();
        return new PlayerSnapshot(
                p.getId().toString(), p.getName(), p.getCurrentHP(), p.getCurrentRank().name(),
                p.getTotalXP() - p.getBaseStats().getTotalAccumulatedXP(), // generalXP = total - statsXP
                p.getCurrentGold(), stats, owned, equipped, titlesUnlocked, titlesEquipped, quests,
                ce.getTotalCareerHours(), ce.getTotalFlowSessions(), ce.getTotalSessions(),
                milestones, gates, history,
                gt.getPerfectDayStreak(), gt.getMaxPerfectDayStreak(),
                burnout,
                p.getLastActiveDate() == null ? null : p.getLastActiveDate().toString(),
                today, weekly, questHistory);
    }

    static Player toDomain(PlayerSnapshot s) {
        Map<StatType, Stat> statMap = new LinkedHashMap<>();
        for (PlayerSnapshot.StatSnap snap : s.stats()) {
            StatType type = StatType.valueOf(snap.type());
            statMap.put(type, new Stat(type, snap.level(), snap.xp()));
        }
        Stats stats = new Stats(statMap.get(StatType.STRENGTH), statMap.get(StatType.INTELLECT),
                statMap.get(StatType.WISDOM), statMap.get(StatType.DISCIPLINE), statMap.get(StatType.CHARISMA));

        Inventory inventory = new Inventory();
        Set<String> allIds = new HashSet<>(s.inventoryOwned());
        allIds.addAll(s.inventoryEquipped().values());
        allIds.forEach(id -> ItemCatalog.findById(id).ifPresent(inventory::addItem));
        s.inventoryEquipped().values().forEach(id -> {
            if (inventory.getItem(id).isPresent()) inventory.equip(id);
        });

        Set<Title> unlocked = new HashSet<>();
        s.titlesUnlocked().forEach(n -> unlocked.add(Title.unlock(TitleType.valueOf(n))));
        List<Title> equippedTitles = new ArrayList<>();
        s.titlesEquipped().forEach(n -> equippedTitles.add(Title.unlock(TitleType.valueOf(n))));
        TitleInventory titleInventory = new TitleInventory(unlocked, equippedTitles);

        GateTracker gateTracker = new GateTracker();
        for (PlayerSnapshot.DaySnap d : s.dailyHistory()) {
            Map<QuestRank, Integer> counts = new LinkedHashMap<>();
            d.completedQuestsCount().forEach((rank, n) -> counts.put(QuestRank.valueOf(rank), n));
            gateTracker.addDailyHistory(new GateTracker.DailyHistory(
                    LocalDate.parse(d.date()), d.perfectDay(), d.minHP(), d.pagesRead(), d.careerHours(),
                    d.burnout(), d.debuffsActive(), new ArrayList<>(d.consumablesBought()),
                    new HashSet<>(d.completedQuestIds()), counts));
        }
        s.gatesCompleted().forEach(g -> gateTracker.markGateAsCompleted(SystemQuestType.valueOf(g)));
        gateTracker.setTotalCareerHours(s.careerHours());
        gateTracker.restorePerfectDayStreaks(s.perfectDayStreak(), s.maxPerfectDayStreak());

        if (s.today() != null) {
            PlayerSnapshot.TodaySnap t = s.today();
            Map<QuestRank, Integer> todayCounts = new LinkedHashMap<>();
            t.completedQuestsCount().forEach((rank, n) -> todayCounts.put(QuestRank.valueOf(rank), n));
            gateTracker.restoreDailyState(t.pagesRead(), t.careerHours(), t.minHP(),
                    t.debuffsActive(), t.junkConsumed(), t.perfectDayAchieved(), t.burnoutOccurred(),
                    new ArrayList<>(t.consumablesBought()), new HashSet<>(t.completedQuestIds()), todayCounts);
        }

        Set<MilestoneType> milestones = new HashSet<>();
        s.milestones().forEach(m -> milestones.add(MilestoneType.valueOf(m)));

        BurnoutLock lock = s.burnout() == null ? null : BurnoutLock.reconstitute(
                UUID.fromString(s.burnout().id()), Instant.parse(s.burnout().triggeredAt()),
                Instant.parse(s.burnout().expiresAt()));

        WeeklyManager weeklyManager = toWeeklyManager(s.weekly());

        Player player = Player.restore(
                UUID.fromString(s.id()), s.name(), s.currentHP(), PlayerRank.valueOf(s.rank()),
                stats, Wallet.of(s.gold()), s.generalXP(), inventory, titleInventory,
                new DebuffTracker(), new TemporaryBuffTracker(), gateTracker,
                new CareerEngine(s.careerHours(), s.careerFlowSessions(), s.careerSessions()),
                new MilestoneTracker(milestones), weeklyManager, lock);

        for (PlayerSnapshot.QuestSnap q : s.activeQuests()) {
            player.addUserQuest(UserQuest.reconstitute(
                    QuestId.from(q.id()), q.name(), q.description(), QuestRank.valueOf(q.rank()),
                    q.deadline() == null ? null : LocalDate.parse(q.deadline()),
                    QuestStatus.valueOf(q.status()), Instant.parse(q.createdAt()),
                    q.completedAt() == null ? null : Instant.parse(q.completedAt()),
                    q.failedAt() == null ? null : Instant.parse(q.failedAt())));
        }

        if (s.questHistory() != null) {
            for (PlayerSnapshot.QuestSnap q : s.questHistory()) {
                player.addQuestToHistory(UserQuest.reconstitute(
                        QuestId.from(q.id()), q.name(), q.description(), QuestRank.valueOf(q.rank()),
                        q.deadline() == null ? null : LocalDate.parse(q.deadline()),
                        QuestStatus.valueOf(q.status()), Instant.parse(q.createdAt()),
                        q.completedAt() == null ? null : Instant.parse(q.completedAt()),
                        q.failedAt() == null ? null : Instant.parse(q.failedAt())));
            }
        }

        if (s.lastActiveDate() != null) {
            player.setLastActiveDate(LocalDate.parse(s.lastActiveDate()));
        }

        return player;
    }

    private static WeeklyManager toWeeklyManager(PlayerSnapshot.WeeklySnap snap) {
        if (snap == null) return new WeeklyManager();

        List<WeeklyQuest> quests = new ArrayList<>();
        for (PlayerSnapshot.WeeklyQuestSnap q : snap.quests()) {
            quests.add(WeeklyQuest.reconstitute(
                    QuestId.from(q.id()), WeeklyQuestType.valueOf(q.type()), QuestStatus.valueOf(q.status()),
                    LocalDate.parse(q.weekStartDate()), q.progress(), Instant.parse(q.createdAt()),
                    q.completedAt() == null ? null : Instant.parse(q.completedAt())));
        }
        Map<LocalDate, Set<DailyQuestType>> dailyCompletions = new HashMap<>();
        snap.dailyCompletions().forEach((date, names) -> {
            Set<DailyQuestType> habits = new HashSet<>();
            names.forEach(n -> habits.add(DailyQuestType.valueOf(n)));
            dailyCompletions.put(LocalDate.parse(date), habits);
        });
        LocalDate lastReset = snap.lastResetDate() == null ? null : LocalDate.parse(snap.lastResetDate());
        return new WeeklyManager(quests, lastReset, dailyCompletions, snap.perfectDaysThisWeek());
    }

    private PlayerSnapshotMapper() {}
}
