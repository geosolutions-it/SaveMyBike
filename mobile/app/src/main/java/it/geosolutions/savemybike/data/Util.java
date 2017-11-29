package it.geosolutions.savemybike.data;

import android.os.Environment;
import android.util.Log;

import java.io.File;
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

    /**
     * creates if necessary the smb directory
     * @return
     */
    public static boolean createSMBDirectory(){

        File exportDir = new File(Environment.getExternalStorageDirectory(), Constants.APP_DIR);

        if (!exportDir.exists()) {
            if(!exportDir.mkdirs()){
                Log.w("Util","Error creating SaveMyBike dir");
                return false;
            }
        }
        return true;

    }

    public static File getSMBDirectory(){

        return new File(Environment.getExternalStorageDirectory(), Constants.APP_DIR);
    }
}
