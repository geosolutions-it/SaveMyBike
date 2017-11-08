package it.geosolutions.savemybike.data.dataProviders;

import android.location.Location;

import java.util.ArrayList;

import it.geosolutions.savemybike.data.service.SaveMyBikeService;
import it.geosolutions.savemybike.data.session.SessionLogic;
import it.geosolutions.savemybike.model.DataPoint;

/**
 * Created by Robert Oehler on 05.05.17.
 *
 * A class to simulate GPS updates
 */

public class GPSSimulator implements IDataProvider {

   public enum SimulationMode{

       TEST_RIDE,
       LIMITLESS
    }

    private SimulationMode currentMode = SimulationMode.TEST_RIDE;

    /**
     * speed
     */
    private static int SIMULATION_INTERVAL = 1000;
    private static final int START_THRESHOLD = 2000;

    private static final float SIM_MOVE_SPEED_IN_MS = 6f;
    private static final float SIM_ACCURACY = 15f;
    private final static short DEFAULT_ELEV = 0;

    private final SaveMyBikeService mService;
    private SessionLogic sessionLogic;
    private int currentSimulationIndex;
    private boolean cancelled = false;

    private ArrayList<DataPoint> locations;

    public GPSSimulator(final SaveMyBikeService service, final SessionLogic pSessionLogic){

        this.mService = service;
        this.sessionLogic = pSessionLogic;
        this.sessionLogic.setSimulating(true);
        
    }

    /**
     * runnable which is executed during simulation - until all available locations are passed
     */
    private Runnable locSimulator = new Runnable() {

        public void run() {

            if(currentSimulationIndex < locations.size()){

                Location loc = new Location("");

                final DataPoint dataPoint = locations.get(currentSimulationIndex);

                loc.setLatitude(dataPoint.latitude);
                loc.setLongitude(dataPoint.longitude);
                loc.setAltitude(dataPoint.elevation);
                loc.setAccuracy(SIM_ACCURACY);
                loc.setSpeed(SIM_MOVE_SPEED_IN_MS);

                sessionLogic.evaluateNewLocation(loc);

                currentSimulationIndex++;

                if(!cancelled) {
                    mService.getHandler().postDelayed(this, SIMULATION_INTERVAL);
                }
            }
        }
    };

    @Override
    public void start() {

        switch (currentMode) {
            case TEST_RIDE:
                locations = simulatePisaGombitelli();
                break;
            case LIMITLESS:

                locations = new ArrayList<>();

                double baseLat = 52.563008;
                double baseLon = 13.402869;
                double offset  = 0.0001d;

                for(int i = 0; i < 10000; i++){

                    double lat = baseLat + i * offset;
                    double lon = baseLon + i * offset;

                    locations.add(new DataPoint(lat, lon, 0 , 0));
                }

                break;
        }

        mService.getHandler().postDelayed(locSimulator, GPSSimulator.START_THRESHOLD);
        
    }

    @Override
    public void stop() {

        cancelled = true;
    }
    
    private ArrayList<DataPoint> simulatePisaGombitelli(){

        ArrayList<DataPoint> debugLocations = new ArrayList<>();

        debugLocations.add(new DataPoint(43.725398,10.397701,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.725862,10.397867,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.726251,10.398002,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.726440,10.398068,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.726871,10.398088,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.726855,10.397500,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727026,10.396843,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727274,10.394311,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727414,10.393290,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727514,10.392839,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727561,10.392646,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727685,10.392839,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.727584,10.392723,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.728034,10.393195,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.72929,10.3929160,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.731476,10.392573,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.734112,10.391822,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.738174,10.390728,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.743414,10.389354,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.748762,10.387874,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.750947,10.387402,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.755303,10.387895,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.756279,10.388281,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.757302,10.388432,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.757403,10.388592,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.757596,10.388619,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.757724,10.388474,0,DEFAULT_ELEV));
        debugLocations.add(new DataPoint(43.758011,10.388442,0,DEFAULT_ELEV));
        return debugLocations;
    }

    public void setMode(SimulationMode currentMode) {
        this.currentMode = currentMode;
    }

    @Override
    public String getName() {
        return "GPSSimulator";
    }
}
