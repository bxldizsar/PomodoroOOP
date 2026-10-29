package pomodoro.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

// modalis beallitasok ablak, az idok percben
public class BeallitasokDialog extends JDialog
{
    private JSpinner munkaSpinner;
    private JSpinner rovidSpinner;
    private JSpinner hosszuSpinner;
    private boolean jovahagyva = false;

    public BeallitasokDialog(Frame szulo, int munkaPerc, int rovidPerc, int hosszuPerc)
    {
        super(szulo, "Beállítások", true); // true = modalis

        munkaSpinner = new JSpinner(new SpinnerNumberModel(munkaPerc, 1, 180, 1));
        rovidSpinner = new JSpinner(new SpinnerNumberModel(rovidPerc, 1, 60, 1));
        hosszuSpinner = new JSpinner(new SpinnerNumberModel(hosszuPerc, 1, 120, 1));

        JPanel mezok = new JPanel(new GridLayout(3, 2, 8, 8));
        mezok.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        mezok.add(new JLabel("Munka (perc):"));
        mezok.add(munkaSpinner);
        mezok.add(new JLabel("Rövid szünet (perc):"));
        mezok.add(rovidSpinner);
        mezok.add(new JLabel("Hosszú szünet (perc):"));
        mezok.add(hosszuSpinner);

        JButton okGomb = new JButton("OK");
        JButton megseGomb = new JButton("Mégse");

        okGomb.addActionListener(e ->
        {
            jovahagyva = true;
            dispose();
        });
        megseGomb.addActionListener(e -> dispose());

        JPanel gombok = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gombok.add(okGomb);
        gombok.add(megseGomb);

        setLayout(new BorderLayout());
        add(mezok, BorderLayout.CENTER);
        add(gombok, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okGomb);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(szulo);
    }

    public boolean isJovahagyva()
    {
        return jovahagyva;
    }

    public int getMunkaPerc()
    {
        return (Integer) munkaSpinner.getValue();
    }

    public int getRovidSzunetPerc()
    {
        return (Integer) rovidSpinner.getValue();
    }

    public int getHosszuSzunetPerc()
    {
        return (Integer) hosszuSpinner.getValue();
    }
}