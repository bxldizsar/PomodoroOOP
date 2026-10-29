package pomodoro.main;

import javax.swing.SwingUtilities;
import pomodoro.controller.PomodoroController;
import pomodor.model.PomodoroModel;
import pomodor.view.PomodoroView;

public class Main
{
    public static void main(String[] args)
    {
        // a feluletet az EDT-n kell letrehozni
        SwingUtilities.invokeLater(() ->
        {
            PomodoroModel model = new PomodoroModel();
            PomodoroView view = new PomodoroView(model);
            new PomodoroController(model, view);
            view.setVisible(true);
        });
    }
}