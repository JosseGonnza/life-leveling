<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=9df2ea&animation=fadeIn&height=120&section=header"/>

# 🧬 Life Leveling
> **"Gamifica tu existencia. Domina tu realidad."**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Coding-red?style=for-the-badge)
![Type](https://img.shields.io/badge/Type-Real_Life_RPG-purple?style=for-the-badge)

---

## 🎮 ¿Qué es Life Leveling?

**Life Leveling** no es una simple lista de tareas. Es un **RPG Hardcore de Supervivencia** aplicado a la vida real.

Tu vida se convierte en un sistema numérico donde cada acción tiene una consecuencia directa. Dormir mal te resta vida. Programar te da experiencia. Gastar dinero en caprichos reduce tu capacidad de inversión futura.

**El objetivo:** Sobrevivir a la rutina, escalar la jerarquía social (Rangos) y alcanzar el **Nivel 100** para convertirte en un **Monarca**.

---

## 💀 Sistema de Salud (Hardcore Survival)

En Life Leveling, la energía no es gratis. **No existe la regeneración pasiva.** Si no duermes, mueres (metafóricamente... o entras en Burnout).

### 🩸 HP (Health Points)
* **Max HP:** 100
* **Min HP:** 0 (Trigger de **GAME OVER Temporal**)
* **Regeneración:** Solo activa (Dormir, Comer, Skincare).

### 📉 Fuentes de Daño (El Coste de la Productividad)
Trabajar y entrenar desgasta tu cuerpo. Necesitas gestionar tu energía.

| Actividad | Daño Base | Mitigación (Items) |
| :--- | :---: | :--- |
| **💻 CODE (Trabajo)** | `-3 HP / hora` | 🖱️ Ratón Ergonómico (-1) <br> 🪑 Herman Miller (Daño = 0) |
| **🏋️ GYM (Entreno)** | `-5 HP / sesión` | 👟 Zapatillas Running (Daño = 0) |
| **🍔 Comida Basura** | `Debuff Pesadez` | `-5 HP` al final del día. |

### 💚 Fuentes de Curación (Recuperación)
Solo hay 3 formas orgánicas de recuperar HP.

1.  **💤 SLEEP (Crítico):**
  * `< 6h`: **+0 HP** (El cuerpo no recupera).
  * `6h - 7h`: **+15 HP**.
  * `> 7h`: **+30 HP** (Recuperación óptima).
2.  **🧴 SKINCARE:** `+10 HP`.
3.  **🥗 DIET (Comer limpio):** `+5 HP` (+10 con *Air Fryer*).

---

## 🔥 Mecánica de BURNOUT (Game Over)

Si tu HP toca **0**, el sistema te bloquea. No es un aviso, es una penalización real.

1.  **💸 El Impuesto:** Pierdes inmediatamente el **10% de tu Oro Total** (Gastos médicos de emergencia).
2.  **🔒 El Bloqueo:** Durante **24 Horas** reales, se bloquean todas las misiones productivas (CODE, GYM, READ).
3.  **🚑 La Salida:** Solo puedes realizar acciones de cura (Dormir, Comer). Si tras 24h tu HP sigue en 0, el bloqueo se reinicia y pagas una tasa de mantenimiento diaria.

> **"El Burnout no es un bug, es una feature. Aprende a descansar."**

---

## 📈 Sistema de Progresión (Dual Leveling)

El juego utiliza un sistema de **doble nivel** inspirado en los RPGs coreanos. Tu progreso se mide en dos ejes paralelos.

### 1. Nivel General (Player Rank)
Tu estatus total. Define qué Misiones de Rango (Gates) puedes intentar.
* **Fórmula Hardcore:** `XP = 45 * Nivel^2`
* **Hitos:**
  * Nivel 10: Rango E (Novato).
  * Nivel 50: Rango C (Veterano).
  * Nivel 100: Rango S (Monarca).

### 2. Stats Individuales (The Pentagram)
Tus habilidades específicas. Crecen de forma lineal (`Coste = Nivel * 10`).

| Stat | Atributo | Fuente Principal |
| :---: | :--- | :--- |
| **STR** | Fuerza Física | 🏋️ GYM |
| **INT** | Capacidad Lógica | 💻 CODE |
| **WIS** | Sabiduría | 📚 READ |
| **DIS** | Disciplina | 🧹 TIDY |
| **CHA** | Carisma / Imagen | 🧴 SKINCARE |

---

## 🚀 The Career Engine (Algoritmo de XP)

Para los desarrolladores, hemos creado un motor de XP especial para la habilidad **INTELLECT**. No premia solo "estar sentado", premia el **Deep Work**.

* **XP Base:** `Horas * 60` (INT) + `Horas * 20` (DIS).
* **🔥 Flow State Bonus:** Si la sesión dura **> 3 Horas**, recibes un bonus plano de `+50 XP WIS` (Sabiduría), simulando la comprensión profunda de la arquitectura.

> *Una sesión de 4 horas vale más que cuatro sesiones de 1 hora.*

---

## 📜 Sistema de Misiones (The Quest Engine)

En Life Leveling, cada acción productiva es una misión. El sistema se divide en cuatro tipos de contratos:

### 1. 🔄 Daily Quests (La Rutina de los 7 Hábitos)
Son las tareas recurrentes que se regeneran cada día a las **00:00**. Son tu fuente principal de supervivencia.

| ID | Misión | Input | Recompensa Principal | Penalización |
| :--- | :--- | :--- | :--- | :--- |
| **💤 SLEEP** | Descanso | Horas | `+30 HP` (Si > 7h) | `Debuff Fatiga` (Si < 6h) |
| **💻 CODE** | Career | Horas | `INT` + `DIS` XP | `-3 HP / hora` |
| **🏋️ GYM** | Deporte | Check | `+50 STR` XP | `-5 HP` |
| **🥗 DIET** | Dieta | Check | `+5 HP` | Ninguna |
| **🧴 SKIN** | Cuidado | Check | `+10 HP` | Ninguna |
| **📚 READ** | Sabiduría | Páginas | `WIS` XP | Ninguna |
| **🧹 TIDY** | Orden | Check | `+50 DIS` XP | `Debuff Caos` (Si fallas 3 días) |

#### ✨ Mecánica: THE PERFECT DAY
Si completas las 7 Daily Quests en un mismo día:
1.  **Sanación Divina:** Tu HP se restaura al **100%** (independientemente del daño sufrido).
2.  **Jackpot:** Recibes un Bonus de XP y Oro masivo.
3.  **Streak:** Aumenta tu racha de "Días Perfectos" (necesaria para abrir Gates).

---

### 2. 📝 User Quests (Riesgo vs Recompensa)
Tú defines tus propios objetivos (ej: "Ir al Banco", "Terminar Proyecto X").
**La Regla de Oro:** Para aceptar una misión, debes **apostar tu propia vida**.

| Rango | Dificultad | Recompensa (XP) | 💀 Castigo por Fallo |
| :---: | :--- | :---: | :--- |
| **E** | Trivial | 10 XP | `0 HP` |
| **D** | Fácil | 50 XP | `-5 HP` |
| **C** | Media | 150 XP | `-15 HP` |
| **B** | Difícil | 500 XP | `-20 HP` |
| **A** | Crítica | 1,500 XP | `-30 HP` |
| **S** | Vital | 5,000 XP | **-50 HP (Daño Masivo)** |

> *Si fallas una misión de Rango S, pierdes la mitad de tu vida. Elige sabiamente.*

---

### 3. ⛩️ System Quests (The Gates)
Son los "Jefes Finales". No aparecen hasta que cumples los requisitos. Superarlas es la única forma de subir de **Rango Profesional** (y aumentar tus ingresos).

* **GATE 1 (Novato ➔ D):** Sobrevive 7 "Perfect Days" seguidos.
* **GATE 2 (Iniciado ➔ C):** Acumula 20h de CODE y completa 3 User Quests en una semana.
* **GATE 7 (Monarca ➔ S):** Alcanza el Nivel 100 y acumula 1,000,000 G.

#### ⛓️ Quest Especial: REDEMPTION
Si entras en **BURNOUT** (0 HP) tres veces en un mes, el sistema te marca como "Indigno".
* **Castigo:** Se bloquean todas las Gates y User Quests superiores a Rango C.
* **Salida:** Debes mantener tu HP > 80 durante 14 días consecutivos para recuperar tus privilegios.

---

### 4. 👑 Elder Quests (Endgame Nvl 75+)
Los **Juicios del Monarca**. Desafíos extremos para jugadores de nivel máximo.

* **ELDER GATE 2 (Voto de Pobreza):** Pasa 30 días sin comprar NINGÚN item de categoría "Lujo".
* **ELDER GATE 7 (La Ascensión):** Consigue 20 Perfect Days en un solo mes.
* **Recompensas:** Items Únicos (Café Infinito, Llave Maestra) y Títulos Legendarios.

---

## 💰 Economía y Mercado (The Vault)

El Oro (`G`) en Life Leveling representa el **valor de mercado de tu tiempo**. No ganas dinero por "ser nivel alto", ganas dinero por tu **Rango Profesional**.

### 💸 Ingeniería Financiera (Ingresos)
Tu salario por hora trabajada (`CODE`) se multiplica según tu Rango (definido por las Gates superadas).

| Rango | Clase | Multiplicador | Ingreso Est. Mensual | Contexto |
| :---: | :--- | :---: | :--- | :--- |
| **E** | Novato | `x 1.0` | ~5,500 G | Supervivencia. |
| **C** | Junior+ | `x 1.5` | ~8,250 G | Ahorro para Tier 1. |
| **B** | Mid-Level | `x 2.5` | ~13,750 G | Clase Media. |
| **A** | Senior | `x 4.0` | ~22,000 G | Clase Alta. |
| **S** | **Architect** | `x 8.0` | **~44,000 G** | **Libertad Financiera.** |

---

### 🎒 La Armería (Equipamiento)
La ropa y los muebles no son cosméticos. Son herramientas que **modifican tus Stats** y **mitigan daño**.
Existen 12 Slots de equipamiento (`HEAD`, `BODY`, `DESK`, `CHAIR`, etc.) y 3 Tiers de calidad.

#### Ejemplos de Items:
* **🧢 Gorra de la Suerte (Tier 1):** `+1% DIS XP`. Te ayuda a concentrarte.
* **👟 Zapatillas Running (Tier 2):** Hacen que el `GYM` cueste **0 HP** (tus rodillas no sufren).
* **⌨️ Teclado Mecánico (Tier 2):** `+5% INT XP`. El sonido de la productividad.
* **🪑 Herman Miller Aeron (Tier 3):** El trono del rey. Trabajar (`CODE`) cuesta **0 HP**. Tu espalda es indestructible.

---

### 🧪 Alquimia (Consumibles)
La tienda también tiene pociones y remedios para emergencias. Cuidado con los efectos secundarios.

* **☕ Café Premium:** `+50 HP`. Curación limpia.
* **⚡ Energy Drink:** Elimina el debuff `Fatiga` temporalmente, pero si abusas te provoca `Taquicardia`.
* **🍔 Comida Basura:** Cura `+40 HP` (engaño), pero te aplica el debuff `Pesadez` (-5 HP al final del día).
* **💉 Inyección de Adrenalina:** El único item capaz de sacarte del estado **BURNOUT** instantáneamente (Cuesta 5,000 G).

---

### 🏆 Tesoros (Endgame Goals)
¿Para qué ahorras? Para ganar el juego.
Estos items no dan stats, dan la victoria.

1.  **🖥️ Setup Dev Pro (150,000 G):** La herramienta definitiva.
2.  **🚗 Coche Nuevo (350,000 G):** Libertad de movimiento.
3.  **🗽 Libertad Financiera (500,000 G):** **GAME OVER.** Has ganado al capitalismo.

---

## 🎖️ Sistema de Títulos (The Hall of Fame)

En Life Leveling, los logros no son solo medallas para mirar. Son **Objetos Mágicos** que otorgan buffs pasivos reales.
Sin embargo, no puedes beneficiarte de todos a la vez. Debes elegir tu estrategia.

### 🧩 Mecánica de Slots
* **Nivel 1 - 49:** Tienes **1 Slot** de Título activo.
* **Nivel 50 (Leyenda):** Desbloqueas el **2º Slot**.
* **Estrategia:** ¿Hoy toca programar? Equípate *Code Monkey*. ¿Estás enfermo? Equípate *Survivor*.

---

### 📜 Catálogo de Prestigio (Ejemplos)

| Título | Requisito (Hito) | Efecto (Buff Activo) | Lore |
| :--- | :--- | :--- | :--- |
| **Survivor** | Llegar a Nivel 10 | `+3% HP Rec` (Dormir cura más) | "Has sobrevivido la primera semana." |
| **Code Monkey** | 100 Horas de CODE | `+5% INT XP` | "Sabes copiar de StackOverflow con estilo." |
| **Deep Worker** | 50 Sesiones de Flow (>3h) | `+5% DIS XP` | "Tu concentración dobla cucharas." |
| **Lobo de Wall St.** | Tener 200,000 G | `+5% Gold Gain` | "El dinero llama al dinero." |
| **Minimalista** | 30 días sin Lujos (Elder Quest) | `+15% Gold Gain` | "La riqueza es no necesitar nada." |
| **MONARCA** | **Nivel 100** | `+20% ALL STATS` | **"El Rey ha llegado."** |

> *Colecciónalos todos. Equipa solo los necesarios.*

---

## 🖥️ La Experiencia de Escritorio (Technical Vision)

Life Leveling no es una web efímera. Es una **aplicación de escritorio nativa** diseñada para ser tu compañero diario, siempre abierta en tu segundo monitor, visualizando tu progreso en tiempo real.

### ⚛️ Tecnología & Stack
Hemos elegido un stack tecnológico moderno, robusto y privado. Sin servidores en la nube, sin suscripciones. Tus datos son tuyos.

* **Core:** Java 21 (LTS) — Lógica de negocio pura, inmutable y de alto rendimiento.
* **UI Engine:** JavaFX Moderno — Una interfaz fluida, reactiva y elegante con modo oscuro nativo.
* **Persistence:** JSON Storage (Jackson) — Sistema de guardado local ("Local-First"). Tus partidas son archivos portables, legibles y editables.
* **Quality:** JUnit 5 + Mockito — Una suite completa.

---

## 🏛️ Ingeniería de Software (Under the Hood)

Detrás de la interfaz bonita hay una arquitectura de grado empresarial diseñada para durar décadas.

### 1. Hexagonal Architecture (Ports & Adapters)
El proyecto sigue estrictamente la arquitectura hexagonal.
* **El Núcleo (Domain):** Es agnóstico. No sabe que existe JavaFX ni JSON. Solo contiene las reglas de tu vida (HP, XP, Burnout).
* **La Ventaja:** Si mañana queremos cambiar la interfaz a Web o Móvil, el 100% de la lógica de negocio se reutiliza sin tocar una sola línea de código.

### 2. Domain-Driven Design (DDD)
No hay "Anemic Models". Aquí los objetos tienen alma.
* Un `Player` no es una bolsa de datos; es una entidad que sabe curarse, entrenar y sufrir.
* El sistema de **Time-Tracking** protege tu integridad: no puedes registrar 25 horas en un día.
* El sistema monetario (`Wallet`) es transaccional y seguro.

### 3. Fiabilidad Matemática
Con casi **1.000 tests unitarios y de integración**, cada cálculo de XP, cada céntimo de oro y cada punto de HP está verificado.
* *¿Burnout al llegar a 0 HP?* Testado.
* *¿Cálculo de intereses compuestos?* Testado.
* *¿Reset de misiones a las 00:00?* Testado.

---

## 🚀 Cómo empezar (Developer Mode)

El proyecto está diseñado para ser clonado y ejecutado localmente.

### Requisitos
* JDK 21+
* Maven 3.8+

### Instalación
```bash
# 1. Clona el repositorio
git clone [https://github.com/JosseGonnza/life-leveling.git](https://github.com/JosseGonnza/life-leveling.git)

# 2. Construye el Núcleo y lanza los Tests
mvn clean install

# 3. Inicia la App de Escritorio (Launcher)
mvn javafx:run
```

---

🔮 El Futuro del Proyecto

Life Leveling está diseñado para evolucionar contigo. Estas son las capacidades nativas que definen la experiencia final:

    [x] Logic Core: Motor matemático de RPG completo (Stats, HP, Economía).

    [x] Quest Engine: Sistema de Misiones Diarias, Semanales y Rangos.

    [ ] Local Persistence: Guardado automático en savegame.json.

    [ ] Visual Dashboard: HUD en tiempo real con barras de HP/XP animadas (JavaFX).

    [ ] Soundscapes: Efectos de sonido para Level Ups y Completar Tareas (Feedback auditivo).

"Tu vida es el único RPG que no permite re-roll. Juégalo bien."

![Author](https://img.shields.io/badge/Author-Jose%20Gonnza-purple)

<img src="https://raw.githubusercontent.com/matfantinel/matfantinel/master/waves.svg" width="100%" height="100">
