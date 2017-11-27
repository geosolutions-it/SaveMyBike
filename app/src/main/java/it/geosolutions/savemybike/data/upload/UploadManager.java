package it.geosolutions.savemybike.data.upload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.data.db.SMBDatabase;
import it.geosolutions.savemybike.model.Session;

/**
 * Created by Robert Oehler on 27.11.17.
 *
 * Handles uploads
 */

public class UploadManager {

    private final static String TAG = "UploadManager";

    private Context context;
    private boolean wifiOnly;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    // A List of all transfers
    private List<TransferObserver> observers;


    public UploadManager(final  Context context, final boolean wifiOnly) {

        this.context = context;
        this.wifiOnly = wifiOnly;
    }

    public void checkForUpload(){

        if(!isOnline()){
            Log.w(TAG, "no internet connection, cannot upload anything");
            return;
        }

        if(wifiOnly && !isWifiConnection()){
            Log.w(TAG, "wifi only but no wifi connection, cannot upload ");
            return;
        }

        SMBDatabase database = new SMBDatabase(context);
        ArrayList<Session> sessionsToUpload = new ArrayList<>();

        try{
            if(database.open()) {
                sessionsToUpload = database.getSessionsToUpload();
            }
        }finally {
            database.close();
        }

        if(BuildConfig.DEBUG){
            Log.i(TAG, String.format(Locale.US, "Found %d sessions to upload", sessionsToUpload.size()));
        }

        if(sessionsToUpload.size() > 0){
            uploadSessions(sessionsToUpload);
        }
    }

    private void uploadSessions(ArrayList<Session> sessionsToUpload) {


        transferUtility = AWSUtil.getTransferUtility(context);

        Log.i(TAG, "transfer utility is ready "+ transferUtility);

        //TODO create CSV
        //TODO upload CSB
        //TODO flag to db that the session was updloaded

    }


    /**
     * checks if the device is online
     * @return true if online, false otherwise
     */
    private boolean isOnline(){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();

        } catch (Exception e) {
            Log.e(TAG, "error achieving online status", e);
            return false;
        }
    }

    /**
     * checks if the current Internet connection is a Wifi connection
     * @return true if Wifi, false otherwise
     */
    private boolean isWifiConnection(){

        ConnectivityManager cm =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null){
            return false;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if(activeNetwork != null && activeNetwork.isConnected()){
            android.net.NetworkInfo connWifiType = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if(connWifiType.isConnected()){
                return true;
            }
        }
        return false;
    }
}
