package com.jsystemtrader.platform.quote;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.report.*;
import com.jsystemtrader.platform.strategy.*;

import java.util.*;

/**
 * Determines when a new bar should be created and signals to QuoteHistory when such times occur.
 * For example, if the strategy subscribes to 5-minute bars, this class will send a signal
 * at every 5-minute interval, such as 10:00, 10:05, 10:10, and so on. There is one instance of
 * BarFactory for every instance of Strategy.
 * <p/>
 * It's important to run a time synchronization software to make sure that the system clock is
 * accurate, so that the signals are sent at the correct time.
 */
public class BarFactory extends TimerTask {
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int DELAY = 750; // Time delay in milliseconds beyond the bar edge.
    private static final Report eventReport = Dispatcher.getReporter();

    private final QuoteHistory qh;
    //private final long frequency;
    private long frequency;
    private long nextBarTime;
    private String strategyName = "";
    

  /*  public BarFactory(Strategy strategy) {
        qh = strategy.getQuoteHistory();
        frequency = strategy.getBarSize().toSeconds() * MILLIS_IN_SECOND;
        // Integer division gives us the number of whole periods
        long completedPeriods = System.currentTimeMillis() / frequency;
        nextBarTime = (completedPeriods + 1) * frequency;
        // delay is to ensure that the last 5 second bar of each period has arrived
        Date start = new Date(nextBarTime + DELAY);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(this, start, frequency);
        eventReport.report(strategy.getName() + ": Bar factory started");
        strategyName = strategy.getName();
    }
    */
    public BarFactory( String name, QuoteHistory qh , BarSize barSize)
    {
    	System.out.println("Bar Factory = "+ name );
        this.qh = qh;
       /* if ( name.equals("KDStrategy2008"))
        {
        	frequency =30 * MILLIS_IN_SECOND;
        }else
        {
        	frequency =  90 * MILLIS_IN_SECOND;
        }
        */
        frequency = barSize.toSeconds() * MILLIS_IN_SECOND;
        
        // Integer division gives us the number of whole periods
        long completedPeriods = System.currentTimeMillis() / frequency;
        nextBarTime = (completedPeriods + 1) * frequency;
        // delay is to ensure that the last 5 second bar of each period has arrived
        Date start = new Date(nextBarTime + DELAY);

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(this, start, frequency);
        eventReport.report(name + ": Bar factory started");
        strategyName = name;
    }

    @Override
    public void run() {
    	
    	/*System.out.println("");
    	System.out.println("===================start from bar factory ====================" );
    	System.out.println("time ="+ new Date(System.currentTimeMillis()).toString() );
    	System.out.println("strategyName = " + strategyName + " is calling OnBar");*/
        qh.onBar(nextBarTime);
        nextBarTime += frequency;
    }

}
