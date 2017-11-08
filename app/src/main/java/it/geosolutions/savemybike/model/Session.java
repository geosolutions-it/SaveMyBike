package it.geosolutions.savemybike.model;

import java.util.ArrayList;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Session {

    public enum SessionState
    {
        WAITING_FOR_FIX,
        ACTIVE,
        STOPPED
    }

    private SessionState state;

    private DataPoint currentDataPoint;

    private long id;
    private long startTime;
    private long endTime;
    private ArrayList<DataPoint> dataPoints;

    public Session(){

        dataPoints = new ArrayList<>();
        state = SessionState.WAITING_FOR_FIX;
    }

    public DataPoint getCurrentDataPoint() {
        if(currentDataPoint == null){
            currentDataPoint = new DataPoint();
        }
        return currentDataPoint;
    }
    public void deepCopyCurrentDataPoint(){

        DataPoint copy = new DataPoint();
        copy.timeStamp = getCurrentDataPoint().timeStamp;
        copy.elevation = getCurrentDataPoint().elevation;
        copy.latitude = getCurrentDataPoint().latitude;
        copy.longitude = getCurrentDataPoint().longitude;
        copy.mode = getCurrentDataPoint().mode;
        copy.bearing = getCurrentDataPoint().bearing;
        copy.accuracy = getCurrentDataPoint().accuracy;
        copy.batConsumptionPerHour = getCurrentDataPoint().batConsumptionPerHour;
        copy.batteryLevel = getCurrentDataPoint().batteryLevel;
        copy.temperature = getCurrentDataPoint().temperature;
        copy.pressure = getCurrentDataPoint().pressure;

        //TODO add additional values

        this.currentDataPoint = copy;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public ArrayList<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
