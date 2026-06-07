package com.lifeleveling.ui;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.domain.quest.condition.GateCondition;
import com.lifeleveling.domain.quest.shared.QuestReward;
import com.lifeleveling.domain.quest.system.GateVerificationResult;
import com.lifeleveling.domain.quest.system.SystemQuestType;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Pantalla Gates (mockup 6.gates2): la gate de ascenso actual con sus requisitos y el botón
 * de desafío, más las especiales (Vault/Redemption) como tarjetas bloqueadas/disponibles.
 * Datos de `gateStatus()` + `vault/redemptionStatus()`; desafiar vía fachada y recarga.
 */
final class GatesScreen {

    static Region build(GameFacade facade, Nav nav) {
        int level = facade.state().level();
        GateVerificationResult status = facade.gateStatus();

        Region mainCard = currentGateCard(status, level, facade, nav);

        HBox middle = new HBox(20, mainCard);
        HBox.setHgrow(mainCard, Priority.ALWAYS);
        VBox.setVgrow(middle, Priority.ALWAYS);

        // Las especiales solo se muestran si se han manifestado (Vault@40, Redemption tras caer).
        List<GateVerificationResult> specialGates = facade.availableSpecialGates();
        if (!specialGates.isEmpty()) {
            VBox specials = new VBox(14);
            specials.setPrefWidth(290);
            for (GateVerificationResult sg : specialGates) {
                SystemQuestType g = sg.gate();
                Runnable onChallenge = g == SystemQuestType.GATE_VAULT
                        ? () -> { facade.challengeVault(); nav.gates(); }
                        : () -> { facade.challengeRedemption(); nav.gates(); };
                specials.getChildren().add(specialCard(g.getIcon() + " " + g.getName(), sg, onChallenge));
            }
            middle.getChildren().add(specials);
        }

        Label title = new Label("PORTALES — EL SISTEMA");
        title.getStyleClass().add("screen-title");

        VBox root = new VBox(12, title, middle, backBar(nav));
        root.getStyleClass().add("screen");
        return root;
    }

    private static Region currentGateCard(GateVerificationResult status, int level, GameFacade facade, Nav nav) {
        VBox card = new VBox(10);
        card.getStyleClass().add("panel");

        SystemQuestType gate = status.gate();
        if (gate == null) {
            card.getChildren().addAll(UiKit.sectionTitle("PORTAL ACTIVO"),
                    UiKit.muted(status.message()));
            return card;
        }

        Label name = new Label(gate.getIcon() + "  " + gate.getName());
        name.getStyleClass().add("player-name");
        Label desc = UiKit.muted(gate.getDescription());
        desc.setWrapText(true);

        VBox reqs = new VBox(6, UiKit.sectionTitle("REQUISITOS"));
        boolean levelMet = level >= gate.getLevelRequirement();
        reqs.getChildren().add(condRow("Nivel " + gate.getLevelRequirement()
                + "  (tú: " + level + ")", levelMet, true));
        for (GateCondition c : gate.getConditions()) {
            boolean met = levelMet && !status.failedConditions().contains(c);
            reqs.getChildren().add(condRow(c.getDescription(), met, levelMet));
        }

        Label reward = new Label("RECOMPENSA:  " + rewardText(gate));
        reward.getStyleClass().add("reward-hint");

        Label result = UiKit.muted("");
        Button challenge = new Button("⛩  ABRIR PORTAL");
        challenge.getStyleClass().add("continue-btn");
        challenge.setOnAction(e -> {
            GateVerificationResult r = facade.challengeGate();
            if (r.success()) nav.gates();
            else result.setText(r.message());
        });

        card.getChildren().addAll(UiKit.sectionTitle("PORTAL DE ASCENSO"), name, desc,
                UiKit.spacer(4), reqs, UiKit.spacer(4), reward, result,
                UiKit.spacer(4), challenge);
        return card;
    }

    private static Region specialCard(String title, GateVerificationResult status, Runnable onChallenge) {
        VBox card = new VBox(8);
        card.getStyleClass().add("panel");

        Label name = UiKit.sectionTitle(title);
        SystemQuestType gate = status.gate();
        Label desc = UiKit.muted(gate == null ? "" : gate.getDescription());
        desc.setWrapText(true);

        Label state = UiKit.muted(status.success() ? "✅ Disponible" : "🔒 " + status.message());
        state.setWrapText(true);

        Button challenge = new Button("Desafiar");
        challenge.getStyleClass().add("nav-btn");
        challenge.setDisable(!status.success());
        challenge.setOnAction(e -> onChallenge.run());

        card.getChildren().addAll(name, desc, state, challenge);
        return card;
    }

    private static Node condRow(String text, boolean met, boolean known) {
        Label mark = new Label(known ? (met ? "✔" : "○") : "•");
        mark.getStyleClass().add(met ? "check-done" : "check-pending");
        Label t = UiKit.muted(text);
        t.setWrapText(true);
        HBox row = new HBox(8, mark, t);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String rewardText(SystemQuestType gate) {
        if (gate.getRankUnlocked() != null) return "🔓 Asciende a Rango " + gate.getRankUnlocked().getDisplayName();
        QuestReward r = gate.getBaseReward();
        if (r.gold() > 0) return "🪙 " + UiKit.num(r.gold()) + " G";
        if (r.generalXP() > 0) return "⭐ " + UiKit.num(r.generalXP()) + " XP";
        return "—";
    }

    private static Region backBar(Nav nav) {
        HBox bar = new HBox(UiKit.navButton("◂ Volver", nav::home));
        bar.getStyleClass().add("bottom-bar");
        return bar;
    }

    private GatesScreen() {}
}
