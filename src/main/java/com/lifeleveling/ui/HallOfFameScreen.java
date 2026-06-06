package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.TitlesView;
import com.lifeleveling.application.dto.TitlesView.TitleEntryView;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Pantalla Hall of Fame (mockup 5.titles): sellos equipados (izq) · colección por categoría (centro)
 * · detalle del título seleccionado con equipar/desequipar (der). Datos de `titles()`.
 */
final class HallOfFameScreen {

    static Region build(GameFacade facade, Nav nav) {
        TitlesView tv = facade.titles();

        VBox detail = new VBox(10);
        detail.getStyleClass().add("panel");
        detail.setPrefWidth(300);
        Consumer<TitleEntryView> showDetail = e -> detail.getChildren().setAll(detailContent(e, tv, facade, nav));

        Region seals = sealsColumn(tv, facade, nav);
        Region collection = collectionColumn(tv, showDetail);

        showDetail.accept(tv.collection().isEmpty() ? null : tv.collection().get(0));

        HBox columns = new HBox(18, seals, collection, detail);
        HBox.setHgrow(collection, Priority.ALWAYS);
        VBox.setVgrow(columns, Priority.ALWAYS);

        Label title = new Label("HALL OF FAME   ·   " + tv.unlockedCount() + "/" + tv.totalCount());
        title.getStyleClass().add("screen-title");

        VBox root = new VBox(12, title, columns, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    // ---- Sellos activos (equipados) ----
    private static Region sealsColumn(TitlesView tv, GameFacade facade, Nav nav) {
        VBox box = new VBox(10, UiKit.sectionTitle("SELLOS ACTIVOS   " + tv.usedSlots() + "/" + tv.availableSlots()));
        if (tv.equipped().isEmpty()) {
            box.getChildren().add(UiKit.muted("Sin sellos equipados."));
        }
        for (TitleEntryView e : tv.equipped()) {
            Label name = new Label(e.name());
            name.getStyleClass().add("item-name");
            name.setWrapText(true);
            Label buffs = UiKit.muted(e.buffs());
            buffs.setWrapText(true);
            Button off = UiKit.navButton("Desequipar", () -> { facade.unequipTitle(e.type()); nav.titles(); });
            VBox card = new VBox(6, name, buffs, off);
            card.getStyleClass().add("seal-card");
            box.getChildren().add(card);
        }
        if (tv.availableSlots() < 2) {
            box.getChildren().add(UiKit.muted("🔒 2º sello a nivel 50"));
        }
        box.getStyleClass().add("panel");
        box.setPrefWidth(260);
        return box;
    }

    // ---- Colección por categoría ----
    private static Region collectionColumn(TitlesView tv, Consumer<TitleEntryView> showDetail) {
        FlowPane grid = new FlowPane(10, 10);
        ScrollPane scroll = new ScrollPane(grid);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Set<String> categories = new LinkedHashSet<>();
        for (TitleEntryView e : tv.collection()) categories.add(e.category());

        List<Button> catButtons = new ArrayList<>();
        FlowPane catBar = new FlowPane(8, 8);
        for (String cat : categories) {
            Button b = new Button(cat);
            b.getStyleClass().add("nav-btn");
            b.setMinWidth(Region.USE_PREF_SIZE);
            b.setOnAction(ev -> {
                catButtons.forEach(x -> x.getStyleClass().setAll("nav-btn"));
                b.getStyleClass().setAll("continue-btn");
                fillGrid(grid, tv, cat, showDetail);
            });
            catButtons.add(b);
            catBar.getChildren().add(b);
        }
        if (!catButtons.isEmpty()) catButtons.get(0).fire();

        VBox box = new VBox(10, UiKit.sectionTitle("COLECCIÓN"), catBar, scroll);
        box.getStyleClass().add("panel");
        return box;
    }

    private static void fillGrid(FlowPane grid, TitlesView tv, String category, Consumer<TitleEntryView> showDetail) {
        grid.getChildren().clear();
        for (TitleEntryView e : tv.collection()) {
            if (!e.category().equals(category)) continue;
            Label name = new Label((e.unlocked() ? "" : "🔒 ") + e.name());
            name.getStyleClass().add(e.unlocked() ? "item-name" : "habit-pending");
            name.setWrapText(true);
            VBox chip = new VBox(name);
            chip.getStyleClass().add(e.unlocked() ? "title-chip" : "title-chip-locked");
            chip.setOnMouseClicked(ev -> showDetail.accept(e));
            grid.getChildren().add(chip);
        }
    }

    // ---- Detalle ----
    private static List<Node> detailContent(TitleEntryView e, TitlesView tv, GameFacade facade, Nav nav) {
        if (e == null) return List.of(UiKit.sectionTitle("DETALLE"), UiKit.muted("Selecciona un título."));

        Label name = new Label(e.name());
        name.getStyleClass().add("player-name");
        name.setWrapText(true);
        Label cat = UiKit.caption(e.category());
        Label buffs = new Label(e.buffs());
        buffs.getStyleClass().add("reward-hint");
        buffs.setWrapText(true);
        Label req = UiKit.muted("Requisito: " + e.requirement());
        req.setWrapText(true);
        Label lore = UiKit.muted(e.lore());
        lore.setWrapText(true);

        Node action;
        if (!e.unlocked()) {
            action = UiKit.muted("🔒 BLOQUEADO");
        } else if (e.equipped()) {
            action = UiKit.navButton("Desequipar", () -> { facade.unequipTitle(e.type()); nav.titles(); });
        } else if (tv.usedSlots() < tv.availableSlots()) {
            Button equip = new Button("Equipar");
            equip.getStyleClass().add("continue-btn");
            equip.setOnAction(ev -> { facade.equipTitle(e.type()); nav.titles(); });
            action = equip;
        } else {
            Button full = new Button("Sin slots libres");
            full.getStyleClass().add("nav-btn");
            full.setDisable(true);
            action = full;
        }

        return List.of(UiKit.sectionTitle("DETALLE"), name, cat, buffs,
                UiKit.spacer(4), req, lore, UiKit.spacer(6), action);
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private HallOfFameScreen() {}
}
