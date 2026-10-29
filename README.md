# Pomodoro-óra Swingben (MVC + Observer)

Asztali Pomodoro-időzítő Java Swingben: 25 perc munka, 5 perc rövid szünet,
minden 4. kör után 15 perc hosszú szünet. Az idők a Beállítások ablakban
módosíthatók.

## Futtatás

Követelmény: JDK 21 és Maven (a NetBeans tartalmazza).

**NetBeansben:** File → Open Project → `Pomodoroora`, majd **Run Project (F6)**.
A fő osztály: `pomodor.Main`.

**Parancssorból** (a projekt mappájában):

```
mvn package
mvn exec:java -Dexec.mainClass=pomodor.Main
```

**Tesztek:** NetBeansben jobb klikk a projekten → **Test** (Alt+F6), vagy `mvn test`.

## Szerkezet

```
src/main/java/pomodor/
├── Main.java                    – elindítja az MVC-t az EDT-n (invokeLater)
├── model/                       – nincs benne javax.swing import
│   ├── Allapot.java             – MUNKA, ROVID_SZUNET, HOSSZU_SZUNET, SZUNETEL
│   ├── PomodoroFigyelo.java     – saját Observer interfész
│   └── PomodoroModel.java       – állapotgép + figyelőlista
├── view/
│   ├── PomodoroView.java        – JFrame, PomodoroFigyelo-t implementál
│   └── BeallitasokDialog.java   – modális JDialog az időkhöz
└── controller/
    └── PomodoroController.java  – ActionListenerek + javax.swing.Timer
src/test/java/pomodor/model/
└── PomodoroModelTest.java       – 10 JUnit 5 teszt, UI nélkül
docs/
├── osztalydiagram.puml / .png   – UML osztálydiagram
└── screenshots/                 – képernyőképek
```

## Működés röviden

- **Modell:** a `PomodoroModel` tárolja az állapotot, a hátralévő másodperceket
  és a befejezett körök számát. Nem tud semmit a Swingről. A `tick()` metódust
  kívülről kell másodpercenként hívni.
- **Állapot és fázis:** a `SZUNETEL` csak azt jelenti, hogy meg van állítva az
  óra. A `fazis` mező megjegyzi, hol tartottunk, így a Start ott folytatja.
- **Observer:** a nézet a `feliratkoz()` metódussal iratkozik fel. A modell
  minden tikkeléskor meghívja a `tikk()`, állapotváltáskor az
  `allapotValtozott()` metódust minden figyelőn.
- **Controller:** a gombok listenerei itt vannak, és csak a modell metódusait
  hívják. Az időzítést `javax.swing.Timer` végzi, nincs `Thread.sleep`.
- **Beállítások:** a modális `BeallitasokDialog` bezárása után a controller
  meghívja a `model.setIdotartamok(...)` metódust.
- **Extra:** fázisváltáskor hangjelzés szól (`Toolkit.getDefaultToolkit().beep()`).

## SOLID

- **S:** a modell, a nézet és a controller mindegyike egy feladatért felel.
- **O:** új figyelő (pl. statisztika) a modell módosítása nélkül hozzáadható.
- **L:** bármelyik `PomodoroFigyelo` használható, a tesztek egy hamis figyelővel futnak.
- **I:** a `PomodoroFigyelo` interfész csak két metódusból áll.
- **D:** a modell az interfésztől függ, nem a konkrét nézettől.

## UML

![Osztálydiagram](docs/osztalydiagram.png)

## Képernyőképek

| Munka | Szünet | Beállítások |
|---|---|---|
| ![](docs/screenshots/munka.png) | ![](docs/screenshots/szunet.png) | ![](docs/screenshots/beallitasok.png) |
