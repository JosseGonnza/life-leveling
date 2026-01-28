package com.lifeleveling.domain.title;

import com.lifeleveling.domain.player.StatType;
import com.lifeleveling.domain.quest.daily.DailyQuestType;

import java.util.List;
import java.util.Optional;

/**
 * TitleType: Catálogo completo de títulos del juego.
 *
 * Cada título tiene:
 *   - Categoría (Survival, Career, Lifestyle, etc.)
 *   - Requisito de desbloqueo (nivel, racha, acumulado, etc.)
 *   - Buffs que otorga cuando está equipado
 *   - Lore/descripción temática
 *
 * Los títulos son "Objetos Mágicos" que modifican stats.
 * Solo aplican los buffs de títulos EQUIPADOS.
 */
public enum TitleType {

    // ========================================================================================
    // A. SUPERVIVENCIA (SURVIVAL)
    // ========================================================================================

    /**
     * Superviviente: Primer título del juego.
     * Requisito: Llegar a Nivel 10 (Rango E).
     * Buff: +3% HP Recovery
     */
    SURVIVOR(
            "Superviviente",
            TitleCategory.SURVIVAL,
            "Has pasado la primera semana sin morir. Prometes.",
            TitleRequirementType.LEVEL,
            10, null, null, 0, 0, null,
            List.of(TitleBuff.hpRecovery(0.03))
    ),

    /**
     * Zombie: Resistencia extrema.
     * Requisito: Sobrevivir 3 días seguidos con HP < 15.
     * Buff: +5% DIS XP
     */
    ZOMBIE(
            "Zombie",
            TitleCategory.SURVIVAL,
            "La cafeína ya no hace efecto. Funciono por pura rabia.",
            TitleRequirementType.LOW_HP_STREAK,
            3, null, null, 15, 0, null,
            List.of(TitleBuff.statXP(StatType.DISCIPLINE, 0.05))
    ),

    /**
     * Fénix: Renacer del burnout.
     * Requisito: Recuperarse de BURNOUT (HP 0 → 100).
     * Buff: +5% STR XP
     */
    PHOENIX(
            "Fénix",
            TitleCategory.SURVIVAL,
            "Lo que no te mata te hace perder un día de sueldo.",
            TitleRequirementType.BURNOUT_RECOVERY,
            1, null, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.STRENGTH, 0.05))
    ),

    /**
     * Guardián del Sueño: Maestro del descanso.
     * Requisito: Racha SLEEP >= 14 días (>7h).
     * Buff: +10% HP Recovery
     */
    SLEEP_GUARDIAN(
            "Guardián del Sueño",
            TitleCategory.SURVIVAL,
            "Tu cama es un templo. Dormir cura 33 HP.",
            TitleRequirementType.HABIT_STREAK,
            14, DailyQuestType.SLEEP, null, 0, 0, null,
            List.of(TitleBuff.hpRecovery(0.10))
    ),

    /**
     * Inmortal: Gestión de energía perfecta.
     * Requisito: No bajar del 50% HP durante 30 días.
     * Buff: +20% HP Recovery
     */
    IMMORTAL(
            "Inmortal",
            TitleCategory.SURVIVAL,
            "Tu gestión de energía es impecable.",
            TitleRequirementType.HIGH_HP_STREAK,
            30, null, null, 50, 0, null,
            List.of(TitleBuff.hpRecovery(0.20))
    ),

    // ========================================================================================
    // B. CARRERA Y CÓDIGO (CAREER)
    // ========================================================================================

    /**
     * Hello World: Primera línea de código.
     * Requisito: Completar la primera CODE.
     * Buff: +2% INT XP
     */
    HELLO_WORLD(
            "Hello World",
            TitleCategory.CAREER,
            "Todo viaje comienza con un print.",
            TitleRequirementType.FIRST_COMPLETION,
            1, DailyQuestType.CODE, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.INTELLECT, 0.02))
    ),

    /**
     * Code Monkey: Constancia en el código.
     * Requisito: Acumular 100 horas de CODE.
     * Buff: +5% INT XP
     */
    CODE_MONKEY(
            "Code Monkey",
            TitleCategory.CAREER,
            "Sabes copiar de StackOverflow con estilo.",
            TitleRequirementType.ACCUMULATED_HOURS,
            100, DailyQuestType.CODE, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.INTELLECT, 0.05))
    ),

    /**
     * 10x Developer: Productividad extrema.
     * Requisito: Acumular 500 horas de CODE.
     * Buff: +10% INT XP
     */
    TEN_X_DEVELOPER(
            "10x Developer",
            TitleCategory.CAREER,
            "Haces el trabajo de 10 juniors (cobrando como 1).",
            TitleRequirementType.ACCUMULATED_HOURS,
            500, DailyQuestType.CODE, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.INTELLECT, 0.10))
    ),

    /**
     * Arquitecto: Maestría del código.
     * Requisito: Acumular 1,000 horas de CODE.
     * Buff: +5% WIS + +5% INT
     */
    ARCHITECT(
            "Arquitecto",
            TitleCategory.CAREER,
            "Ya no picas código, diseñas catedrales lógicas.",
            TitleRequirementType.ACCUMULATED_HOURS,
            1000, DailyQuestType.CODE, null, 0, 0, null,
            List.of(
                    TitleBuff.statXP(StatType.WISDOM, 0.05),
                    TitleBuff.statXP(StatType.INTELLECT, 0.05)
            )
    ),

    /**
     * Deep Worker: Concentración profunda.
     * Requisito: 50 sesiones de Flow (>3h seguidas).
     * Buff: +5% DIS XP
     */
    DEEP_WORKER(
            "Deep Worker",
            TitleCategory.CAREER,
            "Tu concentración dobla cucharas.",
            TitleRequirementType.FLOW_SESSIONS,
            50, null, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.DISCIPLINE, 0.05))
    ),

    // ========================================================================================
    // C. ESTILO DE VIDA (LIFESTYLE)
    // ========================================================================================

    /**
     * Lobo Solitario: Constancia en el gym.
     * Requisito: 50 sesiones de GYM.
     * Buff: +5% STR XP
     */
    LONE_WOLF(
            "Lobo Solitario",
            TitleCategory.LIFESTYLE,
            "El hierro nunca miente.",
            TitleRequirementType.ACCUMULATED_COUNT,
            50, DailyQuestType.GYM, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.STRENGTH, 0.05))
    ),

    /**
     * Templo Sagrado: Cuidado personal extremo.
     * Requisito: Racha DIET + SKINCARE >= 21 días.
     * Buff: +10% CHA XP
     */
    SACRED_TEMPLE(
            "Templo Sagrado",
            TitleCategory.LIFESTYLE,
            "Eres la definición de 'Glow Up'.",
            TitleRequirementType.DUAL_HABIT_STREAK,
            21, DailyQuestType.DIET, DailyQuestType.SKINCARE, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.CHARISMA, 0.10))
    ),

    /**
     * Bibliotecario: Devorador de libros.
     * Requisito: 2,000 páginas leídas.
     * Buff: +8% WIS XP
     */
    LIBRARIAN(
            "Bibliotecario",
            TitleCategory.LIFESTYLE,
            "Sabes cosas que nadie preguntó, pero sabes cosas.",
            TitleRequirementType.ACCUMULATED_PAGES,
            2000, DailyQuestType.READ, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.WISDOM, 0.08))
    ),

    /**
     * Minimalista: Orden y limpieza.
     * Requisito: 50 veces TIDY completado.
     * Buff: +5% WIS XP
     */
    MINIMALIST(
            "Minimalista",
            TitleCategory.LIFESTYLE,
            "Habitación ordenada, mente ordenada.",
            TitleRequirementType.ACCUMULATED_COUNT,
            50, DailyQuestType.TIDY, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.WISDOM, 0.05))
    ),

    // ========================================================================================
    // D. ECONOMÍA Y RIQUEZA (WEALTH)
    // ========================================================================================

    /**
     * Ahorrador: Primer colchón de seguridad.
     * Requisito: Wallet >= 50,000 G.
     * Buff: +2% Gold Gain
     */
    SAVER(
            "Ahorrador",
            TitleCategory.WEALTH,
            "El primer colchón de seguridad.",
            TitleRequirementType.GOLD_THRESHOLD,
            50000, null, null, 0, 0, null,
            List.of(TitleBuff.goldMultiplier(0.02))
    ),

    /**
     * Lobo de Wall St.: Riqueza significativa.
     * Requisito: Wallet >= 200,000 G.
     * Buff: +5% Gold Gain
     */
    WOLF_OF_WALL_ST(
            "Lobo de Wall St.",
            TitleCategory.WEALTH,
            "El dinero llama al dinero.",
            TitleRequirementType.GOLD_THRESHOLD,
            200000, null, null, 0, 0, null,
            List.of(TitleBuff.goldMultiplier(0.05))
    ),

    /**
     * Ballena: Gran gastador.
     * Requisito: Gasto total en tienda >= 500,000 G.
     * Buff: +10% Gold Gain
     */
    WHALE(
            "Ballena",
            TitleCategory.WEALTH,
            "Eres el cliente VIP del sistema.",
            TitleRequirementType.TOTAL_SPENDING,
            500000, null, null, 0, 0, null,
            List.of(TitleBuff.goldMultiplier(0.10))
    ),

    // ========================================================================================
    // E. MAESTRÍA DE STATS (STAT MASTERY) - Nivel 50
    // ========================================================================================

    /**
     * Titán: Maestría STR nivel 50.
     */
    TITAN(
            "Titán",
            TitleCategory.STAT_MASTERY,
            "Tu fuerza es legendaria.",
            TitleRequirementType.STAT_LEVEL,
            50, null, null, 0, 0, StatType.STRENGTH,
            List.of(TitleBuff.statXP(StatType.STRENGTH, 0.05))
    ),

    /**
     * Cyborg: Maestría INT nivel 50.
     */
    CYBORG(
            "Cyborg",
            TitleCategory.STAT_MASTERY,
            "Más máquina que humano.",
            TitleRequirementType.STAT_LEVEL,
            50, null, null, 0, 0, StatType.INTELLECT,
            List.of(TitleBuff.statXP(StatType.INTELLECT, 0.05))
    ),

    /**
     * Oráculo: Maestría WIS nivel 50.
     */
    ORACLE(
            "Oráculo",
            TitleCategory.STAT_MASTERY,
            "Ves más allá del código.",
            TitleRequirementType.STAT_LEVEL,
            50, null, null, 0, 0, StatType.WISDOM,
            List.of(TitleBuff.statXP(StatType.WISDOM, 0.05))
    ),

    /**
     * General: Maestría DIS nivel 50.
     */
    GENERAL(
            "General",
            TitleCategory.STAT_MASTERY,
            "Tu disciplina es inquebrantable.",
            TitleRequirementType.STAT_LEVEL,
            50, null, null, 0, 0, StatType.DISCIPLINE,
            List.of(TitleBuff.statXP(StatType.DISCIPLINE, 0.05))
    ),

    /**
     * Estrella: Maestría CHA nivel 50.
     */
    STAR(
            "Estrella",
            TitleCategory.STAT_MASTERY,
            "Brillas en cualquier reunión.",
            TitleRequirementType.STAT_LEVEL,
            50, null, null, 0, 0, StatType.CHARISMA,
            List.of(TitleBuff.statXP(StatType.CHARISMA, 0.05))
    ),

    // ========================================================================================
    // E. MAESTRÍA DE STATS (STAT MASTERY) - Nivel 100
    // ========================================================================================

    /**
     * Destructor de Mundos: Maestría STR nivel 100.
     */
    WORLD_DESTROYER(
            "Destructor de Mundos",
            TitleCategory.STAT_MASTERY,
            "Doblas el acero con la mirada.",
            TitleRequirementType.STAT_LEVEL,
            100, null, null, 0, 0, StatType.STRENGTH,
            List.of(TitleBuff.statXP(StatType.STRENGTH, 0.10))
    ),

    /**
     * Singularidad: Maestría INT nivel 100.
     */
    SINGULARITY(
            "Singularidad",
            TitleCategory.STAT_MASTERY,
            "Has trascendido la inteligencia humana.",
            TitleRequirementType.STAT_LEVEL,
            100, null, null, 0, 0, StatType.INTELLECT,
            List.of(TitleBuff.statXP(StatType.INTELLECT, 0.10))
    ),

    /**
     * Iluminado: Maestría WIS nivel 100.
     */
    ENLIGHTENED(
            "Iluminado",
            TitleCategory.STAT_MASTERY,
            "La sabiduría absoluta.",
            TitleRequirementType.STAT_LEVEL,
            100, null, null, 0, 0, StatType.WISDOM,
            List.of(TitleBuff.statXP(StatType.WISDOM, 0.10))
    ),

    /**
     * Estoico: Maestría DIS nivel 100.
     */
    STOIC(
            "Estoico",
            TitleCategory.STAT_MASTERY,
            "Nada perturba tu paz interior.",
            TitleRequirementType.STAT_LEVEL,
            100, null, null, 0, 0, StatType.DISCIPLINE,
            List.of(TitleBuff.statXP(StatType.DISCIPLINE, 0.10))
    ),

    /**
     * Ídolo de Masas: Maestría CHA nivel 100.
     */
    IDOL(
            "Ídolo de Masas",
            TitleCategory.STAT_MASTERY,
            "El mundo gira a tu alrededor.",
            TitleRequirementType.STAT_LEVEL,
            100, null, null, 0, 0, StatType.CHARISMA,
            List.of(TitleBuff.statXP(StatType.CHARISMA, 0.10))
    ),

    // ========================================================================================
    // F. ELDER QUESTS (Nivel 75+)
    // ========================================================================================

    /**
     * Minimalista Supremo: ELDER_2 completada.
     * Buff: +15% Gold Gain permanente
     */
    SUPREME_MINIMALIST(
            "Minimalista Supremo",
            TitleCategory.ELDER,
            "30 días sin consumir lujos. Eres libre del materialismo.",
            TitleRequirementType.ELDER_QUEST,
            2, null, null, 0, 0, null,
            List.of(TitleBuff.goldMultiplier(0.15))
    ),

    /**
     * Speed Reader: ELDER_3 completada.
     * Buff: READ otorga x1.5 XP
     */
    SPEED_READER(
            "Speed Reader",
            TitleCategory.ELDER,
            "1,000 páginas en 30 días. Los libros tiemblan ante ti.",
            TitleRequirementType.ELDER_QUEST,
            3, null, null, 0, 0, null,
            List.of(TitleBuff.readXPMultiplier(1.5))
    ),

    /**
     * Metabolismo: ELDER_4 completada.
     * Buff: +25% STR XP permanente
     */
    METABOLISM(
            "Metabolismo",
            TitleCategory.ELDER,
            "25 Gyms + 0 Junk Food. Tu cuerpo es una máquina.",
            TitleRequirementType.ELDER_QUEST,
            4, null, null, 0, 0, null,
            List.of(TitleBuff.statXP(StatType.STRENGTH, 0.25))
    ),

    /**
     * Mente de Acero: ELDER_5 completada.
     * Buff: Inmunidad total al debuff "Caos"
     */
    STEEL_MIND(
            "Mente de Acero",
            TitleCategory.ELDER,
            "30 días sin debuffs. Tu mente es impenetrable.",
            TitleRequirementType.ELDER_QUEST,
            5, null, null, 0, 0, null,
            List.of(TitleBuff.debuffImmunity("CHAOS"))
    ),

    /**
     * Mano del Rey: ELDER_7 completada.
     * Buff: +20% ALL STATS XP (el buff más potente del juego)
     */
    HAND_OF_THE_KING(
            "Mano del Rey",
            TitleCategory.ELDER,
            "20 Perfect Days en un mes. El buff más potente del juego.",
            TitleRequirementType.ELDER_QUEST,
            7, null, null, 0, 0, null,
            List.of(TitleBuff.allStatsXP(0.20))
    ),

    // ========================================================================================
    // G. HITOS DE NIVEL (MILESTONES)
    // ========================================================================================

    /**
     * Veterano: Nivel 20.
     */
    VETERAN(
            "Veterano",
            TitleCategory.MILESTONE,
            "Ya conoces las mecánicas del juego.",
            TitleRequirementType.LEVEL,
            20, null, null, 0, 0, null,
            List.of(TitleBuff.generalXP(0.02))
    ),

    /**
     * Experto: Nivel 30.
     */
    EXPERT(
            "Experto",
            TitleCategory.MILESTONE,
            "Dominas los sistemas básicos.",
            TitleRequirementType.LEVEL,
            30, null, null, 0, 0, null,
            List.of(TitleBuff.generalXP(0.05))
    ),

    /**
     * Maestro: Nivel 40.
     */
    MASTER(
            "Maestro",
            TitleCategory.MILESTONE,
            "Tu sabiduría guía a otros.",
            TitleRequirementType.LEVEL,
            40, null, null, 0, 0, null,
            List.of(TitleBuff.generalXP(0.07))
    ),

    /**
     * Leyenda: Nivel 50 (desbloquea segundo slot de título).
     */
    LEGEND(
            "Leyenda",
            TitleCategory.MILESTONE,
            "Tu nombre será recordado. Segundo slot de título desbloqueado.",
            TitleRequirementType.LEVEL,
            50, null, null, 0, 0, null,
            List.of(TitleBuff.generalXP(0.10))
    ),

    /**
     * Semidiós: Nivel 60.
     */
    DEMIGOD(
            "Semidiós",
            TitleCategory.MILESTONE,
            "Caminas entre mortales.",
            TitleRequirementType.LEVEL,
            60, null, null, 0, 0, null,
            List.of(TitleBuff.goldMultiplier(0.10))
    ),

    /**
     * Titán (Milestone): Nivel 70.
     * Nota: Diferente del Titán de Stat Mastery
     */
    TITAN_MILESTONE(
            "Titán",
            TitleCategory.MILESTONE,
            "Tu poder es inmenso.",
            TitleRequirementType.LEVEL,
            70, null, null, 0, 0, null,
            List.of(TitleBuff.hpRecovery(0.10))
    ),

    /**
     * Inmortal (Milestone): Nivel 80.
     * Nota: Diferente del Inmortal de Survival
     */
    IMMORTAL_MILESTONE(
            "Inmortal",
            TitleCategory.MILESTONE,
            "El tiempo no te afecta.",
            TitleRequirementType.LEVEL,
            80, null, null, 0, 0, null,
            List.of(TitleBuff.hpRecovery(0.05))  // Regeneración pasiva
    ),

    /**
     * Trascendente: Nivel 90.
     */
    TRANSCENDENT(
            "Trascendente",
            TitleCategory.MILESTONE,
            "Has superado los límites humanos.",
            TitleRequirementType.LEVEL,
            90, null, null, 0, 0, null,
            List.of(TitleBuff.allStatsXP(0.05))
    ),

    /**
     * Monarca: Nivel 100 (título final).
     */
    MONARCH(
            "Monarca",
            TitleCategory.MILESTONE,
            "El juego ya no te controla. Tú controlas el juego.",
            TitleRequirementType.LEVEL,
            100, null, null, 0, 0, null,
            List.of(TitleBuff.allStatsXP(0.10))
    );

    /**
     * Tipos de requisitos para desbloquear títulos.
     */
    public enum TitleRequirementType {
        /** Alcanzar un nivel general específico */
        LEVEL,
        /** Racha de un hábito específico */
        HABIT_STREAK,
        /** Racha combinada de dos hábitos */
        DUAL_HABIT_STREAK,
        /** Días seguidos con HP bajo */
        LOW_HP_STREAK,
        /** Días seguidos con HP alto */
        HIGH_HP_STREAK,
        /** Recuperarse de burnout */
        BURNOUT_RECOVERY,
        /** Primera vez que se completa algo */
        FIRST_COMPLETION,
        /** Horas acumuladas de una actividad */
        ACCUMULATED_HOURS,
        /** Páginas leídas acumuladas */
        ACCUMULATED_PAGES,
        /** Conteo acumulado de completar algo */
        ACCUMULATED_COUNT,
        /** Sesiones de Flow (>3h) */
        FLOW_SESSIONS,
        /** Oro actual en wallet */
        GOLD_THRESHOLD,
        /** Gasto total en tienda */
        TOTAL_SPENDING,
        /** Nivel de un stat específico */
        STAT_LEVEL,
        /** Completar una Elder Quest */
        ELDER_QUEST
    }

    // ========================================================================================
    // FIELDS
    // ========================================================================================

    private final String displayName;
    private final TitleCategory category;
    private final String lore;
    private final TitleRequirementType requirementType;
    private final int requirementValue;        // Valor numérico del requisito
    private final DailyQuestType relatedHabit; // Hábito relacionado (si aplica)
    private final DailyQuestType secondHabit;  // Segundo hábito (para DUAL_HABIT_STREAK)
    private final int hpThreshold;             // Umbral de HP (para LOW/HIGH_HP_STREAK)
    private final int elderQuestNumber;        // Número de Elder Quest (1-7)
    private final StatType relatedStat;        // Stat relacionado (para STAT_LEVEL)
    private final List<TitleBuff> buffs;

    TitleType(
            String displayName,
            TitleCategory category,
            String lore,
            TitleRequirementType requirementType,
            int requirementValue,
            DailyQuestType relatedHabit,
            DailyQuestType secondHabit,
            int hpThreshold,
            int elderQuestNumber,
            StatType relatedStat,
            List<TitleBuff> buffs
    ) {
        this.displayName = displayName;
        this.category = category;
        this.lore = lore;
        this.requirementType = requirementType;
        this.requirementValue = requirementValue;
        this.relatedHabit = relatedHabit;
        this.secondHabit = secondHabit;
        this.hpThreshold = hpThreshold;
        this.elderQuestNumber = elderQuestNumber;
        this.relatedStat = relatedStat;
        this.buffs = buffs;
    }

    // ========================================================================================
    // GETTERS
    // ========================================================================================

    public String getDisplayName() {
        return displayName;
    }

    public TitleCategory getCategory() {
        return category;
    }

    public String getLore() {
        return lore;
    }

    public TitleRequirementType getRequirementType() {
        return requirementType;
    }

    public int getRequirementValue() {
        return requirementValue;
    }

    public DailyQuestType getRelatedHabit() {
        return relatedHabit;
    }

    public DailyQuestType getSecondHabit() {
        return secondHabit;
    }

    public int getHpThreshold() {
        return hpThreshold;
    }

    public int getElderQuestNumber() {
        return elderQuestNumber;
    }

    public StatType getRelatedStat() {
        return relatedStat;
    }

    public List<TitleBuff> getBuffs() {
        return buffs;
    }

    // ========================================================================================
    // QUERIES
    // ========================================================================================

    /**
     * Verifica si este título otorga buff a un stat específico.
     */
    public boolean buffsStat(StatType stat) {
        return buffs.stream().anyMatch(b -> b.affectsStat(stat));
    }

    /**
     * Obtiene el multiplicador total para un stat (1.0 si no hay buff).
     */
    public double getStatMultiplier(StatType stat) {
        return buffs.stream()
                .filter(b -> b.affectsStat(stat))
                .mapToDouble(TitleBuff::getMultiplier)
                .reduce(1.0, (a, b) -> a * b);
    }

    /**
     * Obtiene el multiplicador de oro (1.0 si no hay buff).
     */
    public double getGoldMultiplier() {
        return buffs.stream()
                .filter(TitleBuff::affectsGold)
                .mapToDouble(TitleBuff::getMultiplier)
                .reduce(1.0, (a, b) -> a * b);
    }

    /**
     * Obtiene el bonus de recuperación de HP (0.0 si no hay buff).
     */
    public double getHPRecoveryBonus() {
        return buffs.stream()
                .filter(TitleBuff::affectsHPRecovery)
                .mapToDouble(TitleBuff::value)
                .sum();
    }

    /**
     * Verifica si este título da inmunidad a un debuff.
     */
    public boolean givesImmunityTo(String debuffName) {
        return buffs.stream()
                .anyMatch(b -> b.givesDebuffImmunity(debuffName));
    }

    // ========================================================================================
    // DISPLAY
    // ========================================================================================

    /**
     * Formatea los buffs para mostrar en UI.
     */
    public String getBuffsDisplay() {
        return buffs.stream()
                .map(TitleBuff::toDisplayString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sin buffs");
    }

    /**
     * Formatea el requisito para mostrar en UI.
     */
    public String getRequirementDisplay() {
        return switch (requirementType) {
            case LEVEL -> "Alcanzar Nivel " + requirementValue;
            case HABIT_STREAK -> "Racha de " + relatedHabit.getName() + " >= " + requirementValue + " días";
            case DUAL_HABIT_STREAK -> "Racha de " + relatedHabit.getName() + " + " +
                    secondHabit.getName() + " >= " + requirementValue + " días";
            case LOW_HP_STREAK -> requirementValue + " días con HP < " + hpThreshold;
            case HIGH_HP_STREAK -> requirementValue + " días con HP >= " + hpThreshold + "%";
            case BURNOUT_RECOVERY -> "Recuperarse de BURNOUT";
            case FIRST_COMPLETION -> "Completar " + relatedHabit.getName() + " por primera vez";
            case ACCUMULATED_HOURS -> requirementValue + " horas de " + relatedHabit.getName();
            case ACCUMULATED_PAGES -> requirementValue + " páginas leídas";
            case ACCUMULATED_COUNT -> requirementValue + " veces " + relatedHabit.getName();
            case FLOW_SESSIONS -> requirementValue + " sesiones de Flow (>3h)";
            case GOLD_THRESHOLD -> "Wallet >= " + String.format("%,d", requirementValue) + " G";
            case TOTAL_SPENDING -> "Gasto total >= " + String.format("%,d", requirementValue) + " G";
            case STAT_LEVEL -> relatedStat.getDisplayName() + " nivel " + requirementValue;
            case ELDER_QUEST -> "Completar ELDER_" + requirementValue;
        };
    }

    // ========================================================================================
    // SEARCH
    // ========================================================================================

    /**
     * Busca un título por nombre.
     */
    public static Optional<TitleType> fromString(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim().toUpperCase().replace(" ", "_");
        try {
            return Optional.of(TitleType.valueOf(normalized));
        } catch (IllegalArgumentException e) {
            // Buscar por display name
            for (TitleType type : values()) {
                if (type.displayName.equalsIgnoreCase(name.trim())) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los títulos de una categoría.
     */
    public static List<TitleType> getByCategory(TitleCategory category) {
        return java.util.Arrays.stream(values())
                .filter(t -> t.category == category)
                .toList();
    }

    /**
     * Obtiene todos los títulos que requieren un nivel específico.
     */
    public static List<TitleType> getByLevelRequirement(int level) {
        return java.util.Arrays.stream(values())
                .filter(t -> t.requirementType == TitleRequirementType.LEVEL)
                .filter(t -> t.requirementValue == level)
                .toList();
    }
}
