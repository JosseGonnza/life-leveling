package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TitleInventory: Gestiona los títulos desbloqueados y equipados del jugador.
 * * Ahora es clave para calcular buffs dinámicos en Quests (como SLEEP).
 */
public class TitleInventory {

    public static final int SECOND_SLOT_UNLOCK_LEVEL = 50;
    public static final int MAX_TITLE_SLOTS = 2;

    private final Set<Title> unlockedTitles;
    private final List<Title> equippedTitles;

    public TitleInventory() {
        this.unlockedTitles = new HashSet<>();
        this.equippedTitles = new ArrayList<>(MAX_TITLE_SLOTS);
    }

    public TitleInventory(Set<Title> unlockedTitles, List<Title> equippedTitles) {
        this.unlockedTitles = new HashSet<>(unlockedTitles);
        this.equippedTitles = new ArrayList<>(equippedTitles);
    }

    // ========================================================================================
    // UNLOCK & EQUIP
    // ========================================================================================

    public boolean unlock(TitleType type) {
        if (hasTitle(type)) return false;
        unlockedTitles.add(Title.unlock(type));
        return true;
    }

    public boolean equip(TitleType type, int playerLevel) {
        Optional<Title> title = getTitle(type);
        if (title.isEmpty()) throw new IllegalStateException("No tienes el título: " + type.getDisplayName());
        if (isEquipped(type)) return false;

        int maxSlots = playerLevel >= SECOND_SLOT_UNLOCK_LEVEL ? 2 : 1;
        if (equippedTitles.size() >= maxSlots) {
            throw new IllegalStateException("Slots llenos. Desequipa uno primero.");
        }

        equippedTitles.add(title.get());
        return true;
    }

    public boolean unequip(TitleType type) {
        return equippedTitles.removeIf(t -> t.type() == type);
    }

    public void swap(TitleType oldType, TitleType newType, int playerLevel) {
        if (!isEquipped(oldType)) throw new IllegalStateException("Título no equipado: " + oldType);
        unequip(oldType);
        equip(newType, playerLevel);
    }

    // ========================================================================================
    // BUFF CALCULATIONS (Matemáticas)
    // ========================================================================================

    public List<TitleBuff> getActiveBuffs() {
        return equippedTitles.stream()
                .flatMap(t -> t.getBuffs().stream())
                .toList();
    }

    /**
     * Calcula el bonus porcentual total de recuperación de HP.
     * Ej: "Guardián del Sueño" (+10%) + "Superviviente" (+3%) = 0.13
     */
    public double getHPRecoveryBonus() {
        return getActiveBuffs().stream()
                .filter(TitleBuff::affectsHPRecovery)
                .mapToDouble(TitleBuff::value)
                .sum();
    }

    /**
     * Aplica el bonus a una cantidad base.
     * Útil para DailyQuestType.SLEEP.
     */
    public int applyHPRecoveryBonus(int baseHealing) {
        if (baseHealing <= 0) return 0; // No bonificamos el 0
        double bonus = getHPRecoveryBonus();
        return (int) Math.round(baseHealing * (1.0 + bonus));
    }

    public double getStatXPMultiplier(StatType stat) {
        double multiplier = 1.0;
        for (TitleBuff buff : getActiveBuffs()) {
            if (buff.affectsStat(stat)) multiplier *= buff.getMultiplier();
        }
        return multiplier;
    }

    public double getGeneralXPMultiplier() {
        double multiplier = 1.0;
        for (TitleBuff buff : getActiveBuffs()) {
            if (buff.type() == TitleBuff.BuffType.GENERAL_XP_MULTIPLIER) multiplier *= buff.getMultiplier();
        }
        return multiplier;
    }

    public double getGoldMultiplier() {
        double multiplier = 1.0;
        for (TitleBuff buff : getActiveBuffs()) {
            if (buff.affectsGold()) multiplier *= buff.getMultiplier();
        }
        return multiplier;
    }

    public boolean hasImmunityTo(String debuffName) {
        return getActiveBuffs().stream().anyMatch(b -> b.givesDebuffImmunity(debuffName));
    }

    // ========================================================================================
    // QUERIES & HELPERS
    // ========================================================================================

    public boolean hasTitle(TitleType type) { return unlockedTitles.stream().anyMatch(t -> t.type() == type); }
    public boolean isEquipped(TitleType type) { return equippedTitles.stream().anyMatch(t -> t.type() == type); }
    public Optional<Title> getTitle(TitleType type) { return unlockedTitles.stream().filter(t -> t.type() == type).findFirst(); }
    public List<Title> getEquippedTitles() { return Collections.unmodifiableList(equippedTitles); }
}