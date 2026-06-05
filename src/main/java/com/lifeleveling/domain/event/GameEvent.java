package com.lifeleveling.domain.event;

/**
 * Representa un evento del juego que puede ser notificado a listeners.
 * Cada evento tiene un tipo y un mensaje descriptivo.
 */
public record GameEvent(GameEventType type, String message) {

    public GameEvent {
        if (type == null) throw new IllegalArgumentException("Event type cannot be null");
        if (message == null) throw new IllegalArgumentException("Message cannot be null");
    }

    public static GameEvent of(GameEventType type, String message) {
        return new GameEvent(type, message);
    }

    // Factory methods para eventos comunes
    public static GameEvent levelUp(int oldLevel, int newLevel) {
        return new GameEvent(GameEventType.LEVEL_UP,
                String.format("🆙 ¡LEVEL UP! %d -> %d", oldLevel, newLevel));
    }

    public static GameEvent xpGained(String source, int amount, int total) {
        return new GameEvent(GameEventType.XP_GAINED,
                String.format("⭐ Ganada XP General: +%d (Total: %d)", amount, total));
    }

    public static GameEvent goldSpent(int amount, String reason) {
        return new GameEvent(GameEventType.GOLD_SPENT,
                String.format("💸 -%d G (%s)", amount, reason));
    }

    public static GameEvent questCompleted(String questName) {
        return new GameEvent(GameEventType.QUEST_COMPLETED,
                String.format("🎉 Quest completada: %s", questName));
    }

    public static GameEvent questFailed(String questName, int hpDamage) {
        return new GameEvent(GameEventType.QUEST_FAILED,
                String.format("❌ Quest Fallida: %s (-%d HP)", questName, hpDamage));
    }

    public static GameEvent burnout() {
        return new GameEvent(GameEventType.BURNOUT,
                "💔 ¡BURNOUT! HP a 0. Bloqueo de 24h y multa aplicada.");
    }

    public static GameEvent burnoutRecovered() {
        return new GameEvent(GameEventType.BURNOUT_RECOVERED,
                "🔥 Burnout superado. ¡Bienvenido de vuelta!");
    }

    public static GameEvent perfectDay() {
        return new GameEvent(GameEventType.PERFECT_DAY,
                "🌟 ¡PERFECT DAY! +100 XP, +100 G, HP MAX 🌟");
    }

    public static GameEvent debuffApplied(String debuffName) {
        return new GameEvent(GameEventType.DEBUFF_APPLIED,
                String.format("⚠️ CASTIGO: %s", debuffName));
    }

    public static GameEvent debuffCured(String debuffName) {
        return new GameEvent(GameEventType.DEBUFF_CURED,
                String.format("✨ Debuff curado: %s", debuffName));
    }

    public static GameEvent itemConsumed(String itemName) {
        return new GameEvent(GameEventType.ITEM_CONSUMED,
                String.format("🥣 Consumiendo: %s", itemName));
    }

    public static GameEvent buffActivated(String buffName) {
        return new GameEvent(GameEventType.BUFF_ACTIVATED,
                String.format("⚡ Buff Activado: %s", buffName));
    }

    public static GameEvent rankUp(String newRank, double multiplier) {
        return new GameEvent(GameEventType.RANK_UP,
                String.format("🎉 ¡ASCENSO! Nuevo Rango: %s (Ingresos x%.1f)", newRank, multiplier));
    }

    public static GameEvent gateCompleted(String gateName) {
        return new GameEvent(GameEventType.GATE_COMPLETED,
                String.format("⛩️ GATE COMPLETADA: %s", gateName));
    }

    public static GameEvent gateAvailable(String gateName, String description) {
        return new GameEvent(GameEventType.GATE_AVAILABLE,
                String.format("⛩️ NUEVA GATE DISPONIBLE: %s — %s", gateName, description));
    }

    public static GameEvent milestoneReached(String milestoneName) {
        return new GameEvent(GameEventType.MILESTONE_REACHED,
                String.format("🏆 MILESTONE: %s alcanzado!", milestoneName));
    }

    public static GameEvent titleUnlocked(String titleName) {
        return new GameEvent(GameEventType.TITLE_UNLOCKED,
                String.format("🔓 Título desbloqueado: %s", titleName));
    }

    public static GameEvent weeklyQuestCompleted(String questName) {
        return new GameEvent(GameEventType.WEEKLY_QUEST_COMPLETED,
                String.format("🎉 ¡WEEKLY QUEST COMPLETADA! %s", questName));
    }

    public static GameEvent info(String message) {
        return new GameEvent(GameEventType.INFO, message);
    }

    public static GameEvent warning(String message) {
        return new GameEvent(GameEventType.WARNING, "⚠️ " + message);
    }
}
