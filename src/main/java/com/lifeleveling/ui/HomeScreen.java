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

        VBox root = new VBox();
        root.getStyleClass().add("home");
        if (p.burnout()) root.getChildren().add(burnoutBanner());
        root.getChildren().addAll(columns, bottomBar(p, nav));
        return root;
    }

    private static Region burnoutBanner() {
        Label title = new Label("💔 BURNOUT — LOCKDOWN ACTIVO");
        title.getStyleClass().add("burnout-banner-title");
        Label text = new Label("Has colapsado. Solo puedes descansar (SUEÑO/DIETA/CUIDADOS), "
                + "ver tu estado y comprar curas. El resto del sistema está bloqueado 24h.");
        text.setWrapText(true);
        text.getStyleClass().add("burnout-banner-text");
        VBox box = new VBox(4, title, text);
        box.getStyleClass().add("burnout-banner");
        return box;
    }

    private static Region statusColumn(PlayerView p) {
        Label name = new Label(p.name());
        name.getStyleClass().add("player-name");
        Label rank = new Label("RANGO " + p.rankLetter() + "  ·  " + p.rank());
        rank.getStyleClass().add("rank-badge");

        int hpVal = p.currentHP();
        String hpClass = hpVal <= 0 ? "bar-hp-critical" : hpVal < 50 ? "bar-hp-tired" : "bar-hp";
        Region hp = UiKit.bar(hpVal / 100.0, hpClass, 210);
        Label hpText = UiKit.muted(p.currentHP() + " / 100 HP   " + p.hpState());

        HBox slots = new HBox(6);
        for (int i = 0; i < 6; i++) {
            Region slot = new Region();
            slot.getStyleClass().add("debuff-slot");
            slots.getChildren().add(slot);
        }

        VBox box = new VBox(10, name, rank, UiKit.spacer(8),
                UiKit.sectionTitle("ESTADO"), hp, hpText, UiKit.spacer(8),
                UiKit.sectionTitle("DEBUFFS ACTIVOS"), slots);
        box.getStyleClass().add("panel");
        box.setPrefWidth(240);
        return box;
    }

    private static Region levelColumn(PlayerView p) {
        Label lvl = new Label(String.valueOf(p.level()));
        lvl.getStyleClass().add("big-level");
        UiKit.breathe(lvl, javafx.scene.paint.Color.web("#00FFFF"), 16, 34);

        double xpPct = p.xpForLevelSpan() == 0 ? 1.0 : (double) p.xpIntoLevel() / p.xpForLevelSpan();
        Region xp = UiKit.bar(xpPct, "bar-xp", 210);
        Label xpText = UiKit.muted(UiKit.num(p.xpIntoLevel()) + " / " + UiKit.num(p.xpForLevelSpan()) + " XP");

        StatsView s = p.stats();
        StatRadar radar = new StatRadar(220);
        radar.render(s.strength(), s.intellect(), s.wisdom(), s.discipline(), s.charisma());

        VBox box = new VBox(6, UiKit.caption("NIVEL"), lvl, xp, xpText, UiKit.spacer(6), radar);
        box.setAlignment(Pos.TOP_CENTER);
        box.getStyleClass().add("panel");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Region dailyColumn(DailyChecklistView checklist) {
        VBox box = new VBox(8, UiKit.sectionTitle(
                "MISIONES DIARIAS   " + checklist.completedCount() + "/" + checklist.total()));
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
        boolean burnout = p.burnout();
        HBox left = new HBox(10,
                locked(UiKit.navButton("Misiones", nav::quests), burnout),
                locked(UiKit.navButton("Portales", nav::gates), burnout),
                UiKit.navButton("Armería", nav::armory),
                locked(UiKit.navButton("Tesoros", nav::treasures), burnout));
        left.setAlignment(Pos.CENTER_LEFT);

        Button cont = new Button("REGISTRAR HÁBITOS  ▸");
        cont.getStyleClass().add("continue-btn");
        cont.setOnAction(e -> nav.daily());
        UiKit.hoverPop(cont, 1.06);

        HBox right = new HBox(10,
                locked(UiKit.navButton("Juicios", nav::judgments), burnout),
                locked(UiKit.navButton("Hall of Fame", nav::titles), burnout),
                locked(UiKit.navButton("Diario", nav::journal), burnout));
        Label gold = new Label("🪙 " + UiKit.num(p.gold()) + " G");
        gold.getStyleClass().add("gold");

        HBox bar = new HBox(10, left, UiKit.hgrow(), cont, UiKit.hgrow(), right, gold);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    /** Deshabilita un botón de navegación bloqueado durante el Burnout (Biblia 1.5 FASE 2). */
    private static Button locked(Button b, boolean burnout) {
        if (burnout) b.setDisable(true);
        return b;
    }

    private HomeScreen() {}
}
