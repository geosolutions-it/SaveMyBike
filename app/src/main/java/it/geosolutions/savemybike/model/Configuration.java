package it.geosolutions.savemybike.model;

import java.io.Serializable;
import java.util.ArrayList;

import it.geosolutions.savemybike.data.Constants;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Configuration implements Serializable {

    public ArrayList<Vehicle> vehicles;

    public int persistanceInterval;
    public int dataReadInterval;

    public Configuration(ArrayList<Vehicle> vehicles, int dataReadInterval, int persistanceInterval) {
        this.vehicles = vehicles;
        this.dataReadInterval = dataReadInterval;
        this.persistanceInterval = persistanceInterval;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public static Configuration loadConfiguation(){

        //TODO load real configuration

        final ArrayList<Vehicle> vehicles = new ArrayList<>();

        vehicles.add(new Vehicle(Vehicle.VehicleType.FOOT, 1000, 0));
        vehicles.add(new Vehicle(Vehicle.VehicleType.BIKE, 1000, 0));
        vehicles.add(new Vehicle(Vehicle.VehicleType.BUS, 1000, 0));
        vehicles.add(new Vehicle(Vehicle.VehicleType.CAR, 1000, 0));

        vehicles.get(1).setSelected(true);

        return new Configuration(vehicles, Constants.DEFAULT_DATA_READ_INTERVAL, Constants.DEFAULT_PERSISTANCE_INTERVAL);

    }

}
