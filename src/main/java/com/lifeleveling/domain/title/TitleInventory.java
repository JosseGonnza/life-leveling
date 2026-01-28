package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TitleInventory: Gestiona los títulos desbloqueados y equipados del jugador.
 *
 * Mecánica de Slots:
 *   - Nivel 1-49: 1 slot de título activo
 *   - Nivel 50+: 2 slots de títulos activos (desbloqueo de "Leyenda")
 *
 * Regla de Oro: Solo aplican los buffs de títulos EQUIPADOS.
 * Tener un título en la colección no da stats pasivos.
 */
public class TitleInventory {

    /** Nivel mínimo para tener 2 slots de título */
    public static final int SECOND_SLOT_UNLOCK_LEVEL = 50;

    /** Máximo de slots posibles */
    public static final int MAX_TITLE_SLOTS = 2;

    // Títulos desbloqueados (colección)
    private final Set<Title> unlockedTitles;

    // Títulos equipados (máximo 2)
    private final List<Title> equippedTitles;

    // ========================================================================================
    // CONSTRUCTORS
    // ========================================================================================

    public TitleInventory() {
        this.unlockedTitles = new HashSet<>();
        this.equippedTitles = new ArrayList<>(MAX_TITLE_SLOTS);
    }

    /**
     * Constructor para reconstituir desde persistencia.
     */
    public TitleInventory(Set<Title> unlockedTitles, List<Title> equippedTitles) {
        this.unlockedTitles = new HashSet<>(unlockedTitles);
        this.equippedTitles = new ArrayList<>(equippedTitles);
    }

    // ========================================================================================
    // UNLOCK TITLES
    // ========================================================================================

    /**
     * Desbloquea un nuevo título.
     * @return true si se desbloqueó (no lo tenía), false si ya lo tenía
     */
    public boolean unlock(TitleType type) {
        if (hasTitle(type)) {
            return false;
        }

        Title newTitle = Title.unlock(type);
        unlockedTitles.add(newTitle);
        return true;
    }

    /**
     * Desbloquea un título (versión con instancia).
     */
    public boolean unlock(Title title) {
        if (hasTitle(title.type())) {
            return false;
        }
        unlockedTitles.add(title);
        return true;
    }

    /**
     * Verifica si el jugador tiene un título desbloqueado.
     */
    public boolean hasTitle(TitleType type) {
        return unlockedTitles.stream()
                .anyMatch(t -> t.type() == type);
    }

    /**
     * Obtiene un título desbloqueado por tipo.
     */
    public Optional<Title> getTitle(TitleType type) {
        return unlockedTitles.stream()
                .filter(t -> t.type() == type)
                .findFirst();
    }

    // ========================================================================================
    // EQUIP/UNEQUIP
    // ========================================================================================

    /**
     * Calcula cuántos slots de título tiene disponibles el jugador.
     */
    public int getAvailableSlots(int playerLevel) {
        return playerLevel >= SECOND_SLOT_UNLOCK_LEVEL ? 2 : 1;
    }

    /**
     * Equipa un título en un slot disponible.
     * @param type El tipo de título a equipar
     * @param playerLevel El nivel actual del jugador (determina slots disponibles)
     * @return true si se equipó correctamente
     * @throws IllegalStateException si no tiene el título o no hay slots
     */
    public boolean equip(TitleType type, int playerLevel) {
        // Verificar que tiene el título
        Optional<Title> title = getTitle(type);
        if (title.isEmpty()) {
            throw new IllegalStateException("No tienes el título: " + type.getDisplayName());
        }

        // Verificar que no está ya equipado
        if (isEquipped(type)) {
            return false; // Ya está equipado
        }

        // Verificar slots disponibles
        int maxSlots = getAvailableSlots(playerLevel);
        if (equippedTitles.size() >= maxSlots) {
            throw new IllegalStateException(
                    "No hay slots disponibles. Desequipa un título primero. " +
                            "(Slots: " + equippedTitles.size() + "/" + maxSlots + ")"
            );
        }

        equippedTitles.add(title.get());
        return true;
    }

    /**
     * Desequipa un título.
     * @return true si se desequipó, false si no estaba equipado
     */
    public boolean unequip(TitleType type) {
        return equippedTitles.removeIf(t -> t.type() == type);
    }

    /**
     * Desequipa todos los títulos.
     */
    public void unequipAll() {
        equippedTitles.clear();
    }

    /**
     * Verifica si un título está equipado.
     */
    public boolean isEquipped(TitleType type) {
        return equippedTitles.stream()
                .anyMatch(t -> t.type() == type);
    }

    /**
     * Intercambia un título equipado por otro.
     * @param oldType Título a desequipar
     * @param newType Título a equipar
     * @param playerLevel Nivel del jugador
     */
    public void swap(TitleType oldType, TitleType newType, int playerLevel) {
        if (!isEquipped(oldType)) {
            throw new IllegalStateException("El título " + oldType.getDisplayName() + " no está equipado");
        }
        if (!hasTitle(newType)) {
            throw new IllegalStateException("No tienes el título: " + newType.getDisplayName());
        }

        unequip(oldType);
        equip(newType, playerLevel);
    }

    // ========================================================================================
    // BUFF CALCULATIONS
    // ========================================================================================

    /**
     * Obtiene todos los buffs activos de los títulos equipados.
     */
    public List<TitleBuff> getActiveBuffs() {
        return equippedTitles.stream()
                .flatMap(t -> t.getBuffs().stream())
                .toList();
    }

    /**
     * Calcula el multiplicador combinado de XP para un stat.
     * Multiplica todos los buffs aplicables.
     * @return Multiplicador (1.0 si no hay buffs)
     */
    public double getStatXPMultiplier(StatType stat) {
        double multiplier = 1.0;

        for (TitleBuff buff : getActiveBuffs()) {
            if (buff.affectsStat(stat)) {
                multiplier *= buff.getMultiplier();
            }
        }

        return multiplier;
    }

    /**
     * Calcula el multiplicador combinado de XP general.
     * @return Multiplicador (1.0 si no hay buffs)
     */
    public double getGeneralXPMultiplier() {
        double multiplier = 1.0;

        for (TitleBuff buff : getActiveBuffs()) {
            if (buff.type() == TitleBuff.BuffType.GENERAL_XP_MULTIPLIER) {
                multiplier *= buff.getMultiplier();
            }
        }

        return multiplier;
    }

    /**
     * Calcula el multiplicador combinado de oro.
     * @return Multiplicador (1.0 si no hay buffs)
     */
    public double getGoldMultiplier() {
        double multiplier = 1.0;

        for (TitleBuff buff : getActiveBuffs()) {
            if (buff.affectsGold()) {
                multiplier *= buff.getMultiplier();
            }
        }

        return multiplier;
    }

    /**
     * Calcula el bonus total de recuperación de HP.
     * Suma todos los bonuses.
     * @return Bonus porcentual (0.0 si no hay buffs)
     */
    public double getHPRecoveryBonus() {
        return getActiveBuffs().stream()
                .filter(TitleBuff::affectsHPRecovery)
                .mapToDouble(TitleBuff::value)
                .sum();
    }

    /**
     * Aplica el bonus de HP recovery a una curación base.
     * @param baseHealing Curación base
     * @return Curación con bonus aplicado
     */
    public int applyHPRecoveryBonus(int baseHealing) {
        double bonus = getHPRecoveryBonus();
        return (int) Math.round(baseHealing * (1.0 + bonus));
    }

    /**
     * Verifica si el jugador tiene inmunidad a un debuff.
     */
    public boolean hasImmunityTo(String debuffName) {
        return getActiveBuffs().stream()
                .anyMatch(b -> b.givesDebuffImmunity(debuffName));
    }

    /**
     * Obtiene el multiplicador de READ XP (para Speed Reader).
     * @return Multiplicador (1.0 si no hay buff)
     */
    public double getReadXPMultiplier() {
        return getActiveBuffs().stream()
                .filter(b -> b.type() == TitleBuff.BuffType.READ_XP_MULTIPLIER)
                .mapToDouble(TitleBuff::value)
                .findFirst()
                .orElse(1.0);
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    /**
     * Obtiene todos los títulos desbloqueados.
     */
    public Set<Title> getUnlockedTitles() {
        return Collections.unmodifiableSet(unlockedTitles);
    }

    /**
     * Obtiene los títulos equipados.
     */
    public List<Title> getEquippedTitles() {
        return Collections.unmodifiableList(equippedTitles);
    }

    /**
     * Cuenta títulos desbloqueados.
     */
    public int getUnlockedCount() {
        return unlockedTitles.size();
    }

    /**
     * Cuenta títulos equipados.
     */
    public int getEquippedCount() {
        return equippedTitles.size();
    }

    /**
     * Obtiene títulos desbloqueados por categoría.
     */
    public List<Title> getTitlesByCategory(TitleCategory category) {
        return unlockedTitles.stream()
                .filter(t -> t.getCategory() == category)
                .sorted(Comparator.comparing(t -> t.unlockedAt()))
                .toList();
    }

    /**
     * Obtiene títulos que aún no se han desbloqueado.
     */
    public List<TitleType> getLockedTitles() {
        Set<TitleType> unlocked = unlockedTitles.stream()
                .map(Title::type)
                .collect(Collectors.toSet());

        return Arrays.stream(TitleType.values())
                .filter(t -> !unlocked.contains(t))
                .toList();
    }

    /**
     * Calcula el progreso de colección (% desbloqueado).
     */
    public double getCollectionProgress() {
        int total = TitleType.values().length;
        return (double) unlockedTitles.size() / total;
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    /**
     * Formatea los títulos equipados para UI.
     */
    public String getEquippedDisplay() {
        if (equippedTitles.isEmpty()) {
            return "Ningún título equipado";
        }

        return equippedTitles.stream()
                .map(t -> t.type().getDisplayName())
                .collect(Collectors.joining(", "));
    }

    /**
     * Formatea los buffs activos para UI.
     */
    public String getActiveBuffsDisplay() {
        List<TitleBuff> buffs = getActiveBuffs();
        if (buffs.isEmpty()) {
            return "Sin buffs activos";
        }

        return buffs.stream()
                .map(TitleBuff::toDisplayString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Resumen de la colección.
     */
    public String getCollectionSummary() {
        int total = TitleType.values().length;
        return String.format("Títulos: %d/%d (%.0f%%)",
                unlockedTitles.size(), total, getCollectionProgress() * 100);
    }
}
