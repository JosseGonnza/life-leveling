package com.lifeleveling.domain.title;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Title: Representa un título desbloqueado por el jugador.
 *
 * Un título es una instancia de un TitleType que el jugador ha conseguido.
 * Incluye información sobre cuándo se desbloqueó.
 *
 * Los títulos son coleccionables permanentes que pueden equiparse
 * para obtener sus buffs.
 */
public record Title(
        UUID id,
        TitleType type,
        Instant unlockedAt
) {

    public Title {
        Objects.requireNonNull(id, "ID no puede ser null");
        Objects.requireNonNull(type, "TitleType no puede ser null");
        Objects.requireNonNull(unlockedAt, "UnlockedAt no puede ser null");
    }

    // ========================================================================================
    // FACTORY METHODS
    // ========================================================================================

    /**
     * Crea un nuevo título desbloqueado ahora.
     */
    public static Title unlock(TitleType type) {
        return new Title(UUID.randomUUID(), type, Instant.now());
    }

    /**
     * Reconstituye un título desde persistencia.
     */
    public static Title reconstitute(UUID id, TitleType type, Instant unlockedAt) {
        return new Title(id, type, unlockedAt);
    }

    // ========================================================================================
    // DELEGATE METHODS (convenience)
    // ========================================================================================

    public String getDisplayName() {
        return type.getDisplayName();
    }

    public TitleCategory getCategory() {
        return type.getCategory();
    }

    public String getLore() {
        return type.getLore();
    }

    public java.util.List<TitleBuff> getBuffs() {
        return type.getBuffs();
    }

    public String getBuffsDisplay() {
        return type.getBuffsDisplay();
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    /**
     * Formatea el título para mostrar en UI.
     */
    public String toDisplayString() {
        return type.getDisplayName() + " [" + type.getCategory().getDisplayName() + "]";
    }

    /**
     * Formatea con fecha de desbloqueo.
     */
    public String toDetailedString() {
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(java.time.ZoneId.systemDefault());

        return type.getDisplayName() + " - Desbloqueado: " + formatter.format(unlockedAt);
    }

    // ========================================================================================
    // EQUALITY (basada en tipo, no en ID)
    // ========================================================================================

    /**
     * Dos títulos son iguales si son del mismo tipo.
     * Esto previene tener duplicados del mismo título.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Title title = (Title) o;
        return type == title.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
