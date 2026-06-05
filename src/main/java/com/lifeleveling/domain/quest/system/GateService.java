package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.quest.condition.ConditionContext;
import com.lifeleveling.domain.quest.condition.GateCondition;
import com.lifeleveling.domain.quest.condition.GateTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GateService: El Portero (Gatekeeper).
 *
 * Servicio de Dominio responsable de verificar si un jugador puede completar
 * su Gate actual y procesar el ascenso de rango.
 */
public class GateService {

    /**
     * Verifica si el jugador cumple los requisitos para la Gate actual, pero NO aplica el ascenso.
     */
    public GateVerificationResult verifyGateProgress(Player player) {
        Optional<SystemQuestType> nextGateOpt = findNextGate(player);

        if (nextGateOpt.isEmpty()) {
            return new GateVerificationResult(false, null, List.of(), "No hay más Gates disponibles para tu rango actual.");
        }

        SystemQuestType gate = nextGateOpt.get();
        GateTracker tracker = player.getGateTracker();

        // Creamos el contexto. Pasamos 'gate' (el Enum) directamente.
        // Si ConditionContext espera SystemQuest (Entidad), tendrás que ajustar ConditionContext para aceptar SystemQuestType
        // o pasar null si tus condiciones no dependen de la instancia de la quest.
        ConditionContext context = ConditionContext.create(player, null, tracker);

        List<GateCondition> failedConditions = new ArrayList<>();

        // 1. Verificar Nivel (Hardcoded en el Enum, no es una GateCondition en la lista)
        if (!gate.meetsLevelRequirement(player.getLevel())) {
            // Creamos una condición 'fake' o mensaje para indicar fallo de nivel
            // O idealmente, añadir LevelRequirement a la lista de condiciones en el Enum.
            // Aquí asumimos que si falla nivel, fallamos globalmente.
            return new GateVerificationResult(false, gate, List.of(), "Nivel insuficiente. Requieres Nivel " + gate.getLevelRequirement());
        }

        // 2. Verificar Condiciones de la lista
        for (GateCondition condition : gate.getConditions()) {
            if (!condition.isMet(context)) {
                failedConditions.add(condition);
            }
        }

        if (failedConditions.isEmpty()) {
            return GateVerificationResult.success(gate);
        } else {
            return GateVerificationResult.failure(gate, failedConditions);
        }
    }

    /**
     * Intenta completar la Gate y ascender al jugador.
     */
    public GateVerificationResult attemptGateCompletion(Player player) {
        // 1. Verificar requisitos
        GateVerificationResult verification = verifyGateProgress(player);

        if (!verification.success()) {
            return verification;
        }

        SystemQuestType gate = verification.gate();

        // 2. Ascenso de rango (promoteToRank ignora null para gates especiales)
        player.promoteToRank(gate.getRankUnlocked());

        // 3. Registrar en historial
        player.getGateTracker().markGateAsCompleted(gate);

        // 4. Entregar recompensas (respeta rewardStat, p.ej. GATE 3 → INT XP)
        player.applyQuestReward(gate.getBaseReward());

        System.out.println("⛩️ GATE COMPLETADA: " + gate.getName());
        if (gate.getRankUnlocked() != null) {
            System.out.println("   Rank Up: " + gate.getRankUnlocked());
        }

        return verification;
    }

    /**
     * Busca la siguiente Gate basada en el rango actual del jugador.
     * Gestiona lógica de múltiples fases (ej: Rank C tiene Phase 1 y Phase 2).
     */
    private Optional<SystemQuestType> findNextGate(Player player) {
        PlayerRank currentRank = player.getCurrentRank();

        // Cadena lineal de ascenso (Biblia cap 2.4). Vault y Redemption quedan fuera (especiales).
        return switch (currentRank) {
            case E -> Optional.of(SystemQuestType.GATE_E_TO_D);
            case D -> Optional.of(SystemQuestType.GATE_D_TO_C);
            case C -> Optional.of(SystemQuestType.GATE_C_TO_C_PLUS);
            case C_PLUS -> Optional.of(SystemQuestType.GATE_C_PLUS_TO_B);
            case B -> Optional.of(SystemQuestType.GATE_B_TO_A);
            case A -> Optional.of(SystemQuestType.GATE_A_TO_S);
            case S -> Optional.of(SystemQuestType.GATE_ENDGAME);
        };
    }
}