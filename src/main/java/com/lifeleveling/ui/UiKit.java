package com.lifeleveling.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Componentes y helpers compartidos por todas las pantallas (estética SYSTEM UI).
 * Reutilizar esto + las clases CSS es lo que hace que cada pantalla nueva cueste poco.
 * v2: incluye microinteracciones (hover/press/glow) para que la interfaz se sienta viva.
 */
final class UiKit {

    static final Locale ES = Locale.forLanguageTag("es-ES");
    static final Color GLOW_CYAN = Color.web("#00FFFF");

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
        return hoverPop(b, 1.08);
    }

    /** Barra de progreso (track + relleno). El relleno se anima de 0 al valor al aparecer. */
    static Region bar(double pct, String fillStyle, double width) {
        double clamped = Math.max(0, Math.min(1, pct));
        Region track = styled(new Region(), "bar-track");
        track.setPrefSize(width, 13);
        Region fill = styled(new Region(), fillStyle);
        fill.setPrefHeight(13);
        DoubleProperty progress = new SimpleDoubleProperty(0);
        fill.prefWidthProperty().bind(track.widthProperty().multiply(progress));
        fill.setMaxWidth(Region.USE_PREF_SIZE);
        Timeline grow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
                new KeyFrame(Duration.millis(750),
                        new KeyValue(progress, clamped, Interpolator.SPLINE(0.2, 0.8, 0.2, 1))));
        grow.setDelay(Duration.millis(140));
        grow.play();
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

    // ---- Microinteracciones ----

    /** Hover: el nodo crece suave y vuelve al salir. Se siente reactivo sin mover el layout. */
    static <T extends Node> T hoverPop(T node, double peak) {
        ScaleTransition in = new ScaleTransition(Duration.millis(140), node);
        in.setToX(peak); in.setToY(peak);
        ScaleTransition out = new ScaleTransition(Duration.millis(190), node);
        out.setToX(1); out.setToY(1);
        node.setOnMouseEntered(e -> { out.stop(); in.playFromStart(); });
        node.setOnMouseExited(e -> { in.stop(); out.playFromStart(); });
        return node;
    }

    /** Glow que respira (pulso continuo del resplandor). Para elementos héroe: nivel, títulos, marca. */
    static <T extends Node> T breathe(T node, Color color, double min, double max) {
        DropShadow glow = new DropShadow(min, color);
        glow.setSpread(0.45);
        node.setEffect(glow);
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), min, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1.7), new KeyValue(glow.radiusProperty(), max, Interpolator.EASE_BOTH)));
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
        return node;
    }

    /** Animación de entrada de una pantalla: aparece subiendo y desvaneciéndose desde abajo. */
    static void enter(Region region) {
        region.setOpacity(0);
        region.setTranslateY(16);
        FadeTransition fade = new FadeTransition(Duration.millis(280), region);
        fade.setFromValue(0); fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(320), region);
        slide.setFromY(16); slide.setToY(0);
        slide.setInterpolator(Interpolator.SPLINE(0.1, 0.7, 0.1, 1));
        new ParallelTransition(fade, slide).play();
    }

    private static <T extends javafx.scene.Node> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }

    private UiKit() {}
}
