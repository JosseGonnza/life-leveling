package com.lifeleveling.domain.debuff;

import com.lifeleveling.domain.player.StatType;
import java.time.Duration;
import java.util.List;

public enum DebuffType {

    FATIGUE("😫", "Fatiga",
            "Tu cuerpo no responde. Ganas la mitad de XP.",
            List.of(DebuffEffect.globalXpMult(0.5)),
            null), // Dura hasta curarse durmiendo

    CHAOS("🌪️", "Caos",
            "Tu entorno es un desastre. Tu mente también.",
            List.of(DebuffEffect.statPenalty(StatType.WISDOM, 0.20)),
            null), // Persistente

    TRAPPED("🕸️", "Atrapado",
            "Perdiendo el tiempo en el scroll infinito.",
            List.of(DebuffEffect.goldPenalty(150)),
            Duration.ZERO), // Efecto inmediato, no dura en el tiempo

    HEAVINESS("🍔", "Pesadez",
            "Esa hamburguesa te está pasando factura.",
            List.of(DebuffEffect.statPenalty(StatType.STRENGTH, 0.05)),
            Duration.ofHours(24)),

    BOREDOM("😒", "Aburrimiento",
            "Solo trabajar y no jugar te hace ineficiente.",
            List.of(DebuffEffect.statPenalty(StatType.INTELLECT, 0.10)),
            Duration.ofHours(48)),

    TACHYCARDIA("💓", "Taquicardia",
            "Demasiada cafeína. Tu corazón va a mil.",
            List.of(
                    DebuffEffect.statPenalty(StatType.DISCIPLINE, 0.20),
                    DebuffEffect.disableItem("monster_energy") // El Monster ya no cura
            ),
            null); // Dura hasta el lunes

    private final String icon;
    private final String displayName;
    private final String description;
    private final List<DebuffEffect> effects;
    private final Duration defaultDuration; // null si es indefinido/evento

    DebuffType(String icon, String displayName, String description, List<DebuffEffect> effects, Duration defaultDuration) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
        this.effects = effects;
        this.defaultDuration = defaultDuration;
    }

    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<DebuffEffect> getEffects() { return effects; }
    public Duration getDefaultDuration() { return defaultDuration; }

    public String toDisplayString() {
        return icon + " " + displayName;
    }
}