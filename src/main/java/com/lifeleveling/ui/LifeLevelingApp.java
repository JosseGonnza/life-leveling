package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.infrastructure.persistence.JsonPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

import java.nio.file.Path;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Punto de entrada de la cara JavaFX. Monta el marco una vez (barra de título + navegación)
 * e intercambia el centro según la pantalla. Estado persistido en JSON entre sesiones.
 */
public final class LifeLevelingApp extends Application {

    private GameFacade facade;
    private Stage stage;
    private BorderPane shell;
    private double xOffset, yOffset;

    private final Nav nav = new Nav() {
        @Override public void home() { shell.setCenter(HomeScreen.build(facade, this)); }
        @Override public void daily() { shell.setCenter(DailyTasksScreen.build(facade, this)); }
        @Override public void quests() { shell.setCenter(QuestsScreen.build(facade, this)); }
        @Override public void gates() { shell.setCenter(GatesScreen.build(facade, this)); }
        @Override public void armory() { shell.setCenter(ArmoryScreen.build(facade, this)); }
        @Override public void titles() { shell.setCenter(HallOfFameScreen.build(facade, this)); }
        @Override public void journal() { shell.setCenter(JournalScreen.build(facade, this)); }
        @Override public void treasures() { shell.setCenter(TreasuresScreen.build(facade, this)); }
        @Override public void judgments() { shell.setCenter(JudgmentsScreen.build(facade, this)); }
        @Override public void todo(String screen) { System.out.println("⟦SYSTEM⟧ (próximamente) " + screen); }
    };

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        facade = new GameFacade(new JsonPlayerRepository(savePath()), new SystemClock(),
                e -> System.out.println("⟦SYSTEM⟧ " + e.message()));
        if (!facade.loadGame()) {
            facade.newGame("Jose");
        }

        shell = new BorderPane();
        shell.getStyleClass().add("app-root");
        shell.setTop(titleBar());
        nav.home();

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        double w = Math.max(940, screen.getWidth() * 0.75);
        double h = Math.max(580, screen.getHeight() * 0.75);
        Scene scene = new Scene(shell, w, h);
        scene.setFill(Color.web("#050A14"));
        scene.getStylesheets().add(
                getClass().getResource("/com/lifeleveling/ui/system.css").toExternalForm());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Life Leveling");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        applyScreenOverride();
        maybeScreenshot();
    }

    /** Hook de dev: si LL_SCREEN=nombre, abre esa pantalla al arrancar (para revisar/capturar). */
    private void applyScreenOverride() {
        String screen = System.getenv("LL_SCREEN");
        if (screen == null) return;
        switch (screen.toLowerCase()) {
            case "daily" -> nav.daily();
            case "quests" -> nav.quests();
            case "gates" -> nav.gates();
            case "armory" -> nav.armory();
            case "titles" -> nav.titles();
            case "journal" -> nav.journal();
            case "treasures" -> nav.treasures();
            case "judgments" -> nav.judgments();
            default -> nav.home();
        }
    }

    private Region titleBar() {
        Label title = new Label("⟦ LIFE LEVELING · SYSTEM ⟧");
        title.getStyleClass().add("sys-title");
        Button close = new Button("✕");
        close.getStyleClass().add("close-btn");
        close.setOnAction(e -> stage.close());

        HBox bar = new HBox(title, UiKit.hgrow(), close);
        bar.getStyleClass().add("title-bar");
        bar.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
        bar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });
        return bar;
    }

    /** Hook de dev: si LL_SCREENSHOT=ruta, guarda un PNG de la escena y cierra. No afecta al uso normal. */
    private void maybeScreenshot() {
        String path = System.getenv("LL_SCREENSHOT");
        if (path == null) return;
        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> {
            try {
                WritableImage img = stage.getScene().snapshot(null);
                int w = (int) img.getWidth(), h = (int) img.getHeight();
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                PixelReader pr = img.getPixelReader();
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++) bi.setRGB(x, y, pr.getArgb(x, y));
                ImageIO.write(bi, "png", new File(path));
                System.out.println("⟦SHOT⟧ " + path);
            } catch (Exception ex) {
                System.out.println("⟦SHOT⟧ fallo: " + ex.getMessage());
            }
            Platform.exit();
        });
        pause.play();
    }

    /** Ruta del fichero de guardado (override con LL_SAVE; por defecto ~/.life-leveling/savegame.json). */
    private static Path savePath() {
        String override = System.getenv("LL_SAVE");
        if (override != null) return Path.of(override);
        return Path.of(System.getProperty("user.home"), ".life-leveling", "savegame.json");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
