package it.geosolutions.savemybike.data.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.data.dataProviders.BatteryInfo;
import it.geosolutions.savemybike.data.dataProviders.GPSProvider;
import it.geosolutions.savemybike.data.dataProviders.GPSSimulator;
import it.geosolutions.savemybike.data.dataProviders.IDataProvider;
import it.geosolutions.savemybike.data.dataProviders.SensorDataProvider;
import it.geosolutions.savemybike.data.db.SMBDatabase;
import it.geosolutions.savemybike.data.session.SessionLogic;
import it.geosolutions.savemybike.model.Configuration;
import it.geosolutions.savemybike.model.Session;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.activity.SaveMyBikeActivity;

/**
 * Created by Robert Oehler on 28.10.17.
 *
 * A service which manages the recording of sessions
 * It is loosely bound to the UI - activity
 *
 * It starts and stop dataProviders - these provide data to the
 * @link SessionLogic which  collects the data
 *
 * During a record a notification is shown to remind the user that an ongoing
 * GPS connection is active
 */

public class SaveMyBikeService extends Service {

    private static final String TAG = "SaveMyBikeService";

    public static final String PARAM_SIMULATE     = "service.param.simulate";
    public static final String PARAM_CONTINUE_ID  = "service.param.continue.id";
    public static final String PARAM_VEHICLE      = "service.param.vehicle";
    public static final String PARAM_CONFIG       = "service.param.config";

    private static final int NOTIFICATION_ID = 111;

    private ArrayList<IDataProvider> dataProviders = new ArrayList<>();
    private SessionLogic sessionLogic;
    private Handler handler;
    private final IBinder mBinder = new SaveMyBikeBinder();

    private boolean didStop = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "onStartCommand, startId "+ startId);
        }

        //1.parse params
        boolean simulate = false;
        long continueId = -1;
        Vehicle vehicle = null;
        Configuration config = null;

        if(intent != null){
            simulate   =  intent.getBooleanExtra(PARAM_SIMULATE, false);
            continueId = intent.getLongExtra(PARAM_CONTINUE_ID, -1);
            vehicle    = (Vehicle) intent.getSerializableExtra(PARAM_VEHICLE);
            config     = (Configuration) intent.getSerializableExtra(PARAM_CONFIG);
        }

        //2. create or continue a session
        Session session = null;
        if(continueId != -1){
            session = continueSession(continueId, vehicle);
            if(BuildConfig.DEBUG){
                Log.d(TAG, "continuing session "+ continueId);
            }
        }else{
            session = createSession(vehicle);
            if(BuildConfig.DEBUG){
                Log.d(TAG, "starting a new session "+ session.getId());
            }
        }

        //3.session logic
        sessionLogic = new SessionLogic(getBaseContext(), session, vehicle, config);
        sessionLogic.setSimulating(simulate);
        getDataProviders().add(sessionLogic);

        //create data providers:
        //4.a GPS
        if(simulate){

            //in a simulation use the simulator to create GPS locations
            final GPSSimulator gpsSimulator = new GPSSimulator(this, sessionLogic);
            getDataProviders().add(gpsSimulator);
        }else{

            //otherwise use the real GPS
            final GPSProvider gpsProvider = new GPSProvider(getBaseContext(), vehicle, sessionLogic);
            getDataProviders().add(gpsProvider);
        }

        //4.b Sensors
        getDataProviders().add(new SensorDataProvider(this));
        //4.c Battery
        getDataProviders().add(new BatteryInfo(this));

        //5.start all data providers
        for(IDataProvider provider : dataProviders){

            provider.start();
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Starting data provider " + provider.getName());
            }
        }

        //6.finally, show a notification
        Notification n = displayNotificationMessage(getResources().getString(R.string.state_started),true);
        startForeground(NOTIFICATION_ID, n);

        return START_STICKY;
    }


    /**
     * updates the currently used vehicle :
     * 1. the GPS provider will restart with likely different parameters (if necessary)
     * 2. the session logic is updated to use the new vehicle
     * @param newVehicle the new vehicle
     */
    public void vehicleChanged(Vehicle newVehicle) {

        if(dataProviders != null){
            for(IDataProvider provider : dataProviders){
                if(provider instanceof GPSProvider){
                    ((GPSProvider) provider).switchToVehicle(newVehicle);
                }
            }
        }
        if(sessionLogic != null){
            sessionLogic.setVehicle(newVehicle);
        }
    }

    /**
     * stops this session
     * SessionLogic and providers are stopped
     * the state of this ride is set to FINISHED
     * and persisted
     */
    public void stopSession() {

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "Stopping ride");
        }

        this.didStop = true;

        sessionLogic.stop();
        sessionLogic.getSession().setState(Session.SessionState.STOPPED);
        sessionLogic.getSession().setEndTime(System.currentTimeMillis());

        //synchronize ride
        sessionLogic.persistSession();

        for(IDataProvider provider : dataProviders){
            provider.stop();
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Stopping data provider " + provider.getName());
            }
        }

        this.stopSelf();
    }
    /**
     * cleans up this service
     *
     * 1. the ride is persisted
     * 2. a notification that the ride is stopped shown - it autocancels after 1 sec
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        //if the service gets killed and we did not stop - try to persist
        if(!didStop && sessionLogic != null) {
            sessionLogic.persistSession();
        }

        stopForeground(true);

        //show a notification that the session was stopped
        displayNotificationMessage(getResources().getString(R.string.state_stopped),true);

        //and remove it after a second
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SaveMyBikeService.this);
                notificationManager.cancel(NOTIFICATION_ID);
            }
        }, 1000);
    }


    /**
     * continues a session if the provided @param sessionId is available in the database
     * otherwise a new session is created
     * @param sessionId the id of the session to continue
     * @param vehicle the current vehicle
     * @return the continued or the newly created session
     */
    private Session continueSession(long sessionId, final Vehicle vehicle) {

        Session session = null;
        //check database for id and reload it
        final SMBDatabase smbDatabase = new SMBDatabase(getBaseContext());
        try{
            smbDatabase.open();

            session = smbDatabase.getSession(sessionId);

            if(session == null){
                //this session was not found - create a new one
                session = createSession(vehicle);
            }

        } finally {
            smbDatabase.close();
        }

        return session;
    }

    /**
     * creates a new session and inserts it into the database
     * @param vehicle the current vehicle
     * @return the created session
     */
    private Session createSession(final Vehicle vehicle) {

        final Session session = new Session(vehicle.getType());
        //insert to database
        final SMBDatabase smbDatabase = new SMBDatabase(getBaseContext());
        try{
            smbDatabase.open();
            long id = smbDatabase.insertSession(session, false);
            session.setId(id);

        } finally {
            smbDatabase.close();
        }

        return session;
    }

    /**
     * shows a notification message
     * @param message the message for the notification
     * @param autoCancel if to autoCancel
     * @return the notification
     */
    private Notification displayNotificationMessage(String message, boolean autoCancel) {

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int smallIcon = R.mipmap.ic_launcher;
        //TODO use appropriate icon
        //int smallIcon =  R.drawable.not_icon_small_white;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(autoCancel)
                .setContentText(message)
                .setSmallIcon(smallIcon);

        Intent resultIntent = new Intent(this, SaveMyBikeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SaveMyBikeActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        final Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class SaveMyBikeBinder extends Binder {

        public SaveMyBikeService getService() {
            return SaveMyBikeService.this;
        }
    }

    public SessionLogic getSessionLogic() {
        return sessionLogic;
    }

    public Handler getHandler() {
        if(handler == null){
            handler = new Handler();
        }
        return handler;
    }

    public ArrayList<IDataProvider> getDataProviders() {

        if(dataProviders == null){
            dataProviders = new ArrayList<>();
        }

        return dataProviders;
    }
}
