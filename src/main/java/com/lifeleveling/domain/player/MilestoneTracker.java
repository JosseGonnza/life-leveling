package com.lifeleveling.domain.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MilestoneTracker: Gestiona el progreso de hitos de nivel del jugador.
 */
public class MilestoneTracker {

    private final Set<MilestoneType> achievedMilestones;

    public MilestoneTracker() {
        this.achievedMilestones = new HashSet<>();
    }

    // Constructor para reconstitución (persistencia)
    public MilestoneTracker(Set<MilestoneType> achievedMilestones) {
        this.achievedMilestones = new HashSet<>(achievedMilestones);
    }

    /**
     * Verifica el nivel actual del jugador y otorga recompensas por hitos nuevos.
     * @param player El jugador a evaluar y recompensar.
     * @return Lista de hitos desbloqueados en esta comprobación.
     */
    public List<MilestoneType> checkAndAward(Player player) {
        int currentLevel = player.getLevel();
        List<MilestoneType> newMilestones = new ArrayList<>();

        for (MilestoneType milestone : MilestoneType.values()) {
            // Si tiene el nivel Y NO lo ha conseguido aún
            if (currentLevel >= milestone.getLevelRequired() && !hasAchieved(milestone)) {
                applyReward(player, milestone);
                achievedMilestones.add(milestone);
                newMilestones.add(milestone);
                System.out.println("🏆 MILESTONE: " + milestone.name() + " alcanzado!");
            }
        }
        return newMilestones;
    }

    private void applyReward(Player player, MilestoneType milestone) {
        // 1. Oro
        if (milestone.getGoldReward() > 0) {
            player.addGold(milestone.getGoldReward());
        }

        // 2. Título (Si no lo tiene ya)
        if (milestone.getTitleReward() != null) {
            boolean unlocked = player.unlockTitle(milestone.getTitleReward());
            if (unlocked) {
                System.out.println("   🔓 Título desbloqueado: " + milestone.getTitleReward());
            }
        }

        // 3. Item Tier (Lógica simulada)
        if (milestone.getItemReward() != null) {
            // Usamos getName() de tu ItemTier modificado
            System.out.println("   🎁 Tier de Items desbloqueado: " + milestone.getItemReward().getName());
        }
    }

    public boolean hasAchieved(MilestoneType type) {
        return achievedMilestones.contains(type);
    }

    public Set<MilestoneType> getAchievedMilestones() {
        return Set.copyOf(achievedMilestones);
    }
}