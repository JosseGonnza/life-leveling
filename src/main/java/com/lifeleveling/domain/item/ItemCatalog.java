package com.lifeleveling.domain.item;

import com.lifeleveling.domain.debuff.DebuffType;
import com.lifeleveling.domain.player.StatType;

import java.time.Duration; // [NUEVO] Necesario para los buffs
import java.util.*;
import java.util.stream.Collectors;

/**
 * ItemCatalog: La "Base de Datos" Maestra.
 * Contiene TODOS los objetos definidos en la Biblia del Juego.
 */
public class ItemCatalog {

    private static final Map<String, Item> REGISTRY = new HashMap<>();

    // ========================================================================================
    // TIER 1: SUPERVIVENCIA (Nvl 10+)
    // Objetivo: Mitigar daño y sumar primeros stats.
    // ========================================================================================

    public static final Item PJ_PANTS = register(new Item(
            "gear_pj_pants", "Pantalón Pijama", "Comodidad +100. Dignidad -50.", 500,
            ItemTier.TIER_1, ItemSlot.LEGS, ItemCategory.CLOTHING, Map.of(),
            1, 0, false, false, Optional.empty(), Optional.empty(), Optional.empty()
    )); // +1 HP Recup

    public static final Item BEAR_SLIPPERS = register(new Item(
            "gear_slippers", "Pantuflas de Oso", "Calentitas.", 800,
            ItemTier.TIER_1, ItemSlot.FEET, ItemCategory.CLOTHING, Map.of(),
            1, 0, false, false, Optional.empty(), Optional.empty(), Optional.empty()
    )); // +1 HP Recup

    public static final Item DEV_SHIRT = register(Item.createEquip(
            "gear_shirt_dev", "Camiseta 'Dev Mode'", 1500, ItemTier.TIER_1, ItemSlot.BODY, ItemCategory.CLOTHING,
            Map.of(StatType.INTELLECT, 2)
    ));

    public static final Item GYM_JOGGERS = register(Item.createEquip(
            "gear_joggers", "Joggers GymShark", 4500, ItemTier.TIER_1, ItemSlot.LEGS, ItemCategory.CLOTHING,
            Map.of(StatType.STRENGTH, 3)
    ));

    public static final Item LUCKY_CAP = register(Item.createEquip(
            "gear_cap", "Gorra de la Suerte", 1200, ItemTier.TIER_1, ItemSlot.HEAD, ItemCategory.CLOTHING,
            Map.of(StatType.DISCIPLINE, 1)
    ));

    public static final Item ERGO_MOUSE = register(Item.createEquip(
            "gear_mouse", "Ratón Ergonómico", 3000, ItemTier.TIER_1, ItemSlot.PERIPH, ItemCategory.TECH,
            Map.of() // Efecto especial: CODE cuesta -1 HP (Gestionado en Player)
    ));

    public static final Item PROTEIN_SHAKER = register(Item.createEquip(
            "gear_shaker", "Shaker de Proteínas", 1000, ItemTier.TIER_1, ItemSlot.KITCHEN, ItemCategory.FURNITURE,
            Map.of(StatType.STRENGTH, 2)
    ));

    public static final Item CORK_BOARD = register(Item.createEquip(
            "gear_corkboard", "Tablón de Corcho", 1500, ItemTier.TIER_1, ItemSlot.WALL, ItemCategory.DECORATION,
            Map.of(StatType.DISCIPLINE, 1)
    ));

    public static final Item WEIGHTED_BLANKET = register(new Item(
            "gear_blanket", "Manta Pesada", "Te abraza cuando nadie más lo hace.", 4000,
            ItemTier.TIER_1, ItemSlot.BED, ItemCategory.FURNITURE, Map.of(),
            3, 0, false, false, Optional.empty(), Optional.empty(), Optional.empty()
    )); // +3 HP Recup

    public static final Item NEON_LIGHT = register(Item.createEquip(
            "gear_neon", "Neon 'Cyberpunk'", 5000, ItemTier.TIER_1, ItemSlot.WALL, ItemCategory.DECORATION,
            Map.of(StatType.CHARISMA, 2)
    ));


    // ========================================================================================
    // TIER 2: PROFESIONAL (Nvl 20+)
    // Objetivo: Especialización.
    // ========================================================================================

    public static final Item SNEAKERS = register(Item.createEquip(
            "gear_sneakers", "Sneakers Urbanas", 6000, ItemTier.TIER_2, ItemSlot.FEET, ItemCategory.CLOTHING,
            Map.of(StatType.CHARISMA, 2)
    ));

    public static final Item TECH_HOODIE = register(Item.createEquip(
            "gear_hoodie", "Sudadera Técnica", 6000, ItemTier.TIER_2, ItemSlot.BODY, ItemCategory.CLOTHING,
            Map.of(StatType.STRENGTH, 5)
    ));

    public static final Item CHINOS = register(Item.createEquip(
            "gear_chinos", "Chinos Beige", 9000, ItemTier.TIER_2, ItemSlot.LEGS, ItemCategory.CLOTHING,
            Map.of(StatType.CHARISMA, 3)
    ));

    public static final Item CASIO_WATCH = register(Item.createEquip(
            "gear_watch_casio", "Reloj Casio", 9000, ItemTier.TIER_2, ItemSlot.HAND, ItemCategory.CLOTHING,
            Map.of(StatType.DISCIPLINE, 3)
    ));

    public static final Item JETBRAINS_LIC = register(Item.createEquip(
            "soft_jetbrains", "Licencia JetBrains", 6000, ItemTier.TIER_2, ItemSlot.SOFT, ItemCategory.TECH,
            Map.of(StatType.INTELLECT, 3)
    )); // Nota: Incompatible con Agente IA

    public static final Item SECOND_MONITOR = register(Item.createEquip(
            "gear_monitor", "Monitor Secundario", 10000, ItemTier.TIER_2, ItemSlot.DESK, ItemCategory.TECH,
            Map.of(StatType.INTELLECT, 5)
    ));

    public static final Item VISCO_PILLOW = register(new Item(
            "gear_pillow", "Almohada Visco", "Tu cuello deja de crujir.", 8000,
            ItemTier.TIER_2, ItemSlot.BED, ItemCategory.FURNITURE, Map.of(),
            5, 0, false, false, Optional.empty(), Optional.empty(), Optional.empty()
    )); // +5 HP Recup

    public static final Item WHITEBOARD = register(Item.createEquip(
            "gear_whiteboard", "Pizarra Blanca", 8000, ItemTier.TIER_2, ItemSlot.WALL, ItemCategory.DECORATION,
            Map.of(StatType.WISDOM, 5)
    ));

    public static final Item AIR_FRYER = register(new Item(
            "gear_airfryer", "Air Fryer", "Pollo crujiente sin aceite. Magia negra.", 10000,
            ItemTier.TIER_2, ItemSlot.KITCHEN, ItemCategory.FURNITURE, Map.of(),
            5, 0, false, false, Optional.empty(), Optional.empty(), Optional.empty()
    )); // Dieta da +5 HP extra


    // ========================================================================================
    // TIER 3: LEGENDARIO (Nvl 40+)
    // Objetivo: Game Changers.
    // ========================================================================================

    public static final Item BLAZER = register(Item.createEquip(
            "gear_blazer", "Blazer 'Casual Friday'", 12000, ItemTier.TIER_3, ItemSlot.BODY, ItemCategory.CLOTHING,
            Map.of(StatType.CHARISMA, 5)
    ));

    public static final Item PEGASUS_SHOES = register(Item.createEquip(
            "gear_shoes_run", "Running 'Pegasus'", 15000, ItemTier.TIER_3, ItemSlot.FEET, ItemCategory.CLOTHING,
            Map.of() // Efecto: GYM cuesta 0 HP
    ));

    public static final Item SUIT_CLOSER = register(Item.createEquip(
            "gear_suit", "Traje 'The Closer'", 50000, ItemTier.TIER_3, ItemSlot.BODY, ItemCategory.CLOTHING,
            Map.of(StatType.CHARISMA, 15)
    ));

    public static final Item KINDLE = register(Item.createEquip(
            "gear_kindle", "Kindle Paperwhite", 12000, ItemTier.TIER_3, ItemSlot.HAND, ItemCategory.TECH,
            Map.of(StatType.WISDOM, 10)
    ));

    public static final Item MECH_KEYBOARD = register(Item.createEquip(
            "gear_keyboard", "Teclado Mecánico", 15000, ItemTier.TIER_3, ItemSlot.PERIPH, ItemCategory.TECH,
            Map.of(StatType.INTELLECT, 5)
    ));

    public static final Item STREAM_DECK = register(Item.createEquip(
            "gear_streamdeck", "Stream Deck", 20000, ItemTier.TIER_3, ItemSlot.PERIPH, ItemCategory.TECH,
            Map.of(StatType.DISCIPLINE, 5)
    ));

    public static final Item NOISE_CANCEL_HEADPHONES = register(Item.createEquip(
            "gear_headphones", "Auriculares Noise-C", 30000, ItemTier.TIER_3, ItemSlot.HEAD, ItemCategory.TECH,
            Map.of(StatType.DISCIPLINE, 10)
    ));

    public static final Item SMARTWATCH = register(Item.createEquip(
            "gear_smartwatch", "Smartwatch", 35000, ItemTier.TIER_3, ItemSlot.HAND, ItemCategory.TECH,
            Map.of(StatType.DISCIPLINE, 5, StatType.STRENGTH, 5)
    ));

    public static final Item MARKUS_CHAIR = register(Item.createEquip(
            "gear_chair_markus", "Silla 'Markus'", 18000, ItemTier.TIER_3, ItemSlot.CHAIR, ItemCategory.FURNITURE,
            Map.of() // Effect: CODE cuesta -1 HP
    ));

    public static final Item AI_AGENT = register(Item.createEquip(
            "soft_ai_agent", "Agente IA", 25000, ItemTier.TIER_3, ItemSlot.SOFT, ItemCategory.TECH,
            Map.of() // Effect: CODE +20% XP. Incompatible con JetBrains.
    ));

    public static final Item STANDING_DESK = register(Item.createEquip(
            "gear_desk_standing", "Standing Desk", 60000, ItemTier.TIER_3, ItemSlot.DESK, ItemCategory.FURNITURE,
            Map.of(StatType.STRENGTH, 5, StatType.DISCIPLINE, 5) // VIT mapeado a STR
    ));

    public static final Item HERMAN_MILLER = register(Item.createEquip(
            "gear_chair_herman", "Herman Miller Aeron", 150000, ItemTier.TIER_3, ItemSlot.CHAIR, ItemCategory.FURNITURE,
            Map.of() // Effect: CODE cuesta 0 HP. El Trono del Rey.
    ));

    public static final Item ESPRESSO_MACHINE = register(Item.createEquip(
            "gear_espresso_machine", "Cafetera Espresso", 30000, ItemTier.TIER_3, ItemSlot.KITCHEN, ItemCategory.FURNITURE,
            Map.of() // Effect: Café al 75% descuento.
    ));

    public static final Item MATTRESS_PRO = register(new Item(
            "gear_mattress", "Colchón Premium", "Dormir aquí es viajar en el tiempo.", 100000,
            ItemTier.TIER_3, ItemSlot.BED, ItemCategory.FURNITURE, Map.of(),
            0, 0, false, false, Optional.empty(), Optional.empty(), Optional.empty()
    )); // Effect: +100% XP Dormir.

    // ========================================================================================
    // ALQUIMIA (CONSUMIBLES)
    // ========================================================================================

    // A. Energía y Salud
    public static final Item ESPRESSO_SHOT = register(Item.createConsumable(
            "consumable_espresso", "Espresso Shot", 80, ItemCategory.DRINK_SOCIAL, 5, 0
    ));

    // [ACTUALIZADO] Barrita ahora es un PowerUp (+5% STR 1h)
    public static final Item PROTEIN_BAR = register(Item.createPowerUp(
            "consumable_bar", "Barrita de Proteína", 150,
            TemporaryBuffSpec.of(StatType.STRENGTH, 0.05, Duration.ofHours(1))
    ));

    public static final Item LATTE_XL = register(Item.createConsumable(
            "consumable_latte", "Iced Latte XL", 250, ItemCategory.DRINK_SOCIAL, 15, 0
    ));

    public static final Item MONSTER = register(Item.createTemptation(
            "consumable_monster", "Energy Drink (Monster)", 300, 0, DebuffType.TACHYCARDIA
    )); // Elimina Fatiga, causa Taquicardia si abusas

    public static final Item POKE_BOWL = register(Item.createConsumable(
            "consumable_poke", "Poke Bowl Sano", 400, ItemCategory.FOOD_HEALTHY, 25, 0
    ));

    public static final Item BURGER = register(Item.createTemptation(
            "consumable_burger", "Hamburguesa Doble", 500, 40, DebuffType.HEAVINESS
    ));

    public static final Item PREMIUM_COFFEE = register(Item.createConsumable(
            "consumable_coffee_pro", "Café Premium", 1000, ItemCategory.DRINK_SOCIAL, 50, 0
    ));

    // B. Drogas de Rendimiento (Buffs Temporales) - [ACTUALIZADO FASE 2.2]
    public static final Item GUM_MINT = register(Item.createPowerUp(
            "buff_gum", "Chicle de Menta", 50,
            TemporaryBuffSpec.of(StatType.CHARISMA, 0.05, Duration.ofMinutes(30))
    ));

    public static final Item BINAURAL_BEATS = register(Item.createPowerUp(
            "buff_binaural", "Binaural Beats (App)", 100,
            TemporaryBuffSpec.of(StatType.WISDOM, 0.10, Duration.ofHours(1))
    ));

    public static final Item PRE_WORKOUT = register(Item.createPowerUp(
            "buff_preworkout", "Pre-Workout", 350,
            TemporaryBuffSpec.of(StatType.STRENGTH, 0.20, Duration.ofHours(1))
    ));

    public static final Item NOOTROPIC = register(Item.createPowerUp(
            "buff_nootropic", "Nootrópico 'Focus'", 400,
            TemporaryBuffSpec.of(StatType.INTELLECT, 0.20, Duration.ofHours(2))
    ));

    // C. Curas y Antídotos
    public static final Item ALMAX = register(Item.createMedicine(
            "medicine_almax", "Almax 600mg", 150, 0, DebuffType.HEAVINESS
    ));

    public static final Item SPA_SESSION = register(Item.createConsumable(
            "cure_spa", "Sesión de Spa", 2500, ItemCategory.MEDICINE, 100, 0
    )); // Restaura HP al 100%

    public static final Item ADRENALINE = register(Item.createConsumable(
            "cure_adrenaline", "Inyección Adrenalina", 5000, ItemCategory.MEDICINE, 25, 0
    )); // Cura BURNOUT (Especial)

    // D. Caprichos y Lujos
    public static final Item SUSHI = register(Item.createConsumable(
            "luxury_sushi", "Sushi Omakase", 3000, ItemCategory.LUXURY, 50, 0
    ));

    public static final Item STEAM_GAME = register(Item.createMedicine(
            "luxury_game", "Juego Nuevo (Steam)", 4000, 0, DebuffType.BOREDOM
    )); // Cura Aburrimiento

    public static final Item CINEMA = register(Item.createConsumable(
            "luxury_cinema", "Cine + Palomitas", 1500, ItemCategory.ENTERTAINMENT, 10, 0
    ));

    public static final Item WEEKEND_TRIP = register(Item.createConsumable(
            "luxury_trip", "Escapada Fin de Semana", 20000, ItemCategory.LUXURY, 100, 0
    )); // Cura Burnout + Buff masivo

    // ========================================================================================
    // TESOROS (Endgame Goals)
    // ========================================================================================

    public static final Item SETUP_DEV = register(Item.createTreasure(
            "treasure_setup", "Setup Dev Pro", 150000, "La herramienta definitiva."
    ));

    public static final Item EPIC_TRIP = register(Item.createTreasure(
            "treasure_trip", "Viaje Épico", 250000, "Experiencias sobre cosas."
    ));

    public static final Item CAR = register(Item.createTreasure(
            "treasure_car", "Coche Nuevo", 350000, "Libertad de movimiento."
    ));

    public static final Item FINANCIAL_FREEDOM = register(Item.createTreasure(
            "treasure_freedom", "Libertad Financiera", 500000, "Game Over. Has ganado al capitalismo."
    ));

    // ========================================================================================
    // ITEMS ÚNICOS & DESBLOQUEABLES
    // ========================================================================================

    public static final Item TITAN_RING = register(Item.createArtifact(
            "artifact_titan_ring", "Anillo del Titán",
            "Reduce coste HP de todo un 10%.", ItemSlot.HAND
    )); // Nivel 70

    public static final Item INFINITE_COFFEE = register(Item.createArtifact(
            "artifact_coffee_infinite", "Café Infinito",
            "Elimina Fatiga 1 vez al día sin gastar G.", ItemSlot.NONE
    )); // Unlock Elder_1

    public static final Item MASTER_KEY = register(Item.createArtifact(
            "artifact_master_key", "Llave Maestra",
            "Permite hacer 'Reroll' de 1 Weekly Quest.", ItemSlot.NONE
    )); // Unlock Elder_6

    // ========================================================================================
    // LÓGICA DE REGISTRO Y BÚSQUEDA
    // ========================================================================================

    private static Item register(Item item) {
        REGISTRY.put(item.id(), item);
        return item;
    }

    public static Optional<Item> findById(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static List<Item> getAllItems() {
        return new ArrayList<>(REGISTRY.values());
    }

    /**
     * Devuelve el inventario visible en la tienda.
     * Excluye items ocultos/artefactos que no se han desbloqueado.
     */
    public static List<Item> getShopInventory() {
        return REGISTRY.values().stream()
                .filter(item -> !item.isHidden())
                .sorted(Comparator.comparing(Item::price))
                .collect(Collectors.toList());
    }
}