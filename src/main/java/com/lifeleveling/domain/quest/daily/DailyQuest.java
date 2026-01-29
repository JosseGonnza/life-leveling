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
        // ... (Validaciones omitidas para brevedad, son las mismas de siempre) ...
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

    // Factories standard...
    public static DailyQuest create(DailyQuestType type, LocalDate questDate) {
        return new DailyQuest(QuestId.generate(), type, questDate, QuestStatus.PENDING, Instant.now(), null, null, null, 0, 0);
    }

    public static DailyQuest reconstitute(QuestId id, DailyQuestType type, LocalDate questDate, QuestStatus status,
                                          Instant createdAt, Instant completedAt, Boolean bIn, Integer iIn, int streak, int best) {
        return new DailyQuest(id, type, questDate, status, createdAt, completedAt, bIn, iIn, streak, best);
    }

    // ========================================================================================
    // LOGIC
    // ========================================================================================

    @Override
    public QuestReward reward() {
        if (!isCompleted() || type.isExternallyManaged()) return QuestReward.empty();
        return type.requiresBooleanInput() ? type.calculateReward(booleanInput) : type.calculateReward(integerInput);
    }

    /**
     * [MODIFICADO] Obtiene el impacto en HP considerando el input y los títulos.
     * Requiere pasar el inventario de títulos del jugador para calcular buffs.
     */
    public int getHPEffect(TitleInventory titles) {
        if (type == DailyQuestType.SLEEP && integerInput != null) {
            return type.calculateDynamicHP(integerInput, titles);
        }
        // Para el resto (Gym, Diet...), usamos la base (pero podríamos aplicar buffs aquí también si existieran)
        return type.calculateDynamicHP(null, titles);
    }

    // ========================================================================================
    // STATE TRANSITIONS
    // ========================================================================================

    public DailyQuest completeWithInput(boolean input, Instant completedAt) {
        // Validaciones...
        return new DailyQuest(id, type, questDate, QuestStatus.COMPLETED, createdAt, completedAt, input, null, currentStreak + 1, Math.max(bestStreak, currentStreak + 1));
    }

    public DailyQuest completeWithInput(int value, Instant completedAt) {
        // Validaciones...
        return new DailyQuest(id, type, questDate, QuestStatus.COMPLETED, createdAt, completedAt, null, value, currentStreak + 1, Math.max(bestStreak, currentStreak + 1));
    }

    public DailyQuest markAsExternallyCompleted(Instant completedAt) {
        // Validaciones...
        return new DailyQuest(id, type, questDate, QuestStatus.COMPLETED, createdAt, completedAt, null, null, currentStreak + 1, Math.max(bestStreak, currentStreak + 1));
    }

    public DailyQuest fail(Instant failedAt) {
        return new DailyQuest(id, type, questDate, QuestStatus.FAILED, createdAt, null, null, null, 0, bestStreak);
    }

    // Getters básicos
    @Override public QuestId id() { return id; }
    @Override public String name() { return type.getName(); }
    @Override public String description() { return type.getDescription(); }
    @Override public QuestRank rank() { return type.getRank(); }
    @Override public QuestStatus status() { return status; }
    @Override public Instant createdAt() { return createdAt; }
    @Override public DailyQuest complete(Instant at) { throw new UnsupportedOperationException(); }

    public DailyQuestType getType() { return type; }
    public Boolean getBooleanInput() { return booleanInput; }
    public Integer getIntegerInput() { return integerInput; }
    public boolean isCompleted() { return status == QuestStatus.COMPLETED; }
}