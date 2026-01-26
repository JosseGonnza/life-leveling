package com.lifeleveling.domain.quest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class UserQuest implements Quest {

    private final QuestId id;
    private final String name;
    private final String description;
    private final QuestRank rank;
    private final LocalDate deadline;  // Null = sin deadline
    private final QuestStatus status;
    private final Instant createdAt;
    private final Instant completedAt; // Null si no completada
    private final Instant failedAt;    // Null si no fallida

    private UserQuest(
            QuestId id,
            String name,
            String description,
            QuestRank rank,
            LocalDate deadline,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt,
            Instant failedAt,
            boolean skipDateValidation  // ✅ NUEVO PARÁMETRO
    ) {
        validation(id, name, description, rank, deadline, status, createdAt, completedAt, failedAt, skipDateValidation);

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

    private static void validation(
            QuestId id,
            String name,
            String description,
            QuestRank rank,
            LocalDate deadline,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt,
            Instant failedAt,
            boolean skipDateValidation  // ✅ NUEVO PARÁMETRO
    ) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser null o vacío");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede ser null o vacía");
        }
        if (rank == null) {
            throw new IllegalArgumentException("El rango no puede ser null");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("La fecha de creación no puede ser null");
        }

        if (!skipDateValidation) {
            // Validación de deadline en el pasado (solo en create())
            if (deadline != null && deadline.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        String.format("La deadline no puede estar en el pasado: %s", deadline)
                );
            }
        }

        if (completedAt != null && completedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "La fecha de completado no puede ser anterior a la de creación"
            );
        }
        if (failedAt != null && failedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "La fecha de fallo no puede ser anterior a la de creación"
            );
        }

        if (status == QuestStatus.COMPLETED && completedAt == null) {
            throw new IllegalArgumentException(
                    "Una quest COMPLETED debe tener completedAt"
            );
        }
        if ((status == QuestStatus.FAILED || status == QuestStatus.EXPIRED) && failedAt == null) {
            throw new IllegalArgumentException(
                    "Una quest FAILED/EXPIRED debe tener failedAt"
            );
        }
    }

    public static UserQuest create(
            String name,
            String description,
            QuestRank rank,
            LocalDate deadline
    ) {
        return new UserQuest(
                QuestId.generate(),
                name,
                description,
                rank,
                deadline,
                QuestStatus.PENDING,
                Instant.now(),
                null,
                null,
                false  // ✅ NO skip validación = validar fechas
        );
    }

    public static UserQuest createWithoutDeadline(
            String name,
            String description,
            QuestRank rank
    ) {
        return new UserQuest(
                QuestId.generate(),
                name,
                description,
                rank,
                null,  // Sin deadline
                QuestStatus.PENDING,
                Instant.now(),
                null,
                null,
                false  // ✅ NO skip validación
        );
    }

    // ✅ reconstitute() pasa true para SKIP validación de fechas
    public static UserQuest reconstitute(
            QuestId id,
            String name,
            String description,
            QuestRank rank,
            LocalDate deadline,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt,
            Instant failedAt
    ) {
        return new UserQuest(
                id,
                name,
                description,
                rank,
                deadline,
                status,
                createdAt,
                completedAt,
                failedAt,
                true  // ✅ SÍ skip validación = permite fechas pasadas
        );
    }

    @Override
    public QuestId id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public QuestRank rank() {
        return rank;
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
        return QuestReward.fromRank(rank);
    }

    @Override
    public UserQuest complete(Instant completedAt) {
        if (completedAt == null) {
            throw new IllegalArgumentException("El timestamp de completado no puede ser null");
        }
        if (!status.canTransitionTo(QuestStatus.COMPLETED)) {
            throw new IllegalStateException(
                    String.format("No se puede completar una quest en estado %s", status)
            );
        }

        return new UserQuest(
                id,
                name,
                description,
                rank,
                deadline,
                QuestStatus.COMPLETED,
                createdAt,
                completedAt,
                null,
                true  // ✅ Skip validación en transiciones de estado
        );
    }

    @Override
    public UserQuest fail(Instant failedAt) {
        if (failedAt == null) {
            throw new IllegalArgumentException("El timestamp de fallo no puede ser null");
        }
        if (!status.canTransitionTo(QuestStatus.FAILED)) {
            throw new IllegalStateException(
                    String.format("No se puede fallar una quest en estado %s", status)
            );
        }

        return new UserQuest(
                id,
                name,
                description,
                rank,
                deadline,
                QuestStatus.FAILED,
                createdAt,
                null,
                failedAt,
                true  // ✅ Skip validación
        );
    }

    public UserQuest expire(Instant expiredAt) {
        if (expiredAt == null) {
            throw new IllegalArgumentException("El timestamp de expiración no puede ser null");
        }
        if (deadline == null) {
            throw new IllegalStateException(
                    "No se puede expirar una quest sin deadline"
            );
        }
        if (!status.canTransitionTo(QuestStatus.EXPIRED)) {
            throw new IllegalStateException(
                    String.format("No se puede expirar una quest en estado %s", status)
            );
        }

        return new UserQuest(
                id,
                name,
                description,
                rank,
                deadline,
                QuestStatus.EXPIRED,
                createdAt,
                null,
                expiredAt,
                true  // ✅ Skip validación
        );
    }

    public UserQuest start() {
        if (!status.canTransitionTo(QuestStatus.IN_PROGRESS)) {
            throw new IllegalStateException(
                    String.format("No se puede iniciar una quest en estado %s", status)
            );
        }

        return new UserQuest(
                id,
                name,
                description,
                rank,
                deadline,
                QuestStatus.IN_PROGRESS,
                createdAt,
                null,
                null,
                true  // ✅ Skip validación
        );
    }

    public boolean hasDeadline() {
        return deadline != null;
    }

    public boolean isExpired(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("La fecha actual no puede ser null");
        }
        if (!hasDeadline()) {
            return false;
        }

        return currentDate.isAfter(deadline);
    }

    public Integer getDaysRemaining(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("La fecha actual no puede ser null");
        }
        if (!hasDeadline()) {
            return null;
        }

        return (int) java.time.temporal.ChronoUnit.DAYS.between(currentDate, deadline);
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public int calculateMoralDamage() {
        return rank.getMoralDamage();
    }

    public int calculateGoldDamageOnBurnout() {
        return rank.getGoldDamageOnBurnout();
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        // Icono + Rango + Nombre
        sb.append(rank.getIcon()).append(" ");
        sb.append("[").append(rank.name()).append("] ");
        sb.append(name);

        // Deadline
        if (hasDeadline()) {
            int daysRemaining = getDaysRemaining(LocalDate.now());
            sb.append(" | ⏰ ");
            if (daysRemaining > 0) {
                sb.append(daysRemaining).append(" días");
            } else if (daysRemaining == 0) {
                sb.append("¡HOY!");
            } else {
                sb.append("EXPIRADA (").append(Math.abs(daysRemaining)).append(" días)");
            }
        } else {
            sb.append(" | ⏰ Sin límite");
        }

        // Estado
        sb.append(" | ").append(status.toDisplayString());

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "UserQuest[id=%s, name='%s', rank=%s, deadline=%s, status=%s]",
                id, name, rank, deadline, status
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserQuest that = (UserQuest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}