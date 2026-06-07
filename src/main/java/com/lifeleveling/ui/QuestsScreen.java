package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.QuestView;
import com.lifeleveling.domain.quest.shared.QuestRank;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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

    static Region build(GameFacade facade, Nav nav) {
        List<QuestView> quests = facade.activeQuests();

        VBox detail = new VBox(10);
        detail.getStyleClass().add("panel");
        detail.setPrefWidth(320);
        Consumer<QuestView> showDetail = q -> detail.getChildren().setAll(detailContent(q, facade, nav));

        VBox listBox = new VBox(8, UiKit.sectionTitle("CONTRATOS ACTIVOS   " + quests.size()));
        if (quests.isEmpty()) {
            listBox.getChildren().add(UiKit.muted("Sin contratos. Crea uno abajo."));
        }
        for (QuestView q : quests) {
            listBox.getChildren().add(questRow(q, showDetail));
        }
        listBox.getStyleClass().add("panel");
        listBox.setPrefWidth(340);

        if (!quests.isEmpty()) showDetail.accept(quests.get(0));
        else detail.getChildren().add(UiKit.muted("Selecciona o crea un contrato."));

        HBox middle = new HBox(20, listBox, detail);
        HBox.setHgrow(detail, Priority.ALWAYS);
        VBox.setVgrow(middle, Priority.ALWAYS);

        Label title = new Label("MISIONES — CONTRATOS");
        title.getStyleClass().add("screen-title");

        VBox root = new VBox(10, title, middle, createForm(facade, nav), backBar(nav));
        root.getStyleClass().add("screen");
        return root;
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
        Label reward = new Label("+" + UiKit.num(q.rewardXP()) + " XP     /     -" + q.penaltyHP() + " HP");
        reward.getStyleClass().add("reward-hint");
        Label desc = UiKit.muted(q.description() == null || q.description().isBlank()
                ? "(sin descripción)" : q.description());
        desc.setWrapText(true);

        Button complete = new Button("COMPLETAR");
        complete.getStyleClass().add("continue-btn");
        complete.setOnAction(e -> { facade.completeQuest(q.id()); nav.quests(); });
        Button abandon = UiKit.navButton("Abandonar", () -> { facade.failQuest(q.id()); nav.quests(); });

        HBox actions = new HBox(12, complete, UiKit.hgrow(), abandon);
        actions.setAlignment(Pos.CENTER_LEFT);

        return List.of(UiKit.sectionTitle("DETALLE"), name, meta, reward,
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

        DatePicker deadline = new DatePicker();
        deadline.setPromptText("Plazo (opcional)");

        Label hint = new Label();
        hint.getStyleClass().add("reward-hint");
        Runnable refreshHint = () -> {
            QuestRank r = rank.getValue();
            hint.setText("+" + r.getBaseXP() + " XP  /  -" + r.getMoralDamage() + " HP");
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
