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

/**
 * Pantalla The Armory (mockup 4.inventory): tienda + inventario en una, con toggle SHOP ⇄ INVENTORY.
 * SHOP: catálogo (`shopCatalog()`), comprar. INVENTORY: loadout por slot + mochila, equipar/consumir.
 * El cambio de modo y las acciones repintan el centro manteniendo el modo (sin salir de la pantalla).
 */
final class ArmoryScreen {

    static Region build(GameFacade facade, Nav nav) {
        VBox content = new VBox(12);

        Button shopBtn = new Button("TIENDA");
        Button invBtn = new Button("INVENTARIO");

        Runnable showShop = new Runnable() {
            @Override public void run() {
                activate(shopBtn, invBtn);
                content.getChildren().setAll(shopPane(facade, this));
            }
        };
        Runnable showInv = new Runnable() {
            @Override public void run() {
                activate(invBtn, shopBtn);
                content.getChildren().setAll(inventoryPane(facade, this));
            }
        };
        shopBtn.setOnAction(e -> showShop.run());
        invBtn.setOnAction(e -> showInv.run());

        Label title = new Label("ARMERÍA");
        title.getStyleClass().add("screen-title");
        HBox toggle = new HBox(8, shopBtn, invBtn);
        HBox header = new HBox(16, title, UiKit.hgrow(), toggle);
        header.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        if ("inv".equals(System.getenv("LL_ARMORY_MODE"))) showInv.run(); else showShop.run();

        VBox root = new VBox(12, header, scroll, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    // ---- Modo SHOP (agrupado por categoría) ----
    private static Region shopPane(GameFacade facade, Runnable refresh) {
        VBox pane = new VBox(14, UiKit.muted("🪙 " + UiKit.num(facade.state().gold()) + " G disponible"));

        java.util.Map<String, java.util.List<ShopItemView>> byCategory = new java.util.LinkedHashMap<>();
        for (ShopItemView item : facade.shopCatalog()) {
            byCategory.computeIfAbsent(item.category(), k -> new java.util.ArrayList<>()).add(item);
        }
        for (var entry : byCategory.entrySet()) {
            FlowPane grid = new FlowPane(12, 12);
            for (ShopItemView item : entry.getValue()) {
                grid.getChildren().add(shopCard(item, facade, refresh));
            }
            pane.getChildren().addAll(UiKit.sectionTitle(entry.getKey()), grid);
        }
        return pane;
    }

    private static Region shopCard(ShopItemView item, GameFacade facade, Runnable refresh) {
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

        return new VBox(10,
                UiKit.sectionTitle("EQUIPO"), slots,
                UiKit.spacer(6),
                UiKit.sectionTitle("MOCHILA"), bag);
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
