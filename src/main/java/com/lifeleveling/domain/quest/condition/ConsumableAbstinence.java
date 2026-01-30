package com.lifeleveling.domain.quest.condition;

import com.lifeleveling.domain.item.ItemCategory;

/**
 * ConsumableAbstinence: No comprar consumible durante periodo.
 * Soporta bloqueo por ID específico (String) o por Categoría entera (ItemCategory).
 */
public final class ConsumableAbstinence implements GateCondition {

    private final String consumableId;
    private final ItemCategory category; // [NUEVO] Campo para categoría
    private final int days;

    /**
     * Constructor Legacy: Bloquea un ID específico.
     * Ej: "consumable_monster"
     */
    public ConsumableAbstinence(String consumableId, int days) {
        this.consumableId = consumableId;
        this.category = null;
        this.days = days;
    }

    /**
     * Constructor Nuevo: Bloquea una categoría completa.
     * Ej: ItemCategory.LUXURY
     */
    public ConsumableAbstinence(ItemCategory category, int days) {
        this.consumableId = null;
        this.category = category;
        this.days = days;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        // Si tenemos categoría, usamos el método puente del tracker
        if (category != null) {
            return !context.tracker().hasCategoryPurchaseInLastDays(category, days);
        }
        // Si no, usamos la lógica clásica por ID
        return !context.tracker().hasConsumablePurchaseInLastDays(consumableId, days);
    }

    @Override
    public double getProgress(ConditionContext context) {
        return isMet(context) ? 1.0 : 0.0;
    }

    @Override
    public String getProgressText(ConditionContext context) {
        boolean met = isMet(context);
        String target = category != null ? "Cat. " + category.name() : consumableId;
        return String.format("Sin %s (%d días): %s", target, days, met ? "✓" : "✗");
    }

    @Override
    public String getDescription() {
        String target = category != null ? "items de " + category.name() : consumableId;
        return String.format("No comprar %s durante %d días", target, days);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.CONSUMABLE_ABSTINENCE;
    }
}