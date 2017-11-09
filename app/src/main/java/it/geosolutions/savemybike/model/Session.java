package it.geosolutions.savemybike.model;

import java.util.ArrayList;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Session {

    public enum SessionState
    {
        ACTIVE,
        STOPPED
    }

    private SessionState state;
    private Bike bike;
    private Vehicle.VehicleType currentVehicleType;
    private DataPoint currentDataPoint;

    private long id;
    private long lastPersistedIndex;
    private long lastUploadedIndex;
    private long startTime;
    private long endTime;
    private String name;
    private String serverId;
    private String userId;

    private ArrayList<DataPoint> dataPoints;

    public Session(Vehicle.VehicleType currentVehicleType){

        this.currentVehicleType = currentVehicleType;
        dataPoints = new ArrayList<>();
        state = SessionState.ACTIVE;
    }

    public Session(long id, Bike bike, long start, long end, String name, String userId, String sId, int state, int lastUpload, int lastPersist) {
        this.id = id;
        this.bike = bike;
        this.startTime = start;
        this.endTime = end;
        this.name = name;
        this.userId = userId;
        this.serverId = sId;
        this.state = SessionState.values()[state];
        this.lastUploadedIndex = lastUpload;
        this.lastPersistedIndex = lastPersist;
    }

    public DataPoint getCurrentDataPoint() {
        if(currentDataPoint == null){
            currentDataPoint = new DataPoint(this.id, System.currentTimeMillis(), this.currentVehicleType.ordinal());
        }
        return currentDataPoint;
    }
    public void deepCopyCurrentDataPoint(){

        DataPoint copy = new DataPoint(this.id, System.currentTimeMillis(), this.currentVehicleType.ordinal());
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

    public void setDataPoints(ArrayList<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
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

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getUserId() {
        return userId;
    }

    public long getLastPersistedIndex() {
        return lastPersistedIndex;
    }

    public void setLastPersistedIndex(long lastPersistedIndex) {
        this.lastPersistedIndex = lastPersistedIndex;
    }

    public long getLastUploadedIndex() {
        return lastUploadedIndex;
    }

    public void setLastUploadedIndex(long lastUploadedIndex) {
        this.lastUploadedIndex = lastUploadedIndex;
    }

    public void setCurrentVehicleType(Vehicle.VehicleType currentVehicleType) {
        this.currentVehicleType = currentVehicleType;
    }
}
