package com.lifeleveling.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fondo VIVO de toda la app: nebulosa cian a la deriva (blobs desenfocados) + motas de luz
 * flotando. Una sola capa que se monta detrás del shell; como los paneles son de cristal
 * (semi-transparentes) el movimiento se ve a través de toda la interfaz.
 *
 * Un único {@link AnimationTimer} mueve los blobs (nodos cacheados, baratos de trasladar) y
 * repinta las motas en un Canvas. Pensado para correr continuo sin castigar la CPU.
 */
final class AmbientBackground extends Region {

    private static final Color CYAN = Color.web("#00FFFF");
    private static final Color VIOLET = Color.web("#7A5CFF");
    private static final Color DEEP = Color.web("#0A6CFF");

    private final Canvas canvas = new Canvas();
    private final List<Blob> blobs = new ArrayList<>();
    private final Mote[] motes = new Mote[150];
    private final Random rnd = new Random(7);

    private boolean initialized = false;
    private long lastNanos = 0;

    AmbientBackground() {
        setStyle("-fx-background-color: radial-gradient(center 50% 32%, radius 95%, "
                + "#0B1A30 0%, #081325 45%, #050A14 100%);");
        getChildren().add(canvas);
        canvas.setMouseTransparent(true);
        setMouseTransparent(true);

        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long now) {
                double w = getWidth(), h = getHeight();
                if (w <= 0 || h <= 0) return;
                ensureInit(w, h);
                double dt = lastNanos == 0 ? 0.016 : Math.min(0.05, (now - lastNanos) / 1e9);
                lastNanos = now;
                step(w, h, dt);
            }
        };
        timer.start();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
    }

    private void ensureInit(double w, double h) {
        if (initialized) return;
        Color[] palette = { CYAN, CYAN, VIOLET, DEEP, CYAN, VIOLET };
        for (int i = 0; i < palette.length; i++) {
            double r = 200 + rnd.nextDouble() * 220;
            Blob b = new Blob();
            b.x = rnd.nextDouble() * w;
            b.y = rnd.nextDouble() * h;
            b.vx = (rnd.nextDouble() - 0.5) * 26;
            b.vy = (rnd.nextDouble() - 0.5) * 18;
            b.r = r;
            Circle c = new Circle(r);
            Color col = palette[i];
            c.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, col.deriveColor(0, 1, 1, 0.34)),
                    new Stop(0.55, col.deriveColor(0, 1, 1, 0.12)),
                    new Stop(1, Color.TRANSPARENT)));
            c.setEffect(new GaussianBlur(70));
            c.setBlendMode(BlendMode.SCREEN);
            c.setCache(true);
            c.setManaged(false);
            b.node = c;
            blobs.add(b);
            getChildren().add(getChildren().size() - 1, c);
        }
        for (int i = 0; i < motes.length; i++) {
            motes[i] = newMote(w, h, rnd.nextDouble() * h);
        }
        initialized = true;
    }

    private Mote newMote(double w, double h, double startY) {
        Mote m = new Mote();
        m.x = rnd.nextDouble() * w;
        m.y = startY;
        m.size = 0.7 + rnd.nextDouble() * 2.6;
        m.speed = 8 + rnd.nextDouble() * 26;
        m.sway = 6 + rnd.nextDouble() * 20;
        m.phase = rnd.nextDouble() * Math.PI * 2;
        m.twinkle = 0.6 + rnd.nextDouble() * 2.2;
        m.violet = rnd.nextDouble() < 0.25;
        return m;
    }

    private void step(double w, double h, double dt) {
        for (Blob b : blobs) {
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            double m = b.r;
            if (b.x < -m) b.x = w + m;
            if (b.x > w + m) b.x = -m;
            if (b.y < -m) b.y = h + m;
            if (b.y > h + m) b.y = -m;
            b.node.setCenterX(b.x);
            b.node.setCenterY(b.y);
        }

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        g.setGlobalBlendMode(BlendMode.SCREEN);
        double t = lastNanos / 1e9;
        for (Mote mt : motes) {
            mt.y -= mt.speed * dt;
            if (mt.y < -6) {
                Mote fresh = newMote(w, h, h + 6);
                mt.x = fresh.x; mt.y = fresh.y; mt.size = fresh.size; mt.speed = fresh.speed;
                mt.sway = fresh.sway; mt.phase = fresh.phase; mt.twinkle = fresh.twinkle; mt.violet = fresh.violet;
            }
            double px = mt.x + Math.sin(t * 0.6 + mt.phase) * mt.sway;
            double alpha = 0.25 + 0.55 * (0.5 + 0.5 * Math.sin(t * mt.twinkle + mt.phase));
            Color base = mt.violet ? VIOLET : CYAN;
            g.setGlobalAlpha(alpha * 0.35);
            g.setFill(base);
            g.fillOval(px - mt.size * 2.4, mt.y - mt.size * 2.4, mt.size * 4.8, mt.size * 4.8);
            g.setGlobalAlpha(alpha);
            g.setFill(base.brighter());
            g.fillOval(px - mt.size, mt.y - mt.size, mt.size * 2, mt.size * 2);
        }
        g.setGlobalAlpha(1.0);
        g.setGlobalBlendMode(BlendMode.SRC_OVER);
    }

    private static final class Blob {
        double x, y, vx, vy, r;
        Circle node;
    }

    private static final class Mote {
        double x, y, size, speed, sway, phase, twinkle;
        boolean violet;
    }
}
