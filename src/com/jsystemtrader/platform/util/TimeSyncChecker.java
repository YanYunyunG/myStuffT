package com.jsystemtrader.platform.util;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import static com.jsystemtrader.platform.preferences.JSTPreferences.TimeLagAllowed;
import com.jsystemtrader.platform.startup.*;

import java.util.*;

import org.joda.time.DateTimeUtils;

/**
 * Utility class to ensure time synchronization between the machine where
 * JSystemTrader is running and the Interactive Brokers' server(s).
 * <p/>
 * It's recommended that a time sync service be running at all times.
 */
public class TimeSyncChecker {
    private static final String lineSep = System.getProperty("line.separator");

    /**
     * Makes sure that the clock on the machine where JSystemTrader is running
     * is in sync with the Interactive Brokers server.
     *
     * @param serverTime long Time as reported by IB server
     *
     * @throws JSystemTraderException If the difference between the two clocks is greater than the tolerance
     */
    public static void timeCheck(long serverTime) throws JSystemTraderException {
        long timeNow = System.currentTimeMillis();
        // Difference in seconds between IB server time and local machine's time
        long difference = (timeNow - serverTime) / 1000;
        int tolerance = Integer.parseInt(PreferencesHolder.getInstance().get(TimeLagAllowed));

        if (Math.abs(difference) > tolerance) {
        	
        	DateTimeUtils.setCurrentMillisFixed(serverTime);
        	
        	String msg = "This computer's clock is out of sync with the IB server clock." + lineSep;
        	  msg += lineSep + "IB Server Time: " + new Date(serverTime);
              msg += lineSep + "Computer Time: " + new Date(timeNow);
              msg += lineSep + "Rounded Difference: " + difference + " seconds";
              msg += lineSep + "Tolerance: " + tolerance + " seconds";
              msg += lineSep + lineSep;
              System.out.println( msg);
              
        	Date serverDate = new Date(serverTime);
        	String newTime = serverDate.getHours() +":"+ serverDate.getMinutes() +":"+ serverDate.getSeconds();

            try {
                // change the time to server's time
                Process child = Runtime.getRuntime().exec("c:\\changeTime.bat "+ newTime);
                System.out.println("Tried to adjust the time on this computer's clock to the IB server clock's time!!!");
                //this line doesn't work.
               // DateTimeUtils.setCurrentMillisFixed(serverTime);
            } catch (Throwable  e) {
            	e.printStackTrace();
            }
        }
    }
}
