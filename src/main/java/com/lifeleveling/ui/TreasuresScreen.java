package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.TreasureView;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Pantalla Tesoros (cap 3.4 Biblia): money sinks de estatus. Se ven siempre como metas;
 * reclamar exige Rango A (Senior). No otorgan stats, son trofeos.
 */
final class TreasuresScreen {

    private static final Map<String, String> BADGE = Map.of(
            "treasure_setup", "🖥️",
            "treasure_trip", "⛩️",
            "treasure_car", "🏎️",
            "treasure_freedom", "🏛️");

    static Region build(GameFacade facade, Nav nav) {
        boolean unlocked = facade.treasuresUnlocked();

        Label title = new Label("TESOROS   ·   TROFEOS DE ESTATUS");
        title.getStyleClass().add("screen-title");

        VBox content = new VBox(12);
        content.getChildren().add(UiKit.muted("🪙 " + UiKit.num(facade.state().gold()) + " G disponible"));
        if (!unlocked) {
            Label lock = new Label("🔒 Rango A (Senior) requerido para reclamar Tesoros");
            lock.getStyleClass().add("check-pending");
            content.getChildren().add(lock);
        }

        FlowPane grid = new FlowPane(14, 14);
        for (TreasureView t : facade.treasures()) {
            grid.getChildren().add(card(t, unlocked, facade, nav));
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

    private static Region card(TreasureView t, boolean unlocked, GameFacade facade, Nav nav) {
        Label name = new Label(BADGE.getOrDefault(t.id(), "💎") + "  " + t.name());
        name.getStyleClass().add("item-name");
        name.setWrapText(true);
        Label lore = UiKit.muted(t.lore());
        lore.setWrapText(true);
        Label price = new Label("🪙 " + UiKit.num(t.price()) + " G");
        price.getStyleClass().add("price");

        VBox card = new VBox(4, name, lore, UiKit.spacer(2), price, action(t, unlocked, facade, nav));
        boolean claimable = unlocked && t.affordable();
        card.getStyleClass().add(t.owned() || claimable ? "shop-card" : "shop-card-locked");
        return card;
    }

    private static Region action(TreasureView t, boolean unlocked, GameFacade facade, Nav nav) {
        if (t.owned()) {
            Label got = new Label("✓ CONSEGUIDO");
            got.getStyleClass().add("check-done");
            return got;
        }
        Button btn = new Button();
        btn.getStyleClass().add("nav-btn");
        if (!unlocked) {
            btn.setText("🔒 Rango A");
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Alcanza el Rango A (Senior) para reclamar Tesoros"));
        } else if (t.affordable()) {
            btn.setText("Reclamar");
            btn.setOnAction(e -> { facade.buy(t.id()); nav.treasures(); });
        } else {
            btn.setText("Oro insuficiente");
            btn.setDisable(true);
        }
        return btn;
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }
}
