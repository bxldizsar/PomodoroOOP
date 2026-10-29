package pomodor.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PomodoroModelTest {

    private PomodoroModel model;

    // Minden teszt előtt lefut: mindegyik teszt friss modellel indul.
    @BeforeEach
    public void setUp() {
        model = new PomodoroModel();
    }

    // Segédmetódus: n-szer "eltelik egy másodperc".
    private void tick(int n) {
        for (int i = 0; i < n; i++) {
            model.tick();
        }
    }

    // Végigvisz egy munkakört és az utána jövő rövid szünetet.
    private void egyKorRovidSzunettel() {
        tick(model.getMunkaMasodperc());
        tick(model.getRovidSzunetMasodperc());
    }

    // "Hamis" figyelő: nem rajzol semmit, csak feljegyzi, mit kapott.
    private static class TesztFigyelo implements PomodoroFigyelo {
        List<Integer> tikkek = new ArrayList<>();
        List<Allapot> allapotok = new ArrayList<>();

        @Override
        public void tikk(int hatralevoMasodperc) {
            tikkek.add(hatralevoMasodperc);
        }

        @Override
        public void allapotValtozott(Allapot uj) {
            allapotok.add(uj);
        }
    }

    @Test
    public void kezdetbenSzunetel() {
        assertEquals(Allapot.SZUNETEL, model.getAllapot());
        assertEquals(Allapot.MUNKA, model.getFazis());
        assertEquals(1500, model.getHatralevoMasodperc());
        assertEquals(0, model.getBefejezettKorok());
    }

    @Test
    public void startUtanMunka() {
        model.start();
        assertEquals(Allapot.MUNKA, model.getAllapot());
    }

    @Test
    public void tickCsokkentiAzIdot() {
        model.start();
        model.tick();
        assertEquals(1499, model.getHatralevoMasodperc());
    }

    @Test
    public void szunetMegallitjaAzOrat() {
        model.start();
        model.tick();
        model.szunet();
        tick(10); // szünet közben a tick-nek nem szabad hatnia
        assertEquals(Allapot.SZUNETEL, model.getAllapot());
        assertEquals(1499, model.getHatralevoMasodperc());
    }

    @Test
    public void szunetUtanStartOttFolytatja() {
        model.start();
        tick(model.getMunkaMasodperc()); // vége a munkának -> rövid szünet
        model.szunet();
        model.start();
        assertEquals(Allapot.ROVID_SZUNET, model.getAllapot());
    }

    @Test
    public void munkaUtanRovidSzunet() {
        model.start();
        tick(model.getMunkaMasodperc());
        assertEquals(Allapot.ROVID_SZUNET, model.getAllapot());
        assertEquals(300, model.getHatralevoMasodperc());
        assertEquals(1, model.getBefejezettKorok());
    }

    @Test
    public void rovidSzunetUtanMunka() {
        model.start();
        egyKorRovidSzunettel();
        assertEquals(Allapot.MUNKA, model.getAllapot());
        assertEquals(1500, model.getHatralevoMasodperc());
    }

    @Test
    public void negyedikKorUtanHosszuSzunet() {
        model.start();
        for (int i = 0; i < 3; i++) {
            egyKorRovidSzunettel();
        }
        tick(model.getMunkaMasodperc()); // a 4. munkakör vége
        assertEquals(4, model.getBefejezettKorok());
        assertEquals(Allapot.HOSSZU_SZUNET, model.getAllapot());
        assertEquals(900, model.getHatralevoMasodperc());
    }

    @Test
    public void resetMindentAlaphelyzetbeAllit() {
        model.start();
        tick(model.getMunkaMasodperc() + 10); // már a rövid szünetben járunk
        model.reset();
        assertEquals(Allapot.SZUNETEL, model.getAllapot());
        assertEquals(Allapot.MUNKA, model.getFazis());
        assertEquals(1500, model.getHatralevoMasodperc());
        assertEquals(0, model.getBefejezettKorok());
    }

    @Test
    public void figyeloErtesitestKap() {
        TesztFigyelo figyelo = new TesztFigyelo();
        model.feliratkoz(figyelo);
        model.start();
        model.tick();
        assertEquals(Allapot.MUNKA, figyelo.allapotok.get(0));
        assertEquals(1499, figyelo.tikkek.get(0));
    }
}