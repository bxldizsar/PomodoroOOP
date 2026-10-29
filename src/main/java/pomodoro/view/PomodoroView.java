package pomodor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import pomodor.model.Allapot;
import pomodor.model.PomodoroFigyelo;
import pomodor.model.PomodoroModel;

/**
 * A főablak. Feliratkozik a modellre (Observer), és csak megjelenít.
 * A gombok listenereit a controller adja át az add...Listener metódusokkal.
 */
public class PomodoroView extends JFrame implements PomodoroFigyelo {

    private final PomodoroModel model;

    private final JLabel idoFelirat = new JLabel("25:00", SwingConstants.CENTER);
    private final JLabel allapotFelirat = new JLabel("", SwingConstants.CENTER);
    private final JLabel korFelirat = new JLabel("", SwingConstants.CENTER);
    private final JButton startGomb = new JButton("Start");
    private final JButton szunetGomb = new JButton("Szünet");
    private final JButton resetGomb = new JButton("Reset");
    private final JButton beallitasokGomb = new JButton("Beállítások…");
    private final JPanel kozepPanel = new JPanel(new BorderLayout());

    private Allapot elozoAllapot;

    public PomodoroView(PomodoroModel model)
    {
        super("Pomodoro-óra");
        this.model = model;

        idoFelirat.setFont(new Font(Font.MONOSPACED, Font.BOLD, 96));
        allapotFelirat.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        korFelirat.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        idoFelirat.setForeground(Color.WHITE);
        allapotFelirat.setForeground(Color.WHITE);
        korFelirat.setForeground(Color.WHITE);

        JPanel feliratok = new JPanel(new GridLayout(2, 1));
        feliratok.setOpaque(false);
        feliratok.add(allapotFelirat);
        feliratok.add(korFelirat);

        kozepPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        kozepPanel.add(feliratok, BorderLayout.NORTH);
        kozepPanel.add(idoFelirat, BorderLayout.CENTER);

        JPanel gombok = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        gombok.add(startGomb);
        gombok.add(szunetGomb);
        gombok.add(resetGomb);
        gombok.add(beallitasokGomb);

        setLayout(new BorderLayout());
        add(kozepPanel, BorderLayout.CENTER);
        add(gombok, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 380);
        setLocationRelativeTo(null);

        elozoAllapot = model.getAllapot();
        frissit();
        model.feliratkoz(this);
    }

    

    @Override
    public void tikk(int hatralevoMasodperc)
    {
        edtn(() -> idoFelirat.setText(formaz(hatralevoMasodperc)));
    }

    @Override
    public void allapotValtozott(Allapot uj) 
    {
        edtn(() -> {
            
            if (elozoAllapot != Allapot.SZUNETEL && uj != Allapot.SZUNETEL && uj != elozoAllapot)
            {
                Toolkit.getDefaultToolkit().beep();
            }
            elozoAllapot = uj;
            frissit();
        });
    }

   

    private void frissit() 
    {
        Allapot allapot = model.getAllapot();
        idoFelirat.setText(formaz(model.getHatralevoMasodperc()));
        if (allapot == Allapot.SZUNETEL) 
        {
            
            allapotFelirat.setText("Szüneteltetve (" + felirat(model.getFazis()) + ")");
        } else 
        {
            allapotFelirat.setText(felirat(allapot));
        }
        int kor = model.getBefejezettKorok() % 4 + 1;
        korFelirat.setText("Befejezett pomodorók: " + model.getBefejezettKorok() + "   ·   Kör: " + kor + "/4");
        kozepPanel.setBackground(szin(allapot));
        startGomb.setEnabled(allapot == Allapot.SZUNETEL);
        szunetGomb.setEnabled(allapot != Allapot.SZUNETEL);
    }

    private static String felirat(Allapot a)
    {
        switch (a) {
            case MUNKA: return "Munka";
            case ROVID_SZUNET: return "Rövid szünet";
            case HOSSZU_SZUNET: return "Hosszú szünet";
            default: return "Szüneteltetve";
        }
    }

    private static Color szin(Allapot a) 
    {
        switch (a) {
            case MUNKA: return new Color(0xC0392B);         // piros
            case ROVID_SZUNET: return new Color(0x27AE60);  // zöld
            case HOSSZU_SZUNET: return new Color(0x2471A3); // kék
            default: return new Color(0x5D6D7E);            // szürke
        }
    }

    
    private static String formaz(int masodperc) 
    {
        return String.format("%02d:%02d", masodperc / 60, masodperc % 60);
    }

    
    private static void edtn(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) 
        {
            r.run();
        } else 
        {
            SwingUtilities.invokeLater(r);
        }
    }

    

    public void addStartListener(ActionListener l) 
    {
        startGomb.addActionListener(l);
    }

    public void addSzunetListener(ActionListener l) 
    {
        szunetGomb.addActionListener(l);
    }

    public void addResetListener(ActionListener l) 
    {
        resetGomb.addActionListener(l);
    }

    public void addBeallitasokListener(ActionListener l) 
    {
        beallitasokGomb.addActionListener(l);
    }
}