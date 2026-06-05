package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.DailyChecklistView;
import com.lifeleveling.application.dto.PlayerView;
import com.lifeleveling.application.dto.StatsView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

/**
 * Pantalla Home / Status (mockup 1.panel-control). Lee del GameFacade y pinta:
 * status (HP) · nivel + barra XP + radar de stats · checklist de los 7 hábitos · barra inferior.
 */
final class HomeScreen {

    private static final Locale ES = Locale.forLanguageTag("es-ES");

    static Region build(GameFacade facade, Runnable onContinue) {
        PlayerView p = facade.state();
        DailyChecklistView checklist = facade.dailyChecklist();

        HBox columns = new HBox(24, statusColumn(p), levelColumn(p), dailyColumn(checklist));
        columns.getStyleClass().add("columns");
        VBox.setVgrow(columns, Priority.ALWAYS);

        VBox root = new VBox(columns, bottomBar(p, onContinue));
        root.getStyleClass().add("home");
        return root;
    }

    // ---- Columna izquierda: identidad + HP ----
    private static Region statusColumn(PlayerView p) {
        Label name = new Label(p.name());
        name.getStyleClass().add("player-name");
        Label rank = new Label(p.rank() + "  ·  " + "RANK");
        rank.getStyleClass().add("rank-badge");

        Label statusTitle = sectionTitle("STATUS");
        boolean critical = p.currentHP() < 30;
        Region hp = bar(p.currentHP() / 100.0, critical ? "bar-hp-critical" : "bar-hp");
        Label hpText = new Label(p.currentHP() + " / 100 HP   " + p.hpState());
        hpText.getStyleClass().add("muted");

        Label debuffs = sectionTitle("ACTIVE DEBUFFS");
        HBox slots = new HBox(6);
        for (int i = 0; i < 6; i++) {
            Region slot = new Region();
            slot.getStyleClass().add("debuff-slot");
            slots.getChildren().add(slot);
        }

        VBox box = new VBox(10, name, rank, spacer(8), statusTitle, hp, hpText, spacer(8), debuffs, slots);
        box.getStyleClass().add("panel");
        box.setPrefWidth(240);
        return box;
    }

    // ---- Columna centro: nivel + XP + pentagon ----
    private static Region levelColumn(PlayerView p) {
        Label lvlCaption = new Label("PLAYER LEVEL");
        lvlCaption.getStyleClass().add("caption");
        Label lvl = new Label(String.valueOf(p.level()));
        lvl.getStyleClass().add("big-level");

        Region xp = bar(p.xpForLevelSpan() == 0 ? 1.0 : (double) p.xpIntoLevel() / p.xpForLevelSpan(), "bar-xp");
        Label xpText = new Label(num(p.xpIntoLevel()) + " / " + num(p.xpForLevelSpan()) + " XP");
        xpText.getStyleClass().add("muted");

        StatsView s = p.stats();
        StatRadar radar = new StatRadar(220);
        radar.render(s.strength(), s.intellect(), s.wisdom(), s.discipline(), s.charisma());

        VBox box = new VBox(6, lvlCaption, lvl, xp, xpText, spacer(6), radar);
        box.setAlignment(Pos.TOP_CENTER);
        box.getStyleClass().add("panel");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    // ---- Columna derecha: checklist de hábitos ----
    private static Region dailyColumn(DailyChecklistView checklist) {
        Label title = sectionTitle("DAILY QUESTS   " + checklist.completedCount() + "/" + checklist.total());
        VBox box = new VBox(8, title);
        for (DailyChecklistView.Habit h : checklist.habits()) {
            Label label = new Label(h.label());
            label.getStyleClass().add(h.done() ? "habit-done" : "habit-pending");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label mark = new Label(h.done() ? "✔" : "○");
            mark.getStyleClass().add(h.done() ? "check-done" : "check-pending");
            HBox row = new HBox(label, spacer, mark);
            row.getStyleClass().add("habit-row");
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
        box.getStyleClass().add("panel");
        box.setPrefWidth(260);
        return box;
    }

    // ---- Barra inferior: navegación + oro ----
    private static Region bottomBar(PlayerView p, Runnable onContinue) {
        HBox nav = new HBox(10);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.getChildren().addAll(
                navButton("Daily"), navButton("Quests"), navButton("Inventory"));

        Button cont = new Button("CONTINUE  ▸");
        cont.getStyleClass().add("continue-btn");
        cont.setOnAction(e -> onContinue.run());

        HBox right = new HBox(10, navButton("Titles"), navButton("Journal"));
        Label gold = new Label("🪙 " + num(p.gold()) + " G");
        gold.getStyleClass().add("gold");

        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);

        HBox bar = new HBox(10, nav, s1, cont, s2, right, gold);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    // ---- helpers ----
    private static Button navButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        return b;
    }

    private static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        return l;
    }

    private static Region bar(double pct, String fillStyle) {
        double clamped = Math.max(0, Math.min(1, pct));
        Region track = new Region();
        track.getStyleClass().add("bar-track");
        track.setPrefSize(210, 12);
        Region fill = new Region();
        fill.getStyleClass().add(fillStyle);
        fill.setPrefHeight(12);
        fill.prefWidthProperty().bind(track.widthProperty().multiply(clamped));
        fill.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane pane = new StackPane(track, fill);
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setMaxWidth(210);
        return pane;
    }

    private static Region spacer(double h) {
        Region r = new Region();
        r.setMinHeight(h);
        return r;
    }

    private static String num(long n) {
        return String.format(ES, "%,d", n);
    }

    private HomeScreen() {}
}
