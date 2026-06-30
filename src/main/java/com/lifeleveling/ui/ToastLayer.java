package com.lifeleveling.ui;

import com.lifeleveling.domain.event.GameEvent;
import com.lifeleveling.domain.event.GameEventType;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Capa de notificaciones emergentes (toasts) que flota sobre toda la app (arriba-derecha).
 * Acumulable: los eventos entran en una cola y se muestran de uno en uno con entrada escalonada
 * (si subes dos niveles de golpe, sale primero uno y luego el otro), apilándose, con auto-cierre.
 * Se alimenta desde el `Notifier` de la fachada: cada `GameEvent` del dominio se convierte en un toast.
 */
final class ToastLayer extends VBox {

    private static final int MAX_VISIBLE = 4;
    private static final Duration STAGGER = Duration.millis(380);
    private static final Duration LIFETIME = Duration.millis(4200);

    private final Deque<GameEvent> queue = new ArrayDeque<>();
    private int visible = 0;

    ToastLayer() {
        setSpacing(10);
        setAlignment(Pos.TOP_RIGHT);
        setPadding(new Insets(52, 18, 18, 18));
        setFillWidth(false);
        setPickOnBounds(false);
        setMouseTransparent(true); // nunca bloquea clics a la interfaz de debajo
    }

    /** Encola un evento para mostrarlo. Seguro desde cualquier hilo. */
    void push(GameEvent event) {
        Platform.runLater(() -> {
            queue.add(event);
            pump();
        });
    }

    private void pump() {
        if (visible >= MAX_VISIBLE || queue.isEmpty()) return;
        GameEvent event = queue.poll();
        visible++;

        Region toast = buildToast(event);
        getChildren().add(toast);
        animateIn(toast);

        PauseTransition life = new PauseTransition(LIFETIME);
        life.setOnFinished(e -> dismiss(toast));
        life.play();

        PauseTransition stagger = new PauseTransition(STAGGER);
        stagger.setOnFinished(e -> pump());
        stagger.play();
    }

    private void animateIn(Region toast) {
        toast.setTranslateX(70);
        toast.setOpacity(0);
        toast.setScaleX(0.96);
        toast.setScaleY(0.96);
        FadeTransition fade = new FadeTransition(Duration.millis(260), toast);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(340), toast);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.2, 1));
        ScaleTransition pop = new ScaleTransition(Duration.millis(340), toast);
        pop.setToX(1);
        pop.setToY(1);
        new ParallelTransition(fade, slide, pop).play();
    }

    private void dismiss(Region toast) {
        FadeTransition fade = new FadeTransition(Duration.millis(280), toast);
        fade.setToValue(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), toast);
        slide.setToX(70);
        ParallelTransition out = new ParallelTransition(fade, slide);
        out.setOnFinished(e -> {
            getChildren().remove(toast);
            visible--;
            pump();
        });
        out.play();
    }

    private Region buildToast(GameEvent event) {
        Region accent = new Region();
        accent.getStyleClass().add("toast-accent");
        accent.setMinWidth(4);
        accent.setPrefWidth(4);
        accent.setMaxWidth(4);

        Label text = new Label(event.message());
        text.getStyleClass().add("toast-text");
        text.setWrapText(true);
        text.setMaxWidth(300);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox toast = new HBox(12, accent, text);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast", styleFor(event.type()));
        toast.setMaxWidth(Region.USE_PREF_SIZE);
        return toast;
    }

    private static String styleFor(GameEventType type) {
        return switch (type) {
            case LEVEL_UP, RANK_UP, PERFECT_DAY, GATE_COMPLETED, MILESTONE_REACHED, TITLE_UNLOCKED ->
                    "toast-hero";
            case BURNOUT, QUEST_FAILED, DAMAGE_TAKEN, DEBUFF_APPLIED, DEBUFF_WARNING, WARNING,
                 PERFECT_DAY_STREAK_BROKEN, BUFF_EXPIRED ->
                    "toast-bad";
            case INFO, XP_GAINED, GOLD_GAINED, GOLD_SPENT, ITEM_PURCHASED, ITEM_CONSUMED,
                 CODE_SESSION_COMPLETED, NEW_WEEK ->
                    "toast-info";
            default -> "toast-good";
        };
    }
}
