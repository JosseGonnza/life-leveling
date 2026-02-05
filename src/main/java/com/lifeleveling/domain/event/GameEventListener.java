package com.lifeleveling.domain.event;

/**
 * Interface para escuchar eventos del juego.
 * Implementar esta interface permite a la capa de infraestructura
 * (CLI, JavaFX, etc.) reaccionar a eventos del dominio.
 */
@FunctionalInterface
public interface GameEventListener {

    /**
     * Recibe un evento del juego.
     * @param event El evento a procesar
     */
    void onEvent(GameEvent event);

    /**
     * Listener nulo que ignora todos los eventos.
     * Útil como default cuando no hay listeners configurados.
     */
    static GameEventListener silent() {
        return event -> {};
    }

    /**
     * Listener que imprime a consola (útil para debugging/CLI).
     */
    static GameEventListener console() {
        return event -> System.out.println(event.message());
    }

    /**
     * Combina múltiples listeners en uno.
     */
    static GameEventListener composite(GameEventListener... listeners) {
        return event -> {
            for (GameEventListener listener : listeners) {
                if (listener != null) {
                    listener.onEvent(event);
                }
            }
        };
    }

    /**
     * Crea un listener que solo procesa ciertos tipos de eventos.
     */
    default GameEventListener filter(GameEventType... types) {
        return event -> {
            for (GameEventType type : types) {
                if (event.type() == type) {
                    this.onEvent(event);
                    return;
                }
            }
        };
    }
}
