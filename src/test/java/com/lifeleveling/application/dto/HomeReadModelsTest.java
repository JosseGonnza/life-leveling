package com.lifeleveling.application.dto;

import com.lifeleveling.application.GameFacade;
import com.lifeleveling.domain.player.Player;
import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.infrastructure.persistence.InMemoryPlayerRepository;
import com.lifeleveling.infrastructure.time.SystemClock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("M4 - Read models de la Home (PlayerView XP/stats + DailyChecklistView)")
class HomeReadModelsTest {

    @Test
    @DisplayName("PlayerView calcula la barra de XP del nivel actual")
    void playerViewXpBar() {
        Player p = Player.create("Jose");
        p.addGeneralXP(5_500); // a 100 HP el multiplicador es 1.0 → XP intacta

        PlayerView v = PlayerView.from(p);

        assertEquals(10, v.level(), "floor(sqrt(5500/50)) = 10");
        assertEquals(5_500L, v.totalXP());
        assertEquals(500, v.xpIntoLevel(), "5500 - 50*10^2");
        assertEquals(1_050, v.xpForLevelSpan(), "50*11^2 - 50*10^2");
    }

    @Test
    @DisplayName("StatsView mapea cada stat a su accessor correcto (sin cruces)")
    void statsViewWiring() {
        Player p = Player.create("Jose");
        var stats = p.getEffectiveStats();

        StatsView v = StatsView.from(stats);

        assertEquals(stats.getLevel(StatType.STRENGTH), v.strength());
        assertEquals(stats.getLevel(StatType.INTELLECT), v.intellect());
        assertEquals(stats.getLevel(StatType.WISDOM), v.wisdom());
        assertEquals(stats.getLevel(StatType.DISCIPLINE), v.discipline());
        assertEquals(stats.getLevel(StatType.CHARISMA), v.charisma());
    }

    @Test
    @DisplayName("DailyChecklistView refleja los 7 hábitos y marca el completado")
    void dailyChecklistReflectsCompletion() {
        GameFacade facade = new GameFacade(new InMemoryPlayerRepository(), new SystemClock(), e -> {});
        facade.newGame("Jose");

        DailyChecklistView before = facade.dailyChecklist();
        assertEquals(7, before.total());
        assertEquals(0, before.completedCount());
        assertTrue(before.habits().stream().noneMatch(DailyChecklistView.Habit::done));

        facade.gym(true);

        DailyChecklistView after = facade.dailyChecklist();
        assertEquals(1, after.completedCount());
        assertTrue(after.habits().stream()
                .anyMatch(h -> h.code().equals("GYM") && h.done()));
    }
}
