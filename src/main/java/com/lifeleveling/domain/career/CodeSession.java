package com.lifeleveling.domain.career;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public final class CodeSession {

    // ... (Campos existentes) ...
    private final UUID id;
    private final LocalDate date;
    private final Instant registeredAt;
    private final double hours;
    private final CareerReward reward;
    private final boolean flowAchieved;

    // ... (Constructor privado existente) ...
    private CodeSession(UUID id, LocalDate date, Instant registeredAt, double hours, CareerReward reward, boolean flowAchieved) {
        this.id = Objects.requireNonNull(id);
        this.date = Objects.requireNonNull(date);
        this.registeredAt = Objects.requireNonNull(registeredAt);
        this.reward = Objects.requireNonNull(reward);
        this.hours = hours;
        this.flowAchieved = flowAchieved;
    }

    // ... (Factories register y reconstitute igual que antes) ...
    public static CodeSession register(double hours, Instant registeredAt) {
        // ... validaciones ...
        if (hours < CodeSessionConstants.MIN_SESSION_HOURS) throw new IllegalArgumentException("Mínimo " + CodeSessionConstants.MIN_SESSION_HOURS + "h");
        if (hours > CodeSessionConstants.MAX_DAILY_HOURS) throw new IllegalArgumentException("Máximo " + CodeSessionConstants.MAX_DAILY_HOURS + "h");

        boolean flow = hours >= CodeSessionConstants.FLOW_THRESHOLD_HOURS;
        CareerReward reward = CareerReward.calculate(hours, flow);
        LocalDate date = registeredAt.atZone(ZoneId.systemDefault()).toLocalDate();

        return new CodeSession(UUID.randomUUID(), date, registeredAt, hours, reward, flow);
    }

    // [NUEVO] Método para modificar la recompensa (inmutable)
    public CodeSession withReward(CareerReward newReward) {
        return new CodeSession(this.id, this.date, this.registeredAt, this.hours, newReward, this.flowAchieved);
    }

    // ... (Resto de getters y helpers igual que antes) ...
    public UUID getId() { return id; }
    public LocalDate getDate() { return date; }
    public Instant getRegisteredAt() { return registeredAt; }
    public double getHours() { return hours; }
    public CareerReward getReward() { return reward; }
    public boolean isFlowAchieved() { return flowAchieved; }
    public boolean isToday() { return date.equals(LocalDate.now()); }
    public LocalTime getTimeOfDay() { return registeredAt.atZone(ZoneId.systemDefault()).toLocalTime(); }

    // ... (toString y equals) ...
    public String toDisplayString() {
        String timeStr = String.format("%02d:%02d", getTimeOfDay().getHour(), getTimeOfDay().getMinute());
        String flowStr = flowAchieved ? " | 🔥 FLOW!" : "";
        return String.format("🕐 %s | %.1fh | %s%s", timeStr, hours, reward.toDisplayString(), flowStr);
    }
}