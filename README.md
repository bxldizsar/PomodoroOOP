# Pomodoro-óra Swingben (MVC + Observer)

Bónuszfeladat **b-02**. Asztali Pomodoro-időzítő Java Swingben:
25 perc munka, 5 perc rövid szünet, és minden 4. kör után 15 perc hosszú
szünet. Az idők egy Beállítások ablakban módosíthatók.

A fő cél a **tiszta Model–View–Controller (MVC)** szétválasztás és az
**Observer minta** saját kezű megvalósítása: a logika (modell) semmit sem tud
a gombokról és a címkékről, és a Swing nélkül, JUnit tesztekkel is
ellenőrizhető.

---

## Tartalom

1. [Futtatás](#futtatás)
2. [Használat](#használat)
3. [A projekt szerkezete](#a-projekt-szerkezete)
4. [Az MVC felosztás](#az-mvc-felosztás)
5. [Az osztályok részletesen](#az-osztályok-részletesen)
6. [Az Observer minta](#az-observer-minta)
7. [Állapotgép](#állapotgép)
8. [EDT-szabályok](#edt-szabályok)
9. [Tesztek](#tesztek)
10. [UML](#uml)
11. [SOLID](#solid)
12. [Követelmények teljesítése](#követelmények-teljesítése)

---

## Futtatás

Követelmény: **JDK 21** és **Maven** (a NetBeans tartalmazza).

**NetBeansben:** File → Open Project → `Pomodoroora`, majd **Run Project (F6)**.
A fő osztály: `pomodoro.main.Main` (a `pom.xml`-ben az `exec.mainClass`).

**Parancssorból** (a projekt mappájában):

```
mvn package
mvn exec:java -Dexec.mainClass=pomodoro.main.Main
```

**Tesztek:** jobb klikk a projekten → **Test** (Alt+F6), vagy `mvn test`.
10 teszt fut, mind zöld, és egyik sem nyit ablakot.

---

## Használat

| Gomb | Mit csinál |
|---|---|
| **Start** | elindítja (vagy folytatja) az órát |
| **Szünet** | megállítja az órát, a hátralévő idő megmarad |
| **Reset** | mindent alaphelyzetbe állít: munka fázis, 25:00, 0 kör |
| **Beállítások…** | modális ablak, ahol percben megadható a munka, a rövid és a hosszú szünet hossza |

A háttérszín jelzi a fázist: **piros** = munka, **zöld** = rövid szünet,
**kék** = hosszú szünet, **szürke** = szüneteltetve. Fázisváltáskor
hangjelzés szól. Az ablakon látszik a befejezett pomodorók száma és hogy
hányadik körnél tartunk a négyből.

---

## A projekt szerkezete

```
PomodoroOOP/
├── pom.xml                          – JDK 21, JUnit 5
├── docs/
│   └── osztalydiagram.puml          – UML osztálydiagram (PlantUML)
└── src/
    ├── main/java/
    │   ├── pomodor/model/           – a modell, nincs benne javax.swing import
    │   │   ├── Allapot.java         – MUNKA, ROVID_SZUNET, HOSSZU_SZUNET, SZUNETEL
    │   │   ├── PomodoroFigyelo.java – saját Observer interfész
    │   │   └── PomodoroModel.java   – állapotgép + figyelőlista
    │   └── pomodoro/
    │       ├── view/
    │       │   ├── PomodoroView.java      – JFrame, PomodoroFigyelo-t implementál
    │       │   └── BeallitasokDialog.java – modális JDialog az időkhöz
    │       ├── controller/
    │       │   └── PomodoroController.java – ActionListenerek + javax.swing.Timer
    │       └── main/
    │           └── Main.java        – elindítja az MVC-t az EDT-n
    └── test/java/pomodor/model/
        └── PomodoroModelTest.java   – 10 JUnit 5 teszt, UI nélkül
```

A modell a `pomodor.model` csomagban van, a nézet, a controller és a `Main`
a `pomodoro.view`, `pomodoro.controller` és `pomodoro.main` csomagban.
A modell csomagja egyik másikat sem importálja, a függés csak egy irányba
megy (view/controller → model).

---

## Az MVC felosztás

```
   ┌──────────────┐  gombnyomás (ActionListener)   ┌────────────────────┐
   │ PomodoroView │ ─────────────────────────────► │ PomodoroController │
   │   (JFrame)   │                                │  + javax.swing.Timer│
   └──────▲───────┘                                └─────────┬──────────┘
          │ tikk() / allapotValtozott()                      │ start() / szunet()
          │ (Observer értesítés)                             │ reset() / tick()
          │                                                  ▼ setIdotartamok()
          │                                       ┌────────────────────┐
          └────────────────────────────────────── │   PomodoroModel    │
                                                  │  (nincs Swing!)    │
                                                  └────────────────────┘
```

- **Model:** tárolja az állapotot és a szabályokat. Nem tudja, ki figyeli.
- **View:** megjeleníti a modell állapotát, és feliratkozik a változásokra.
  Nem dönt semmiről.
- **Controller:** a gombnyomásokat a modell metódushívásaira fordítja, és ő
  "üti" az órát másodpercenként.

A modell csak a saját `PomodoroFigyelo` interfészét ismeri, a konkrét nézetet
nem. Ezért lehet a teszteket egy hamis figyelővel, ablak nélkül futtatni.

---

## Az osztályok részletesen

### `Allapot` (enum) – `model`

`MUNKA`, `ROVID_SZUNET`, `HOSSZU_SZUNET`, `SZUNETEL`.
A `SZUNETEL` azt jelenti, hogy az óra meg van állítva (a felhasználó nyomta
meg a Szünet gombot, vagy még el sem indult).

### `PomodoroFigyelo` (interfész) – `model`

```java
void tikk(int hatralevoMasodperc);
void allapotValtozott(Allapot uj);
```

A saját Observer interfész. Aki a modell változásairól tudni akar, ezt
implementálja.

### `PomodoroModel` – `model`

Mezők:

| Mező | Mire való |
|---|---|
| `allapot` | az aktuális állapot (fut-e, és ha igen, melyik fázis) |
| `fazis` | melyik fázisban tartunk (`MUNKA` / `ROVID_SZUNET` / `HOSSZU_SZUNET`), szüneteltetés alatt is megmarad |
| `hatralevoMasodperc` | a fázisból hátralévő idő |
| `befejezettKorok` | hány munkakör ért véget |
| `figyelok` | a feliratkozott figyelők listája |
| `munkaMasodperc`, `rovidSzunetMasodperc`, `hosszuSzunetMasodperc` | a fázisok hossza (alapból 1500 / 300 / 900) |

Metódusok:

- **`feliratkoz(PomodoroFigyelo f)`** – hozzáadja a figyelőt a listához.
- **`start()`** – ha az óra áll, az `allapot` visszakapja a `fazis` értékét
  (tehát ott folytatja, ahol abbahagyta), és értesíti a figyelőket.
  Ha már fut, nem csinál semmit.
- **`szunet()`** – ha fut, `SZUNETEL`-re vált és értesít.
- **`reset()`** – `SZUNETEL`, `MUNKA` fázis, teljes munkaidő, 0 kör, és
  értesít (állapot + tikk, hogy a kijelző is frissüljön).
- **`tick()`** – "eltelt egy másodperc". Ha áll az óra, nem csinál semmit.
  Különben csökkenti az időt, és ha elérte a 0-t, átvált a következő fázisra.
  Végül értesíti a figyelőket a hátralévő időről.
- **`kovetkezoFazis()`** (privát) – munka után növeli a körszámlálót; ha az
  osztható 4-gyel, hosszú szünet jön, különben rövid. Szünet után újra munka.
  Az új fázis azonnal indul.
- **`setIdotartamok(munka, rovid, hosszu)`** – beállítja az új hosszakat
  másodpercben, és `reset()`-et hív.
- Getterek: `getAllapot()`, `getFazis()`, `getHatralevoMasodperc()`,
  `getBefejezettKorok()`, és a három időtartam.

A modell **nem importál semmit a `javax.swing` csomagból**, és nem is
időzít: a `tick()`-et kívülről kell hívni. Így a tesztben egy ciklussal
"percek telhetnek el" egy szempillantás alatt.

### `PomodoroView` – `view`

- `JFrame`, ami **implementálja a `PomodoroFigyelo`-t**, és a konstruktor
  végén feliratkozik a modellre.
- Nagy `MM:SS` kijelző (96 pontos, monospace), állapotfelirat, körszámláló,
  és négy gomb: Start, Szünet, Reset, Beállítások.
- **`tikk()`** – frissíti a kijelzőt.
- **`allapotValtozott()`** – frissíti a feliratot, a háttérszínt és a gombok
  engedélyezését (Start csak állva, Szünet csak futás közben aktív), és ha
  valódi fázisváltás történt, hangjelzést ad
  (`Toolkit.getDefaultToolkit().beep()`).
- **`addStartListener`, `addSzunetListener`, `addResetListener`,
  `addBeallitasokListener`** – ezeken át kapja meg a controller listenereit.
  A nézet maga nem tartalmaz logikát a gombokhoz.
- Minden UI-frissítés az `edtn(...)` segédmetóduson megy át (lásd
  [EDT-szabályok](#edt-szabályok)).

### `BeallitasokDialog` – `view`

- **Modális `JDialog`** (`super(szulo, "Beállítások", true)`): amíg nyitva
  van, a főablak nem kezelhető, és a `setVisible(true)` hívás addig vár,
  amíg be nem zárják.
- Három `JSpinner` percben: munka (1–180), rövid szünet (1–60), hosszú
  szünet (1–120).
- **OK** → `jovahagyva = true`, bezár; **Mégse** → csak bezár.
- Getterek: `isJovahagyva()`, `getMunkaPerc()`, `getRovidSzunetPerc()`,
  `getHosszuSzunetPerc()`.

### `PomodoroController` – `controller`

- Megkapja a modellt és a nézetet.
- Létrehoz egy **`javax.swing.Timer`**-t 1000 ms-mal, ami másodpercenként
  `model.tick()`-et hív. Nincs `Thread.sleep`.
- A gombok listenerei itt vannak, és **csak a modell metódusait hívják**:
  - Start → `model.start()` + `idozito.start()`
  - Szünet → `model.szunet()` + `idozito.stop()`
  - Reset → `idozito.stop()` + `model.reset()`
  - Beállítások → megnyitja a dialógust a jelenlegi értékekkel (percben).
    Ha OK-val zárták, megállítja az időzítőt, és meghívja a
    `model.setIdotartamok(...)`-t másodpercre átváltva.

### `Main`

`SwingUtilities.invokeLater`-rel az EDT-n hozza létre a modellt, a nézetet
és a controllert, majd megjeleníti az ablakot.

---

## Az Observer minta

| Szerep | Osztály |
|---|---|
| Subject (megfigyelt) | `PomodoroModel` – `figyelok` lista, `feliratkoz()`, `ertesitTikk()`, `ertesitAllapot()` |
| Observer interfész | `PomodoroFigyelo` |
| Konkrét observer | `PomodoroView` (a tesztekben egy `TesztFigyelo`) |

Folyamat: a modell állapota megváltozik → végigmegy a `figyelok` listán →
mindegyiken meghívja a `tikk()` vagy `allapotValtozott()` metódust. A modell
nem tudja, hogy egy ablak, egy teszt vagy például egy statisztika-gyűjtő
figyeli. Új figyelőhöz a modellt nem kell módosítani.

---

## Állapotgép

```
              start()                 idő lejárt
 SZUNETEL ───────────────► MUNKA ───────────────────► ROVID_SZUNET  (1., 2., 3. kör után)
    ▲  ▲                     │  ▲                         │
    │  │      szunet()       │  │       idő lejárt        │
    │  └─────────────────────┘  └─────────────────────────┘
    │                           │
    │ reset() bárhonnan         │ idő lejárt, 4. kör után
    │                           ▼
    │                     HOSSZU_SZUNET ── idő lejárt ──► MUNKA
```

- A `szunet()` bármelyik futó fázisból `SZUNETEL`-be visz, a `start()` a
  megjegyzett `fazis`-ba hoz vissza.
- A `reset()` mindig `SZUNETEL` + `MUNKA` fázis + 0 kör.

---

## EDT-szabályok

- Az ablak létrehozása `SwingUtilities.invokeLater`-rel, az EDT-n történik
  (`Main`).
- Az időzítést **`javax.swing.Timer`** végzi, ami az EDT-n hívja a
  `tick()`-et. Nincs `Thread.sleep` az EDT-n, így a felület nem fagy le.
- A nézet `edtn()` segédmetódusa: ha már az EDT-n vagyunk, rögtön lefut, ha
  nem, `invokeLater`-rel ütemezi. Így akkor is biztonságos lenne, ha a modellt
  egyszer egy másik szálból hívnák.

---

## Tesztek

`PomodoroModelTest`, 10 JUnit 5 teszt, **UI nélkül**: csak a modellt
használják, a figyelést egy `TesztFigyelo` belső osztály naplózza.

| Teszt | Mit ellenőriz |
|---|---|
| `kezdetbenSzunetel` | induláskor `SZUNETEL`, `MUNKA` fázis, 1500 mp, 0 kör |
| `startUtanMunka` | `start()` után `MUNKA` |
| `tickCsokkentiAzIdot` | egy `tick()` után 1499 mp |
| `szunetMegallitjaAzOrat` | szünetben a `tick()` nem csökkenti az időt |
| `szunetUtanStartOttFolytatja` | rövid szünet közbeni szünet után a Start a rövid szünetet folytatja |
| `munkaUtanRovidSzunet` | munka végén rövid szünet, 300 mp, 1 kör |
| `rovidSzunetUtanMunka` | rövid szünet végén újra munka, 1500 mp |
| `negyedikKorUtanHosszuSzunet` | a 4. munkakör után `HOSSZU_SZUNET`, 900 mp |
| `resetMindentAlaphelyzetbeAllit` | a `reset()` mindent visszaállít |
| `figyeloErtesitestKap` | a feliratkozott figyelő megkapja az állapotváltást és a tikket |

---

## UML

Az osztálydiagram PlantUML forrása: [`docs/osztalydiagram.puml`](docs/osztalydiagram.puml).
Kirajzolni a [PlantUML online szerkesztővel](https://www.plantuml.com/plantuml/)
vagy a NetBeans/VS Code PlantUML bővítményével lehet.

A diagramon a `PomodoroModel` `<<Subject>>`, a `PomodoroFigyelo`
`<<Observer>>` sztereotípiát kapott, és látszik, hogy a `PomodoroView`
implementálja a figyelőt, a modell pedig csak az interfészt ismeri.

---

## SOLID

- **S:** a modell, a nézet és a controller mindegyike egy feladatért felel; a
  beállítások ablak külön osztály.
- **O:** új figyelő (pl. statisztika, tálcaikon) a modell módosítása nélkül
  hozzáadható.
- **L:** bármelyik `PomodoroFigyelo` használható, a tesztek egy hamis
  figyelővel futnak.
- **I:** a `PomodoroFigyelo` interfész csak két metódusból áll.
- **D:** a modell az interfésztől függ, nem a konkrét nézettől.

---

## Követelmények teljesítése

| Követelmény | Hol |
|---|---|
| `PomodoroModel`: állapot, hátralévő mp, befejezett körök, nincs Swing import | `pomodor.model.PomodoroModel` |
| Saját `PomodoroFigyelo` Observer, értesítés tikkeléskor és állapotváltáskor | `PomodoroFigyelo`, `ertesitTikk()`, `ertesitAllapot()` |
| `PomodoroView` (JFrame): MM:SS, állapotfelirat, Start/Szünet/Reset, színváltás | `pomodoro.view.PomodoroView` |
| Listenerek a controllerben, csak modellhívások | `pomodoro.controller.PomodoroController` |
| Időzítés `javax.swing.Timer`-rel | `PomodoroController` |
| Konfigurálható idők modális `JDialog`-ban | `BeallitasokDialog` |
| Csomagok: model, view, controller | `pomodor.model`, `pomodoro.view`, `pomodoro.controller` |
| UML (PlantUML) az Observer kapcsolattal | `docs/osztalydiagram.puml` |
| README a futtatásról | ez a fájl |
| Min. 5 JUnit teszt a modellre, UI nélkül | `PomodoroModelTest` (10 teszt) |
| Extra: hangjelzés állapotváltáskor | `PomodoroView.allapotValtozott()` |
