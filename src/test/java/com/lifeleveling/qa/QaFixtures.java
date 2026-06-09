package com.lifeleveling.qa;

import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.PlayerRank;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.condition.GateTracker;
import com.lifeleveling.domain.quest.shared.QuestRank;
import com.lifeleveling.domain.quest.system.SystemQuestType;
import com.lifeleveling.domain.quest.user.UserQuest;
import com.lifeleveling.infrastructure.persistence.JsonPlayerRepository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generador de partidas de prueba (fixtures) para QA. Compone Players coherentes por código
 * (no simula día a día) y los vuelca a ./fixtures/&lt;nombre&gt;.json.
 *
 * Regenerar todos:  mvn -q test-compile exec:java
 * Ver un escenario: LL_SAVE=fixtures/burnout.json mvn javafx:run
 *   (combinable con LL_SCREEN=nombre / LL_HP=n / LL_SCREENSHOT=ruta)
 */
public final class QaFixtures {

    private static final Path DIR = Path.of("fixtures");

    public static void main(String[] args) {
        System.out.println("Generando fixtures de QA en ./fixtures …");
        write("nuevo", nuevo());
        write("cansado", cansado());
        write("burnout", burnout());
        write("nv10-gate1", nv10Gate1());
        write("nv40-vault", nv40Vault());
        write("rangoA-tesoros", rangoATesoros());
        write("nv75-elder", nv75Elder());
        write("redemption", redemption());
        System.out.println("\n✅ Listo. Uso: LL_SAVE=fixtures/<x>.json mvn javafx:run");
    }

    // ---- Escenarios ----

    private static Player nuevo() {
        return Player.create("Cazador Novato");
    }

    private static Player cansado() {
        Player p = midGame("Cazador Agotado");
        p.takeDamage(p.getCurrentHP() - 30); // HP 30 → TIRED (bloquea misiones B+ y portales)
        return p;
    }

    private static Player burnout() {
        Player p = midGame("Cazador Quemado");
        p.takeDamage(p.getCurrentHP()); // HP 0 → BURNOUT (lockdown gris total)
        return p;
    }

    private static Player nv10Gate1() {
        Player p = Player.create("Cazador Nv10");
        levelUpTo(p, 10); // aparece Gate 1 (E→D) + Tienda Tier 1
        addSampleQuest(p);
        return p;
    }

    private static Player nv40Vault() {
        Player p = Player.create("Cazador Nv40");
        levelUpTo(p, 40);
        completeGatesUpTo(p, PlayerRank.C_PLUS);
        p.addGold(25_000); // The Vault aparece a Nv40 y exige mantener 20k
        addSampleQuest(p);
        return p;
    }

    private static Player rangoATesoros() {
        Player p = Player.create("Cazador Senior");
        levelUpTo(p, 55);
        completeGatesUpTo(p, PlayerRank.A); // Rango A → desbloquea pestaña Tesoros
        p.addGold(120_000);
        return p;
    }

    private static Player nv75Elder() {
        Player p = Player.create("Monarca en Ciernes");
        levelUpTo(p, 75);
        completeGatesUpTo(p, PlayerRank.S); // Nv75 + Rango S → pestaña Juicios/Elder
        p.addGold(300_000);
        return p;
    }

    private static Player redemption() {
        Player p = midGame("Cazador Caido");
        GateTracker t = p.getGateTracker();
        LocalDate today = LocalDate.now();
        for (int i = 20; i <= 22; i++) { // 3 burnouts en el último mes → Redemption disponible
            t.addDailyHistory(burnoutDay(today.minusDays(i)));
        }
        return p;
    }

    // ---- Helpers ----

    /** Perfil de media partida: ~Nv30, Rango C, con algo de oro y una quest activa. */
    private static Player midGame(String name) {
        Player p = Player.create(name);
        levelUpTo(p, 30);
        completeGatesUpTo(p, PlayerRank.C);
        p.addGold(3_000);
        addSampleQuest(p);
        return p;
    }

    /** Inyecta XP (en HEALTHY, x1) hasta el nivel objetivo. Reparte algo en stats para que el radar se vea. */
    private static void levelUpTo(Player p, int targetLevel) {
        long needed = 50L * targetLevel * targetLevel;
        int perStat = (int) (needed * 0.08); // ~40% repartido entre los 5 stats
        for (StatType s : StatType.values()) p.addXP(s, perStat);
        while (p.getLevel() < targetLevel) p.addGeneralXP(1_000); // ajuste fino
    }

    /** Marca como superada la cadena de gates hasta el rango dado y asciende al jugador. */
    private static void completeGatesUpTo(Player p, PlayerRank rank) {
        SystemQuestType[] chain = {
                SystemQuestType.GATE_E_TO_D, SystemQuestType.GATE_D_TO_C,
                SystemQuestType.GATE_C_TO_C_PLUS, SystemQuestType.GATE_C_PLUS_TO_B,
                SystemQuestType.GATE_B_TO_A, SystemQuestType.GATE_A_TO_S
        };
        for (SystemQuestType g : chain) {
            p.getGateTracker().markGateAsCompleted(g);
            p.promoteToRank(g.getRankUnlocked());
            if (g.getRankUnlocked() == rank) break;
        }
    }

    private static void addSampleQuest(Player p) {
        p.addUserQuest(UserQuest.create("Entregar proyecto", "Sprint del cliente",
                QuestRank.C, LocalDate.now().plusDays(5)));
    }

    private static GateTracker.DailyHistory burnoutDay(LocalDate date) {
        return new GateTracker.DailyHistory(date, false, 0, 0, 0.0, true, false,
                List.of(), Set.of(), Map.of());
    }

    private static void write(String name, Player player) {
        player.setLastActiveDate(LocalDate.now());
        new JsonPlayerRepository(DIR.resolve(name + ".json")).save(player);
        System.out.printf("  • %-16s Nv%-3d Rango %-6s HP %d%n",
                name, player.getLevel(), player.getCurrentRank().name(), player.getCurrentHP());
    }

    private QaFixtures() {}
}
