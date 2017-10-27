package it.geosolutions.savemybike.model;

import java.util.ArrayList;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Configuration {

    private ArrayList<Vehicle> vehicles;

    public Configuration(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }
}
