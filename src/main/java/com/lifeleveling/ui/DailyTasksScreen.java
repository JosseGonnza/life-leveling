package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.application.dto.DailyChecklistView;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pantalla Daily Tasks (mockup 2.daily-quest): registrar los 7 hábitos del día.
 * SLEEP/CODE/READ con input numérico; GYM/DIET/SKINCARE/TIDY con check. Los ya
 * completados hoy se muestran cerrados (no se vuelven a aplicar). CONFIRMAR aplica
 * solo lo pendiente y vuelve a la Home.
 */
final class DailyTasksScreen {

    private static final Set<String> NUMERIC = Set.of("SLEEP", "CODE", "READ");

    static Region build(GameFacade facade, Nav nav) {
        DailyChecklistView checklist = facade.dailyChecklist();
        Map<String, TextField> fields = new HashMap<>();
        Map<String, CheckBox> checks = new HashMap<>();

        VBox list = new VBox(8);
        for (DailyChecklistView.Habit h : checklist.habits()) {
            list.getChildren().add(row(h, fields, checks));
        }
        list.getStyleClass().add("panel");
        VBox.setVgrow(list, Priority.ALWAYS);

        Label title = new Label("TAREAS DEL SISTEMA — HOY");
        title.getStyleClass().add("screen-title");
        Label progress = UiKit.muted(checklist.completedCount() + " / " + checklist.total() + " COMPLETADOS");

        Button back = UiKit.navButton("◂ Volver", nav::home);
        Button confirm = new Button("CONFIRMAR CAMBIOS");
        confirm.getStyleClass().add("continue-btn");
        confirm.setOnAction(e -> {
            apply(facade, fields, checks);
            nav.home();
        });
        HBox actions = new HBox(12, back, UiKit.hgrow(), confirm);
        actions.setAlignment(Pos.CENTER);
        actions.getStyleClass().add("bottom-bar");

        VBox root = new VBox(6, title, progress, UiKit.spacer(6), list, actions);
        root.getStyleClass().add("screen");
        return root;
    }

    private static Region row(DailyChecklistView.Habit h, Map<String, TextField> fields, Map<String, CheckBox> checks) {
        Label label = new Label(h.label());
        label.getStyleClass().add(h.done() ? "habit-done" : "habit-pending");

        Region control;
        if (h.done()) {
            Label mark = new Label("✔ HECHO");
            mark.getStyleClass().add("check-done");
            control = mark;
        } else if (NUMERIC.contains(h.code())) {
            TextField field = new TextField();
            field.getStyleClass().add("sys-field");
            field.setPrefWidth(80);
            field.setPromptText(h.code().equals("READ") ? "págs" : "horas");
            fields.put(h.code(), field);
            control = field;
        } else {
            CheckBox check = new CheckBox();
            check.getStyleClass().add("sys-check");
            checks.put(h.code(), check);
            control = check;
        }

        HBox row = new HBox(label, UiKit.hgrow(), control);
        row.getStyleClass().add("habit-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static void apply(GameFacade facade, Map<String, TextField> fields, Map<String, CheckBox> checks) {
        int sleep = parseInt(fields.get("SLEEP"));
        if (sleep > 0) facade.sleep(sleep);
        int pages = parseInt(fields.get("READ"));
        if (pages > 0) facade.read(pages);
        double code = parseDouble(fields.get("CODE"));
        if (code > 0) facade.workCode(code);

        if (isChecked(checks.get("DIET"))) facade.diet(true);
        if (isChecked(checks.get("GYM"))) facade.gym(true);
        if (isChecked(checks.get("SKINCARE"))) facade.skincare(true);
        if (isChecked(checks.get("TIDY"))) facade.tidy(true);
    }

    private static boolean isChecked(CheckBox box) {
        return box != null && box.isSelected();
    }

    private static int parseInt(TextField field) {
        if (field == null) return 0;
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(TextField field) {
        if (field == null) return 0;
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private DailyTasksScreen() {}
}
