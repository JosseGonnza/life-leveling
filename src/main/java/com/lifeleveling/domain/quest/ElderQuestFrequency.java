package com.lifeleveling.domain.quest;

public enum ElderQuestFrequency {
    UNIQUE("Única", "Se completa una vez y otorga un beneficio permanente."),
    MONTHLY("Mensual", "Se reinicia el día 1 de cada mes. Debe completarse dentro del mes natural.");

    private final String displayName;
    private final String description;

    ElderQuestFrequency(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }
}