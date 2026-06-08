package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.JournalView;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Pantalla System Log / Journal (mockup 9.analytical-system): timeline de días cerrados (izq)
 * + analytics de stats y totales/rachas (der). Datos de `journal()` (sobre `DailyHistory`).
 */
final class JournalScreen {

    static Region build(GameFacade facade, Nav nav) {
        JournalView j = facade.journal();

        HBox columns = new HBox(18, timelineColumn(j), analyticsColumn(j));
        VBox.setVgrow(columns, Priority.ALWAYS);

        Label title = new Label("DIARIO");
        title.getStyleClass().add("screen-title");

        VBox root = new VBox(12, title, columns, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    private static Region timelineColumn(JournalView j) {
        VBox list = new VBox(8);
        if (j.timeline().isEmpty()) {
            list.getChildren().add(UiKit.muted("Aún no se ha cerrado ningún día.\nRegistra tus hábitos hoy; el día se cierra solo al abrir la app en otra fecha."));
        }
        for (JournalView.DayEntry d : j.timeline()) {
            list.getChildren().add(dayRow(d));
        }
        ScrollPane scroll = new ScrollPane(list);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox box = new VBox(10, UiKit.sectionTitle("CRONOLOGÍA   " + j.daysLogged() + " días"), scroll);
        box.getStyleClass().add("panel");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Region dayRow(JournalView.DayEntry d) {
        Label date = new Label(d.date());
        date.getStyleClass().add("item-name");
        String marks = (d.perfectDay() ? "🌟 Día Perfecto  " : "") + (d.burnout() ? "💀 Burnout  " : "");
        Label tags = new Label(marks.isBlank() ? "—" : marks.trim());
        tags.getStyleClass().add(d.burnout() ? "check-pending" : "check-done");

        Label stats = UiKit.muted(String.format(
                "CODE %.1fh   ·   %d págs   ·   %d quests   ·   HP mín %d",
                d.careerHours(), d.pagesRead(), d.quests(), d.minHP()));

        VBox card = new VBox(4, new HBox(10, date, UiKit.hgrow(), tags), stats);
        card.getStyleClass().add("habit-row");
        return card;
    }

    private static Region analyticsColumn(JournalView j) {
        VBox stats = new VBox(6, UiKit.sectionTitle("ATRIBUTOS (XP)"));
        for (JournalView.StatLine s : j.stats()) {
            Label line = UiKit.muted(String.format("%-18s  Nv %-3d   %s XP",
                    s.name() + " (" + s.abbr() + ")", s.level(), UiKit.num(s.xp())));
            stats.getChildren().add(line);
        }

        VBox totals = new VBox(6, UiKit.sectionTitle("TOTALES"),
                kv("Días Perfectos", String.valueOf(j.perfectDaysTotal())),
                kv("Racha actual / máx", j.perfectDayStreak() + " / " + j.maxPerfectDayStreak()),
                kv("Burnouts superados", String.valueOf(j.burnoutsRecovered())),
                kv("Páginas leídas", UiKit.num(j.totalPagesRead())),
                kv("Oro gastado", UiKit.num(j.totalGoldSpent()) + " G"));

        VBox box = new VBox(16, stats, totals);
        box.getStyleClass().add("panel");
        box.setPrefWidth(320);
        return box;
    }

    private static Region kv(String key, String value) {
        Label k = UiKit.muted(key);
        Label v = new Label(value);
        v.getStyleClass().add("reward-hint");
        HBox row = new HBox(10, k, UiKit.hgrow(), v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private JournalScreen() {}
}
