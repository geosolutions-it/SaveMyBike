package it.geosolutions.savemybike.model;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class DataPoint {

    public long sessionId;
    public int vehicleMode;

    public long timeStamp;

    //GPS
    public double latitude = Double.NaN;
    public double longitude = Double.NaN;
    /**
     * height above the WGS84 ellipsoid in metres
     */
    public double elevation;
    public float accuracy;
    public float gps_bearing;
    public float speed;

    //sensors
    public int batteryLevel;
    public float batConsumptionPerHour;
    public float accelerationX;
    public float accelerationY;
    public float accelerationZ;
    public float humidity;
    public float proximity;
    public float lumen;
    public float deviceBearing;
    public float deviceRoll;
    public float devicePitch;

    /**
     * temperature in celsius
     */
    public float temperature;
    public float pressure;

    public int mode;

    public DataPoint(long sessionId, long time,  int vehicleMode) {

        this.sessionId = sessionId;
        this.timeStamp = time;
        this.vehicleMode = vehicleMode;
    }

    public DataPoint(long sessionId, long time, int vehicleMode, double lat, double lon){
        this(sessionId, time, vehicleMode);

        this.latitude  = lat;
        this.longitude = lon;
    }

    public DataPoint(long id,
                     int vehicle,
                     double lat,
                     double lon,
                     long time,
                     double elev,
                     float bear,
                     float accu,
                     float spd,
                     float press,
                     int bat_l,
                     float bat_c,
                     float accX,
                     float accY,
                     float accZ,
                     float hum,
                     float prx,
                     float lgt,
                     float deviceBearing,
                     float deviceRoll,
                     float devicePitch,
                     float temp) {

        this.sessionId = id;
        this.vehicleMode = vehicle;
        this.latitude = lat;
        this.longitude = lon;
        this.timeStamp = time;
        this.elevation = elev;
        this.gps_bearing = bear;
        this.accuracy = accu;
        this.speed = spd;
        this.pressure = press;
        this.batteryLevel = bat_l;
        this.batConsumptionPerHour = bat_c;
        this.accelerationX = accX;
        this.accelerationY = accY;
        this.accelerationZ = accZ;
        this.humidity = hum;
        this.proximity = prx;
        this.lumen = lgt;
        this.deviceBearing = deviceBearing;
        this.deviceRoll = deviceRoll;
        this.devicePitch = devicePitch;
        this.temperature = temp;

    }
}
