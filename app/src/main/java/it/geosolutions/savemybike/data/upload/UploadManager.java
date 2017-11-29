package it.geosolutions.savemybike.data.upload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.data.Util;
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

    /**
     * checks if sessions need to be uploaded
     * launches upload if necessary
     */
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

    /**
     * uploads the sessions @param sessionsToUpload
     * @param sessionsToUpload sessions to upload
     */
    private void uploadSessions(ArrayList<Session> sessionsToUpload) {

        //TODO use
        transferUtility = AWSUtil.getTransferUtility(context);


        //sd ready ?
        if(!Environment.getExternalStorageDirectory().canWrite()){
            Log.w(TAG, "cannot write to external memory");
            return;
        }

        if (!Util.createSMBDirectory()) {
            Log.w(TAG, "could not create app dir");
            return;
        }

        //create CSV
        CSVCreator csvCreator = new CSVCreator();

        for(Session session : sessionsToUpload){

            String sessionFile = csvCreator.createCSV(session);

            String dataPointsFile = csvCreator.createCSV(session.getDataPoints(), Long.toString(session.getId()));


            //TODO upload CSV
            //TODO flag to db that the session was uploaded

            createZip(String.format(Locale.US,"data_%d.zip", session.getId()), dataPointsFile);

            //TODO clean up created files
        }

    }

    /**
     * creates a zip of @param fileToZip
     * @param zipFile the name of the zipFile to create
     * @param fileToZip the file to zip
     * @return the path to the created file or null if the the operation failed
     */
    private String createZip(String zipFile, String fileToZip){

        File file = new File(Util.getSMBDirectory().getPath() + String.format(Locale.US, "/%s", zipFile));

        try  {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(file);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[1024];

            Log.i(TAG, "zipping : " + fileToZip);

            FileInputStream fi = new FileInputStream(fileToZip);
            origin = new BufferedInputStream(fi, 1024);
            ZipEntry entry = new ZipEntry(fileToZip.substring(fileToZip.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();


            out.close();
        } catch(Exception e) {
           Log.e(TAG, "error zipping "+ fileToZip);
           return null;
        }
        return file.getAbsolutePath();
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
