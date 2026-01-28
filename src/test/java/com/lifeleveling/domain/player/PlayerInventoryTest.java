package com.lifeleveling.domain.player;

import com.lifeleveling.domain.item.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integración Player <-> Inventory")
class PlayerInventoryTest {

    @Test
    @DisplayName("getEffectiveStats() debe sumar Base + Equipo")
    void effectiveStats_shouldSumEquipment() {
        // 1. Crear Player (Nivel 1, todo a 1)
        Player player = Player.create("Tester");

        // Verificamos base
        assertEquals(1, player.getBaseStats().getLevel(StatType.INTELLECT));

        // 2. Crear Item "Gafas de Matrix" (+10 INT)
        Item glasses = Item.createEquip(
                "matrix_glasses", "Gafas Matrix", 500,
                ItemTier.TIER_2, ItemSlot.HEAD, ItemCategory.CLOTHING,
                Map.of(StatType.INTELLECT, 10)
        );

        // 3. Simular Compra y Equipamiento
        // (Hack: inyectamos oro para poder comprar)
        player.addGold(1000);
        player.buyItem(glasses);
        player.equipItem("matrix_glasses");

        // 4. Verificar Stats Efectivos
        Stats effective = player.getEffectiveStats();
        Stats base = player.getBaseStats();

        // El base debe seguir siendo 1 (la XP real no cambió)
        assertEquals(1, base.getLevel(StatType.INTELLECT));

        // El efectivo debe ser 11 (1 base + 10 gafas)
        assertEquals(11, effective.getLevel(StatType.INTELLECT));

        // Otro stat no afectado debe seguir igual
        assertEquals(1, effective.getLevel(StatType.STRENGTH));
    }

    @Test
    @DisplayName("buyItem() descuenta oro y añade al inventario")
    void buyItem_transactionLogic() {
        Player player = Player.create("Richie Rich");
        player.addGold(5000);

        Item expensiveWatch = Item.createEquip(
                "rolex", "Reloj Caro", 2000,
                ItemTier.TIER_3, ItemSlot.HAND, ItemCategory.LUXURY, Map.of()
        );

        player.buyItem(expensiveWatch);

        assertEquals(3000, player.getCurrentGold()); // 5000 - 2000
        assertTrue(player.getInventory().hasItem("rolex"));
    }
}