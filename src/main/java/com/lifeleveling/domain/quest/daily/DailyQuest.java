package com.lifeleveling.domain.quest.daily;

import com.lifeleveling.domain.quest.shared.*;
import com.lifeleveling.domain.title.TitleInventory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class DailyQuest implements Quest {

    private final QuestId id;
    private final DailyQuestType type;
    private final LocalDate questDate;
    private final QuestStatus status;
    private final Instant createdAt;
    private final Instant completedAt;
    private final Boolean booleanInput;
    private final Integer integerInput;
    private final int currentStreak;
    private final int bestStreak;

    private DailyQuest(QuestId id, DailyQuestType type, LocalDate questDate, QuestStatus status,
                       Instant createdAt, Instant completedAt, Boolean booleanInput, Integer integerInput,
                       int currentStreak, int bestStreak) {
        if (id == null) throw new IllegalArgumentException("El ID no puede ser null");
        if (type == null) throw new IllegalArgumentException("El tipo no puede ser null");
        if (questDate == null) throw new IllegalArgumentException("La fecha no puede ser null");
        if (status == null) throw new IllegalArgumentException("El estado no puede ser null");
        if (currentStreak < 0 || bestStreak < 0) throw new IllegalArgumentException("Las rachas no pueden ser negativas");
        if (bestStreak < currentStreak) throw new IllegalArgumentException("bestStreak no puede ser menor que currentStreak");
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

    // ========================================================================================
    // FACTORIES
    // ========================================================================================

    public static DailyQuest create(DailyQuestType type, LocalDate questDate) {
        return new DailyQuest(QuestId.generate(), type, questDate, QuestStatus.PENDING,
                Instant.now(), null, null, null, 0, 0);
    }

    public static DailyQuest createWithStreak(DailyQuestType type, LocalDate questDate, int currentStreak, int bestStreak) {
        return new DailyQuest(QuestId.generate(), type, questDate, QuestStatus.PENDING,
                Instant.now(), null, null, null, currentStreak, bestStreak);
    }

    public static DailyQuest reconstitute(QuestId id, DailyQuestType type, LocalDate questDate, QuestStatus status,
                                          Instant createdAt, Instant completedAt, Boolean bIn, Integer iIn,
                                          int streak, int best) {
        return new DailyQuest(id, type, questDate, status, createdAt, completedAt, bIn, iIn, streak, best);
    }

    // ========================================================================================
    // STATE TRANSITIONS
    // ========================================================================================

    public DailyQuest completeWithInput(boolean input, Instant completedAt) {
        if (completedAt == null) throw new IllegalArgumentException("El timestamp no puede ser null");
        if (!type.requiresBooleanInput()) throw new IllegalArgumentException(type + " no acepta input booleano");
        if (!input) throw new IllegalArgumentException("No se puede completar una quest con 'false'");
        return new DailyQuest(id, type, questDate, QuestStatus.COMPLETED, createdAt, completedAt, input, null,
                currentStreak + 1, Math.max(bestStreak, currentStreak + 1));
    }

    public DailyQuest completeWithInput(int value, Instant completedAt) {
        if (completedAt == null) throw new IllegalArgumentException("El timestamp no puede ser null");
        if (!type.requiresNumericInput()) throw new IllegalArgumentException(type + " no acepta input numérico");
        if (!type.meetsCondition(value))
            throw new IllegalArgumentException("El valor " + value + " no cumple la condición de " + type);
        return new DailyQuest(id, type, questDate, QuestStatus.COMPLETED, createdAt, completedAt, null, value,
                currentStreak + 1, Math.max(bestStreak, currentStreak + 1));
    }

    public DailyQuest markAsExternallyCompleted(Instant completedAt) {
        if (completedAt == null) throw new IllegalArgumentException("El timestamp no puede ser null");
        if (!type.isExternallyManaged()) throw new IllegalArgumentException(type + " no es de gestión externa");
        return new DailyQuest(id, type, questDate, QuestStatus.COMPLETED, createdAt, completedAt, null, null,
                currentStreak + 1, Math.max(bestStreak, currentStreak + 1));
    }

    public DailyQuest fail(Instant failedAt) {
        return new DailyQuest(id, type, questDate, QuestStatus.FAILED, createdAt, null, null, null, 0, bestStreak);
    }

    // ========================================================================================
    // REWARD & HP
    // ========================================================================================

    @Override
    public QuestReward reward() {
        if (!isCompleted() || type.isExternallyManaged()) return QuestReward.empty();
        return type.requiresBooleanInput() ? type.calculateReward(booleanInput) : type.calculateReward(integerInput);
    }

    public int getHPEffect() {
        return type.getHpEffect();
    }

    public int getHPEffect(TitleInventory titles) {
        if (type == DailyQuestType.SLEEP && integerInput != null) {
            return type.calculateDynamicHP(integerInput, titles);
        }
        return type.calculateDynamicHP(null, titles);
    }

    // ========================================================================================
    // STREAK & DATE QUERIES
    // ========================================================================================

    public boolean hasActiveStreak() { return currentStreak > 0; }
    public boolean isNewStreakRecord() { return currentStreak > 0 && currentStreak == bestStreak; }
    public boolean isForToday() { return questDate.equals(LocalDate.now()); }
    public boolean isPast() { return questDate.isBefore(LocalDate.now()); }

    // ========================================================================================
    // QUEST INTERFACE
    // ========================================================================================

    @Override public QuestId id() { return id; }
    @Override public String name() { return type.getName(); }
    @Override public String description() { return type.getDescription(); }
    @Override public QuestRank rank() { return type.getRank(); }
    @Override public QuestStatus status() { return status; }
    @Override public Instant createdAt() { return createdAt; }

    @Override
    public DailyQuest complete(Instant at) {
        throw new UnsupportedOperationException("Usa completeWithInput() para las Daily Quests.");
    }

    // ========================================================================================
    // GETTERS
    // ========================================================================================

    public DailyQuestType getType() { return type; }
    public LocalDate getQuestDate() { return questDate; }
    public Boolean getBooleanInput() { return booleanInput; }
    public Integer getIntegerInput() { return integerInput; }
    public Instant getCompletedAt() { return completedAt; }
    public int getCurrentStreak() { return currentStreak; }
    public int getBestStreak() { return bestStreak; }
    public boolean isCompleted() { return status == QuestStatus.COMPLETED; }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder(type.toDisplayString());
        if (isCompleted()) {
            sb.append(" ✓ Completada");
            if (integerInput != null) {
                sb.append(" (").append(integerInput);
                if (type == DailyQuestType.SLEEP) sb.append(" horas");
                else if (type == DailyQuestType.READ) sb.append(" págs");
                sb.append(")");
            }
            if (hasActiveStreak()) sb.append(" | 🔥 Racha: ").append(currentStreak);
            if (isNewStreakRecord()) sb.append(" | ⭐ ¡RÉCORD!");
        } else {
            sb.append(" | ").append(status.toDisplayString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((DailyQuest) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
