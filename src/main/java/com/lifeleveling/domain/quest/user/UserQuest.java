package com.lifeleveling.domain.quest.user;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;
import java.time.LocalDate;
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
        // Validaciones internas (igual que antes)
        if (id == null) throw new IllegalArgumentException("ID null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nombre vacío");
        if (rank == null) throw new IllegalArgumentException("Rango null");
        if (status == null) throw new IllegalArgumentException("Estado null");
        if (!skipDateValidation && deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline en el pasado");
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

    // Factories (create, reconstitute...) se mantienen igual que tu versión anterior
    public static UserQuest create(String name, String description, QuestRank rank, LocalDate deadline) {
        return new UserQuest(QuestId.generate(), name, description, rank, deadline, QuestStatus.PENDING, Instant.now(), null, null, false);
    }

    public static UserQuest reconstitute(QuestId id, String name, String description, QuestRank rank, LocalDate deadline, QuestStatus status, Instant created, Instant completed, Instant failed) {
        return new UserQuest(id, name, description, rank, deadline, status, created, completed, failed, true);
    }

    // ========================================================================================
    // LÓGICA DE FALLO Y PENALIZACIONES [FASE 2.3]
    // ========================================================================================

    /**
     * Falla la misión y aplica el castigo al jugador.
     * Convierte el daño letal (si HP < 0) en deuda de oro.
     * * @param player Jugador que sufre la penalización.
     * @param failedAt Momento del fallo.
     * @return Nueva instancia de la quest en estado FAILED.
     */
    public UserQuest fail(Player player, Instant failedAt) {
        if (player == null) throw new IllegalArgumentException("Player no puede ser null");

        // 1. Obtener daño base del rango (Ej: Rango S = 50 HP)
        int moralDamage = rank.getMoralDamage();
        int currentHP = player.getCurrentHP();

        // 2. Calcular si el daño es letal (bajaría de 0 HP)
        if (currentHP - moralDamage < 0) {
            // Caso: Vida o Bolsa
            int excessDamage = Math.abs(currentHP - moralDamage);
            int goldPenalty = excessDamage * 10; // Ratio: 1 HP = 10 G [Biblia Cap 2.3]

            // a) Quitar toda la vida restante (Burnout trigger)
            if (currentHP > 0) {
                player.takeDamage(currentHP);
            }

            // b) Cobrar la diferencia en oro
            try {
                player.spendGold(goldPenalty);
                System.out.println("💀 PENALIZACIÓN CRÍTICA: -" + currentHP + " HP y -" + goldPenalty + " G (Deuda de Sangre).");
            } catch (IllegalStateException e) {
                // Si no tiene oro suficiente, spendGold lanza excepción.
                // Aquí podríamos dejarle la wallet a 0 o propagar el error.
                // Por ahora dejamos que propague para que la UI maneje el aviso "Sin fondos".
                throw e;
            }
        } else {
            // Caso: Daño normal
            player.takeDamage(moralDamage);
            System.out.println("💔 Misión Fallada: -" + moralDamage + " HP (Daño Moral).");
        }

        // 3. Retornar quest fallida (Estado inmutable)
        return fail(failedAt);
    }

    // Sobrecarga simple para transición de estado (sin jugador)
    @Override
    public UserQuest fail(Instant failedAt) {
        if (failedAt == null) throw new IllegalArgumentException("FailedAt null");
        if (!status.canTransitionTo(QuestStatus.FAILED)) throw new IllegalStateException("Estado inválido para fallar: " + status);

        return new UserQuest(id, name, description, rank, deadline, QuestStatus.FAILED, createdAt, null, failedAt, true);
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
    @Override public QuestReward reward() { return QuestReward.fromRank(rank); }

    public boolean hasDeadline() { return deadline != null; }
    public LocalDate getDeadline() { return deadline; }

    public boolean isExpired(LocalDate currentDate) {
        return hasDeadline() && currentDate.isAfter(deadline);
    }

    public String toDisplayString() {
        return String.format("%s [%s] %s | %s",
                rank.getIcon(), rank.name(), name, status.toDisplayString());
    }
}