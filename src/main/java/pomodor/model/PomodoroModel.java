/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pomodor.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author boldi
 */
public class PomodoroModel 
{
    private Allapot allapot;
    private int hatralevoMasodperc;
    private int befejezettKorok;
    private List<PomodoroFigyelo> figyelok = new ArrayList<>();
    private Allapot fazis;
    private int munkaMasodperc = 1500;
    private int rovidSzunetMasodperc = 300;
    private int hosszuSzunetMasodperc = 900;
    
    public PomodoroModel()
    {
        reset();
    }
      
      public void start()
    {
        if(allapot == Allapot.SZUNETEL)
        {
            allapot = fazis;
            ertesitAllapot();
        }
    }
    
    public void szunet()
    {
        if(allapot != Allapot.SZUNETEL)
        {
            allapot = Allapot.SZUNETEL;
            ertesitAllapot();
        }
    }
    
    public void reset()
    {
        allapot = Allapot.SZUNETEL;
        fazis = Allapot.MUNKA;
        hatralevoMasodperc = munkaMasodperc ;
        befejezettKorok = 0; 
        ertesitAllapot();
        ertesitTikk();
        
    }
    
    public void tick()
    {
        if(allapot == Allapot.SZUNETEL)
        {
            return;
        }
        else 
        {
            hatralevoMasodperc = hatralevoMasodperc - 1;
        
        }
        if(hatralevoMasodperc == 0)
        {
        kovetkezoFazis();
        }
        ertesitTikk();
    }
    
    public void feliratkoz(PomodoroFigyelo f)
    {
        figyelok.add(f);
    }
    
    private void ertesitTikk()
    {
        for(PomodoroFigyelo f : figyelok)
        {
            f.tikk(hatralevoMasodperc);
        }
    }
    
    private void ertesitAllapot()
    {
        for(PomodoroFigyelo f : figyelok)
        {
            f.allapotValtozott(allapot);
        }
    }
    
    private void kovetkezoFazis()
    {
        if (fazis == Allapot.MUNKA)
        {
            befejezettKorok++; 
            
            if(befejezettKorok % 4 == 0)
            {
                fazis = Allapot.HOSSZU_SZUNET;
                hatralevoMasodperc = hosszuSzunetMasodperc;
            }
            else
            {
                fazis = Allapot.ROVID_SZUNET;
                hatralevoMasodperc = rovidSzunetMasodperc;
            }
        }
        else 
        {
            fazis = Allapot.MUNKA;
            hatralevoMasodperc = munkaMasodperc;
        }
        allapot = fazis; // az uj fazis rogton indul
        ertesitAllapot(); // szolunk a  figyelonek
    }
    
    public void setIdotartamok(int munka, int rovidSzunet, int hosszuSzunet) 
    {
        munkaMasodperc = munka;
        rovidSzunetMasodperc = rovidSzunet;
        hosszuSzunetMasodperc = hosszuSzunet;
        reset();
    }
    
    public Allapot getAllapot()
    {
        return allapot;
    }
    
    public Allapot getFazis() 
    {
        return fazis;
    }
    
    public int getHatralevoMasodperc()
    {
        return hatralevoMasodperc;
    }
    
    public int getBefejezettKorok() 
    {
        return befejezettKorok;
    }

    public int getMunkaMasodperc() 
    {
        return munkaMasodperc;
    }

    public int getRovidSzunetMasodperc() 
    {
        return rovidSzunetMasodperc;
    }

    public int getHosszuSzunetMasodperc() 
    {
        return hosszuSzunetMasodperc;
    }
    
}
