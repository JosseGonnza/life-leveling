package com.lifeleveling.application.port;

import com.lifeleveling.domain.event.GameEvent;

/**
 * Puerto de salida para feedback al usuario. La fachada reenvía aquí los GameEvent
 * que emite el dominio. La cara concreta lo implementa (consola, modales JavaFX, SSE web…).
 */
public interface Notifier {

    void onEvent(GameEvent event);
}
