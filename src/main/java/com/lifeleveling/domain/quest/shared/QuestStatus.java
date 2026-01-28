package com.lifeleveling.domain.quest.shared;

public enum QuestStatus {

    PENDING("📋", "Pendiente"),
    IN_PROGRESS("⚡", "En Progreso"),
    COMPLETED("✅", "Completada"),
    FAILED("❌", "Fallida"),
    EXPIRED("⏰", "Expirada");

    private final String icon;
    private final String displayName;

    QuestStatus(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == EXPIRED;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean isUnsuccessful() {
        return this == FAILED || this == EXPIRED;
    }

    public boolean canTransitionTo(QuestStatus target) {
        if (target == null) {
            throw new IllegalArgumentException("El estado objetivo no puede ser null");
        }
        if (isTerminal()) {
            return false;
        }

        return switch (this) {
            case PENDING -> target == IN_PROGRESS || target == COMPLETED ||
                    target == FAILED || target == EXPIRED;
            case IN_PROGRESS -> target == COMPLETED || target == FAILED || target == EXPIRED;
            default -> false;
        };
    }

    public String toDisplayString() {
        return icon + " " + displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }
}
