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

        // Campos para el Sistema de Debuffs
        Optional<DebuffType> causesDebuff, // El item aplica este castigo al consumirse
        Optional<DebuffType> curesDebuff,  // El item cura este castigo al consumirse

        // [NUEVO] Campo para Buffs Temporales (Fase 2.1)
        Optional<TemporaryBuffSpec> temporaryBuff
) {
    public Item {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(tier);
        Objects.requireNonNull(slot);
        Objects.requireNonNull(category);
        Objects.requireNonNull(causesDebuff);
        Objects.requireNonNull(curesDebuff);
        Objects.requireNonNull(temporaryBuff); // [NUEVO] Prevenir nulls

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
                Optional.empty(), Optional.empty(),
                Optional.empty() // [NUEVO] Sin buff temporal
        );
    }

    // Consumibles Básicos (Sin efectos secundarios)
    public static Item createConsumable(String id, String name, int price, ItemCategory cat, int hpRec, int hpDmg) {
        return new Item(
                id, name, "", price, ItemTier.CONSUMABLE, ItemSlot.NONE, cat, Map.of(),
                hpRec, hpDmg, true, false,
                Optional.empty(), Optional.empty(),
                Optional.empty() // [NUEVO]
        );
    }

    // Consumible Medicinal (Cura un Debuff)
    public static Item createMedicine(String id, String name, int price, int hpRec, DebuffType cures) {
        return new Item(
                id, name, "Medicamento", price, ItemTier.CONSUMABLE, ItemSlot.NONE, ItemCategory.MEDICINE, Map.of(),
                hpRec, 0, true, false,
                Optional.empty(), Optional.of(cures),
                Optional.empty() // [NUEVO]
        );
    }

    // Consumible Tóxico/Tentación (Causa un Debuff)
    public static Item createTemptation(String id, String name, int price, int hpRec, DebuffType causes) {
        return new Item(
                id, name, "Delicioso pero peligroso", price, ItemTier.CONSUMABLE, ItemSlot.NONE, ItemCategory.FOOD_JUNK, Map.of(),
                hpRec, 0, true, false,
                Optional.of(causes), Optional.empty(),
                Optional.empty() // [NUEVO]
        );
    }

    // [NUEVO] Consumible Potenciador (Otorga Buff Temporal)
    // Ej: "Nootrópico" -> +20% INT por 2 horas
    public static Item createPowerUp(String id, String name, int price, TemporaryBuffSpec buffSpec) {
        return new Item(
                id, name, "Potenciador temporal", price, ItemTier.CONSUMABLE, ItemSlot.NONE, ItemCategory.SUPPLEMENT, Map.of(),
                0, 0, true, false,
                Optional.empty(), Optional.empty(),
                Optional.of(buffSpec) // [NUEVO] Con buff
        );
    }

    // Tesoros
    public static Item createTreasure(String id, String name, int price, String description) {
        return new Item(
                id, name, description, price, ItemTier.TIER_3, ItemSlot.NONE, ItemCategory.TREASURE, Map.of(),
                0, 0, false, false,
                Optional.empty(), Optional.empty(),
                Optional.empty() // [NUEVO]
        );
    }

    // Artefactos Ocultos
    public static Item createArtifact(String id, String name, String description, ItemSlot slot) {
        return new Item(
                id, name, description, 0, ItemTier.TIER_3, slot, ItemCategory.ARTIFACT, Map.of(),
                0, 0, false, true,
                Optional.empty(), Optional.empty(),
                Optional.empty() // [NUEVO]
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

    // [NUEVO] Helper
    public boolean hasTemporaryBuff() {
        return temporaryBuff.isPresent();
    }

    /**
     * Identifica si el item es una fuente de cafeína potente.
     */
    public boolean isCaffeineSource() {
        return category == ItemCategory.DRINK_ENERGY ||
                (category == ItemCategory.DRINK_SOCIAL && id.toLowerCase().contains("cafe"));
    }

    public String toDisplayString() {
        if (isHidden) return "🔒 ??? (Item Oculto)";
        String base = String.format("%s %s | %d G", slot.getIcon(), name, price);

        if (causesDebuff.isPresent()) {
            base += " ⚠️ " + causesDebuff.get().getDisplayName();
        }
        // [NUEVO] Indicador visual si tiene buff (opcional)
        if (temporaryBuff.isPresent()) {
            base += " ⚡ Buff";
        }
        return base;
    }
}