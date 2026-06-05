package com.lifeleveling.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Radar pentagonal de las 5 stats (STR/INT/WIS/DIS/CHA), dibujado a mano sobre un Canvas.
 * JavaFX no trae chart de tipo radar, así que lo pintamos nosotros.
 */
final class StatRadar extends Canvas {

    private static final String[] LABELS = {"STR", "INT", "WIS", "DIS", "CHA"};
    private static final Color GRID = Color.web("#1E3A4C");
    private static final Color FILL = Color.web("#00FFFF", 0.18);
    private static final Color LINE = Color.web("#00FFFF");
    private static final Color TEXT = Color.web("#9FB3C8");

    StatRadar(double size) {
        super(size, size);
    }

    /** Pinta el pentagon con los 5 valores (niveles de stat). Se autoescala al mayor. */
    void render(int str, int intel, int wis, int dis, int cha) {
        int[] values = {str, intel, wis, dis, cha};
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth(), h = getHeight();
        g.clearRect(0, 0, w, h);

        double cx = w / 2, cy = h / 2 + 4;
        double radius = Math.min(w, h) / 2 - 26;
        int max = Math.max(10, maxOf(values)); // techo suave para que no quede gigante

        // Anillos de rejilla
        g.setStroke(GRID);
        g.setLineWidth(1);
        for (double ring = 0.25; ring <= 1.0; ring += 0.25) {
            drawPolygon(g, cx, cy, radius * ring, null);
        }
        // Ejes
        for (int i = 0; i < 5; i++) {
            double[] p = vertex(cx, cy, radius, i);
            g.strokeLine(cx, cy, p[0], p[1]);
        }

        // Polígono de valores
        double[] xs = new double[5], ys = new double[5];
        for (int i = 0; i < 5; i++) {
            double r = radius * (values[i] / (double) max);
            double[] p = vertex(cx, cy, r, i);
            xs[i] = p[0];
            ys[i] = p[1];
        }
        g.setFill(FILL);
        g.fillPolygon(xs, ys, 5);
        g.setStroke(LINE);
        g.setLineWidth(2);
        g.strokePolygon(xs, ys, 5);

        // Etiquetas + valores
        g.setFill(TEXT);
        g.setFont(Font.font("monospace", 11));
        g.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < 5; i++) {
            double[] p = vertex(cx, cy, radius + 15, i);
            g.fillText(LABELS[i] + " " + values[i], p[0], p[1] + 4);
        }
    }

    private void drawPolygon(GraphicsContext g, double cx, double cy, double r, Color fill) {
        double[] xs = new double[5], ys = new double[5];
        for (int i = 0; i < 5; i++) {
            double[] p = vertex(cx, cy, r, i);
            xs[i] = p[0];
            ys[i] = p[1];
        }
        if (fill != null) {
            g.setFill(fill);
            g.fillPolygon(xs, ys, 5);
        }
        g.strokePolygon(xs, ys, 5);
    }

    /** Vértice i del pentágono (empezando arriba, sentido horario). */
    private double[] vertex(double cx, double cy, double r, int i) {
        double angle = -Math.PI / 2 + i * (2 * Math.PI / 5);
        return new double[]{cx + r * Math.cos(angle), cy + r * Math.sin(angle)};
    }

    private int maxOf(int[] v) {
        int m = 0;
        for (int x : v) m = Math.max(m, x);
        return m;
    }
}
