package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.quest.condition.GateCondition;
import java.util.List;

/**
 * Resultado del intento de completar una Gate.
 *
 * @param success Si el jugador ha logrado completar la Gate.
 * @param gate La Gate que se intentó completar.
 * @param failedConditions Lista de condiciones que NO se cumplieron (vacía si success=true).
 * @param message Mensaje descriptivo para el usuario.
 */
public record GateVerificationResult(
        boolean success,
        SystemQuestType gate,
        List<GateCondition> failedConditions,
        String message
) {
    public static GateVerificationResult success(SystemQuestType gate) {
        return new GateVerificationResult(true, gate, List.of(), "¡Gate Completada! Has ascendido de rango.");
    }

    public static GateVerificationResult failure(SystemQuestType gate, List<GateCondition> failed) {
        return new GateVerificationResult(false, gate, failed, "No cumples todos los requisitos.");
    }
}