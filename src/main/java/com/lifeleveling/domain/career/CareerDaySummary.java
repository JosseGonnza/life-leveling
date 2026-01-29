package com.lifeleveling.domain.career;

/**
 * Value Object que representa el resumen de un día de trabajo.
 *
 * Útil para mostrar en la UI el progreso diario acumulado.
 */
public record CareerDaySummary(
        double totalHours,
        int totalIntellectXP,
        int totalDisciplineXP,
        int totalWisdomXP,
        int totalHPCost,
        int sessionCount,
        int flowCount
) {

    /**
     * Resumen vacío (día sin sesiones).
     */
    public static CareerDaySummary empty() {
        return new CareerDaySummary(0.0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Total de XP ganada en el día.
     */
    public int getTotalXP() {
        return totalIntellectXP + totalDisciplineXP + totalWisdomXP;
    }

    /**
     * ¿Se ha registrado al menos una sesión hoy?
     */
    public boolean hasActivity() {
        return sessionCount > 0;
    }

    /**
     * ¿Se alcanzó el Flow State al menos una vez?
     */
    public boolean hasFlow() {
        return flowCount > 0;
    }

    /**
     * Promedio de horas por sesión (para métricas).
     */
    public double getAverageSessionHours() {
        if (sessionCount == 0) return 0.0;
        return totalHours / sessionCount;
    }

    /**
     * Formato para mostrar en la cabecera de la UI.
     * Ejemplo: "Hoy: 5.0h | 2 sesiones | 1 🔥"
     */
    public String toHeaderString() {
        if (!hasActivity()) {
            return "Hoy: Sin actividad";
        }

        String flowStr = flowCount > 0 ? String.format(" | %d 🔥", flowCount) : "";
        return String.format("Hoy: %.1fh | %d sesiones%s", totalHours, sessionCount, flowStr);
    }

    /**
     * Formato detallado para el panel de resumen.
     */
    public String toDetailString() {
        if (!hasActivity()) {
            return "📊 Sin sesiones registradas hoy";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 Total: %.1fh | -%d HP\n", totalHours, totalHPCost));
        sb.append(String.format("   🧠 +%d INT | 🛡️ +%d DIS", totalIntellectXP, totalDisciplineXP));

        if (totalWisdomXP > 0) {
            sb.append(String.format(" | 🦉 +%d WIS", totalWisdomXP));
        }

        sb.append(String.format("\n   Total: +%d XP", getTotalXP()));

        if (flowCount > 0) {
            sb.append(String.format(" | 🔥 Flow ×%d", flowCount));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "CareerDaySummary[hours=%.1f, sessions=%d, flows=%d, XP=%d, HP=-%d]",
                totalHours, sessionCount, flowCount, getTotalXP(), totalHPCost
        );
    }
}
