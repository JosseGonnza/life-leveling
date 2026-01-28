package com.lifeleveling.domain.quest.elder;

import com.lifeleveling.domain.player.HPState;
import com.lifeleveling.domain.quest.condition.ConditionContext;
import com.lifeleveling.domain.quest.shared.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public final class ElderQuest implements Quest {

    private final QuestId id;
    private final ElderQuestType type;
    private final QuestStatus status;
    private final LocalDate startDate; // Cuándo empezó el periodo actual (día 1 del mes para mensuales)
    private final Instant createdAt;
    private final Instant completedAt;

    private ElderQuest(
            QuestId id,
            ElderQuestType type,
            QuestStatus status,
            LocalDate startDate,
            Instant createdAt,
            Instant completedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
        this.startDate = Objects.requireNonNull(startDate);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.completedAt = completedAt;
    }

    public static ElderQuest create(ElderQuestType type) {
        return new ElderQuest(
                QuestId.generate(),
                type,
                QuestStatus.IN_PROGRESS, // Las Elder siempre están activas si tienes el nivel
                LocalDate.now().withDayOfMonth(1), // Alineamos al día 1 por defecto
                Instant.now(),
                null
        );
    }

    /**
     * Evalúa si se han cumplido todas las condiciones.
     */
    public boolean areConditionsMet(ConditionContext context) {
        return type.getConditions().stream()
                .allMatch(condition -> condition.isMet(context));
    }

    /**
     * Calcula el progreso promedio de las condiciones (0.0 a 1.0)
     */
    public double getProgress(ConditionContext context) {
        if (type.getConditions().isEmpty()) return 0.0;

        return type.getConditions().stream()
                .mapToDouble(c -> c.getProgress(context))
                .average()
                .orElse(0.0);
    }

    /**
     * Verifica si la quest necesita resetearse (solo para Mensuales).
     * Se debe llamar al inicio de la sesión o en el DailyReset.
     */
    public ElderQuest checkReset(LocalDate currentDate) {
        if (type.getFrequency() != ElderQuestFrequency.MONTHLY) {
            return this;
        }

        YearMonth questMonth = YearMonth.from(startDate);
        YearMonth currentMonth = YearMonth.from(currentDate);

        // Si hemos cambiado de mes
        if (currentMonth.isAfter(questMonth)) {
            // CORRECCIÓN: Generamos un ID nuevo para la nueva misión mensual
            return new ElderQuest(
                    QuestId.generate(), // <--- ANTES PONÍA: this.id
                    this.type,
                    QuestStatus.IN_PROGRESS,
                    currentDate.withDayOfMonth(1), // Primer día del nuevo mes
                    this.createdAt, // Mantenemos la fecha de desbloqueo original
                    null
            );
        }

        return this;
    }

    @Override
    public Quest complete(Instant completedAt) {
        if (status != QuestStatus.IN_PROGRESS) {
            return this;
        }
        return new ElderQuest(id, type, QuestStatus.COMPLETED, startDate, createdAt, completedAt);
    }

    @Override
    public Quest fail(Instant failedAt) {
        // Las Elder Quests raramente fallan, simplemente no se cumplen y se resetean al mes siguiente.
        // Pero mantenemos el método por contrato.
        return new ElderQuest(id, type, QuestStatus.FAILED, startDate, createdAt, completedAt);
    }

    // ================= IMPL INTERFACE QUEST =================

    @Override
    public QuestId id() { return id; }

    @Override
    public String name() { return type.getName(); }

    @Override
    public String description() { return type.getDescription(); }

    @Override
    public QuestRank rank() { return QuestRank.S_PLUS; } // Rango Legendario por defecto

    @Override
    public QuestStatus status() { return status; }

    @Override
    public Instant createdAt() { return createdAt; }

    @Override
    public QuestReward reward() { return type.getReward(); }

    // Las Elder Quests requieren Nivel 75+, así que HEALTHY es implícito,
    // pero podemos forzarlo.
    @Override
    public boolean canStartWith(HPState hpState) {
        return hpState == HPState.HEALTHY;
    }

    public ElderQuestType getType() { return type; }
}