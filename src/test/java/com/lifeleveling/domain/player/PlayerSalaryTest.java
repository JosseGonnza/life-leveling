package com.lifeleveling.domain.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Salario del JOB y oro fijo (Biblia cap 3.1)")
class PlayerSalaryTest {

    @Nested
    class JobSalary {
        @Test
        @DisplayName("8h a rango E (1.0×) = 250 G (calibración base)")
        void eightHoursAtRankE_pays250() {
            Player player = Player.create("Tester");
            int earned = player.registerJobSession(8);
            assertEquals(250, earned);
            assertEquals(250, player.getCurrentGold());
        }

        @Test
        @DisplayName("8h a rango C+ (2.0×) = 500 G")
        void eightHoursAtCPlus_pays500() {
            Player player = Player.create("Tester");
            player.promoteToRank(PlayerRank.C_PLUS);
            assertEquals(500, player.registerJobSession(8));
        }

        @Test
        @DisplayName("8h a rango S (8.0×) = 2000 G")
        void eightHoursAtS_pays2000() {
            Player player = Player.create("Tester");
            player.promoteToRank(PlayerRank.S);
            assertEquals(2_000, player.registerJobSession(8));
        }

        @Test
        @DisplayName("Redondeo estándar: 1h a rango E = 31 G (round(31.25))")
        void oneHour_roundsTo31() {
            Player player = Player.create("Tester");
            assertEquals(31, player.registerJobSession(1));
        }

        @Test
        @DisplayName("Floor Rule: la jornada se topa en 16h")
        void hoursAreCappedAt16() {
            Player player = Player.create("Tester");
            // 20h solicitadas -> 16h computadas: round(16 * 31.25) = 500
            assertEquals(500, player.registerJobSession(20));
        }

        @Test
        @DisplayName("0 o negativo no paga ni descuenta")
        void zeroOrNegative_isNoOp() {
            Player player = Player.create("Tester");
            assertEquals(0, player.registerJobSession(0));
            assertEquals(0, player.registerJobSession(-5));
            assertEquals(0, player.getCurrentGold());
        }

        @Test
        @DisplayName("El JOB desgasta HP (3/h base) y no da XP")
        void jobCostsHpAndGivesNoXp() {
            Player player = Player.create("Tester");
            int hpBefore = player.getCurrentHP();
            int levelBefore = player.getLevel();

            player.registerJobSession(8); // 8 * 3 = 24 HP de daño (sin mitigación)

            assertEquals(hpBefore - 24, player.getCurrentHP());
            assertEquals(levelBefore, player.getLevel(), "El JOB no da XP");
        }
    }

    @Nested
    class FixedGold {
        @Test
        @DisplayName("addGold NO aplica el multiplicador de rango (oro de misiones es fijo)")
        void addGold_ignoresRankMultiplier() {
            Player player = Player.create("Tester");
            player.promoteToRank(PlayerRank.S); // 8.0× salarial

            player.addGold(1_000);

            assertEquals(1_000, player.getCurrentGold(), "El rango no debe inflar el oro fijo");
        }
    }
}
