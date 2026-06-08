package com.lifeleveling.application;

import com.lifeleveling.application.dto.DailyChecklistView;
import com.lifeleveling.application.dto.ElderQuestView;
import com.lifeleveling.application.dto.InventoryView;
import com.lifeleveling.application.dto.JournalView;
import com.lifeleveling.application.dto.PlayerView;
import com.lifeleveling.application.dto.QuestView;
import com.lifeleveling.application.dto.ShopItemView;
import com.lifeleveling.application.dto.TitlesView;
import com.lifeleveling.application.dto.TreasureView;
import com.lifeleveling.application.dto.WeeklyQuestView;
import com.lifeleveling.domain.item.Item;
import com.lifeleveling.domain.item.ItemCatalog;
import com.lifeleveling.domain.item.ItemCategory;
import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.title.TitleType;
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
import java.util.ArrayList;
import java.util.List;

/**
 * GameFacade: única puerta de entrada para la "cara" (CLI/JavaFX/web).
 * Mantiene la partida activa, delega en los servicios, persiste tras cada comando
 * y reenvía los eventos del dominio al Notifier. No conoce ninguna tecnología de UI.
 */
public final class GameFacade {

    private final PlayerRepository repository;
    private final Notifier notifier;
    private final Clock clock;

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
        this.clock = clock;
        this.quests = new QuestService(clock);
        this.day = new DayService(clock);
    }

    // ----- Ciclo de vida de la partida -----

    public PlayerView newGame(String name) {
        this.current = Player.create(name);
        wireEvents();
        ensureWeeklies();
        persist();
        return state();
    }

    public boolean loadGame() {
        var loaded = repository.load();
        loaded.ifPresent(p -> {
            this.current = p;
            wireEvents();
            ensureWeeklies();
            catchUpDay();
        });
        return loaded.isPresent();
    }

    /**
     * Garantiza que existan las Weekly Quests de la semana en curso. Idempotente dentro de la misma
     * semana: si ya están generadas no hace nada; si faltan (partida nueva o save sin semanales) las
     * crea sin esperar al lunes (el pool es determinista por semana).
     */
    private void ensureWeeklies() {
        current.getWeeklyManager().performWeeklyReset(clock.today());
    }

    /**
     * Cierre automático de día al reabrir (Opción A): si el día ha cambiado desde la última
     * sesión, cierra UNA vez el día en curso y reanuda hoy, perdonando los días muertos
     * (sin penalizar huecos). Persiste el resultado.
     */
    private void catchUpDay() {
        LocalDate today = clock.today();
        LocalDate last = current.getLastActiveDate();
        if (last != null && today.isAfter(last)) {
            day.endDay(current, last); // archiva el día bajo la fecha real trabajada, no HOY
        }
        persist();
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

    public List<QuestView> questHistory() {
        return game().getQuestHistory().stream().map(QuestView::from).toList();
    }

    public List<WeeklyQuestView> weeklyQuests() {
        LocalDate today = clock.today();
        return game().getWeeklyManager().getActiveQuests().stream()
                .map(q -> WeeklyQuestView.from(q, today))
                .toList();
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

    /** Gates especiales que se han manifestado y aún no se han superado (las únicas que la UI debe mostrar). */
    public List<GateVerificationResult> availableSpecialGates() {
        List<GateVerificationResult> out = new ArrayList<>();
        for (SystemQuestType special : List.of(SystemQuestType.GATE_VAULT, SystemQuestType.GATE_REDEMPTION)) {
            if (gates.hasAppeared(game(), special) && !game().getGateTracker().isGateCompleted(special)) {
                out.add(gates.statusOf(game(), special));
            }
        }
        return out;
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

    public List<ShopItemView> shopCatalog() {
        int gold = game().getCurrentGold();
        int level = game().getLevel();
        return ItemCatalog.getShopInventory().stream()
                .filter(item -> item.category() != ItemCategory.TREASURE)
                .map(item -> ShopItemView.from(item, gold, level))
                .toList();
    }

    /** Tesoros (money sinks de estatus). La pantalla los muestra siempre; el botón comprar exige Rango A. */
    public List<TreasureView> treasures() {
        int gold = game().getCurrentGold();
        var inv = game().getInventory();
        return ItemCatalog.getAllItems().stream()
                .filter(item -> item.category() == ItemCategory.TREASURE)
                .sorted(java.util.Comparator.comparingInt(Item::price))
                .map(item -> TreasureView.from(item, gold, inv.hasItem(item.id())))
                .toList();
    }

    /** La pestaña Tesoros se desbloquea al alcanzar Rango A (Senior). */
    public boolean treasuresUnlocked() {
        return game().getCurrentRank().ordinal() >= PlayerRank.A.ordinal();
    }

    /** La pestaña Juicios (Elder Quests) se desbloquea al alcanzar el Nivel General 75. */
    public boolean elderUnlocked() {
        return game().getLevel() >= 75;
    }

    /** Los 7 Juicios del Monarca con su progreso actual (read-only: sin reclamar ni Season Lock en v1). */
    public List<ElderQuestView> elderQuests() {
        var ctx = new com.lifeleveling.domain.quest.condition.ConditionContext(
                game(), null, game().getGateTracker(), clock.today());
        return java.util.Arrays.stream(com.lifeleveling.domain.quest.elder.ElderQuestType.values())
                .map(type -> ElderQuestView.from(type, ctx))
                .toList();
    }

    public InventoryView inventory() {
        return InventoryView.from(game().getInventory());
    }

    public TitlesView titles() {
        return TitlesView.from(game().getTitleInventory(), game().getLevel());
    }

    public JournalView journal() {
        return JournalView.from(game(), 30);
    }

    public PlayerView equipTitle(String titleType) {
        game().equipTitle(TitleType.valueOf(titleType));
        return persistAndView();
    }

    public PlayerView unequipTitle(String titleType) {
        game().unequipTitle(TitleType.valueOf(titleType));
        return persistAndView();
    }

    public PlayerView swapTitle(String oldType, String newType) {
        game().swapTitle(TitleType.valueOf(oldType), TitleType.valueOf(newType));
        return persistAndView();
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
        current.setLastActiveDate(clock.today());
        repository.save(current);
    }

    private PlayerView persistAndView() {
        persist();
        return state();
    }
}
