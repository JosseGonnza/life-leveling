package com.lifeleveling.ui;

/**
 * Navegación entre pantallas. La implementa la App; cada pantalla la recibe para
 * cablear sus botones sin conocer el resto de la UI.
 */
interface Nav {
    void home();
    void daily();
    void quests();
    void gates();
    void armory();
    void titles();
    void continueDay();
    /** Pantallas aún no implementadas: feedback temporal. */
    void todo(String screenName);
}
