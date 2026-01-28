package com.lifeleveling.domain.quest.weekly;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.quest.shared.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * WeeklyQuest: Desafío semanal rotativo.
 *
 * Características:
 *   - 3 quests activas por semana (seleccionadas de 6 posibles)
 *   - Reset automático cada Lunes 00:00
 *   - Sin penalización por no completar ("Bonus Tracks")
 *   - Tracking de progreso acumulado durante la semana
 *
 * Flujo:
 *   1. Lunes 00:00: Sistema selecciona 3 quests aleatorias
 *   2. Durante la semana: Jugador progresa según actividad
 *   3. Domingo 23:59: Si completó, recibe recompensa
 *   4. Lunes 00:00: Reset y nueva selección
 */
public final class WeeklyQuest implements Quest {

    private final QuestId id;
    private final WeeklyQuestType type;
    private final QuestStatus status;
    private final LocalDate weekStartDate;  // Lunes de la semana
    private final int currentProgress;      // Progreso acumulado
    private final Instant createdAt;
    private final Instant completedAt;

    private WeeklyQuest(
            QuestId id,
            WeeklyQuestType type,
            QuestStatus status,
            LocalDate weekStartDate,
            int currentProgress,
            Instant createdAt,
            Instant completedAt
    ) {
        this.id = Objects.requireNonNull(id, "ID no puede ser null");
        this.type = Objects.requireNonNull(type, "Type no puede ser null");
        this.status = Objects.requireNonNull(status, "Status no puede ser null");
        this.weekStartDate = Objects.requireNonNull(weekStartDate, "WeekStartDate no puede ser null");
        this.currentProgress = currentProgress;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt no puede ser null");
        this.completedAt = completedAt;

        if (currentProgress < 0) {
            throw new IllegalArgumentException("El progreso no puede ser negativo: " + currentProgress);
        }
    }

    // ========================================================================================
    // FACTORY METHODS
    // ========================================================================================

    /**
     * Crea una nueva Weekly Quest para la semana actual.
     */
    public static WeeklyQuest create(WeeklyQuestType type) {
        return create(type, LocalDate.now());
    }

    /**
     * Crea una nueva Weekly Quest para una fecha específica.
     * La fecha se ajusta al Lunes de esa semana.
     */
    public static WeeklyQuest create(WeeklyQuestType type, LocalDate referenceDate) {
        Objects.requireNonNull(type, "Type no puede ser null");
        Objects.requireNonNull(referenceDate, "Reference date no puede ser null");

        LocalDate monday = getWeekStart(referenceDate);

        return new WeeklyQuest(
                QuestId.generate(),
                type,
                QuestStatus.IN_PROGRESS,
                monday,
                0,
                Instant.now(),
                null
        );
    }

    /**
     * Reconstituye una Weekly Quest desde persistencia.
     */
    public static WeeklyQuest reconstitute(
            QuestId id,
            WeeklyQuestType type,
            QuestStatus status,
            LocalDate weekStartDate,
            int currentProgress,
            Instant createdAt,
            Instant completedAt
    ) {
        return new WeeklyQuest(id, type, status, weekStartDate, currentProgress, createdAt, completedAt);
    }

    // ========================================================================================
    // PROGRESS TRACKING
    // ========================================================================================

    /**
     * Añade progreso a la quest.
     * Retorna una nueva instancia con el progreso actualizado.
     */
    public WeeklyQuest addProgress(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("El progreso a añadir no puede ser negativo: " + amount);
        }
        if (status != QuestStatus.IN_PROGRESS) {
            return this; // No se puede añadir progreso si no está activa
        }

        int newProgress = this.currentProgress + amount;

        // Auto-complete si alcanza el objetivo
        if (type.isConditionMet(newProgress) && status == QuestStatus.IN_PROGRESS) {
            return new WeeklyQuest(
                    id, type, QuestStatus.COMPLETED, weekStartDate,
                    newProgress, createdAt, Instant.now()
            );
        }

        return new WeeklyQuest(
                id, type, status, weekStartDate,
                newProgress, createdAt, completedAt
        );
    }

    /**
     * Establece el progreso directamente (útil para cálculos batch).
     */
    public WeeklyQuest setProgress(int newProgress) {
        if (newProgress < 0) {
            throw new IllegalArgumentException("El progreso no puede ser negativo: " + newProgress);
        }
        if (status != QuestStatus.IN_PROGRESS) {
            return this;
        }

        // Auto-complete si alcanza el objetivo
        if (type.isConditionMet(newProgress)) {
            return new WeeklyQuest(
                    id, type, QuestStatus.COMPLETED, weekStartDate,
                    newProgress, createdAt, Instant.now()
            );
        }

        return new WeeklyQuest(
                id, type, status, weekStartDate,
                newProgress, createdAt, completedAt
        );
    }

    // ========================================================================================
    // WEEK MANAGEMENT
    // ========================================================================================

    /**
     * Verifica si la quest pertenece a la semana actual.
     */
    public boolean isCurrentWeek() {
        return isCurrentWeek(LocalDate.now());
    }

    /**
     * Verifica si la quest pertenece a la semana de una fecha dada.
     */
    public boolean isCurrentWeek(LocalDate referenceDate) {
        LocalDate currentWeekStart = getWeekStart(referenceDate);
        return weekStartDate.equals(currentWeekStart);
    }

    /**
     * Verifica si la quest ha expirado (pasó la semana sin completarse).
     */
    public boolean isExpired() {
        return isExpired(LocalDate.now());
    }

    /**
     * Verifica si la quest ha expirado para una fecha dada.
     */
    public boolean isExpired(LocalDate referenceDate) {
        if (status == QuestStatus.COMPLETED) {
            return false;
        }
        return !isCurrentWeek(referenceDate);
    }

    /**
     * Marca la quest como expirada si corresponde.
     */
    public WeeklyQuest checkExpiration(LocalDate referenceDate) {
        if (isExpired(referenceDate) && status == QuestStatus.IN_PROGRESS) {
            // Weekly Quests no penalizan, simplemente expiran
            return new WeeklyQuest(
                    id, type, QuestStatus.EXPIRED, weekStartDate,
                    currentProgress, createdAt, null
            );
        }
        return this;
    }

    /**
     * Obtiene la fecha de fin de la semana (Domingo).
     */
    public LocalDate getWeekEndDate() {
        return weekStartDate.plusDays(6);
    }

    /**
     * Días restantes en la semana (0-6).
     */
    public int getDaysRemaining() {
        return getDaysRemaining(LocalDate.now());
    }

    /**
     * Días restantes desde una fecha de referencia.
     */
    public int getDaysRemaining(LocalDate referenceDate) {
        if (!isCurrentWeek(referenceDate)) {
            return 0;
        }
        LocalDate endDate = getWeekEndDate();
        long days = endDate.toEpochDay() - referenceDate.toEpochDay();
        return (int) Math.max(0, days + 1); // +1 porque incluye el día actual
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
        return type.getRank();
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
        return type.getReward();
    }

    @Override
    public boolean canStartWith(HPState hpState) {
        // Weekly Quests siempre se pueden hacer, son "bonus"
        return hpState != HPState.CRITICAL;
    }

    @Override
    public Quest complete(Instant completedAt) {
        if (status != QuestStatus.IN_PROGRESS) {
            return this;
        }
        return new WeeklyQuest(
                id, type, QuestStatus.COMPLETED, weekStartDate,
                currentProgress, createdAt, completedAt
        );
    }

    @Override
    public Quest fail(Instant failedAt) {
        // Weekly Quests no "fallan" como tal, simplemente expiran
        // Pero mantenemos el método por contrato
        if (status != QuestStatus.IN_PROGRESS) {
            return this;
        }
        return new WeeklyQuest(
                id, type, QuestStatus.EXPIRED, weekStartDate,
                currentProgress, createdAt, null
        );
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    public WeeklyQuestType getType() {
        return type;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTarget() {
        return type.getTarget();
    }

    public double getProgressPercentage() {
        return type.getProgressPercentage(currentProgress);
    }

    public boolean isConditionMet() {
        return type.isConditionMet(currentProgress);
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    // ========================================================================================
    // UI HELPERS
    // ========================================================================================

    public String toDisplayString() {
        String progressStr = type.formatProgress(currentProgress);
        String statusStr = status == QuestStatus.COMPLETED ? " ✅" : "";
        return type.toDisplayString() + " [" + progressStr + "]" + statusStr;
    }

    public String getProgressText() {
        return type.formatProgress(currentProgress);
    }

    // ========================================================================================
    // STATIC HELPERS
    // ========================================================================================

    /**
     * Obtiene el Lunes de la semana para una fecha dada.
     */
    public static LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Obtiene el Domingo de la semana para una fecha dada.
     */
    public static LocalDate getWeekEnd(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    // ========================================================================================
    // EQUALS & HASHCODE
    // ========================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyQuest that = (WeeklyQuest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WeeklyQuest{" +
                "type=" + type +
                ", status=" + status +
                ", progress=" + currentProgress + "/" + getTarget() +
                ", week=" + weekStartDate +
                '}';
    }
}
