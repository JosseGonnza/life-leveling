package com.lifeleveling.domain.player;

import com.lifeleveling.domain.buff.TemporaryBuff;
import com.lifeleveling.domain.item.TemporaryBuffSpec;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TemporaryBuffTracker {

    private final List<TemporaryBuff> activeBuffs;

    public TemporaryBuffTracker() {
        this.activeBuffs = new ArrayList<>();
    }

    // Constructor para persistencia
    public TemporaryBuffTracker(List<TemporaryBuff> activeBuffs) {
        this.activeBuffs = new ArrayList<>(activeBuffs);
    }

    public void addBuff(TemporaryBuffSpec spec, String sourceName, Instant now) {
        // Regla: Solo un buff activo por Stat. Si comes otro, reemplazas el anterior.
        removeBuffsForStat(spec.targetStat());

        TemporaryBuff buff = new TemporaryBuff(
                spec.targetStat(),
                spec.multiplier(),
                now.plus(spec.duration()),
                sourceName
        );
        activeBuffs.add(buff);
    }

    public void cleanExpiredBuffs(Instant now) {
        activeBuffs.removeIf(buff -> !buff.isActive(now));
    }

    public double getStatXPMultiplier(StatType stat, Instant now) {
        return activeBuffs.stream()
                .filter(buff -> buff.isActive(now) && buff.targetStat() == stat)
                .mapToDouble(buff -> 1.0 + buff.multiplier())
                .reduce(1.0, (a, b) -> a * b);
    }

    private void removeBuffsForStat(StatType stat) {
        activeBuffs.removeIf(b -> b.targetStat() == stat);
    }

    public List<TemporaryBuff> getActiveBuffs() {
        return List.copyOf(activeBuffs);
    }
}