package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.ElderQuestView;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Pantalla Juicios del Monarca (cap 2.5 Biblia): los 7 Elder Quests del endgame.
 * Se desbloquea al Nivel 75. v1 read-only: muestra objetivo + progreso real + recompensa,
 * sin "reclamar" (Judgment Day) ni Season Lock aún.
 */
final class JudgmentsScreen {

    static Region build(GameFacade facade, Nav nav) {
        boolean unlocked = facade.elderUnlocked();

        Label title = new Label("JUICIOS DEL MONARCA   ·   NIVEL 75+");
        title.getStyleClass().add("screen-title");

        VBox content = new VBox(12);
        if (!unlocked) {
            Label lock = new Label("🔒 Nivel 75 requerido — el Endgame del Monarca");
            lock.getStyleClass().add("check-pending");
            content.getChildren().add(lock);
        }

        FlowPane grid = new FlowPane(14, 14);
        for (ElderQuestView e : facade.elderQuests()) {
            grid.getChildren().add(card(e));
        }
        content.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("screen-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(12, title, scroll, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    private static Region card(ElderQuestView e) {
        Label name = new Label(e.icon() + "  " + e.name());
        name.getStyleClass().add("item-name");
        name.setWrapText(true);
        Label freq = UiKit.caption(e.frequency().toUpperCase());
        Label lore = UiKit.muted(e.lore());
        lore.setWrapText(true);

        VBox card = new VBox(6, name, freq, lore);
        card.setPrefWidth(330);

        for (String obj : e.objectives()) {
            Label o = UiKit.muted("▸ " + obj);
            o.setWrapText(true);
            card.getChildren().add(o);
        }
        card.getChildren().add(UiKit.bar(e.progress(), "bar-xp", 300));

        Label reward = new Label("🎁 " + e.reward());
        reward.getStyleClass().add("price");
        reward.setWrapText(true);
        card.getChildren().add(reward);

        if (e.completed()) {
            Label done = new Label("✓ Cumplido (pendiente de veredicto)");
            done.getStyleClass().add("check-done");
            card.getChildren().add(done);
        }

        card.getStyleClass().add(e.completed() ? "shop-card" : "shop-card-locked");
        return card;
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }
}
