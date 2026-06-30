package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.InventoryView;
import com.lifeleveling.application.dto.ShopItemView;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Pantalla The Armory (mockup 4.inventory): tienda + inventario en una, con toggle SHOP ⇄ INVENTORY.
 * SHOP: catálogo (`shopCatalog()`) en grid + panel de detalle a la derecha (clic en ítem → qué hace).
 * INVENTORY: loadout por slot + mochila, equipar/consumir.
 */
final class ArmoryScreen {

    static Region build(GameFacade facade, Nav nav) {
        VBox content = new VBox(12);
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setFillWidth(true);

        Button shopBtn = new Button("TIENDA");
        Button invBtn = new Button("INVENTARIO");

        Runnable showShop = new Runnable() {
            @Override public void run() {
                activate(shopBtn, invBtn);
                Region pane = shopPane(facade, this);
                VBox.setVgrow(pane, Priority.ALWAYS);
                content.getChildren().setAll(pane);
            }
        };
        Runnable showInv = new Runnable() {
            @Override public void run() {
                activate(invBtn, shopBtn);
                Region pane = inventoryPane(facade, this);
                VBox.setVgrow(pane, Priority.ALWAYS);
                content.getChildren().setAll(pane);
            }
        };
        shopBtn.setOnAction(e -> showShop.run());
        invBtn.setOnAction(e -> showInv.run());

        Label title = new Label("ARMERÍA");
        title.getStyleClass().add("screen-title");
        HBox toggle = new HBox(8, shopBtn, invBtn);
        HBox header = new HBox(16, title, UiKit.hgrow(), toggle);
        header.setAlignment(Pos.CENTER_LEFT);

        if ("inv".equals(System.getenv("LL_ARMORY_MODE"))) showInv.run(); else showShop.run();

        VBox root = new VBox(12, header, content, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    // ---- Modo SHOP (grid agrupado por categoría + panel de detalle) ----
    private static Region shopPane(GameFacade facade, Runnable refresh) {
        boolean burnout = facade.state().burnout();

        VBox grid = new VBox(14, UiKit.muted("🪙 " + UiKit.num(facade.state().gold()) + " G disponible"));
        if (burnout) {
            Label note = new Label("💔 BURNOUT: la tienda solo despacha curas y la vía de salida rápida.");
            note.setWrapText(true);
            note.getStyleClass().add("burnout-banner-text");
            VBox banner = new VBox(note);
            banner.getStyleClass().add("burnout-banner");
            grid.getChildren().add(banner);
        }

        VBox detail = new VBox(10);
        detail.getStyleClass().add("panel");
        detail.setPrefWidth(320);
        detail.setMinWidth(320);
        Consumer<ShopItemView> select = item -> fillDetail(detail, item, facade, refresh);

        java.util.Map<String, java.util.List<ShopItemView>> byCategory = new java.util.LinkedHashMap<>();
        for (ShopItemView item : facade.shopCatalog()) {
            if (burnout && !item.burnoutSafe()) continue;
            byCategory.computeIfAbsent(item.category(), k -> new java.util.ArrayList<>()).add(item);
        }
        ShopItemView first = null;
        for (var entry : byCategory.entrySet()) {
            FlowPane row = new FlowPane(12, 12);
            for (ShopItemView item : entry.getValue()) {
                if (first == null) first = item;
                row.getChildren().add(shopCard(item, facade, refresh, select));
            }
            grid.getChildren().addAll(UiKit.sectionTitle(entry.getKey()), row);
        }

        if (first != null) select.accept(first);
        else detail.getChildren().add(UiKit.muted("No hay ítems disponibles."));

        ScrollPane scroll = new ScrollPane(grid);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        HBox.setHgrow(scroll, Priority.ALWAYS);

        HBox pane = new HBox(16, scroll, detail);
        return pane;
    }

    private static void fillDetail(VBox detail, ShopItemView item, GameFacade facade, Runnable refresh) {
        Label name = new Label(item.name());
        name.getStyleClass().add("player-name");
        name.setWrapText(true);
        Label cat = UiKit.caption(item.category().toUpperCase(UiKit.ES)
                + (item.slot().equals("—") ? "" : "  ·  " + item.slot()));

        Label effect = new Label(item.effect());
        effect.getStyleClass().add("reward-hint");
        effect.setWrapText(true);

        Label desc = UiKit.muted(item.description());
        desc.setWrapText(true);

        Label price = new Label("🪙 " + UiKit.num(item.price()) + " G");
        price.getStyleClass().add("price");

        Button buy = new Button(item.unlocked() ? "Comprar" : "🔒 Requiere nivel " + item.requiredLevel());
        buy.getStyleClass().add("continue-btn");
        buy.setMaxWidth(Double.MAX_VALUE);
        buy.setDisable(!item.unlocked() || !item.affordable());
        if (item.unlocked()) {
            buy.setOnAction(e -> { facade.buy(item.id()); refresh.run(); });
            if (!item.affordable()) buy.setText("Sin oro suficiente");
        }

        detail.getChildren().setAll(
                UiKit.sectionTitle("DETALLE"),
                name, cat,
                UiKit.spacer(4), effect,
                UiKit.spacer(2), desc,
                UiKit.spacer(6), price, buy);
    }

    private static Region shopCard(ShopItemView item, GameFacade facade, Runnable refresh,
                                   Consumer<ShopItemView> select) {
        Label name = new Label(item.name());
        name.getStyleClass().add("item-name");
        name.setWrapText(true);
        Label effect = UiKit.muted(item.effect());
        effect.setWrapText(true);
        Label price = new Label("🪙 " + UiKit.num(item.price()) + " G");
        price.getStyleClass().add("price");

        Button buy = new Button(item.unlocked() ? "Comprar" : "🔒 Nivel " + item.requiredLevel());
        buy.getStyleClass().add("nav-btn");
        buy.setDisable(!item.unlocked() || !item.affordable());
        if (item.unlocked()) {
            buy.setOnAction(e -> { facade.buy(item.id()); refresh.run(); });
        } else {
            buy.setTooltip(new Tooltip("Requiere nivel " + item.requiredLevel()));
        }

        VBox card = new VBox(4, name, effect, UiKit.spacer(2), price, buy);
        card.getStyleClass().add(item.unlocked() && item.affordable() ? "shop-card" : "shop-card-locked");
        card.setOnMouseClicked(e -> select.accept(item));
        return card;
    }

    // ---- Modo INVENTORY ----
    private static Region inventoryPane(GameFacade facade, Runnable refresh) {
        InventoryView inv = facade.inventory();

        FlowPane slots = new FlowPane(10, 10);
        for (InventoryView.SlotView s : inv.loadout()) {
            slots.getChildren().add(slotChip(s));
        }

        VBox bag = new VBox(8);
        if (inv.owned().isEmpty()) {
            bag.getChildren().add(UiKit.muted("Mochila vacía. Compra en la TIENDA."));
        }
        for (InventoryView.OwnedItemView item : inv.owned()) {
            bag.getChildren().add(ownedRow(item, facade, refresh));
        }

        VBox pane = new VBox(10,
                UiKit.sectionTitle("EQUIPO"), slots,
                UiKit.spacer(6),
                UiKit.sectionTitle("MOCHILA"), bag);
        ScrollPane scroll = new ScrollPane(pane);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static Region slotChip(InventoryView.SlotView s) {
        Label icon = new Label(s.icon());
        Label slotName = UiKit.caption(s.slot());
        Label item = UiKit.muted(s.filled() ? s.itemName() : "—");
        item.setWrapText(true);
        VBox chip = new VBox(2, icon, slotName, item);
        chip.setAlignment(Pos.CENTER);
        chip.getStyleClass().add("slot-chip");
        if (s.filled()) chip.getStyleClass().add("slot-chip-filled");
        return chip;
    }

    private static Region ownedRow(InventoryView.OwnedItemView item, GameFacade facade, Runnable refresh) {
        Label name = new Label(item.name());
        name.getStyleClass().add("habit-done");
        Label cat = UiKit.caption(item.category());

        HBox row = new HBox(10, name, UiKit.hgrow(), cat);
        if (item.equippable()) {
            Button equip = UiKit.navButton("Equipar", () -> { facade.equip(item.id()); refresh.run(); });
            row.getChildren().add(equip);
        }
        if (item.consumable()) {
            Button use = UiKit.navButton(item.onCooldown() ? "En cooldown" : "Consumir",
                    () -> { facade.consume(item.id()); refresh.run(); });
            use.setDisable(item.onCooldown());
            row.getChildren().add(use);
        }
        row.getStyleClass().add("habit-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static void activate(Button active, Button inactive) {
        active.getStyleClass().setAll("continue-btn");
        inactive.getStyleClass().setAll("nav-btn");
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private ArmoryScreen() {}
}
