package com.lifeleveling.domain.career;

/**
 * Value Object que representa las recompensas calculadas de una sesión CODE.
 *
 * Según la Biblia (Cap 1.2 §4 - Career Engine):
 *   - XP Primaria (INT): Horas × 60
 *   - XP Secundaria (DIS): Horas × 20
 *   - Bonus Flow (WIS): +50 flat si sesión >= 3.0h
 *   - HP Cost: Horas × 3 (mitigable con equipo)
 *
 * Inmutable por diseño DDD.
 */
public record CareerReward(
        int intellectXP,
        int disciplineXP,
        int wisdomXP,
        int hpCost
) {

    public CareerReward {
        if (intellectXP < 0) {
            throw new IllegalArgumentException("XP de Intelecto no puede ser negativa: " + intellectXP);
        }
        if (disciplineXP < 0) {
            throw new IllegalArgumentException("XP de Disciplina no puede ser negativa: " + disciplineXP);
        }
        if (wisdomXP < 0) {
            throw new IllegalArgumentException("XP de Sabiduría no puede ser negativa: " + wisdomXP);
        }
        if (hpCost < 0) {
            throw new IllegalArgumentException("Coste HP no puede ser negativo: " + hpCost);
        }
    }

    /**
     * Crea una recompensa vacía (útil para tests o estados iniciales).
     */
    public static CareerReward empty() {
        return new CareerReward(0, 0, 0, 0);
    }

    /**
     * Calcula la recompensa para una sesión de código.
     *
     * @param hours Horas trabajadas en esta sesión
     * @param flowAchieved Si la sesión alcanzó el umbral de Flow (>= 3h)
     * @return CareerReward con XP y HP calculados
     */
    public static CareerReward calculate(double hours, boolean flowAchieved) {
        if (hours < 0) {
            throw new IllegalArgumentException("Las horas no pueden ser negativas: " + hours);
        }

        int intXP = (int) Math.round(hours * CodeSessionConstants.XP_INT_PER_HOUR);
        int disXP = (int) Math.round(hours * CodeSessionConstants.XP_DIS_PER_HOUR);
        int wisXP = flowAchieved ? CodeSessionConstants.FLOW_BONUS_WIS : 0;
        int hpCost = (int) Math.round(hours * CodeSessionConstants.HP_COST_PER_HOUR);

        return new CareerReward(intXP, disXP, wisXP, hpCost);
    }

    /**
     * Suma total de XP ganada en esta sesión.
     */
    public int getTotalXP() {
        return intellectXP + disciplineXP + wisdomXP;
    }

    /**
     * Verifica si esta recompensa incluye bonus de Flow.
     */
    public boolean hasFlowBonus() {
        return wisdomXP > 0;
    }

    /**
     * Formatea la recompensa para mostrar en UI.
     * Ejemplo: "+180 INT | +60 DIS | +50 WIS | -9 HP"
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("+%d INT | +%d DIS", intellectXP, disciplineXP));

        if (wisdomXP > 0) {
            sb.append(String.format(" | +%d WIS 🔥", wisdomXP));
        }

        sb.append(String.format(" | -%d HP", hpCost));

        return sb.toString();
    }

    /**
     * Versión compacta para listas.
     * Ejemplo: "+290 XP | -9 HP"
     */
    public String toCompactString() {
        String flowIndicator = hasFlowBonus() ? " 🔥" : "";
        return String.format("+%d XP%s | -%d HP", getTotalXP(), flowIndicator, hpCost);
    }

    @Override
    public String toString() {
        return String.format("CareerReward[INT=%d, DIS=%d, WIS=%d, HP=-%d, total=%d XP]",
                intellectXP, disciplineXP, wisdomXP, hpCost, getTotalXP());
    }
}
