package it.geosolutions.savemybike.data;

import java.util.Locale;

/**
 * Created by Robert Oehler on 12.11.17.
 *
 */

public class Util {

    /**
     * converts an amount of milliseconds into a human readable String
     * @param time millis
     * @return the String
     */
    public static String longToTimeString(long time){

        boolean negative = false;
        if(time < 0){
            negative = true;
            time  = Math.abs(time);
        }

        String format = String.format(Locale.US,"%%0%dd", 2);
        String seconds = String.format(format, (time % Constants.ONE_MINUTE) / 1000);
        String minutes = String.format(format, (time % Constants.ONE_HOUR) / Constants.ONE_MINUTE);

        if(time < Constants.ONE_HOUR){
            return String.format(Locale.US,"%s%s:%s", negative ? "-" : "", minutes, seconds);
        }else{
            String hours = String.format(format, time / Constants.ONE_HOUR);
            return String.format(Locale.US,"%s%s:%s", negative ? "-" : "", hours, minutes);
        }

    }
}
