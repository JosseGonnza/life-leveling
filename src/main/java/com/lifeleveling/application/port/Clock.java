package com.lifeleveling.application.port;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Puerto de tiempo. Abstrae el "paso del tiempo" para que la capa de aplicación
 * (rachas, cierre de día, triggers) sea testeable inyectando relojes deterministas.
 */
public interface Clock {

    Instant now();

    LocalDate today();
}
