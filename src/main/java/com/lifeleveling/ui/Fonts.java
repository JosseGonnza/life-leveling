package com.lifeleveling.ui;

import javafx.scene.text.Font;

import java.io.InputStream;

/**
 * Registra las familias tipográficas del juego (Orbitron display + Rajdhani cuerpo)
 * desde los TTF embebidos. Hay que llamar a {@link #load()} una vez antes de montar la escena;
 * a partir de ahí el CSS puede referenciarlas por familia ("Orbitron Black", "Rajdhani", …).
 */
final class Fonts {

    private static boolean loaded = false;

    static void load() {
        if (loaded) return;
        register("fonts/Orbitron-Black.ttf");
        register("fonts/Orbitron-Bold.ttf");
        register("fonts/Rajdhani-Regular.ttf");
        register("fonts/Rajdhani-Medium.ttf");
        register("fonts/Rajdhani-Bold.ttf");
        loaded = true;
    }

    private static void register(String resource) {
        try (InputStream in = Fonts.class.getResourceAsStream(resource)) {
            if (in != null) Font.loadFont(in, 12);
        } catch (Exception ignored) {
        }
    }

    private Fonts() {}
}
