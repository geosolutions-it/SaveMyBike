package it.geosolutions.savemybike.data;

/**
 * Created by Robert Oehler on 02.11.17.
 *
 */

public class Constants {

    public final static int DEFAULT_DATA_READ_INTERVAL = 1000;
    public final static int DEFAULT_PERSISTANCE_INTERVAL = 15000;

    public final static long ONE_HOUR = 3600000;
    public final static long ONE_MINUTE = 60000;

    public final static double MAX_LATITUDE = 85.05112878;
    public final static double MIN_LATITUDE = -85.05112878;
    public final static double MAX_LONGITUDE = 180;
    public final static double MIN_LONGITUDE = -180;

    public static final String DEFAULT_CONFIGURATION_FILE  = "conf.json";
    public static final String DEFAULT_SESSION_NAME = "Session";

    public static final String SERVICE_NAME = "it.geosolutions.savemybike.data.service.SaveMyBikeService";

    public final static String UNIT_KMH = "km/h";
    public final static String UNIT_MPH = "mph";
    public final static String UNIT_KM = "km";
    public final static String UNIT_MI = "mi";
    public final static String UNIT_M = "m";
    public final static String UNIT_FT = "ft";

    public final static float KM_TO_MILES = 0.621371192f;
    public final static float METER_TO_FEET = 3.2808399f;

}
