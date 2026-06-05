package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

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
 * Punto de entrada de la cara JavaFX. Ventana sin marco (estética Sistema), monta la Home
 * leyendo del GameFacade. Estado en memoria + seed de demo para no salir en blanco.
 */
public final class LifeLevelingApp extends Application {

    private GameFacade facade;
    private Stage stage;
    private double xOffset, yOffset;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        var repo = new InMemoryPlayerRepository();
        facade = new GameFacade(repo, new SystemClock(),
                e -> System.out.println("⟦SYSTEM⟧ " + e.message()));
        if (!facade.loadGame()) {
            facade.newGame("Jose");
            seedDemo();
        }
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Life Leveling");
        render();
        stage.show();
        maybeScreenshot();
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

    private void render() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(titleBar());
        root.setCenter(HomeScreen.build(facade, this::onContinue));

        Scene scene = new Scene(root, 940, 580);
        scene.setFill(Color.web("#050A14"));
        scene.getStylesheets().add(
                getClass().getResource("/com/lifeleveling/ui/system.css").toExternalForm());
        stage.setScene(scene);
    }

    private void onContinue() {
        facade.endDay();
        render();
    }

    private Region titleBar() {
        Label title = new Label("⟦ LIFE LEVELING · SYSTEM ⟧");
        title.getStyleClass().add("sys-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("✕");
        close.getStyleClass().add("close-btn");
        close.setOnAction(e -> stage.close());

        HBox bar = new HBox(title, spacer, close);
        bar.getStyleClass().add("title-bar");
        bar.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
        bar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });
        return bar;
    }

    /** Estado de muestra para ver la Home poblada. Borra este método cuando haya partida real. */
    private void seedDemo() {
        facade.workJob(8);
        facade.workCode(4);
        facade.read(40);
        facade.sleep(8);
        facade.gym(true);
        facade.skincare(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
