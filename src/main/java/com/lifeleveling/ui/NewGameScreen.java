package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Pantalla de Nueva Partida: el Sistema pide el nombre del Cazador antes de empezar.
 * Se muestra cuando no hay partida guardada; al confirmar crea la partida y entra a la Home.
 */
final class NewGameScreen {

    private static final int MAX_NAME = 24;

    static Region build(GameFacade facade, Nav nav) {
        Label brand = new Label("⟦ LIFE LEVELING ⟧");
        brand.getStyleClass().add("screen-title");
        Label prompt = UiKit.muted("El Sistema te ha elegido. Identifícate, Cazador.");

        TextField name = new TextField();
        name.getStyleClass().add("sys-field");
        name.setPromptText("Tu nombre");
        name.setMaxWidth(320);
        name.setTextFormatter(new TextFormatter<String>(c ->
                c.getControlNewText().length() <= MAX_NAME ? c : null));

        Button start = new Button("DESPERTAR  ▸");
        start.getStyleClass().add("continue-btn");
        start.setDisable(true);
        name.textProperty().addListener((o, a, b) -> start.setDisable(b.trim().isEmpty()));

        Runnable begin = () -> {
            String chosen = name.getText().trim();
            if (chosen.isEmpty()) return;
            facade.newGame(chosen);
            nav.home();
        };
        start.setOnAction(e -> begin.run());
        name.setOnAction(e -> { if (!start.isDisable()) begin.run(); });

        VBox box = new VBox(16, brand, prompt, name, start);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("screen");
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    private NewGameScreen() {}
}
