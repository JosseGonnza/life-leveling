package com.lifeleveling.domain.item;

import com.lifeleveling.domain.player.StatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Efectos de consumibles fieles a la Biblia (cap 3.3)")
class ConsumableEffectsTest {

    @Test
    @DisplayName("Barrita de Proteína: +2 HP y buff +5% STR")
    void proteinBar_healsAndBuffsStrength() {
        Item bar = ItemCatalog.PROTEIN_BAR;
        assertEquals(2, bar.hpRecovery());
        assertTrue(bar.temporaryBuff().isPresent());
        assertEquals(StatType.STRENGTH, bar.temporaryBuff().get().targetStat());
        assertEquals(0.05, bar.temporaryBuff().get().multiplier(), 0.0001);
    }

    @Test
    @DisplayName("Cine + Palomitas: +10 HP y buff +10% CHA")
    void cinema_healsAndBuffsCharisma() {
        Item cinema = ItemCatalog.CINEMA;
        assertEquals(10, cinema.hpRecovery());
        assertTrue(cinema.temporaryBuff().isPresent());
        assertEquals(StatType.CHARISMA, cinema.temporaryBuff().get().targetStat());
        assertEquals(0.10, cinema.temporaryBuff().get().multiplier(), 0.0001);
    }
}
