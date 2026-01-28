package com.lifeleveling.domain.title;

/**
 * TitleCategory: Categorías de títulos en el juego.
 *
 * Cada categoría agrupa títulos temáticamente relacionados.
 */
public enum TitleCategory {

    /**
     * SURVIVAL: Premios a la resiliencia y gestión de energía.
     * Ejemplos: Superviviente, Zombie, Fénix, Guardián del Sueño, Inmortal
     */
    SURVIVAL("Supervivencia", "Premios a la resiliencia y gestión de energía"),

    /**
     * CAREER: Recompensas del Career Engine (CODE).
     * Ejemplos: Hello World, Code Monkey, 10x Developer, Arquitecto, Deep Worker
     */
    CAREER("Carrera", "Recompensas del Career Engine"),

    /**
     * LIFESTYLE: Constancia en los hábitos secundarios.
     * Ejemplos: Lobo Solitario, Templo Sagrado, Bibliotecario, Minimalista
     */
    LIFESTYLE("Estilo de Vida", "Constancia en los hábitos diarios"),

    /**
     * WEALTH: Poder adquisitivo y coleccionismo.
     * Ejemplos: Ahorrador, Lobo de Wall St., Ballena
     */
    WEALTH("Riqueza", "Demuestra tu poder adquisitivo"),

    /**
     * STAT_MASTERY: Prestigio por alcanzar niveles altos en stats individuales.
     * Títulos al llegar a nivel 50 y 100 de cada stat.
     */
    STAT_MASTERY("Maestría de Stats", "Prestigio por dominar tus atributos"),

    /**
     * ELDER: Títulos otorgados al completar Elder Quests (Nivel 75+).
     * Ejemplos: Minimalista Supremo, Speed Reader, Metabolismo, Mente de Acero
     */
    ELDER("Juicios del Monarca", "Recompensas de las Elder Quests"),

    /**
     * MILESTONE: Títulos otorgados al alcanzar niveles múltiplos de 10.
     * Ejemplos: Survivor (10), Veterano (20), Leyenda (50), Monarca (100)
     */
    MILESTONE("Hitos de Nivel", "Recompensas por subir de nivel");

    private final String displayName;
    private final String description;

    TitleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
