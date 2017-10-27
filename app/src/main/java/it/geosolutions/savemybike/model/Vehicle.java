package it.geosolutions.savemybike.model;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Vehicle {

    public enum VehicleType
    {
        FOOT,
        BIKE,
        BUS,
        CAR
    }

    private VehicleType mType;

    private int minimumGPSTime;
    private int minimumGPSDistance;

    private boolean selected;

    public Vehicle(VehicleType mType, int minimumGPSTime, int minimumGPSDistance) {
        this.mType = mType;
        this.minimumGPSTime = minimumGPSTime;
        this.minimumGPSDistance = minimumGPSDistance;
    }

    public VehicleType getType() {
        return mType;
    }

    public int getMinimumGPSTime() {
        return minimumGPSTime;
    }

    public int getMinimumGPSDistance() {
        return minimumGPSDistance;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
