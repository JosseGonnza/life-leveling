package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.QuestView;
import com.lifeleveling.application.dto.WeeklyQuestView;
import com.lifeleveling.domain.quest.shared.QuestRank;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pantalla Quests (mockup 7.user-quest): lista de contratos activos + detalle + alta de uno nuevo.
 * Datos de `activeQuests()` (las quests viven en el agregado Player). Completar/abandonar/crear
 * mutan vía fachada y recargan la pantalla (`nav.quests()`).
 */
final class QuestsScreen {

    private static final List<String> TABS = List.of("ACTIVAS", "REALIZADAS", "FALLADAS");

    static Region build(GameFacade facade, Nav nav) {
        Label title = new Label("MISIONES — CONTRATOS");
        title.getStyleClass().add("screen-title");

        final String[] tab = { "ACTIVAS" };
        HBox tabs = new HBox(8);
        VBox content = new VBox(10);
        VBox.setVgrow(content, Priority.ALWAYS);

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            tabs.getChildren().clear();
            for (String t : TABS) {
                Button b = new Button(t);
                b.getStyleClass().add(t.equals(tab[0]) ? "continue-btn" : "nav-btn");
                b.setOnAction(e -> { tab[0] = t; render[0].run(); });
                tabs.getChildren().add(b);
            }
            content.getChildren().setAll(tabContent(tab[0], facade, nav));
        };
        render[0].run();

        VBox middle = new VBox(10);
        Region banner = stateBanner(facade);
        if (banner != null) middle.getChildren().add(banner);
        middle.getChildren().addAll(weeklySection(facade), tabs, content);
        ScrollPane scroll = new ScrollPane(middle);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("screen-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(10, title, scroll, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    private static Region stateBanner(GameFacade facade) {
        var s = facade.state();
        String msg;
        if (s.burnout()) msg = "💀 BURNOUT — Solo puedes acometer misiones de Rango E/D hasta recuperar HP.";
        else if (s.highRankLocked()) msg = "⚠️ CANSADO — Bloqueadas las misiones de Rango B o superior (recupera HP).";
        else return null;
        Label l = new Label(msg);
        l.getStyleClass().add("check-pending");
        l.setWrapText(true);
        return l;
    }

    private static Region tabContent(String tab, GameFacade facade, Nav nav) {
        boolean active = tab.equals("ACTIVAS");
        List<QuestView> quests = switch (tab) {
            case "REALIZADAS" -> facade.questHistory().stream()
                    .filter(q -> q.status().equals("COMPLETED")).toList();
            case "FALLADAS" -> facade.questHistory().stream()
                    .filter(q -> q.status().equals("FAILED") || q.status().equals("EXPIRED")).toList();
            default -> facade.activeQuests();
        };

        VBox detail = new VBox(10);
        detail.getStyleClass().add("panel");
        detail.setPrefWidth(320);
        Consumer<QuestView> showDetail = q -> detail.getChildren().setAll(
                active ? detailContent(q, facade, nav) : historyDetail(q));

        VBox listBox = new VBox(8, UiKit.sectionTitle(tab + "   " + quests.size()));
        if (quests.isEmpty()) {
            listBox.getChildren().add(UiKit.muted(active ? "Sin contratos. Crea uno abajo." : "Nada por aquí todavía."));
        }
        for (QuestView q : quests) {
            listBox.getChildren().add(questRow(q, showDetail));
        }
        listBox.getStyleClass().add("panel");
        listBox.setPrefWidth(340);

        if (!quests.isEmpty()) showDetail.accept(quests.get(0));
        else detail.getChildren().add(UiKit.muted("—"));

        HBox middle = new HBox(20, listBox, detail);
        HBox.setHgrow(detail, Priority.ALWAYS);
        VBox.setVgrow(middle, Priority.ALWAYS);

        VBox box = new VBox(10, middle);
        VBox.setVgrow(box, Priority.ALWAYS);
        if (active) box.getChildren().add(createForm(facade, nav));
        return box;
    }

    private static java.util.List<javafx.scene.Node> historyDetail(QuestView q) {
        boolean completed = q.status().equals("COMPLETED");
        Label name = new Label(q.name());
        name.getStyleClass().add("player-name");
        Label meta = UiKit.muted("Rango " + q.rank() + "  ·  " + (completed ? "✅ Completada" : "❌ Fallida"));
        Label outcome = new Label(completed
                ? "Ganaste  +" + UiKit.num(q.rewardXP()) + " XP"
                : "Perdiste  -" + q.penaltyHP() + " HP");
        outcome.getStyleClass().add("reward-hint");
        Label desc = UiKit.muted(q.description() == null || q.description().isBlank()
                ? "(sin descripción)" : q.description());
        desc.setWrapText(true);
        return List.of(UiKit.sectionTitle("DETALLE"), name, meta, outcome, UiKit.spacer(4), desc);
    }

    private static Region weeklySection(GameFacade facade) {
        List<WeeklyQuestView> weeklies = facade.weeklyQuests();
        long done = weeklies.stream().filter(WeeklyQuestView::completed).count();

        VBox box = new VBox(8, UiKit.sectionTitle("MISIONES SEMANALES   " + done + "/" + weeklies.size()));
        if (weeklies.isEmpty()) {
            box.getChildren().add(UiKit.muted("Sin semanales esta semana."));
        }
        HBox cards = new HBox(12);
        for (WeeklyQuestView w : weeklies) {
            Region card = weeklyCard(w);
            HBox.setHgrow(card, Priority.ALWAYS);
            cards.getChildren().add(card);
        }
        if (!weeklies.isEmpty()) box.getChildren().add(cards);
        box.getStyleClass().add("panel");
        return box;
    }

    private static Region weeklyCard(WeeklyQuestView w) {
        Label name = new Label(w.name() + (w.completed() ? "  ✅" : ""));
        name.getStyleClass().add("habit-done");
        Label desc = UiKit.muted(w.description());
        desc.setWrapText(true);

        double pct = w.target() <= 0 ? 0 : (double) w.currentProgress() / w.target();
        Region bar = UiKit.bar(pct, "bar-xp", 180);
        Label progress = UiKit.muted(w.progressText());

        String plazo = w.completed() ? "completada" : w.daysRemaining() + " días restantes";
        HBox meta = new HBox(10, UiKit.caption(rewardText(w)),
                UiKit.hgrow(), UiKit.muted(plazo));

        VBox card = new VBox(5, name, desc, bar, progress, meta);
        card.getStyleClass().add("habit-row");
        card.setMinWidth(220);
        return card;
    }

    private static String rewardText(WeeklyQuestView w) {
        StringBuilder sb = new StringBuilder();
        if (w.rewardXP() > 0) sb.append("+").append(UiKit.num(w.rewardXP())).append(" XP");
        if (w.rewardGold() > 0) {
            if (sb.length() > 0) sb.append("   ");
            sb.append("+").append(UiKit.num(w.rewardGold())).append(" G");
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }

    private static Region questRow(QuestView q, Consumer<QuestView> onSelect) {
        Label name = new Label(q.name());
        name.getStyleClass().add("habit-done");
        Label rank = new Label(q.rank());
        rank.getStyleClass().add("rank-badge");
        String plazo = q.deadline() == null ? "sin plazo" : "vence " + q.deadline();
        HBox row = new HBox(rank, UiKit.hgrow(), UiKit.muted(plazo));
        VBox card = new VBox(4, name, row);
        card.getStyleClass().add("habit-row");
        card.setOnMouseClicked(e -> onSelect.accept(q));
        return card;
    }

    private static java.util.List<javafx.scene.Node> detailContent(QuestView q, GameFacade facade, Nav nav) {
        Label name = new Label(q.name());
        name.getStyleClass().add("player-name");
        Label meta = UiKit.muted("Rango " + q.rank()
                + (q.deadline() == null ? "  ·  sin plazo" : "  ·  vence " + q.deadline()));
        Label reward = new Label("Recompensa al completar:  +" + UiKit.num(q.rewardXP()) + " XP");
        reward.getStyleClass().add("reward-hint");
        Label penalty = UiKit.muted("Si fallas o vence el plazo:  -" + q.penaltyHP() + " HP");
        Label desc = UiKit.muted(q.description() == null || q.description().isBlank()
                ? "(sin descripción)" : q.description());
        desc.setWrapText(true);

        Button complete = new Button("COMPLETAR");
        complete.getStyleClass().add("continue-btn");
        if (q.playableNow()) {
            complete.setOnAction(e -> { facade.completeQuest(q.id()); nav.quests(); });
        } else {
            complete.setDisable(true);
            complete.setTooltip(new Tooltip("Demasiado cansado para completar una misión de Rango " + q.rank()));
        }
        Button abandon = UiKit.navButton("Abandonar", () -> { facade.failQuest(q.id()); nav.quests(); });

        HBox actions = new HBox(12, complete, UiKit.hgrow(), abandon);
        actions.setAlignment(Pos.CENTER_LEFT);

        return List.of(UiKit.sectionTitle("DETALLE"), name, meta, reward, penalty,
                UiKit.spacer(4), desc, UiKit.spacer(6), actions);
    }

    private static Region createForm(GameFacade facade, Nav nav) {
        TextField name = new TextField();
        name.getStyleClass().add("sys-field");
        name.setPromptText("Nombre del contrato");
        HBox.setHgrow(name, Priority.ALWAYS);

        TextField description = new TextField();
        description.getStyleClass().add("sys-field");
        description.setPromptText("Descripción (opcional)");
        HBox.setHgrow(description, Priority.ALWAYS);

        ComboBox<QuestRank> rank = new ComboBox<>();
        rank.getItems().addAll(QuestRank.values());
        rank.setValue(QuestRank.E);
        rank.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(QuestRank r) {
                return r == null ? "" : r.getLetter() + "  ·  " + r.getDifficultyName();
            }
            @Override public QuestRank fromString(String s) { return null; }
        });

        DatePicker deadline = new DatePicker();
        deadline.setPromptText("Plazo (opcional)");

        Label hint = new Label();
        hint.getStyleClass().add("reward-hint");
        Runnable refreshHint = () -> {
            QuestRank r = rank.getValue();
            hint.setText("+" + r.getBaseXP() + " XP al completar  ·  -" + r.getMoralDamage() + " HP si fallas");
        };
        rank.valueProperty().addListener((o, a, b) -> refreshHint.run());
        refreshHint.run();

        Label error = new Label();
        error.getStyleClass().add("muted");

        Button create = new Button("CREAR CONTRATO");
        create.getStyleClass().add("continue-btn");
        create.setOnAction(e -> {
            String n = name.getText() == null ? "" : name.getText().trim();
            if (n.isEmpty()) { error.setText("⚠️ El nombre no puede estar vacío."); return; }
            LocalDate dl = deadline.getValue();
            if (dl != null && dl.isBefore(LocalDate.now())) { error.setText("⚠️ El deadline no puede estar en el pasado."); return; }
            facade.createQuest(n, description.getText() == null ? "" : description.getText().trim(), rank.getValue(), dl);
            nav.quests();
        });

        Runnable refreshCreateState = () -> {
            boolean allowed = facade.canAttemptRank(rank.getValue());
            create.setDisable(!allowed);
            create.setTooltip(allowed ? null
                    : new Tooltip("Demasiado cansado para iniciar una misión de Rango " + rank.getValue().getLetter()));
        };
        rank.valueProperty().addListener((o, a, b) -> refreshCreateState.run());
        refreshCreateState.run();

        HBox line1 = new HBox(10, name, description);
        HBox line2 = new HBox(10, rank, deadline, hint, UiKit.hgrow(), create);
        line2.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(8, UiKit.sectionTitle("NUEVO CONTRATO"), line1, line2, error);
        form.getStyleClass().add("panel");
        return form;
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private QuestsScreen() {}
}
