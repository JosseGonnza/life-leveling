package com.lifeleveling.application;

import com.lifeleveling.application.dto.DailyChecklistView;
import com.lifeleveling.application.dto.PlayerView;
import com.lifeleveling.application.dto.QuestView;
import com.lifeleveling.application.port.Clock;
import com.lifeleveling.application.port.Notifier;
import com.lifeleveling.application.port.PlayerRepository;
import com.lifeleveling.application.service.CareerService;
import com.lifeleveling.application.service.DailyHabitService;
import com.lifeleveling.application.service.DayService;
import com.lifeleveling.application.service.GateChallengeService;
import com.lifeleveling.application.service.QuestService;
import com.lifeleveling.application.service.ShopService;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.GateVerificationResult;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import com.lifeleveling.domain.quest.user.UserQuest;

import java.time.LocalDate;
import java.util.List;

/**
 * GameFacade: única puerta de entrada para la "cara" (CLI/JavaFX/web).
 * Mantiene la partida activa, delega en los servicios, persiste tras cada comando
 * y reenvía los eventos del dominio al Notifier. No conoce ninguna tecnología de UI.
 */
public final class GameFacade {

    private final PlayerRepository repository;
    private final Notifier notifier;

    private final CareerService career = new CareerService();
    private final DailyHabitService habits = new DailyHabitService();
    private final ShopService shop = new ShopService();
    private final QuestService quests;
    private final GateChallengeService gates = new GateChallengeService();
    private final DayService day;

    private Player current;

    public GameFacade(PlayerRepository repository, Clock clock, Notifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
        this.quests = new QuestService(clock);
        this.day = new DayService(clock);
    }

    // ----- Ciclo de vida de la partida -----

    public PlayerView newGame(String name) {
        this.current = Player.create(name);
        wireEvents();
        persist();
        return state();
    }

    public boolean loadGame() {
        var loaded = repository.load();
        loaded.ifPresent(p -> {
            this.current = p;
            wireEvents();
        });
        return loaded.isPresent();
    }

    // ----- Comandos -----

    public PlayerView workCode(double hours) {
        career.code(game(), hours);
        return persistAndView();
    }

    public PlayerView workJob(int hours) {
        career.job(game(), hours);
        return persistAndView();
    }

    public PlayerView sleep(int hours) {
        habits.sleep(game(), hours);
        return persistAndView();
    }

    public PlayerView read(int pages) {
        habits.read(game(), pages);
        return persistAndView();
    }

    public PlayerView diet(boolean completed) {
        habits.diet(game(), completed);
        return persistAndView();
    }

    public PlayerView gym(boolean completed) {
        habits.gym(game(), completed);
        return persistAndView();
    }

    public PlayerView skincare(boolean completed) {
        habits.skincare(game(), completed);
        return persistAndView();
    }

    public PlayerView tidy(boolean completed) {
        habits.tidy(game(), completed);
        return persistAndView();
    }

    public PlayerView buy(String itemId) {
        shop.buy(game(), itemId);
        return persistAndView();
    }

    public PlayerView consume(String itemId) {
        shop.consume(game(), itemId);
        return persistAndView();
    }

    public PlayerView equip(String itemId) {
        shop.equip(game(), itemId);
        return persistAndView();
    }

    public UserQuest createQuest(String name, String description, QuestRank rank) {
        return createQuest(name, description, rank, null);
    }

    public UserQuest createQuest(String name, String description, QuestRank rank, LocalDate deadline) {
        UserQuest quest = quests.create(game(), name, description, rank, deadline);
        persist();
        return quest;
    }

    public List<QuestView> activeQuests() {
        return game().getActiveUserQuests().stream().map(QuestView::from).toList();
    }

    public PlayerView completeQuest(String questId) {
        quests.complete(game(), questId);
        return persistAndView();
    }

    public PlayerView failQuest(String questId) {
        quests.fail(game(), questId);
        return persistAndView();
    }

    public PlayerView completeQuest(UserQuest quest) {
        return completeQuest(quest.id().toString());
    }

    public PlayerView failQuest(UserQuest quest) {
        return failQuest(quest.id().toString());
    }

    public GateVerificationResult gateStatus() {
        return gates.status(game());
    }

    public GateVerificationResult challengeGate() {
        GateVerificationResult result = gates.challenge(game());
        persist();
        return result;
    }

    public GateVerificationResult vaultStatus() {
        return gates.statusOf(game(), SystemQuestType.GATE_VAULT);
    }

    public GateVerificationResult challengeVault() {
        GateVerificationResult result = gates.challengeOf(game(), SystemQuestType.GATE_VAULT);
        persist();
        return result;
    }

    public GateVerificationResult redemptionStatus() {
        return gates.statusOf(game(), SystemQuestType.GATE_REDEMPTION);
    }

    public GateVerificationResult challengeRedemption() {
        GateVerificationResult result = gates.challengeOf(game(), SystemQuestType.GATE_REDEMPTION);
        persist();
        return result;
    }

    public void confirmGate(String confirmationId) {
        gates.confirm(game(), confirmationId);
        persist();
    }

    public PlayerView endDay() {
        day.endDay(game());
        return persistAndView();
    }

    // ----- Consultas -----

    public PlayerView state() {
        return PlayerView.from(game());
    }

    public DailyChecklistView dailyChecklist() {
        return DailyChecklistView.from(game());
    }

    // ----- Internos -----

    private Player game() {
        if (current == null) throw new IllegalStateException("No hay partida activa (usa newGame o loadGame)");
        return current;
    }

    private void wireEvents() {
        current.getEventPublisher().addListener(notifier::onEvent);
    }

    private void persist() {
        repository.save(current);
    }

    private PlayerView persistAndView() {
        persist();
        return state();
    }
}
