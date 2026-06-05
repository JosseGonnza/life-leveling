package com.lifeleveling.domain.debuff;

import com.lifeleveling.domain.player.StatType;
import java.time.Duration;
import java.util.List;

public enum DebuffType {

    FATIGUE("😫", "Fatiga",
            "Tu cuerpo no responde. Ganas la mitad de XP.",
            List.of(DebuffEffect.globalXpMult(0.5)),
            null,
            "#708090", // Gris pizarra (SlateGray)
            "Tus párpados pesan... si no duermes bien hoy, caerás."),

    CHAOS("🌪️", "Caos",
            "Tu entorno es un desastre. Tu mente también.",
            List.of(DebuffEffect.statPenalty(StatType.WISDOM, 0.20)),
            null,
            "#9370DB", // MediumPurple
            "La montaña de ropa crece. El Caos se acerca..."),

    TRAPPED("🕸️", "Atrapado",
            "Perdiendo el tiempo en el scroll infinito.",
            List.of(DebuffEffect.goldPenalty(150)),
            Duration.ZERO,
            "#556B2F", // DarkOliveGreen
            "Estás perdiendo el control del tiempo..."),

    HEAVINESS("🍔", "Pesadez",
            "Esa hamburguesa te está pasando factura.",
            List.of(DebuffEffect.statPenalty(StatType.STRENGTH, 0.05)),
            Duration.ofHours(24),
            "#D2691E", // Chocolate
            "Tu estómago ruge. Quizás no deberías haber comido eso."),

    BOREDOM("😒", "Aburrimiento",
            "Solo trabajar y no jugar te hace ineficiente.",
            List.of(DebuffEffect.statPenalty(StatType.INTELLECT, 0.10)),
            Duration.ofHours(48),
            "#B0C4DE", // LightSteelBlue
            "La vida se vuelve gris. Necesitas divertirte un poco."),

    TACHYCARDIA("💓", "Taquicardia",
            "Demasiada cafeína. Tu corazón va a mil.",
            List.of(
                    DebuffEffect.statPenalty(StatType.DISCIPLINE, 0.20),
                    DebuffEffect.disableItem("consumable_monster")
            ),
            null,
            "#DC143C", // Crimson
            "Tu corazón va demasiado rápido. Cuidado con la cafeína.");

    private final String icon;
    private final String displayName;
    private final String description;
    private final List<DebuffEffect> effects;
    private final Duration defaultDuration;

    // [FASE 10] UI Fields
    private final String colorHex;
    private final String warningMessage;

    DebuffType(String icon, String displayName, String description, List<DebuffEffect> effects,
               Duration defaultDuration, String colorHex, String warningMessage) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
        this.effects = effects;
        this.defaultDuration = defaultDuration;
        this.colorHex = colorHex;
        this.warningMessage = warningMessage;
    }

    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<DebuffEffect> getEffects() { return effects; }
    public Duration getDefaultDuration() { return defaultDuration; }

    // Getters UI
    public String getColorHex() { return colorHex; }
    public String getWarningMessage() { return warningMessage; }

    public String toDisplayString() {
        return icon + " " + displayName;
    }
}