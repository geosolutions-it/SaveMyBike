package it.geosolutions.savemybike.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Configuration implements Serializable {

    public ArrayList<Vehicle> vehicles;

    public int persistanceInterval;
    public int dataReadInterval;

    public Configuration(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

}
