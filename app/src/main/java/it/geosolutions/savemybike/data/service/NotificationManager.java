package it.geosolutions.savemybike.data.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.data.Constants;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.activity.SaveMyBikeActivity;

/**
 * Created by Robert Oehler on 14.11.17.
 *
 *
 */

public class NotificationManager extends BroadcastReceiver {

    private final static String TAG = "NotificationManager";

    public static final int NOTIFICATION_ID = 111;
    private static final int REQUEST_CODE = 100;

    private SaveMyBikeService mService;
    private final android.app.NotificationManager mNotificationManager;

    private final PendingIntent mModeIntent;
    private final PendingIntent mStopIntent;

    private boolean mStarted = false;

    private String mCurrentMessage;
    private Vehicle mVehicle;

    public NotificationManager(final SaveMyBikeService service){

        mService = service;
        mNotificationManager = (android.app.NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        String pkg = mService.getPackageName();
        mModeIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(Constants.NOTIFICATION_UPDATE_MODE).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(Constants.NOTIFICATION_UPDATE_STOP).setPackage(pkg),PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public Notification startNotification(final String message, Vehicle vehicle) {
        if (!mStarted) {

            mCurrentMessage = message;
            mVehicle = vehicle;

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Constants.NOTIFICATION_UPDATE_MODE);
                filter.addAction(Constants.NOTIFICATION_UPDATE_STOP);
                mService.registerReceiver(this, filter);
                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
                return notification;
            }
        }
        return null;
    }


    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {

        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received intent with action " + action);
        switch (action) {

            case Constants.NOTIFICATION_UPDATE_MODE:

                stopNotification();
                // change state
                int currentType = mVehicle.getType().ordinal();
                currentType++;
                if(currentType >= Vehicle.VehicleType.values().length){
                    currentType = 0;
                }
                mService.vehicleChanged(mService.vehicleFromType(currentType));
                mService.sendBroadcast(new Intent(Constants.INTENT_VEHICLE_UPDATE));

                break;
            case Constants.NOTIFICATION_UPDATE_STOP:

                mService.stopSession();
                mService.sendBroadcast(new Intent(Constants.INTENT_STOP_FROM_SERVICE));

                break;
            default:
                Log.w(TAG, "Unknown intent ignored. Action = " + action);
        }
    }

    private Notification createNotification(){

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        int modeSrc = R.drawable.ic_directions_bike;
        switch (mVehicle.getType()) {

                case FOOT:
                    modeSrc = R.drawable.ic_directions_walk;
                    break;
                case BIKE:
                    modeSrc = R.drawable.ic_directions_bike;
                    break;
                case BUS:
                    modeSrc = R.drawable.ic_directions_bus;
                    break;
                case CAR:
                    modeSrc = R.drawable.ic_directions_car;
                    break;
            }

        NotificationCompat.Action modeAction = new NotificationCompat.Action.Builder(modeSrc, mService.getString(R.string.mode), mModeIntent).build();
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.ic_stop, mService.getString(R.string.stop), mStopIntent).build();

        notificationBuilder
                .addAction(modeAction)
                .addAction(stopAction)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(0)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(mCurrentMessage)
                .setContentText(mService.getSessionLogic().getVehicle().getType().name());


        return notificationBuilder.build();
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, SaveMyBikeActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
