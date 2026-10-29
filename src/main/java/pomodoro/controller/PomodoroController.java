package pomodoro.controller;

import javax.swing.Timer;
import pomodor.model.PomodoroModel;
import pomodoro.view.BeallitasokDialog;
import pomodor.view.PomodoroView;

// osszekoti a gombokat a modellel es vezerli az idozitot
public class PomodoroController
{
    private PomodoroModel model;
    private PomodoroView view;
    private Timer idozito;

    public PomodoroController(PomodoroModel model, PomodoroView view)
    {
        this.model = model;
        this.view = view;

        // a swing timer az EDT-n hivja a tick-et masodpercenkent, nincs Thread.sleep
        idozito = new Timer(1000, e -> model.tick());

        view.addStartListener(e ->
        {
            model.start();
            idozito.start();
        });

        view.addSzunetListener(e ->
        {
            model.szunet();
            idozito.stop();
        });

        view.addResetListener(e ->
        {
            idozito.stop();
            model.reset();
        });

        view.addBeallitasokListener(e -> beallitasokMegnyitasa());
    }

    private void beallitasokMegnyitasa()
    {
        BeallitasokDialog dialog = new BeallitasokDialog(view,
                model.getMunkaMasodperc() / 60,
                model.getRovidSzunetMasodperc() / 60,
                model.getHosszuSzunetMasodperc() / 60);

        dialog.setVisible(true); // modalis, itt var amig be nem zarjak

        if(dialog.isJovahagyva())
        {
            idozito.stop();
            model.setIdotartamok(
                    dialog.getMunkaPerc() * 60,
                    dialog.getRovidSzunetPerc() * 60,
                    dialog.getHosszuSzunetPerc() * 60);
        }
    }
}