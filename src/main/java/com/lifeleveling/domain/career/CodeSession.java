package com.lifeleveling.domain.career;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una sesión individual de código.
 *
 * Según la Biblia (Cap 2.1 §3B):
 *   - El usuario puede meter inputs varias veces al día
 *   - El sistema suma las horas y el daño HP acumulado
 *   - El Flow Bonus se calcula POR SESIÓN (no por total diario)
 *
 * Cada sesión es inmutable una vez creada.
 */
public final class CodeSession {

    private final UUID id;
    private final LocalDate date;
    private final Instant registeredAt;
    private final double hours;
    private final CareerReward reward;
    private final boolean flowAchieved;

    private CodeSession(
            UUID id,
            LocalDate date,
            Instant registeredAt,
            double hours,
            CareerReward reward,
            boolean flowAchieved
    ) {
        this.id = Objects.requireNonNull(id, "ID no puede ser null");
        this.date = Objects.requireNonNull(date, "Fecha no puede ser null");
        this.registeredAt = Objects.requireNonNull(registeredAt, "Timestamp no puede ser null");
        this.reward = Objects.requireNonNull(reward, "Reward no puede ser null");

        if (hours < 0) {
            throw new IllegalArgumentException("Las horas no pueden ser negativas: " + hours);
        }
        if (hours > CodeSessionConstants.MAX_DAILY_HOURS) {
            throw new IllegalArgumentException(
                    String.format("Las horas (%.1f) exceden el máximo diario (%.1f)",
                            hours, CodeSessionConstants.MAX_DAILY_HOURS)
            );
        }

        this.hours = hours;
        this.flowAchieved = flowAchieved;
    }

    // ========================================================================================
    // FACTORÍAS
    // ========================================================================================

    /**
     * Crea una nueva sesión de código registrada ahora.
     *
     * @param hours Horas trabajadas en esta sesión
     * @return Nueva CodeSession con recompensas calculadas
     */
    public static CodeSession register(double hours) {
        return register(hours, Instant.now());
    }

    /**
     * Crea una nueva sesión de código con timestamp específico.
     * Útil para tests y reconstrucción desde persistencia.
     *
     * @param hours Horas trabajadas
     * @param registeredAt Momento del registro
     * @return Nueva CodeSession con recompensas calculadas
     */
    public static CodeSession register(double hours, Instant registeredAt) {
        validateHours(hours);

        boolean flow = hours >= CodeSessionConstants.FLOW_THRESHOLD_HOURS;
        CareerReward reward = CareerReward.calculate(hours, flow);
        LocalDate date = registeredAt.atZone(ZoneId.systemDefault()).toLocalDate();

        return new CodeSession(
                UUID.randomUUID(),
                date,
                registeredAt,
                hours,
                reward,
                flow
        );
    }

    /**
     * Reconstruye una sesión desde persistencia (JSON/DB).
     */
    public static CodeSession reconstitute(
            UUID id,
            LocalDate date,
            Instant registeredAt,
            double hours,
            CareerReward reward,
            boolean flowAchieved
    ) {
        return new CodeSession(id, date, registeredAt, hours, reward, flowAchieved);
    }

    private static void validateHours(double hours) {
        if (hours < CodeSessionConstants.MIN_SESSION_HOURS) {
            throw new IllegalArgumentException(
                    String.format("La sesión mínima es %.1f horas. Registrado: %.1f",
                            CodeSessionConstants.MIN_SESSION_HOURS, hours)
            );
        }
        if (hours > CodeSessionConstants.MAX_DAILY_HOURS) {
            throw new IllegalArgumentException(
                    String.format("El máximo por sesión es %.1f horas. Registrado: %.1f",
                            CodeSessionConstants.MAX_DAILY_HOURS, hours)
            );
        }
    }

    // ========================================================================================
    // GETTERS
    // ========================================================================================

    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public double getHours() {
        return hours;
    }

    public CareerReward getReward() {
        return reward;
    }

    public boolean isFlowAchieved() {
        return flowAchieved;
    }

    // ========================================================================================
    // HELPERS
    // ========================================================================================

    /**
     * Obtiene la hora del día en que se registró (para mostrar en UI).
     */
    public LocalTime getTimeOfDay() {
        return registeredAt.atZone(ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * Verifica si esta sesión pertenece al día actual.
     */
    public boolean isToday() {
        return date.equals(LocalDate.now());
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    /**
     * Formato para lista de sesiones en UI.
     * Ejemplo: "🕐 09:15 | 3.0h | +180 INT +60 DIS +50 WIS | 🔥 FLOW!"
     */
    public String toDisplayString() {
        String timeStr = String.format("%02d:%02d", getTimeOfDay().getHour(), getTimeOfDay().getMinute());
        String flowStr = flowAchieved ? " | 🔥 FLOW!" : "";

        return String.format("🕐 %s | %.1fh | %s%s",
                timeStr,
                hours,
                reward.toDisplayString(),
                flowStr
        );
    }

    /**
     * Formato compacto para resúmenes.
     * Ejemplo: "3.0h 🔥"
     */
    public String toCompactString() {
        return String.format("%.1fh%s", hours, flowAchieved ? " 🔥" : "");
    }

    @Override
    public String toString() {
        return String.format("CodeSession[id=%s, date=%s, hours=%.1f, flow=%s, reward=%s]",
                id, date, hours, flowAchieved, reward);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeSession that = (CodeSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
