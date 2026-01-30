package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;

/**
 * SystemQuest: Las Gates épicas de ascenso de rango.
 */
public final class SystemQuest implements Quest {

    private final QuestId id;
    private final SystemQuestType type;
    private final QuestStatus status;
    private final Instant createdAt;
    private final Instant completedAt;

    private SystemQuest(QuestId id, SystemQuestType type, QuestStatus status, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // ========================================================================================
    // FACTORIES
    // ========================================================================================

    public static SystemQuest create(SystemQuestType type) {
        return new SystemQuest(QuestId.generate(), type, QuestStatus.PENDING, Instant.now(), null);
    }

    public static SystemQuest reconstitute(QuestId id, SystemQuestType type, QuestStatus status, Instant createdAt, Instant completedAt) {
        return new SystemQuest(id, type, status, createdAt, completedAt);
    }

    // ========================================================================================
    // LOGICA DE COMPLETADO (CORE FASE 2.4)
    // ========================================================================================

    /**
     * Completa la System Quest aplicando todos los efectos al jugador.
     */
    public SystemQuest complete(Player player, Instant completedAt) {
        if (player == null) throw new IllegalArgumentException("Player no puede ser null");
        if (completedAt == null) throw new IllegalArgumentException("Timestamp no puede ser null");
        if (!status.canTransitionTo(QuestStatus.COMPLETED)) {
            throw new IllegalStateException("No se puede completar una quest en estado " + status);
        }

        // 1. Dar recompensas base
        player.addGeneralXP(type.getBaseXP());
        player.addGold(type.getBaseGold());

        // 2. Ascenso de Rango
        // [CORRECCIÓN] Como type.getRankUnlocked() ya devuelve PlayerRank, lo pasamos directo.
        if (type.getRankUnlocked() != null) {
            player.promoteToRank(type.getRankUnlocked());
        }

        // 3. Marcar en el historial del jugador
        player.getGateTracker().markGateAsCompleted(type);

        System.out.println("⛩️ GATE COMPLETADA: " + type.getName());

        // Retornar nueva instancia completada
        return new SystemQuest(id, type, QuestStatus.COMPLETED, createdAt, completedAt);
    }

    /**
     * @deprecated Usar complete(Player, Instant) para SystemQuests.
     */
    @Override
    @Deprecated
    public SystemQuest complete(Instant completedAt) {
        throw new UnsupportedOperationException(
                "SystemQuest requiere un Player para completarse. Usa complete(player, timestamp)."
        );
    }

    @Override
    public SystemQuest fail(Instant failedAt) {
        throw new UnsupportedOperationException("Las System Quests no pueden fallar.");
    }

    public SystemQuest start() {
        if (!status.canTransitionTo(QuestStatus.IN_PROGRESS)) throw new IllegalStateException("Estado inválido");
        return new SystemQuest(id, type, QuestStatus.IN_PROGRESS, createdAt, null);
    }

    // ========================================================================================
    // GETTERS & UI
    // ========================================================================================

    @Override public QuestId id() { return id; }
    @Override public String name() { return type.getName(); }
    @Override public String description() { return type.getDescription(); }

    /**
     * [CORRECCIÓN] La interfaz Quest pide QuestRank, pero nosotros guardamos PlayerRank.
     * Hacemos la conversión semántica aquí.
     */
    @Override
    public QuestRank rank() {
        if (type.getRankUnlocked() == null) return QuestRank.E; // Fallback por seguridad
        try {
            return QuestRank.valueOf(type.getRankUnlocked().name());
        } catch (IllegalArgumentException e) {
            return QuestRank.E; // Si no hay equivalencia directa
        }
    }

    @Override public QuestStatus status() { return status; }
    @Override public Instant createdAt() { return createdAt; }
    @Override public QuestReward reward() { return isCompleted() ? type.getBaseReward() : QuestReward.empty(); }

    public SystemQuestType getType() { return type; }

    public boolean isAvailableFor(int level, boolean prevGateDone) {
        return !isCompleted() && type.meetsLevelRequirement(level) && (!type.hasPreviousGate() || prevGateDone);
    }

    public String formatReward() {
        StringBuilder sb = new StringBuilder();
        QuestReward r = type.getBaseReward();
        if (r.generalXP() > 0) sb.append(String.format("⭐ +%,d XP", r.generalXP()));
        if (r.gold() > 0) sb.append(String.format(" | 💰 +%,d G", r.gold()));
        if (type.getRankUnlocked() != null) sb.append(" | 🔓 Rango ").append(type.getRankUnlocked().name());
        return sb.toString();
    }
}