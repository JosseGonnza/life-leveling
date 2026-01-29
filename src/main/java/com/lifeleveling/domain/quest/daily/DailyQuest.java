package com.lifeleveling.domain.quest.daily;

import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/*
 * Características:
 *   - Reseteo automático a las 00:00 cada día
 *   - Input flexible: Boolean (checkbox) o Integer (valor numérico)
 *   - Tracking de racha (streak) individual
 *   - Efectos en HP (recovery o cost)
 *   - Contribuyen al "Perfect Day Bonus" (7/7)
 */

public final class DailyQuest implements Quest {

    private final QuestId id;
    private final DailyQuestType type;
    private final LocalDate questDate;  // Día al que pertenece esta quest
    private final QuestStatus status;
    private final Instant createdAt;
    private final Instant completedAt;

    // Input values (uno de los dos será null según el tipo)
    private final Boolean booleanInput;
    private final Integer integerInput;

    // Streak tracking
    private final int currentStreak;  // Racha actual
    private final int bestStreak;     // Mejor racha histórica

    private DailyQuest(
            QuestId id,
            DailyQuestType type,
            LocalDate questDate,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt,
            Boolean booleanInput,
            Integer integerInput,
            int currentStreak,
            int bestStreak
    ) {
        validation(id, type, questDate, status, createdAt, completedAt, booleanInput, integerInput, currentStreak, bestStreak);

        this.id = id;
        this.type = type;
        this.questDate = questDate;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.booleanInput = booleanInput;
        this.integerInput = integerInput;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
    }

    private static void validation(QuestId id, DailyQuestType type, LocalDate questDate, QuestStatus status, Instant createdAt, Instant completedAt, Boolean booleanInput, Integer integerInput, int currentStreak, int bestStreak) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser null");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo no puede ser null");
        }
        if (questDate == null) {
            throw new IllegalArgumentException("La fecha de la quest no puede ser null");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("La fecha de creación no puede ser null");
        }

        // IMPORTANTE: Solo validar input si el estado es COMPLETED
        // Si está FAILED o PENDING, el input puede ser null
        if (status == QuestStatus.COMPLETED) {
            if (type.requiresBooleanInput() && booleanInput == null) {
                throw new IllegalArgumentException(
                        String.format("%s requiere input Boolean", type)
                );
            }
            if (type.requiresNumericInput() && integerInput == null) {
                throw new IllegalArgumentException(
                        String.format("%s requiere input Integer", type)
                );
            }
        }

        // Validación de streaks
        if (currentStreak < 0) {
            throw new IllegalArgumentException(
                    String.format("La racha actual no puede ser negativa: %d", currentStreak)
            );
        }
        if (bestStreak < 0) {
            throw new IllegalArgumentException(
                    String.format("La mejor racha no puede ser negativa: %d", bestStreak)
            );
        }
        if (bestStreak < currentStreak) {
            throw new IllegalArgumentException(
                    String.format("La mejor racha (%d) no puede ser menor que la actual (%d)",
                            bestStreak, currentStreak)
            );
        }

        // Validación de coherencia de estado
        if (status == QuestStatus.COMPLETED && completedAt == null) {
            throw new IllegalArgumentException(
                    "Una quest COMPLETED debe tener completedAt"
            );
        }
    }

    public static DailyQuest create(DailyQuestType type, LocalDate questDate) {
        return new DailyQuest(
                QuestId.generate(),
                type,
                questDate,
                QuestStatus.PENDING,
                Instant.now(),
                null,
                null,
                null,
                0,  // Sin racha inicial
                0
        );
    }

    public static DailyQuest createWithStreak(
            DailyQuestType type,
            LocalDate questDate,
            int currentStreak,
            int bestStreak
    ) {
        return new DailyQuest(
                QuestId.generate(),
                type,
                questDate,
                QuestStatus.PENDING,
                Instant.now(),
                null,
                null,
                null,
                currentStreak,
                bestStreak
        );
    }

    public static DailyQuest reconstitute(
            QuestId id,
            DailyQuestType type,
            LocalDate questDate,
            QuestStatus status,
            Instant createdAt,
            Instant completedAt,
            Boolean booleanInput,
            Integer integerInput,
            int currentStreak,
            int bestStreak
    ) {
        return new DailyQuest(
                id,
                type,
                questDate,
                status,
                createdAt,
                completedAt,
                booleanInput,
                integerInput,
                currentStreak,
                bestStreak
        );
    }

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
        return type.getRank();  // Siempre Rango E
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

        // Calcular reward según input
        if (type.requiresBooleanInput()) {
            return type.calculateReward(booleanInput);
        } else {
            return type.calculateReward(integerInput);
        }
    }

    @Override
    public DailyQuest complete(Instant completedAt) {
        throw new UnsupportedOperationException(
                "Usa completeWithInput() para DailyQuests (requieren input)"
        );
    }

    @Override
    public DailyQuest fail(Instant failedAt) {
        if (failedAt == null) {
            throw new IllegalArgumentException("El timestamp de fallo no puede ser null");
        }
        if (!status.canTransitionTo(QuestStatus.FAILED)) {
            throw new IllegalStateException(
                    String.format("No se puede fallar una quest en estado %s", status)
            );
        }

        return new DailyQuest(
                id,
                type,
                questDate,
                QuestStatus.FAILED,
                createdAt,
                null,  // No hay completedAt
                null,  // No hay booleanInput (se falló)
                null,  // No hay integerInput (se falló)
                0,     // Racha se resetea
                bestStreak  // Best streak se mantiene
        );
    }

    public DailyQuest completeWithInput(boolean input, Instant completedAt) {
        if (completedAt == null) {
            throw new IllegalArgumentException("El timestamp de completado no puede ser null");
        }
        if (!type.requiresBooleanInput()) {
            throw new IllegalArgumentException(
                    String.format("%s requiere input INTEGER, no BOOLEAN", type)
            );
        }
        if (!status.canTransitionTo(QuestStatus.COMPLETED)) {
            throw new IllegalStateException(
                    String.format("No se puede completar una quest en estado %s", status)
            );
        }
        if (!type.meetsCondition(input)) {
            throw new IllegalArgumentException(
                    String.format("%s requiere input=true para completarse", type)
            );
        }

        int newStreak = currentStreak + 1;
        int newBest = Math.max(bestStreak, newStreak);

        return new DailyQuest(
                id,
                type,
                questDate,
                QuestStatus.COMPLETED,
                createdAt,
                completedAt,
                input,
                null,
                newStreak,
                newBest
        );
    }

    public DailyQuest completeWithInput(int value, Instant completedAt) {
        if (completedAt == null) {
            throw new IllegalArgumentException("El timestamp de completado no puede ser null");
        }
        if (!type.requiresNumericInput()) {
            throw new IllegalArgumentException(
                    String.format("%s requiere input BOOLEAN, no INTEGER", type)
            );
        }
        if (!status.canTransitionTo(QuestStatus.COMPLETED)) {
            throw new IllegalStateException(
                    String.format("No se puede completar una quest en estado %s", status)
            );
        }
        if (!type.meetsCondition(value)) {
            throw new IllegalArgumentException(
                    String.format("%s no cumple la condición con valor: %d", type, value)
            );
        }

        int newStreak = currentStreak + 1;
        int newBest = Math.max(bestStreak, newStreak);

        return new DailyQuest(
                id,
                type,
                questDate,
                QuestStatus.COMPLETED,
                createdAt,
                completedAt,
                null,
                value,
                newStreak,
                newBest
        );
    }

    public DailyQuestType getType() {
        return type;
    }

    public LocalDate getQuestDate() {
        return questDate;
    }

    public boolean isForToday() {
        return questDate.equals(LocalDate.now());
    }

    public boolean isPast() {
        return questDate.isBefore(LocalDate.now());
    }

    public Boolean getBooleanInput() {
        return booleanInput;
    }

    public Integer getIntegerInput() {
        return integerInput;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public boolean hasActiveStreak() {
        return currentStreak > 0;
    }

    public boolean isNewStreakRecord() {
        return currentStreak == bestStreak && currentStreak > 0;
    }

    public int getHPEffect() {
        // [FIX] Lógica dinámica para SLEEP
        if (type == DailyQuestType.SLEEP && integerInput != null) {
            return type.calculateDynamicHP(integerInput);
        }
        return type.getHpEffect();
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        // Icono + Nombre
        sb.append(type.toDisplayString());

        // Input value
        if (booleanInput != null) {
            sb.append(" | ").append(booleanInput ? "✓" : "✗");
        } else if (integerInput != null) {
            sb.append(" | ").append(integerInput);
            if (type == DailyQuestType.SLEEP) {
                sb.append(" horas");
            } else if (type == DailyQuestType.READ) {
                sb.append(" páginas");
            }
        }

        // Estado
        sb.append(" | ").append(status.toDisplayString());

        // Racha
        if (hasActiveStreak()) {
            sb.append(" | 🔥 Racha: ").append(currentStreak);
            if (isNewStreakRecord()) {
                sb.append(" ⭐ ¡RÉCORD!");
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "DailyQuest[type=%s, date=%s, status=%s, streak=%d/%d]",
                type, questDate, status, currentStreak, bestStreak
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyQuest that = (DailyQuest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}