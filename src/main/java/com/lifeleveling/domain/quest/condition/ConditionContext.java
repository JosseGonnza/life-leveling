package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.system.SystemQuest;

import java.time.LocalDate;

/**
 * ConditionContext: Contexto con toda la información necesaria para evaluar condiciones.
 *
 * Este objeto se pasa a cada condición para que pueda determinar si está cumplida.
 * Contiene referencias al jugador, historial de quests, tracking de eventos, etc.
 *
 * Inmutable y thread-safe.
 */
public record ConditionContext(
        Player player,
        SystemQuest gate,
        GateTracker tracker,
        LocalDate currentDate
) {

    public ConditionContext {
        if (player == null) {
            throw new IllegalArgumentException("Player no puede ser null");
        }
        if (tracker == null) {
            throw new IllegalArgumentException("Tracker no puede ser null");
        }
        if (currentDate == null) {
            throw new IllegalArgumentException("CurrentDate no puede ser null");
        }
    }

    /**
     * Crea un contexto para evaluar una gate.
     *
     * @param player Jugador actual
     * @param gate Gate que se está evaluando
     * @param tracker Tracker con historial y contadores
     * @return Nuevo contexto
     */
    public static ConditionContext create(
            Player player,
            SystemQuest gate,
            GateTracker tracker
    ) {
        return new ConditionContext(player, gate, tracker, LocalDate.now());
    }

    /**
     * Crea un contexto para testing con fecha específica.
     */
    public static ConditionContext createForTesting(
            Player player,
            SystemQuest gate,
            GateTracker tracker,
            LocalDate date
    ) {
        return new ConditionContext(player, gate, tracker, date);
    }

    // ========================================================================================
    // ACCESOS CONVENIENTES
    // ========================================================================================

    /**
     * Obtiene el nivel actual del jugador.
     */
    public int getPlayerLevel() {
        return player.getLevel();
    }

    /**
     * Obtiene el HP actual del jugador.
     */
    public int getCurrentHP() {
        return player.getCurrentHP();
    }

    /**
     * Obtiene el Gold actual del jugador.
     */
    public int getCurrentGold() {
        return player.getCurrentGold();
    }

    /**
     * Obtiene el ID de la gate actual.
     */
    public String getGateId() {
        return gate != null ? gate.id().toString() : "GENERIC_CONTEXT";
    }
}