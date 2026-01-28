package com.lifeleveling.domain.quest.system;

import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;
import java.util.Objects;

/**
 * SystemQuest: Las Gates épicas de ascenso de rango.
 *
 * Características:
 *   - Secuenciales: Solo visible si cumples nivel Y gate anterior
 *   - Únicas: Solo se pueden completar una vez (permanentes)
 *   - Épicas: Recompensas masivas y desbloqueos de rango
 *   - Narrativa: Marcan hitos reales en la progresión
 *
 * Filosofía:
 *   "Las puertas no se abren solas. Debes demostrar que mereces cruzarlas."
 *
 * Diferencias con otras quests:
 *   - NO resetean (permanentes)
 *   - NO tienen deadline (se completan cuando se cumplen requisitos)
 *   - Desbloquean rangos y funcionalidades
 *   - Pueden requerir condiciones externas (conseguir trabajo, mudarse, etc.)
 *
 * Ejemplo:
 *   SystemQuest gate = SystemQuest.create(SystemQuestType.GATE_A_TO_S);
 *   // ... (Usuario consigue trabajo)
 *   gate = gate.complete(Instant.now());
 *   // → Rango S desbloqueado + 20,000 XP + 50,000 Gold
 */
public final class SystemQuest implements Quest {

    private final QuestId id;
    private final SystemQuestType type;
    private final QuestStatus status;
    private final Instant createdAt;
    private final Instant completedAt;

    // ========================================================================================
    // CONSTRUCTOR PRIVADO
    // ========================================================================================

    private SystemQuest(
            QuestId id,
            SystemQuestType type,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt
    ) {
        // Validaciones básicas
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo no puede ser null");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("La fecha de creación no puede ser null");
        }

        // Validación de coherencia de estado
        if (status == QuestStatus.COMPLETED && completedAt == null) {
            throw new IllegalArgumentException(
                    "Una quest COMPLETED debe tener completedAt"
            );
        }

        // SystemQuests no pueden estar FAILED o EXPIRED
        if (status == QuestStatus.FAILED || status == QuestStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Las System Quests no pueden fallar o expirar, solo completarse"
            );
        }

        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // ========================================================================================
    // FACTORY METHODS
    // ========================================================================================

    /**
     * Crea una nueva SystemQuest.
     *
     * @param type Tipo de gate
     * @return Nueva SystemQuest en estado PENDING
     */
    public static SystemQuest create(SystemQuestType type) {
        return new SystemQuest(
                QuestId.generate(),
                type,
                QuestStatus.PENDING,
                Instant.now(),
                null
        );
    }

    /**
     * Reconstituye una SystemQuest desde persistencia.
     */
    public static SystemQuest reconstitute(
            QuestId id,
            SystemQuestType type,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt
    ) {
        return new SystemQuest(id, type, status, createdAt, completedAt);
    }

    // ========================================================================================
    // QUEST INTERFACE IMPLEMENTATION
    // ========================================================================================

    @Override
    public QuestId id() {
        return id;
    }

    @Override
    public String name() {
        return type.getName();
    }

    @Override
    public String description() {
        return type.getDescription();
    }

    @Override
    public QuestRank rank() {
        return type.getRankUnlocked();
    }

    @Override
    public QuestStatus status() {
        return status;
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public QuestReward reward() {
        if (!isCompleted()) {
            return QuestReward.empty();
        }

        return type.getBaseReward();
    }

    // ========================================================================================
    // STATE TRANSITIONS
    // ========================================================================================

    @Override
    public SystemQuest complete(Instant completedAt) {
        if (completedAt == null) {
            throw new IllegalArgumentException("El timestamp de completado no puede ser null");
        }

        if (!status.canTransitionTo(QuestStatus.COMPLETED)) {
            throw new IllegalStateException(
                    String.format("No se puede completar una quest en estado %s", status)
            );
        }

        return new SystemQuest(
                id,
                type,
                QuestStatus.COMPLETED,
                createdAt,
                completedAt
        );
    }

    @Override
    public SystemQuest fail(Instant failedAt) {
        throw new UnsupportedOperationException(
                "Las System Quests no pueden fallar. Solo se completan o permanecen pendientes."
        );
    }

    /**
     * Inicia la quest (PENDING → IN_PROGRESS).
     *
     * Se marca como IN_PROGRESS cuando el jugador cumple requisitos
     * y está trabajando activamente en ella.
     */
    public SystemQuest start() {
        if (!status.canTransitionTo(QuestStatus.IN_PROGRESS)) {
            throw new IllegalStateException(
                    String.format("No se puede iniciar una quest en estado %s", status)
            );
        }

        return new SystemQuest(
                id,
                type,
                QuestStatus.IN_PROGRESS,
                createdAt,
                null
        );
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    /**
     * Obtiene el tipo de System Quest.
     */
    public SystemQuestType getType() {
        return type;
    }

    /**
     * Obtiene el timestamp de completado (null si no completada).
     */
    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Obtiene el requisito de nivel mínimo.
     */
    public int getLevelRequirement() {
        return type.getLevelRequirement();
    }

    /**
     * Obtiene la gate anterior requerida (null si es la primera).
     */
    public SystemQuestType getPreviousGate() {
        return type.getPreviousGate();
    }

    /**
     * Obtiene el rango que desbloquea al completarse.
     */
    public QuestRank getRankUnlocked() {
        return type.getRankUnlocked();
    }

    /**
     * Verifica si es la primera gate (no requiere gate anterior).
     */
    public boolean isFirstGate() {
        return type.isFirstGate();
    }

    /**
     * Verifica si tiene una gate anterior requerida.
     */
    public boolean requiresPreviousGate() {
        return type.hasPreviousGate();
    }

    /**
     * Verifica si es una gate especial (triggered por evento).
     */
    public boolean isSpecialGate() {
        return type.isSpecialGate();
    }

    /**
     * Verifica si es la gate final (endgame).
     */
    public boolean isEndgame() {
        return type.isEndgame();
    }

    /**
     * Verifica si el jugador cumple el requisito de nivel.
     */
    public boolean meetsLevelRequirement(int playerLevel) {
        return type.meetsLevelRequirement(playerLevel);
    }

    /**
     * Verifica si la quest está disponible para el jugador.
     *
     * Una SystemQuest está disponible si:
     *   1. Cumple el requisito de nivel
     *   2. La gate anterior está completada (si aplica)
     *   3. No está ya completada
     */
    public boolean isAvailableFor(int playerLevel, boolean previousGateCompleted) {
        if (isCompleted()) {
            return false;  // Ya completada
        }

        if (!meetsLevelRequirement(playerLevel)) {
            return false;  // Nivel insuficiente
        }

        if (requiresPreviousGate() && !previousGateCompleted) {
            return false;  // Gate anterior no completada
        }

        return true;
    }

    // ========================================================================================
    // UI FORMAT
    // ========================================================================================

    /**
     * Formatea la quest para mostrar en UI.
     *
     * Ejemplo: "🟡 [S] Cambio de Clase: Junior Developer | Lvl 60 | 📋 Pendiente"
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        // Icono + Rango + Nombre
        sb.append(type.getIcon()).append(" ");
        sb.append("[").append(type.getRankUnlocked().name()).append("] ");
        sb.append(type.getName());

        // Requisito de nivel
        sb.append(" | Lvl ").append(type.getLevelRequirement());

        // Estado
        sb.append(" | ").append(status.toDisplayString());

        return sb.toString();
    }

    /**
     * Formatea la recompensa para mostrar en UI.
     *
     * Ejemplo: "⭐ +20,000 XP | 💰 +50,000 G | 🔓 Rango S"
     */
    public String formatReward() {
        StringBuilder sb = new StringBuilder();

        QuestReward reward = type.getBaseReward();

        if (reward.generalXP() > 0) {
            sb.append(String.format("⭐ +%,d XP", reward.generalXP()));
        }

        if (reward.gold() > 0) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(String.format("💰 +%,d G", reward.gold()));
        }

        if (sb.length() > 0) sb.append(" | ");
        sb.append("🔓 Rango ").append(type.getRankUnlocked().name());

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "SystemQuest[type=%s, status=%s, rankUnlocked=%s]",
                type.name(), status, type.getRankUnlocked()
        );
    }

    // ========================================================================================
    // EQUALS & HASHCODE (basado en ID)
    // ========================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemQuest that = (SystemQuest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}