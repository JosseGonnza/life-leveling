package com.lifeleveling.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Locale;

/**
 * Componentes y helpers compartidos por todas las pantallas (estética SYSTEM UI).
 * Reutilizar esto + las clases CSS es lo que hace que cada pantalla nueva cueste poco.
 */
final class UiKit {

    static final Locale ES = Locale.forLanguageTag("es-ES");

    static Label sectionTitle(String text) {
        return styled(new Label(text), "section-title");
    }

    static Label caption(String text) {
        return styled(new Label(text), "caption");
    }

    static Label muted(String text) {
        return styled(new Label(text), "muted");
    }

    static Button navButton(String text, Runnable onClick) {
        Button b = styled(new Button(text), "nav-btn");
        b.setOnAction(e -> onClick.run());
        return b;
    }

    /** Barra de progreso (track + relleno proporcional). `fillStyle` = clase CSS del relleno. */
    static Region bar(double pct, String fillStyle, double width) {
        double clamped = Math.max(0, Math.min(1, pct));
        Region track = styled(new Region(), "bar-track");
        track.setPrefSize(width, 12);
        Region fill = styled(new Region(), fillStyle);
        fill.setPrefHeight(12);
        fill.prefWidthProperty().bind(track.widthProperty().multiply(clamped));
        fill.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane pane = new StackPane(track, fill);
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setMaxWidth(width);
        return pane;
    }

    static Region spacer(double height) {
        Region r = new Region();
        r.setMinHeight(height);
        return r;
    }

    /** Hueco elástico horizontal (empuja lo siguiente a la derecha). */
    static Region hgrow() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    static String num(long n) {
        return String.format(ES, "%,d", n);
    }

    private static <T extends javafx.scene.Node> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }

    private UiKit() {}
}
