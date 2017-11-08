package it.geosolutions.savemybike.model;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class DataPoint {

    public long timeStamp;

    //GPS
    public double latitude;
    public double longitude;
    /**
     * height above the WGS84 ellipsoid in metres
     */
    public double elevation;
    public float accuracy;
    public float bearing;

    //sensors
    public int batteryLevel;
    public float batConsumptionPerHour;
    /**
     * temperature in celsius
     */
    public float temperature;
    public float pressure;

    public int mode;

    public DataPoint() {
        this.timeStamp = System.currentTimeMillis();
    }

    public DataPoint(double latitude, double longitude, long timeStamp, double elevation) {
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

}
