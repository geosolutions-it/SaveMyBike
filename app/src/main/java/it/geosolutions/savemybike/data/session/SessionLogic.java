package it.geosolutions.savemybike.data.session;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import java.util.TimerTask;

import it.geosolutions.savemybike.data.Constants;
import it.geosolutions.savemybike.data.dataProviders.IDataProvider;
import it.geosolutions.savemybike.model.Configuration;
import it.geosolutions.savemybike.model.DataPoint;
import it.geosolutions.savemybike.model.Session;
import it.geosolutions.savemybike.model.Vehicle;

/**
 * Created by Robert Oehler on 30.10.17.
 *
 * Class which collects the data of a recording and
 * periodically {@link Constants#DEFAULT_DATA_READ_INTERVAL} adds
 * dataPoints of the current data situation to a list
 *
 * This list is periodically {@link Constants#DEFAULT_PERSISTANCE_INTERVAL}
 * persisted to the database
 */

public class SessionLogic implements IDataProvider {

    private final static String TAG = "SessionLogic";

    private Context context;
    private Vehicle vehicle;
    private Session session;
    private Handler handler;

    //configurable
    private int persistanceInterval;
    private int dataReadInterval;

    //temporay
    private boolean stopped = false;
    private boolean hasGPSFix = false;
    private boolean isSimulating;
    private long lastSessionPersistTime;

    public SessionLogic(Context context, Session session, Vehicle vehicle, Configuration configuration) {

        this.context = context;
        this.session = session;
        this.vehicle = vehicle;

        if(configuration != null){
            this.dataReadInterval = configuration.dataReadInterval;
            this.persistanceInterval = configuration.persistanceInterval;
        }else{
            this.dataReadInterval = Constants.DEFAULT_DATA_READ_INTERVAL;
            this.persistanceInterval = Constants.DEFAULT_PERSISTANCE_INTERVAL;
        }
    }

    public void start(){

        this.stopped = false;
        startTasks();
    }
    public void stop(){

        this.stopped = true;
        this.hasGPSFix = false;

        stopTasks();
    }

    /**
     * starts tasks
     */
    private void startTasks() {

        getHandler().removeCallbacks(getDataReadTask());
        getHandler().postDelayed(getDataReadTask(), dataReadInterval);

        getHandler().removeCallbacks(getPersistanceTask());
        getHandler().postDelayed(getPersistanceTask(), persistanceInterval);
    }

    /**
     * stops the tasks
     */
    private void stopTasks() {

        getHandler().removeCallbacks(getDataReadTask());
        getHandler().removeCallbacks(getPersistanceTask());
    }

    /**
     * evaluates a new location received from GPS:
     *
     * when not having a GPX fix yet register that the fix was acquired
     * the location is then used to update the current session data
     *
     * Currently there is NO filtering applied, all locations are registered
     *
     * @param newLocation the newly acquired location
     */
    public void evaluateNewLocation(Location newLocation){

        if(stopped){
            return;
        }

        if(session == null){
            Log.w(TAG, "session null");
            return;
        }

        if(!hasGPSFix){
            //fix, when
            if(session.getState() == Session.SessionState.WAITING_FOR_FIX){
                //set start time when we were waiting for a fix and a start time has been never set

                session.setStartTime(System.currentTimeMillis());
                session.setState(Session.SessionState.ACTIVE);
            }
            hasGPSFix = true;
        }

        //update session with the current data from the location

        session.getCurrentDataPoint().latitude  = newLocation.getLatitude();
        session.getCurrentDataPoint().longitude = newLocation.getLongitude();
        session.getCurrentDataPoint().elevation = newLocation.getAltitude();
        session.getCurrentDataPoint().accuracy  = newLocation.getAccuracy();
        session.getCurrentDataPoint().bearing   = newLocation.getBearing();

        //TODO add more props ?
    }

    private Runnable persistanceTask;
    private Runnable dataReadTask;

    /**
     * a task which adds the current dataPoint to the list
     * of dataPoints to persist
     * the current point is then deep copied to prepare
     * the next dataPoint with the current data as base
     */
    private Runnable getDataReadTask (){
        if(dataReadTask == null){
            dataReadTask = new Runnable() {
                @Override
                public void run() {

                    if(stopped){
                        return;
                    }

                    //add a new data point to the list of data-points

                    DataPoint newDataPoint = session.getCurrentDataPoint();
                    newDataPoint.mode = vehicle.getType().ordinal();

                    session.getDataPoints().add(newDataPoint);
                    session.deepCopyCurrentDataPoint();

                    getHandler().postDelayed(this, dataReadInterval);
                }
            };
        }
        return dataReadTask;
    }

    /**
     * a task which persists the current session
     */
    private Runnable getPersistanceTask() {

        if(persistanceTask == null){
            persistanceTask = new TimerTask() {
                @Override
                public void run() {

                    //15 sec interval for persistance event
                    if (System.currentTimeMillis() - lastSessionPersistTime >= persistanceInterval && session.getDataPoints().size() > 0) {

                        persistSession();

                    }else{

                        getHandler().postDelayed(this, persistanceInterval);
                    }
                }
            };
        }
        return persistanceTask;
    }

    public void persistSession() {

        //TODO persist and remember which were persisted

        this.lastSessionPersistTime = System.currentTimeMillis();
    }

    private Handler getHandler() {
        if(handler == null){
            handler = new Handler();
        }
        return handler;
    }

    public Session getSession() {
        return session;
    }

    public void setSimulating(boolean simulating) {
        isSimulating = simulating;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public String getName() {
        return "SessionLogic";
    }
}
