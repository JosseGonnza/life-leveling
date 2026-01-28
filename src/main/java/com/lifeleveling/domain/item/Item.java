package com.lifeleveling.domain.item;

import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.player.StatType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Item(
        String id,
        String name,
        String description,
        int price,
        ItemTier tier,
        ItemSlot slot,
        ItemCategory category,
        Map<StatType, Integer> statBonuses,
        int hpRecovery,
        int hpDamage,
        boolean isConsumable,
        boolean isHidden, // Item secreto/desbloqueable

        // Nuevos campos para el Sistema de Debuffs
        Optional<DebuffType> causesDebuff, // El item aplica este castigo al consumirse
        Optional<DebuffType> curesDebuff   // El item cura este castigo al consumirse
) {
    public Item {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(tier);
        Objects.requireNonNull(slot);
        Objects.requireNonNull(category);
        Objects.requireNonNull(causesDebuff); // Prevenir nulls en los Optional
        Objects.requireNonNull(curesDebuff);

        if (price < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        if (statBonuses == null) statBonuses = Collections.emptyMap();
    }

    // ========================================================================================
    // FACTORÍAS
    // ========================================================================================

    // Equipamiento normal (Visible)
    public static Item createEquip(String id, String name, int price, ItemTier tier, ItemSlot slot, ItemCategory cat, Map<StatType, Integer> bonuses) {
        return new Item(
                id, name, "", price, tier, slot, cat, bonuses,
                0, 0, false, false,
                Optional.empty(), Optional.empty() // No causan ni curan debuffs
        );
    }

    // Consumibles Básicos (Sin efectos secundarios)
    public static Item createConsumable(String id, String name, int price, ItemCategory cat, int hpRec, int hpDmg) {
        return new Item(
                id, name, "", price, ItemTier.CONSUMABLE, ItemSlot.NONE, cat, Map.of(),
                hpRec, hpDmg, true, false,
                Optional.empty(), Optional.empty()
        );
    }

    // Consumible Medicinal (Cura un Debuff)
    // Ej: Almax -> Cura HEAVINESS
    public static Item createMedicine(String id, String name, int price, int hpRec, DebuffType cures) {
        return new Item(
                id, name, "Medicamento", price, ItemTier.CONSUMABLE, ItemSlot.NONE, ItemCategory.MEDICINE, Map.of(),
                hpRec, 0, true, false,
                Optional.empty(), Optional.of(cures)
        );
    }

    // Consumible Tóxico/Tentación (Causa un Debuff)
    // Ej: Hamburguesa -> Causa HEAVINESS
    public static Item createTemptation(String id, String name, int price, int hpRec, DebuffType causes) {
        return new Item(
                id, name, "Delicioso pero peligroso", price, ItemTier.CONSUMABLE, ItemSlot.NONE, ItemCategory.FOOD_JUNK, Map.of(),
                hpRec, 0, true, false,
                Optional.of(causes), Optional.empty()
        );
    }

    // Tesoros
    public static Item createTreasure(String id, String name, int price, String description) {
        return new Item(
                id, name, description, price, ItemTier.TIER_3, ItemSlot.NONE, ItemCategory.TREASURE, Map.of(),
                0, 0, false, false,
                Optional.empty(), Optional.empty()
        );
    }

    // Artefactos Ocultos
    public static Item createArtifact(String id, String name, String description, ItemSlot slot) {
        return new Item(
                id, name, description, 0, ItemTier.TIER_3, slot, ItemCategory.ARTIFACT, Map.of(),
                0, 0, false, true,
                Optional.empty(), Optional.empty()
        );
    }

    // ========================================================================================
    // HELPERS (Lógica de Dominio)
    // ========================================================================================

    public boolean hasDebuffEffect() {
        return causesDebuff.isPresent();
    }

    public boolean hasCureEffect() {
        return curesDebuff.isPresent();
    }

    /**
     * Identifica si el item es una fuente de cafeína potente.
     * Importante para la lógica de Taquicardia (el café no cura si ya tienes taquicardia).
     */
    public boolean isCaffeineSource() {
        // Lógica "Hardcoded" segura: detectamos por ID o Categoría + Nombre
        return category == ItemCategory.DRINK_ENERGY ||
                (category == ItemCategory.DRINK_SOCIAL && id.toLowerCase().contains("cafe"));
    }

    public String toDisplayString() {
        if (isHidden) return "🔒 ??? (Item Oculto)";
        String base = String.format("%s %s | %d G", slot.getIcon(), name, price);

        if (causesDebuff.isPresent()) {
            base += " ⚠️ " + causesDebuff.get().getDisplayName();
        }
        return base;
    }
}