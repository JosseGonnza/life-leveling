package com.lifeleveling.domain.career;

/**
 * Value Object que representa las recompensas calculadas de una sesión CODE.
 */
public record CareerReward(
        int intellectXP,
        int disciplineXP,
        int wisdomXP,
        int hpCost
) {

    // ... (Constructor y validaciones se mantienen igual) ...
    public CareerReward {
        if (intellectXP < 0) throw new IllegalArgumentException("XP de Intelecto no puede ser negativa");
        if (disciplineXP < 0) throw new IllegalArgumentException("XP de Disciplina no puede ser negativa");
        if (wisdomXP < 0) throw new IllegalArgumentException("XP de Sabiduría no puede ser negativa");
        if (hpCost < 0) throw new IllegalArgumentException("Coste HP no puede ser negativo");
    }

    public static CareerReward empty() {
        return new CareerReward(0, 0, 0, 0);
    }

    public static CareerReward calculate(double hours, boolean flowAchieved) {
        if (hours < 0) throw new IllegalArgumentException("Las horas no pueden ser negativas");
        int intXP = (int) Math.round(hours * CodeSessionConstants.XP_INT_PER_HOUR);
        int disXP = (int) Math.round(hours * CodeSessionConstants.XP_DIS_PER_HOUR);
        int wisXP = flowAchieved ? CodeSessionConstants.FLOW_BONUS_WIS : 0;
        int hpCost = (int) Math.round(hours * CodeSessionConstants.HP_COST_PER_HOUR);
        return new CareerReward(intXP, disXP, wisXP, hpCost);
    }

    // [NUEVO] Método para añadir bonus de Intelecto (Agente IA)
    public CareerReward plusIntellect(int extraXP) {
        return new CareerReward(this.intellectXP + extraXP, this.disciplineXP, this.wisdomXP, this.hpCost);
    }

    // ... (Resto de métodos getters y displays igual que antes) ...
    public int getTotalXP() { return intellectXP + disciplineXP + wisdomXP; }
    public boolean hasFlowBonus() { return wisdomXP > 0; }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("+%d INT | +%d DIS", intellectXP, disciplineXP));
        if (wisdomXP > 0) sb.append(String.format(" | +%d WIS 🔥", wisdomXP));
        sb.append(String.format(" | -%d HP", hpCost));
        return sb.toString();
    }

    public String toCompactString() {
        String flowIndicator = hasFlowBonus() ? " 🔥" : "";
        return String.format("+%d XP%s | -%d HP", getTotalXP(), flowIndicator, hpCost);
    }
}