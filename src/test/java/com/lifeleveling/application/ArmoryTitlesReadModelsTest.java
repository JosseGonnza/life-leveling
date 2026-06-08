package com.lifeleveling.application;

import com.lifeleveling.application.dto.InventoryView;
import com.lifeleveling.application.dto.ShopItemView;
import com.lifeleveling.application.dto.TitlesView;
import com.lifeleveling.domain.title.TitleType;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M4 - Read models de The Armory (shop/inventory) y Hall of Fame (titles)")
class ArmoryTitlesReadModelsTest {

    private GameFacade newFacade() {
        GameFacade f = new GameFacade(new InMemoryPlayerRepository(), new SystemClock(), e -> {});
        f.newGame("Jose");
        return f;
    }

    @Test
    @DisplayName("shopCatalog: sin oro, los ítems con precio>0 no son asequibles")
    void shopCatalogAffordability() {
        GameFacade f = newFacade(); // 0 G de inicio

        var catalog = f.shopCatalog();
        assertFalse(catalog.isEmpty());
        assertTrue(catalog.stream().filter(i -> i.price() > 0).noneMatch(ShopItemView::affordable),
                "sin oro nada de pago debe ser asequible");
        assertTrue(catalog.stream().allMatch(i -> i.category() != null));
    }

    @Test
    @DisplayName("shopCatalog: a nivel 1 el equipo por tier está bloqueado y los consumibles libres")
    void shopCatalogTierLock() {
        GameFacade f = newFacade(); // nivel 1

        var catalog = f.shopCatalog();
        // Los consumibles (tier minLevel 0) siempre desbloqueados
        assertTrue(catalog.stream().filter(i -> i.requiredLevel() == 0).allMatch(ShopItemView::unlocked),
                "los consumibles deben estar desbloqueados a nivel 1");
        // El equipamiento de tier (minLevel 10/20/40) bloqueado a nivel 1
        assertTrue(catalog.stream().filter(i -> i.requiredLevel() >= 10).noneMatch(ShopItemView::unlocked),
                "el equipo por tier debe estar bloqueado a nivel 1");
        assertTrue(catalog.stream().anyMatch(i -> i.requiredLevel() == 10), "debe haber ítems de Tier 1 (nivel 10)");
    }

    @Test
    @DisplayName("inventory: comprar añade el ítem a lo poseído; el loadout arranca vacío")
    void inventoryReflectsPurchase() {
        GameFacade f = newFacade();
        f.workJob(8); // 250 G

        InventoryView before = f.inventory();
        assertTrue(before.loadout().stream().noneMatch(InventoryView.SlotView::filled), "loadout vacío al inicio");

        f.buy("consumable_espresso");

        assertTrue(f.inventory().owned().stream().anyMatch(i -> i.id().equals("consumable_espresso")),
                "el espresso comprado debe estar en el inventario");
    }

    @Test
    @DisplayName("titles: la colección lista todos los títulos; sin ninguno equipado al inicio")
    void titlesCollection() {
        GameFacade f = newFacade();

        TitlesView titles = f.titles();
        assertEquals(TitleType.values().length, titles.totalCount());
        assertEquals(TitleType.values().length, titles.collection().size());
        assertEquals(0, titles.usedSlots());
        assertTrue(titles.equipped().isEmpty());
    }
}
