<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=9df2ea&animation=fadeIn&height=120&section=header"/>

# 🎮 Life Leveling

> **Transforma tu vida real en una aventura RPG. Rastrea hábitos, completa misiones, gestiona el burnout y sube de nivel mientras construyes la vida que deseas.**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Arquitectura](https://img.shields.io/badge/Arquitectura-DDD%20%2B%20Hexagonal-blue.svg)](https://github.com/yourusername/life-leveling-v2)
![Maven](https://img.shields.io/badge/Maven-Building-C71A36?logo=apachemaven)
[![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-yellow.svg)]()

---

## 📖 **¿Qué es Life Leveling?**

Life Leveling es un **sistema RPG de vida real** donde gestionas tus hábitos, tareas y crecimiento profesional como si fueras un personaje de videojuego. Inspirado en *Solo Leveling* y *Skyrim*, este sistema gamifica tu rutina diaria con:

- **❤️ HP (Energía Mental):** Gestiona el burnout y tus niveles de energía
- **📊 5 Stats:** Fuerza, Inteligencia, Disciplina, Carisma, Sabiduría
- **⚔️ Sistema de Misiones:** Hábitos diarios, tareas personalizadas y desafíos de rango
- **💰 Economía:** Gana Oro, compra objetos, desbloquea equipo
- **👑 Títulos y Logros:** Desbloquea buffs completando hitos
- **🔥 Mecánicas de Burnout:** Sobrevive o sufre las consecuencias
- **🎯 Progresión de Rangos:** Escala desde E (Recluta) hasta S++ (Monarca)

---

## 🎯 **Características Principales**

### 🧬 **Sistema del Jugador**

#### ❤️ **Gestión de HP (No es salud, es ENERGÍA)**
- **HP Máximo:** 100 puntos (Hard Cap)
- **Estados:**
    - **HEALTHY (50-100 HP):** 100% de ganancia de XP y Oro
    - **TIRED (1-49 HP):** 50% de ganancia de XP, prohibido iniciar misiones Rango B+
    - **CRITICAL (0 HP):** **BURNOUT** - Bloqueo de 24h + Pérdida del 10% de Oro

#### 📈 **Sistema Dual de XP (Ratio 1:1)**
- **XP de Stats:** Experiencia específica de atributos (Fuerza, Inteligencia, etc.)
- **XP General:** Vinculada 1:1 con Stats XP. Ganar 10 STR XP otorga automáticamente 10 XP General
- **Buffs:** Los multiplicadores se aplican ANTES de distribuirse a ambas pools
- **Nivel Máximo:** Stats cap en 100, Nivel General cap en 100

#### 🔄 **Convertidores de Recursos (Clases Pasivas)**
Siempre activos, no requieren equipamiento:
- **Motor de Trabajo (Empleo):** Convierte horas trabajadas en Oro (31.25 G/hora)
- **Motor de Carrera (Programar):** Convierte minutos de código en INT XP
    - Minutos 0-120: 1 XP/min (100%)
    - Minutos 121+: 0.5 XP/min (50%) - *Diminishing Returns*

---

### ⚔️ **Sistema de Misiones**

#### 🔁 **Misiones Diarias (Los 7 Hábitos)**
Reseteo automático a las 00:00. Tracking individual de rachas.

| Hábito | Input | Condición | Recompensa | Efecto Extra |
|--------|-------|-----------|------------|--------------|
| 💤 **Descanso (+7h)** | Horas dormidas | ≥ 7 | +70 XP | +15 HP |
| 🥗 **Dieta Limpia** | Checkbox | ✓ | +50 XP | Mantiene HP |
| 🏋️ **Deporte** | Checkbox | ✓ | +50 XP | - |
| 💻 **Sesión Código** | Minutos | > 0 | +1 XP/min | Ver Motor Carrera |
| 📚 **Leer (10p)** | Páginas | ≥ 10 | +5 XP/pág | - |
| ✨ **Skincare** | Checkbox | ✓ | +50 XP | - |
| 🧹 **Orden (10m)** | Checkbox | ✓ | +50 XP | Evita debuff |

**✨ BONUS DÍA PERFECTO:**
- **Condición:** Completar 7/7 hábitos
- **Recompensa:** +100 Oro + HP al 100% + Racha +1

#### 📝 **Misiones de Usuario (Tareas Personalizadas)**
- Crea tareas con título, rango y fecha límite
- **Penalización por fallo:** Daño a HP basado en rango (E=0, D=-5, C=-15, B=-20, A=-30, S=-50)
- Si HP=0: El daño se convierte en pérdida de Oro (1 HP = 10 G)

#### 🚪 **Misiones de Sistema (The Gates)**
10 desafíos predefinidos para subir de rango:

1. **E→D:** "La Semana del Infierno" (Nvl 10) - 7 días perfectos consecutivos
2. **D→C:** "El Primer Encargo" (Nvl 25) - 3 User Quests + 20h código
3. **C→B (Fase 1):** "Biblioteca de Babel" (Nvl 35) - Curso Rango B + 100 páginas
4. **C→B (Fase 2):** "Hackathon Personal" (Nvl 35) - Proyecto en 7 días sin burnout
5. **Gate Económica:** "Capital Semilla" (Nvl 40) - Acumular 20,000 G
6. **B→A:** "Cacería de Empleo" (Nvl 50) - Portfolio + 10 aplicaciones
7. **A→S:** "Junior Developer" (Nvl 60) - Conseguir nuevo trabajo
8. **S→S+:** "Territorio Propio" (Nvl 75) - Independencia financiera
9. **Redención:** "Salir del Abismo" (Trigger: 3 burnouts/mes) - 14 días HP>80
10. **S++:** "Monarca de las Sombras" (Nvl 100) - Endgame total

---

### 💰 **Economía y Tienda**

#### 💊 **Consumibles**
- **Poción HP (Café):** 150 G → +15 HP
- **Café Premium:** 300 G → Elimina debuff Fatiga por 4h
- **Netflix/Spotify:** 500-1,000 G → Entretenimiento mensual
- **Agente IA:** 2,000 G → +10% velocidad en Code Quests
- **Festín Trampa (Glovo):** 2,500 G → +60 HP instantáneo / **-20 HP al día siguiente**
- **Inyección Adrenalina:** 5,000 G → **CURA BURNOUT** (HP=1, desbloquea misiones)

#### 🛡️ **Equipamiento (Sistema de Slots)**
Un objeto por slot lógico. Buffs permanentes una vez comprados.

| Item | Slot | Precio | Buff | Requisito |
|------|------|--------|------|-----------|
| Teclado Mecánico | Periph | 10,000 G | +5% INT XP | - |
| Monitor Secundario | Desk | 15,000 G | +10% INT XP | - |
| Silla Ergonómica | Chair | 20,000 G | Reduce daño HP | - |
| Kindle | Hand | 12,000 G | +10% WIS XP | - |
| **Set Ropa Deporte** 🔒 | Body | 5,000 G | +5% STR XP | Título: *Lobo Solitario* |
| **Almohada Visco** 🔒 | Bed | 3,000 G | +5% Sleep Recovery | Título: *Guardián Sueño* |
| **Smartwatch** 🔒 | Wrist | 25,000 G | +2% General XP | Título: *Caminante Hierro* |

#### 💎 **Tesoros (Metas a Largo Plazo)**
- Setup Dev Pro: 40,000 G
- Viaje Épico: 300,000 G
- Coche Nuevo: 500,000 G
- Libertad Financiera: 1,000,000 G

---

### 👑 **Títulos y Logros**

**Sistema:** Equipable (1 slot activo). Solo el título equipado otorga su buff.
**Segundo slot:** Se desbloquea al alcanzar Nivel 50.

#### 🔥 **Por Hábitos (15 Títulos Total)**
1. **Lobo Solitario:** 30 días en Gym → +5% STR XP
2. **Guardián del Sueño:** 14 días racha Sueño → +10% HP Recovery
3. **Templo Sagrado:** 20 días racha Dieta → +5 Max HP
4. **Narciso:** 50 veces Skincare → +5% CHA XP
5. **Caminante de Hierro:** 7 Perfect Days consecutivos → +5% General XP

#### 🧠 **Por Carrera**
6. **Code Monkey:** 100 horas código → +10% INT XP
7. **Ratón de Biblioteca:** 300 páginas leídas → +5% WIS XP
8. **Hello World:** Primer día programando → +5% INT XP
9. **Minimalista:** 30 días ordenando → +5% WIS XP

#### ⚡ **Por Maestría (Stat Nivel 50)**
10. **Titán:** STR 50 → +5% STR XP
11. **Cyborg:** INT 50 → +5% INT XP
12. **Oráculo:** WIS 50 → +5% WIS XP
13. **General:** DIS 50 → +5% DIS XP
14. **Estrella:** CHA 50 → +5% CHA XP

#### 💰 **Meta-Juego**
15. **El Ahorrador:** 10,000 G acumulados → +5% Gold Gain
16. **Fénix:** Salir de Burnout → +5% STR XP
17. **Monarca de las Sombras:** Nivel 100 → +10% TODAS las Stats

---

### 👾 **Bestiario (Debuffs y Monstruos)**

Tu verdadero enemigo eres tú mismo:

1. **🧛 Vampiro de Sueño (Fatiga)**
- **Trigger:** Dormir < 6 horas
- **Efecto:** -20 HP + 50% XP al día siguiente
- **Cura:** Dormir +7h o Café Premium

2. **🍽️ Montaña de Platos (Caos)**
- **Trigger:** 3 días sin ordenar
- **Efecto:** -20% WIS XP (Persistente)
- **Cura:** Completar `DQ_TIDY`

3. **📱 Doomscrolling (Ladrón de Tiempo)**
- **Trigger:** Manual (Confesión)
- **Efecto:** -100 G por cada 30 min reportados
- **Filosofía:** "El tiempo es oro, literalmente"

4. **🍔 Comida Basura (Veneno)**
- **Trigger:** Manual o fallar `DQ_DIET`
- **Efecto:** -10 HP + Debuff "Pesadez" (-5% STR por 24h)

---

## 🏗️ **Arquitectura del Sistema**

Este proyecto sigue los principios de **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal**:

```
📦 com.lifeleveling
├── 📂 domain (Lógica de negocio pura - SIN dependencias externas)
│   ├── player/
│   │   ├── Player.java (Aggregate Root)
│   │   ├── Stats.java (Value Object)
│   │   ├── HPState.java (Enum: HEALTHY/TIRED/CRITICAL)
│   │   ├── Wallet.java (Value Object)
│   │   └── BurnoutLock.java (Entity)
│   ├── quest/
│   │   ├── Quest.java (Sealed Interface)
│   │   ├── DailyQuest.java
│   │   ├── UserQuest.java
│   │   ├── SystemQuest.java
│   │   └── QuestRank.java (Enum)
│   ├── economy/
│   │   ├── ShopItem.java (Sealed Interface)
│   │   ├── Consumable.java
│   │   ├── Equipment.java
│   │   └── ItemSlot.java (Enum)
│   ├── achievement/
│   │   ├── Title.java (Record)
│   │   └── TitleCondition.java (Sealed)
│   └── debuff/
│       ├── Debuff.java (Sealed Interface)
│       └── DebuffType.java (Enum)
├── 📂 application (Casos de Uso)
│   ├── CompleteQuestUseCase.java
│   ├── BuyItemUseCase.java
│   ├── ApplyDebuffUseCase.java
│   └── CheckBurnoutUseCase.java
├── 📂 infrastructure (Persistencia & Configuración)
│   ├── persistence/
│   │   ├── PlayerRepository.java (Interface)
│   │   ├── JsonPlayerRepository.java
│   │   └── JsonFileHandler.java
│   └── config/
│       ├── titles.json
│       ├── shop_items.json
│       ├── system_quests.json
│       └── daily_quests.json
└── 📂 ui (Interfaces)
    └── cli/ (MVP - Línea de comandos)
```

### **Stack Tecnológico**
- **Java 21** (Records, Sealed Classes, Pattern Matching)
- **Persistencia:** JSON (MVP) → PostgreSQL (futuro)
- **Estrategia de Guardado:** Repository Pattern + Snapshots Diarios
- **Sistema de Eventos:** Domain Events para tracking de logros

---

## 📊 **Fórmulas y Mecánicas Clave**

### **Curva de Experiencia**

#### **Nivel General (Player Level)**
- **Nivel 1-5 (Curva Manual):**
    - Nvl 1: 0 XP
    - Nvl 2: 300 XP
    - Nvl 3: 800 XP
    - Nvl 4: 1,500 XP
    - Nvl 5: 2,500 XP

- **Nivel 6-100 (Fórmula Cuadrática):**
  ```
  XP_Total_Requerida = 250 × (Nivel²)
  ```
    - Nvl 10: 25,000 XP
    - Nvl 50: 625,000 XP
    - Nvl 100: 2,500,000 XP

#### **Stats Individuales**
```
Coste_Nivel_N = N × 100
```
- Fuerza 1→2: 100 XP
- Fuerza 49→50: 4,900 XP
- **Total para maxear 1 Stat:** 495,000 XP
- **Total 5 Stats:** 2,475,000 XP

### **Tabla de Rangos y Recompensas**

| Rango | Dificultad | Tiempo Aprox | XP | Oro | Ejemplos            |
|-------|------------|--------------|----|----|---------------------|
| **E** | Rutinaria | 5-15 min | 10 | 15 G | Hacer cama, fregar  |
| **D** | Común | 30-60 min | 50 | 50 G | Gym, cocinar        |
| **C** | Rara | 1-2 horas | 150 | 150 G | Sesión estudio      |
| **B** | Élite | 3-5 horas | 500 | 400 G | Proyecto personal   |
| **A** | Heroica | Días/Hitos | 1,500 | 2,000 G | Terminar curso      |
| **S** | Legendaria | Meses | 5,000 | 10,000 G | Nuevo trabajo       |
| **S+** | Mítica | 1-2 años | 20,000 | 50,000 G | Ascenso       |
| **S++** | Divina | Endgame | 100,000 | 500,000 G | Libertad financiera |

---

## 🚀 **Empezar a Jugar**

### **Requisitos**
- Java 21 o superior
- Maven 3.8+

### **Instalación**
```bash
# Clonar el repositorio
git clone https://github.com/nanana/life-leveling.git
cd life-leveling-v2

# Compilar el proyecto
mvn clean install

# Ejecutar (CLI MVP)
java -jar target/life-leveling.jar
```

### **Configuración Inicial**
Al primer arranque, el sistema creará:
```
~/.lifeleveling/
├── player.json          # Tu personaje
├── questlog.json        # Misiones activas
├── history/             # Snapshots diarios
│   └── player_2024-01-24.json
└── config/              # Configuración del juego
    ├── titles.json
    ├── shop_items.json
    ├── system_quests.json
    └── daily_quests.json
```

---

## 🎮 **Filosofía de Diseño**

Life Leveling está inspirado en:
- **Solo Leveling:** Sistema de progresión oculto, puertas de rango, el grind hacia el poder
- **Skyrim:** "Lo que haces te hace mejor en ello" (crecimiento orgánico de habilidades)
- **Economía RPG:** Escasez de oro en early game, abundancia en late game
- **Prevención de Burnout:** El sistema de HP te obliga a descansar o sufrir penalizaciones

El objetivo es hacer que la productividad en la vida real sea **adictiva** sin promover hábitos poco saludables.

---

## 🗺️ **Roadmap**

- [x] Master Design Document
- [x] Manual del Jugador
- [ ] **Fase 1:** Domain Model (Entidades, Value Objects, Agregados)
- [ ] **Fase 2:** Diagramas Mermaid (Class, State, Sequence)
- [ ] **Fase 3:** Configuración JSON (Títulos, Items, Quests)
- [ ] **Fase 4:** Use Cases & Application Layer
- [ ] **Fase 5:** Infraestructura (JSON Repository)
- [ ] **Fase 6:** Interfaz CLI (MVP)
- [ ] **Fase 7:** Interfaz Desktop (JavaFX)
- [ ] **Fase 8:** Migración a PostgreSQL
- [ ] **Fase 9:** Analytics & Gráficos (Tracking histórico)
- [ ] **Fase 10:** App Móvil (Futuro)

---

## 🎯 **Mensajes del Sistema**

> *"El Sistema te ha elegido. No me decepciones."*

> *"Hasta ahora has vivido en Modo Automático. Eso se acabó."*

> *"Tu objetivo: Convertirte en el Monarca de las Sombras (y no morir de Burnout en el intento)."*

---



![Author](https://img.shields.io/badge/Author-Jose%20Gonnza-purple)
```
    ⚔️ LIFE LEVELING v2.0 ⚔️
    
    ESTADO: 🟢 SYSTEM ONLINE
    JUGADOR: [Tu Nombre]
    RANGO: E (Recluta)
    
    "La vida es un RPG. Disfrútalo."
```

<img src="https://raw.githubusercontent.com/matfantinel/matfantinel/master/waves.svg" width="100%" height="100">
