package com.lifeleveling.application;

import com.lifeleveling.application.dto.InventoryView;
import com.lifeleveling.application.dto.ElderQuestView;
import com.lifeleveling.application.dto.ShopItemView;
import com.lifeleveling.application.dto.TitlesView;
import com.lifeleveling.application.dto.QuestView;
import com.lifeleveling.application.dto.TreasureView;
import com.lifeleveling.domain.quest.shared.QuestRank;
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
    @DisplayName("treasures: 4 tesoros ordenados por precio, bloqueados a rango E y fuera de la tienda")
    void treasuresReadModel() {
        GameFacade f = newFacade(); // rango E

        assertFalse(f.treasuresUnlocked(), "a rango E los tesoros están bloqueados");

        var tr = f.treasures();
        assertEquals(4, tr.size());
        assertTrue(tr.stream().noneMatch(TreasureView::owned), "nada conseguido al inicio");
        assertEquals("treasure_setup", tr.get(0).id(), "ordenados por precio ascendente (150k primero)");
        assertEquals("treasure_freedom", tr.get(3).id(), "Libertad Financiera (500k) la última");

        assertTrue(f.shopCatalog().stream().noneMatch(i -> i.id().startsWith("treasure_")),
                "los tesoros no deben aparecer en la tienda normal");
    }

    @Test
    @DisplayName("juicios: 7 elder quests, bloqueados a nivel 1, con progreso evaluable y recompensa")
    void elderQuestsReadModel() {
        GameFacade f = newFacade(); // nivel 1

        assertFalse(f.elderUnlocked(), "a nivel 1 los Juicios están bloqueados (requiere Nv 75)");

        var elders = f.elderQuests();
        assertEquals(7, elders.size());
        assertTrue(elders.stream().allMatch(e -> !e.objectives().isEmpty()), "cada Juicio tiene objetivos");
        assertTrue(elders.stream().allMatch(e -> e.progress() >= 0.0 && e.progress() <= 1.0), "progreso en [0,1]");
        assertTrue(elders.stream().allMatch(e -> !e.reward().isBlank()), "cada Juicio muestra recompensa");
        // Nota: algunas condiciones de abstinencia/racha dan `completed` vacuosamente sin el ciclo de
        // temporada (Judgment Day) — comportamiento del endgame aún no cableado, no se asierta aquí.
    }

    @Test
    @DisplayName("estado HP: jugador sano sin bloqueos; canAttemptRank permite todos los rangos")
    void healthyStateNoLocks() {
        GameFacade f = newFacade();

        var s = f.state();
        assertFalse(s.burnout(), "jugador nuevo no está en burnout");
        assertFalse(s.highRankLocked(), "jugador sano no tiene bloqueo de alto rango");
        for (QuestRank r : QuestRank.values()) {
            assertTrue(f.canAttemptRank(r), "estado sano permite el rango " + r.getLetter());
        }
        assertTrue(f.activeQuests().stream().allMatch(QuestView::playableNow));
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
