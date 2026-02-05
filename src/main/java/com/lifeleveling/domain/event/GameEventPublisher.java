package com.lifeleveling.domain.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Publicador de eventos del juego.
 * Gestiona la lista de listeners y distribuye eventos a todos ellos.
 *
 * Uso típico:
 * 1. Crear un publisher y registrar listeners
 * 2. Inyectar el publisher en Player y otros objetos de dominio
 * 3. Los objetos de dominio llaman a publish() para emitir eventos
 */
public class GameEventPublisher {

    private final List<GameEventListener> listeners = new ArrayList<>();

    public GameEventPublisher() {}

    /**
     * Registra un listener para recibir eventos.
     */
    public void addListener(GameEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Elimina un listener.
     */
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Elimina todos los listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Publica un evento a todos los listeners registrados.
     */
    public void publish(GameEvent event) {
        if (event == null) return;
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /**
     * Publica un evento de tipo INFO.
     */
    public void info(String message) {
        publish(GameEvent.info(message));
    }

    /**
     * Publica un evento de tipo WARNING.
     */
    public void warning(String message) {
        publish(GameEvent.warning(message));
    }

    /**
     * Verifica si hay listeners registrados.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Crea un publisher con un listener de consola preconfigurado.
     * Útil para desarrollo y debugging.
     */
    public static GameEventPublisher withConsoleListener() {
        GameEventPublisher publisher = new GameEventPublisher();
        publisher.addListener(GameEventListener.console());
        return publisher;
    }

    /**
     * Crea un publisher silencioso (sin listeners).
     * Útil para tests o cuando no se necesitan notificaciones.
     */
    public static GameEventPublisher silent() {
        return new GameEventPublisher();
    }
}
