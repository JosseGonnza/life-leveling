package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.DailyChecklistView;
import com.lifeleveling.application.dto.PlayerView;
import com.lifeleveling.application.dto.StatsView;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Pantalla Home / Status (mockup 1.panel-control). Lee del GameFacade y pinta:
 * status (HP) · nivel + barra XP + radar de stats · checklist de los 7 hábitos · barra inferior.
 */
final class HomeScreen {

    static Region build(GameFacade facade, Nav nav) {
        PlayerView p = facade.state();
        DailyChecklistView checklist = facade.dailyChecklist();

        HBox columns = new HBox(24, statusColumn(p), levelColumn(p), dailyColumn(checklist));
        columns.getStyleClass().add("columns");
        VBox.setVgrow(columns, Priority.ALWAYS);

        VBox root = new VBox(columns, bottomBar(p, nav));
        root.getStyleClass().add("home");
        return root;
    }

    private static Region statusColumn(PlayerView p) {
        Label name = new Label(p.name());
        name.getStyleClass().add("player-name");
        Label rank = new Label(p.rank() + "  ·  RANK");
        rank.getStyleClass().add("rank-badge");

        boolean critical = p.currentHP() < 30;
        Region hp = UiKit.bar(p.currentHP() / 100.0, critical ? "bar-hp-critical" : "bar-hp", 210);
        Label hpText = UiKit.muted(p.currentHP() + " / 100 HP   " + p.hpState());

        HBox slots = new HBox(6);
        for (int i = 0; i < 6; i++) {
            Region slot = new Region();
            slot.getStyleClass().add("debuff-slot");
            slots.getChildren().add(slot);
        }

        VBox box = new VBox(10, name, rank, UiKit.spacer(8),
                UiKit.sectionTitle("STATUS"), hp, hpText, UiKit.spacer(8),
                UiKit.sectionTitle("ACTIVE DEBUFFS"), slots);
        box.getStyleClass().add("panel");
        box.setPrefWidth(240);
        return box;
    }

    private static Region levelColumn(PlayerView p) {
        Label lvl = new Label(String.valueOf(p.level()));
        lvl.getStyleClass().add("big-level");

        double xpPct = p.xpForLevelSpan() == 0 ? 1.0 : (double) p.xpIntoLevel() / p.xpForLevelSpan();
        Region xp = UiKit.bar(xpPct, "bar-xp", 210);
        Label xpText = UiKit.muted(UiKit.num(p.xpIntoLevel()) + " / " + UiKit.num(p.xpForLevelSpan()) + " XP");

        StatsView s = p.stats();
        StatRadar radar = new StatRadar(220);
        radar.render(s.strength(), s.intellect(), s.wisdom(), s.discipline(), s.charisma());

        VBox box = new VBox(6, UiKit.caption("PLAYER LEVEL"), lvl, xp, xpText, UiKit.spacer(6), radar);
        box.setAlignment(Pos.TOP_CENTER);
        box.getStyleClass().add("panel");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Region dailyColumn(DailyChecklistView checklist) {
        VBox box = new VBox(8, UiKit.sectionTitle(
                "DAILY QUESTS   " + checklist.completedCount() + "/" + checklist.total()));
        for (DailyChecklistView.Habit h : checklist.habits()) {
            Label label = new Label(h.label());
            label.getStyleClass().add(h.done() ? "habit-done" : "habit-pending");
            Label mark = new Label(h.done() ? "✔" : "○");
            mark.getStyleClass().add(h.done() ? "check-done" : "check-pending");
            HBox row = new HBox(label, UiKit.hgrow(), mark);
            row.getStyleClass().add("habit-row");
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
        box.getStyleClass().add("panel");
        box.setPrefWidth(260);
        return box;
    }

    private static Region bottomBar(PlayerView p, Nav nav) {
        HBox left = new HBox(10,
                UiKit.navButton("Daily", nav::daily),
                UiKit.navButton("Quests", nav::quests),
                UiKit.navButton("Gates", nav::gates),
                UiKit.navButton("Armory", nav::armory));
        left.setAlignment(Pos.CENTER_LEFT);

        Button cont = new Button("CONTINUE  ▸");
        cont.getStyleClass().add("continue-btn");
        cont.setOnAction(e -> nav.continueDay());

        HBox right = new HBox(10,
                UiKit.navButton("Titles", () -> nav.todo("Titles")),
                UiKit.navButton("Journal", () -> nav.todo("Journal")));
        Label gold = new Label("🪙 " + UiKit.num(p.gold()) + " G");
        gold.getStyleClass().add("gold");

        HBox bar = new HBox(10, left, UiKit.hgrow(), cont, UiKit.hgrow(), right, gold);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private HomeScreen() {}
}
