package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.infrastructure.persistence.JsonPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

import java.nio.file.Path;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private StackPane rootStack;
    private ToastLayer toasts;
    private double xOffset, yOffset;

    private final Nav nav = new Nav() {
        @Override public void home() { show(HomeScreen.build(facade, this)); }
        @Override public void daily() { show(DailyTasksScreen.build(facade, this)); }
        @Override public void armory() { show(ArmoryScreen.build(facade, this)); }
        @Override public void quests() { if (denyDuringBurnout()) return; show(QuestsScreen.build(facade, this)); }
        @Override public void gates() { if (denyDuringBurnout()) return; show(GatesScreen.build(facade, this)); }
        @Override public void titles() { if (denyDuringBurnout()) return; show(HallOfFameScreen.build(facade, this)); }
        @Override public void journal() { if (denyDuringBurnout()) return; show(JournalScreen.build(facade, this)); }
        @Override public void treasures() { if (denyDuringBurnout()) return; show(TreasuresScreen.build(facade, this)); }
        @Override public void judgments() { if (denyDuringBurnout()) return; show(JudgmentsScreen.build(facade, this)); }
        @Override public void todo(String screen) { System.out.println("⟦SYSTEM⟧ (próximamente) " + screen); }
    };

    /** Pinta la pantalla en el centro, la anima al entrar y aplica el tema según el estado. */
    private void show(Region center) {
        shell.setCenter(center);
        applyBurnoutTheme();
        UiKit.enter(center);
    }

    /** En Burnout solo se permiten Home, Daily (descanso) y Armería (curas). El resto se deniega. */
    private boolean denyDuringBurnout() {
        if (!facade.state().burnout()) return false;
        System.out.println("⟦SYSTEM⟧ Bloqueado por Burnout: solo puedes descansar y curarte.");
        nav.home();
        return true;
    }

    /** Tema BURNOUT: desatura toda la interfaz (incluido el fondo vivo) a gris. Biblia 1.5: El Colapso. */
    private void applyBurnoutTheme() {
        if (rootStack == null) return;
        if (facade.state().burnout()) {
            javafx.scene.effect.ColorAdjust grayscale = new javafx.scene.effect.ColorAdjust();
            grayscale.setSaturation(-1.0);
            rootStack.setEffect(grayscale);
        } else {
            rootStack.setEffect(null);
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Fonts.load();
        ToastLayer toasts = new ToastLayer();
        this.toasts = toasts;
        facade = new GameFacade(new JsonPlayerRepository(savePath()), new SystemClock(),
                e -> { System.out.println("⟦SYSTEM⟧ " + e.message()); toasts.push(e); });

        boolean hasGame = facade.loadGame();
        // QA: los hooks de dev (LL_SCREEN/LL_HP) necesitan una partida; si no hay save, sembramos una.
        if (!hasGame && System.getenv("LL_SCREEN") != null) {
            facade.newGame("Cazador");
            hasGame = true;
        }

        shell = new BorderPane();
        shell.getStyleClass().add("app-root");
        shell.setTop(titleBar());

        // El fondo vivo va detrás; el shell (transparente) flota encima; los toasts, por encima de todo.
        rootStack = new StackPane(new AmbientBackground(), shell, toasts);

        if (hasGame) {
            applyHpOverride();
            nav.home();
        } else {
            shell.setCenter(NewGameScreen.build(facade, nav));
            UiKit.enter((Region) shell.getCenter());
        }

        javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
        double w = Math.max(940, screen.getWidth() * 0.75);
        double h = Math.max(580, screen.getHeight() * 0.75);
        Scene scene = new Scene(rootStack, w, h);
        scene.setFill(Color.web("#050A14"));
        scene.getStylesheets().add(
                getClass().getResource("/com/lifeleveling/ui/system.css").toExternalForm());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Life Leveling");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        applyScreenOverride();
        maybeSplash();
        maybeToastDemo();
        maybeScreenshot();
    }

    /** Hook de dev: si LL_TOAST=1, siembra notificaciones de muestra (para QA/capturas del overlay). */
    private void maybeToastDemo() {
        if (System.getenv("LL_TOAST") == null) return;
        toasts.push(com.lifeleveling.domain.event.GameEvent.levelUp(54, 55));
        toasts.push(com.lifeleveling.domain.event.GameEvent.levelUp(55, 56));
        toasts.push(com.lifeleveling.domain.event.GameEvent.perfectDay());
        toasts.push(com.lifeleveling.domain.event.GameEvent.questCompleted("Entregar el proyecto"));
        toasts.push(com.lifeleveling.domain.event.GameEvent.gateCompleted("El Despertar"));
        toasts.push(com.lifeleveling.domain.event.GameEvent.questFailed("Leer 50 páginas", 15));
    }

    /** Splash de arranque: la marca despierta con un pulso y se desvanece para revelar el juego.
     *  Se salta en modo captura/QA (LL_SCREENSHOT/LL_SCREEN) para no contaminar las pantallas. */
    private void maybeSplash() {
        if (System.getenv("LL_SCREENSHOT") != null || System.getenv("LL_SCREEN") != null) return;

        Label brand = new Label("⟦ LIFE LEVELING ⟧");
        brand.getStyleClass().add("splash-brand");
        Label sub = new Label("DESPERTANDO AL CAZADOR…");
        sub.getStyleClass().add("splash-sub");
        VBox box = new VBox(20, brand, sub);
        box.setAlignment(Pos.CENTER);

        StackPane overlay = new StackPane(box);
        overlay.getStyleClass().add("splash-root");
        rootStack.getChildren().add(overlay);

        UiKit.breathe(brand, Color.web("#00FFFF"), 24, 50);
        brand.setScaleX(0.82);
        brand.setScaleY(0.82);
        ScaleTransition pop = new ScaleTransition(Duration.millis(750), brand);
        pop.setToX(1.0);
        pop.setToY(1.0);
        pop.setInterpolator(Interpolator.SPLINE(0.1, 0.7, 0.1, 1));
        FadeTransition fadeIn = new FadeTransition(Duration.millis(520), box);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        new ParallelTransition(pop, fadeIn).play();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), overlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(1600));
        fadeOut.setOnFinished(e -> rootStack.getChildren().remove(overlay));
        fadeOut.play();
    }

    /** Hook de dev: si LL_HP=n, baja el HP del jugador al arrancar (para revisar el gating por estado). */
    private void applyHpOverride() {
        String hp = System.getenv("LL_HP");
        if (hp == null) return;
        try {
            facade.debugDamageTo(Integer.parseInt(hp.trim()));
        } catch (NumberFormatException ignored) {
        }
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
        UiKit.breathe(title, Color.web("#00FFFF"), 6, 16);
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
        double delay = System.getenv("LL_TOAST") != null ? 1500 : 700;
        PauseTransition pause = new PauseTransition(Duration.millis(delay));
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
