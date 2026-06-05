package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;
import java.util.Objects;

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
        if (id == null) throw new IllegalArgumentException("El ID no puede ser null");
        if (type == null) throw new IllegalArgumentException("El tipo de gate no puede ser null");
        if (status == null) throw new IllegalArgumentException("El estado no puede ser null");
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
        if (status == QuestStatus.FAILED || status == QuestStatus.EXPIRED) {
            throw new IllegalArgumentException("Las System Quests no pueden estar en estado " + status);
        }
        if (status == QuestStatus.COMPLETED && completedAt == null) {
            throw new IllegalArgumentException("Una gate COMPLETED requiere completedAt");
        }
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
     * Transición pura a COMPLETED (sin aplicar efectos al jugador).
     * Para aplicar recompensas y ascenso de rango, usar complete(Player, Instant).
     */
    @Override
    public SystemQuest complete(Instant completedAt) {
        if (completedAt == null) throw new IllegalArgumentException("Timestamp no puede ser null");
        if (!status.canTransitionTo(QuestStatus.COMPLETED)) {
            throw new IllegalStateException("No se puede completar una quest en estado " + status);
        }
        return new SystemQuest(id, type, QuestStatus.COMPLETED, createdAt, completedAt);
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

    // ========================================================================================
    // QUERIES (delegan en el tipo)
    // ========================================================================================

    public Instant getCompletedAt() { return completedAt; }
    public int getLevelRequirement() { return type.getLevelRequirement(); }
    public SystemQuestType getPreviousGate() { return type.getPreviousGate(); }
    public PlayerRank getRankUnlocked() { return type.getRankUnlocked(); }
    public boolean isFirstGate() { return type.isFirstGate(); }
    public boolean requiresPreviousGate() { return type.hasPreviousGate(); }
    public boolean meetsLevelRequirement(int playerLevel) { return type.meetsLevelRequirement(playerLevel); }
    public boolean isSpecialGate() { return type.isSpecialGate(); }
    public boolean isEndgame() { return type.isEndgame(); }

    public String toDisplayString() {
        return type.getIcon() + " [" + rank().name() + "] " + name()
                + " | Lvl " + type.getLevelRequirement() + " | " + status.toDisplayString();
    }

    @Override
    public String toString() {
        return "SystemQuest{type=" + type + ", status=" + status + ", id=" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((SystemQuest) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}