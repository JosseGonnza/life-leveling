package com.lifeleveling.domain.quest.user;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class UserQuest implements Quest {

    private final QuestId id;
    private final String name;
    private final String description;
    private final QuestRank rank;
    private final LocalDate deadline;
    private final QuestStatus status;
    private final Instant createdAt;
    private final Instant completedAt;
    private final Instant failedAt;

    private UserQuest(QuestId id, String name, String description, QuestRank rank, LocalDate deadline,
                      QuestStatus status, Instant createdAt, Instant completedAt, Instant failedAt,
                      boolean skipDateValidation) {
        if (id == null) throw new IllegalArgumentException("ID null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nombre vacío");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Descripción vacía");
        if (rank == null) throw new IllegalArgumentException("Rango null");
        if (status == null) throw new IllegalArgumentException("Estado null");
        if (!skipDateValidation && deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline en el pasado");
        }
        // Validar coherencia de estado
        if (status == QuestStatus.COMPLETED && completedAt == null) {
            throw new IllegalArgumentException("Quest COMPLETED debe tener completedAt");
        }
        if ((status == QuestStatus.FAILED || status == QuestStatus.EXPIRED) && failedAt == null && !skipDateValidation) {
            throw new IllegalArgumentException("Quest FAILED/EXPIRED debe tener failedAt");
        }

        this.id = id;
        this.name = name.trim();
        this.description = description.trim();
        this.rank = rank;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
    }

    // ========================================================================================
    // FACTORY METHODS
    // ========================================================================================

    public static UserQuest create(String name, String description, QuestRank rank, LocalDate deadline) {
        return new UserQuest(QuestId.generate(), name, description, rank, deadline,
                QuestStatus.PENDING, Instant.now(), null, null, false);
    }

    public static UserQuest createWithoutDeadline(String name, String description, QuestRank rank) {
        return new UserQuest(QuestId.generate(), name, description, rank, null,
                QuestStatus.PENDING, Instant.now(), null, null, false);
    }

    public static UserQuest reconstitute(QuestId id, String name, String description, QuestRank rank,
                                          LocalDate deadline, QuestStatus status, Instant created,
                                          Instant completed, Instant failed) {
        return new UserQuest(id, name, description, rank, deadline, status, created, completed, failed, true);
    }

    // ========================================================================================
    // LÓGICA DE FALLO Y PENALIZACIONES
    // ========================================================================================

    /**
     * Falla la misión y aplica el castigo al jugador.
     * Delega la lógica de penalización a Player.applyUserQuestFailure().
     *
     * @param player   Jugador que sufre la penalización.
     * @param failedAt Momento del fallo.
     * @return Nueva instancia de la quest en estado FAILED.
     */
    public UserQuest fail(Player player, Instant failedAt) {
        if (player == null) throw new IllegalArgumentException("Player no puede ser null");
        if (failedAt == null) throw new IllegalArgumentException("FailedAt null");

        // Delegar al Player la aplicación de penalizaciones
        // Player.applyUserQuestFailure() maneja correctamente el clamping de oro
        player.applyUserQuestFailure(this);

        return fail(failedAt);
    }

    @Override
    public UserQuest fail(Instant failedAt) {
        if (failedAt == null) throw new IllegalArgumentException("FailedAt null");
        if (!status.canTransitionTo(QuestStatus.FAILED)) {
            throw new IllegalStateException("Estado inválido para fallar: " + status);
        }
        return new UserQuest(id, name, description, rank, deadline, QuestStatus.FAILED,
                createdAt, null, failedAt, true);
    }

    /**
     * Marca la quest como expirada (solo si tiene deadline).
     */
    public UserQuest expire(Instant expiredAt) {
        if (expiredAt == null) throw new IllegalArgumentException("ExpiredAt null");
        if (!hasDeadline()) throw new IllegalStateException("No se puede expirar una quest sin deadline");
        if (!status.canTransitionTo(QuestStatus.EXPIRED)) {
            throw new IllegalStateException("Estado inválido para expirar: " + status);
        }
        return new UserQuest(id, name, description, rank, deadline, QuestStatus.EXPIRED,
                createdAt, null, expiredAt, true);
    }

    // ========================================================================================
    // RESTRICCIONES DE INICIO
    // ========================================================================================

    /**
     * Verifica si el jugador tiene suficiente salud/energía para iniciar esta misión.
     * User Quests de rango alto (B+) requieren estado HEALTHY.
     */
    @Override
    public boolean canStartWith(HPState hpState) {
        return rank.canStartWith(hpState);
    }

    // ========================================================================================
    // OTROS MÉTODOS (Complete, Start, Getters...)
    // ========================================================================================

    @Override
    public UserQuest complete(Instant completedAt) {
        if (completedAt == null) throw new IllegalArgumentException("CompletedAt null");
        if (!status.canTransitionTo(QuestStatus.COMPLETED)) throw new IllegalStateException("No se puede completar");
        return new UserQuest(id, name, description, rank, deadline, QuestStatus.COMPLETED, createdAt, completedAt, null, true);
    }

    public UserQuest start() {
        if (!status.canTransitionTo(QuestStatus.IN_PROGRESS)) throw new IllegalStateException("No se puede iniciar");
        return new UserQuest(id, name, description, rank, deadline, QuestStatus.IN_PROGRESS, createdAt, null, null, true);
    }

    @Override public QuestId id() { return id; }
    @Override public String name() { return name; }
    @Override public String description() { return description; }
    @Override public QuestRank rank() { return rank; }
    @Override public QuestStatus status() { return status; }
    @Override public Instant createdAt() { return createdAt; }
    @Override public QuestReward reward() { return QuestReward.ofGeneralXP(rank.getBaseXP()); } // C1: las User Quests solo dan XP, no oro

    public Instant getCompletedAt() { return completedAt; }
    public Instant getFailedAt() { return failedAt; }

    public boolean hasDeadline() { return deadline != null; }
    public LocalDate getDeadline() { return deadline; }

    public boolean isExpired(LocalDate currentDate) {
        return hasDeadline() && currentDate.isAfter(deadline);
    }

    // ========================================================================================
    // CÁLCULOS DE PENALIZACIÓN
    // ========================================================================================

    /**
     * Calcula el daño moral (HP) que causa fallar esta quest.
     */
    public int calculateMoralDamage() {
        return rank.getMoralDamage();
    }

    /**
     * Calcula la penalización en oro si el daño HP causa burnout.
     * Ratio: 1 HP = 10 G (según La Biblia Cap 2.3)
     */
    public int calculateGoldDamageOnBurnout() {
        return rank.getMoralDamage() * 10;
    }

    /**
     * Calcula los días restantes hasta la deadline.
     * @return días restantes, o null si no tiene deadline
     */
    public Integer getDaysRemaining(LocalDate fromDate) {
        if (fromDate == null) throw new IllegalArgumentException("fromDate no puede ser null");
        if (!hasDeadline()) return null;
        return (int) ChronoUnit.DAYS.between(fromDate, deadline);
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rank.getIcon()).append(" [").append(rank.name()).append("] ").append(name);

        // Deadline info
        sb.append(" | ⏰ ");
        if (!hasDeadline()) {
            sb.append("Sin límite");
        } else {
            LocalDate today = LocalDate.now();
            if (status == QuestStatus.EXPIRED) {
                long daysAgo = ChronoUnit.DAYS.between(deadline, today);
                sb.append("EXPIRADA hace ").append(daysAgo).append(" días");
            } else if (today.equals(deadline)) {
                sb.append("¡HOY!");
            } else {
                int daysRemaining = (int) ChronoUnit.DAYS.between(today, deadline);
                if (daysRemaining < 0) {
                    sb.append("Expirada (").append(Math.abs(daysRemaining)).append(" días)");
                } else {
                    sb.append(daysRemaining).append(" días");
                }
            }
        }

        // Status
        sb.append(" | 📋 ").append(status.toDisplayString());

        return sb.toString();
    }

    // ========================================================================================
    // EQUALS, HASHCODE, TOSTRING
    // ========================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserQuest userQuest = (UserQuest) o;
        return Objects.equals(id, userQuest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserQuest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rank=" + rank +
                ", status=" + status +
                ", deadline=" + deadline +
                '}';
    }
}