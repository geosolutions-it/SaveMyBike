package it.geosolutions.savemybike.data;

/**
 * Created by Robert Oehler on 02.11.17.
 *
 */

public class Constants {

    public final static int DEFAULT_DATA_READ_INTERVAL = 1000;
    public final static int DEFAULT_PERSISTANCE_INTERVAL = 15000;
    public final static boolean DEFAULT_WIFI_ONLY = true;

    public final static String AWS_REGION    = "us-west-2";
    public final static String AWS_POOL      = "us-west-2_glXagvW4j";
    public final static String AWS_CLIENT_ID_WO_SECRET = "5rgu4dnktbi8i9k56biff7t3jf";

    public final static String AWS_CLIENT_ID_W_SECRET = "1he4dlrhs9aenll11gmfllhvgd";
    public final static String AWS_CLIENT_SECRET = "131mec34kbuk262oag23q9g85oe3sqjdecg0l2lo5k5gp92po0lu";

    public final static String AWS_USER = "robert";
    public final static String AWS_PASS = "P@ssw0rd";

    public final static String AWS_BUCKET_NAME = "smb-test-lamb";

    public final static String APP_DIR = "SaveMyBike/";

    public final static long ONE_HOUR = 3600000;
    public final static long ONE_MINUTE = 60000;

    public final static double MAX_LATITUDE = 85.05112878;
    public final static double MIN_LATITUDE = -85.05112878;
    public final static double MAX_LONGITUDE = 180;
    public final static double MIN_LONGITUDE = -180;

    public static final String DEFAULT_CONFIGURATION_FILE  = "conf.json";
    public static final String DEFAULT_SESSION_NAME = "Session";

    public static final String SERVICE_NAME = "it.geosolutions.savemybike.data.service.SaveMyBikeService";

    public static final String NOTIFICATION_UPDATE_MODE = "it.geosolutions.savemybike.intent.mode";
    public static final String NOTIFICATION_UPDATE_STOP = "it.geosolutions.savemybike.intent.stop";

    public static final String INTENT_STOP_FROM_SERVICE = "it.geosolutions.savemybike.stop.from.service";
    public static final String INTENT_VEHICLE_UPDATE    = "it.geosolutions.savemybike.vehicle_update";

    public static final String PREF_WIFI_ONLY_UPLOAD = "it.geosolutions.savemybike.pref.wifi_only";

    public final static String UNIT_KMH = "km/h";
    public final static String UNIT_MPH = "mph";
    public final static String UNIT_KM = "km";
    public final static String UNIT_MI = "mi";
    public final static String UNIT_M = "m";
    public final static String UNIT_FT = "ft";

    public final static float KM_TO_MILES = 0.621371192f;
    public final static float METER_TO_FEET = 3.2808399f;

}
