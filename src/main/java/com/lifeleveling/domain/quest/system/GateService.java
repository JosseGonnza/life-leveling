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

        return verifyGate(player, nextGateOpt.get());
    }

    /**
     * Verifica una Gate concreta sin asumir que sea la siguiente de la cadena lineal.
     * Necesario para las gates especiales (Vault/Redemption), que viven fuera de findNextGate.
     */
    public GateVerificationResult verifyGate(Player player, SystemQuestType gate) {
        GateTracker tracker = player.getGateTracker();

        // Las especiales no existen para el jugador hasta que se manifiestan (Vault@nivel 40,
        // Redemption tras caer). No se pueden ni ver ni desafiar antes.
        if (gate.isSpecialGate() && !hasAppeared(player, gate)) {
            return new GateVerificationResult(false, gate, List.of(), "Esta Gate aún no se ha manifestado.");
        }

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
        Optional<SystemQuestType> nextGateOpt = findNextGate(player);
        if (nextGateOpt.isEmpty()) {
            return new GateVerificationResult(false, null, List.of(), "No hay más Gates disponibles para tu rango actual.");
        }
        return attemptGate(player, nextGateOpt.get());
    }

    /**
     * Intenta completar una Gate concreta (incluye las especiales Vault/Redemption).
     */
    public GateVerificationResult attemptGate(Player player, SystemQuestType gate) {
        // 0. Una Gate ya superada no se vuelve a reclamar (evita repetir recompensa).
        if (player.getGateTracker().isGateCompleted(gate)) {
            return new GateVerificationResult(false, gate, List.of(), "Esta Gate ya ha sido superada.");
        }

        // 1. Verificar requisitos
        GateVerificationResult verification = verifyGate(player, gate);

        if (!verification.success()) {
            return verification;
        }

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

    private static final int REDEMPTION_BURNOUTS = 3;

    /**
     * ¿Se ha manifestado esta Gate para el jugador? Las lineales aparecen por la cadena de rango;
     * las especiales solo cuando se activa su disparador: The Vault al alcanzar el nivel 40,
     * Redemption tras caer en desgracia (3 burnouts en el último mes).
     */
    public boolean hasAppeared(Player player, SystemQuestType gate) {
        return switch (gate) {
            case GATE_VAULT -> player.getLevel() >= SystemQuestType.GATE_VAULT.getLevelRequirement();
            case GATE_REDEMPTION -> player.getGateTracker().getBurnoutsInLastMonth() >= REDEMPTION_BURNOUTS;
            default -> true;
        };
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