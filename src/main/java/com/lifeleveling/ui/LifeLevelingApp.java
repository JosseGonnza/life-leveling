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
 * e intercambia el centro según la pantalla. Estado en memoria + seed de demo.
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
        @Override public void continueDay() { facade.endDay(); home(); }
        @Override public void todo(String screen) { System.out.println("⟦SYSTEM⟧ (próximamente) " + screen); }
    };

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        facade = new GameFacade(new JsonPlayerRepository(savePath()), new SystemClock(),
                e -> System.out.println("⟦SYSTEM⟧ " + e.message()));
        if (!facade.loadGame()) {
            facade.newGame("Jose");
            seedDemo();
        }

        shell = new BorderPane();
        shell.getStyleClass().add("app-root");
        shell.setTop(titleBar());
        nav.home();
        switch (System.getenv("LL_SCREEN") == null ? "" : System.getenv("LL_SCREEN")) { // hook de dev para capturas
            case "daily" -> nav.daily();
            case "quests" -> nav.quests();
            case "gates" -> nav.gates();
            case "armory" -> nav.armory();
            case "titles" -> nav.titles();
            case "journal" -> nav.journal();
            default -> { }
        }

        Scene scene = new Scene(shell, 940, 580);
        scene.setFill(Color.web("#050A14"));
        scene.getStylesheets().add(
                getClass().getResource("/com/lifeleveling/ui/system.css").toExternalForm());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Life Leveling");
        stage.setScene(scene);
        stage.show();
        maybeScreenshot();
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

    /** Estado de muestra para ver las pantallas pobladas. Borra este método cuando haya partida real. */
    private void seedDemo() {
        facade.workJob(16);
        facade.workCode(4);
        facade.read(40);
        facade.sleep(8);
        facade.gym(true);
        facade.skincare(true);
        facade.createQuest("Code Review: Proyecto Z", "Revisar el PR del equipo antes del viernes.",
                com.lifeleveling.domain.quest.shared.QuestRank.B, java.time.LocalDate.now().plusDays(3));
        facade.createQuest("Aprender JavaFX", "Pintar 3 pantallas del Sistema.",
                com.lifeleveling.domain.quest.shared.QuestRank.C, null);
        facade.buy("consumable_espresso");
        facade.buy("consumable_bar");
        // Solo para capturas del Journal: cierra el día para que el timeline tenga una entrada.
        if (System.getenv("LL_SEED_CLOSEDAY") != null) facade.endDay();
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
