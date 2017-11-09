package it.geosolutions.savemybike.ui.activity;

import android.Manifest;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.data.service.SaveMyBikeService;
import it.geosolutions.savemybike.model.Configuration;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.fragment.BikeListFragment;
import it.geosolutions.savemybike.ui.fragment.RecordFragment;
import it.geosolutions.savemybike.ui.fragment.StatsFragment;

public class SaveMyBikeActivity extends AppCompatActivity {

    private final static String TAG = "SaveMyBikeActivity";

    private SaveMyBikeService mService;

    private Configuration configuration;
    private Vehicle currentVehicle;

    protected static final byte PERMISSION_REQUEST = 122;

    public enum PermissionIntent
    {
        LOCATION,
        SD_CARD
    }
    protected PermissionIntent mPermissionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        changeFragment(0);

        configuration = Configuration.loadConfiguation();
        currentVehicle = getCurrentVehicleFromConfig();
    }


    public void startRecording() {

        //check location permission
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionNecessary(Manifest.permission.ACCESS_FINE_LOCATION, PermissionIntent.LOCATION))) {

            //start service using bindService
            Intent serviceIntent = new Intent(this, SaveMyBikeService.class);

            //TODO configure params
            boolean simulate = false;
            int continueId = -1;
            Configuration configuration = null;

            serviceIntent.putExtra(SaveMyBikeService.PARAM_SIMULATE, simulate);
            serviceIntent.putExtra(SaveMyBikeService.PARAM_CONTINUE_ID, continueId);
            serviceIntent.putExtra(SaveMyBikeService.PARAM_VEHICLE, currentVehicle);
            serviceIntent.putExtra(SaveMyBikeService.PARAM_CONFIG, configuration);

            bindService(serviceIntent, getServiceConnection(), Context.BIND_AUTO_CREATE);

        }
    }


    /**
     * To load fragments for sample
     * @param position menu index
     */
    private void changeFragment(int position) {

        Fragment currentFragment = getCurrentFragment();

        Fragment fragment = null;
        switch (position){
            case 0:
                if(currentFragment != null && currentFragment instanceof  RecordFragment){
                    Log.i(TAG, "already showing record");
                    return;
                }
                fragment = new RecordFragment();
                break;
            case 1:
                if(currentFragment != null && currentFragment instanceof  StatsFragment){
                    Log.i(TAG, "already showing stats");
                    return;
                }
                fragment = new StatsFragment();
                break;
            case 2:
                if(currentFragment != null && currentFragment instanceof  BikeListFragment){
                    Log.i(TAG, "already showing bike list");
                    return;
                }
                fragment = new BikeListFragment();
                break;
        }

        getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            if(binder instanceof SaveMyBikeService.SaveMyBikeBinder) {
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "onServiceConnected");
                }
                mService = ((SaveMyBikeService.SaveMyBikeBinder)binder).getService();
            }else{
                Log.w(TAG, "unexpected : binder is no saveMyBikeBinder");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceDisconnected");
            }
            mService = null;
        }
    };

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_record:
                    changeFragment(0);
                    return true;
                case R.id.navigation_stats:
                    changeFragment(1);
                    return true;
                case R.id.navigation_bikes:
                    changeFragment(2);
                    return true;
            }
            return false;
        }

    };

    public Configuration getConfiguration() {
        return configuration;
    }

    public Vehicle getCurrentVehicleFromConfig(){

        for(Vehicle vehicle : configuration.getVehicles()){
            if(vehicle.isSelected()){
                return vehicle;
            }
        }

        return null;
    }

    public SaveMyBikeService getService() {
        return mService;
    }

    /**
     * changes the current vehicle in the configuration and updates the UI if a record fragment is currently visible
     * @param vehicleType the new vehicle type
     */
    public void changeVehicle(Vehicle.VehicleType vehicleType){

        for(Vehicle vehicle : configuration.getVehicles()){
            if(vehicle.getType() == vehicleType){
                vehicle.setSelected(true);
                Fragment currentFragment = getCurrentFragment();

                if(currentFragment != null && currentFragment instanceof RecordFragment){
                    ((RecordFragment) currentFragment).selectVehicle(vehicle);
                }
                currentVehicle = vehicle;

                if(mService != null){
                    mService.vehicleChanged(vehicle);
                }

            }else{
                vehicle.setSelected(false);
            }
        }
    }


    /**
     * ////////////// ANDROID 6 permissions /////////////////
     * checks if the permission @param is granted and if not requests it
     * @param permission the permission to check
     * @return if a permission is necessary
     */
    public boolean permissionNecessary(final String permission, final PermissionIntent intent) {

        boolean required = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED;

        if (required) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST);
            mPermissionIntent = intent;
            return true;
        }

        return false;
    }

    /**
     * ////////////// ANDROID 6 permissions /////////////////
     * returns the result of the permission request
     * @param requestCode a requestCode
     * @param permissions the requested permission
     * @param grantResults the result of the user decision
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (PERMISSION_REQUEST == requestCode) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //the permission was denied by the user, show a message
                if(permissions.length > 0) {
                    if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //sdcard
                        //TODO show a message when external storage was denied ?
                    } else if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) || permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {

                        //location
                        Toast.makeText(getBaseContext(),R.string.permission_location_required, Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }

            //did grant, what did we want to do ?
            switch (mPermissionIntent){
                case LOCATION:

                    startRecording();

                    break;
                case SD_CARD:
                    //TODO
                    break;

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    private Fragment getCurrentFragment(){

        return getFragmentManager().findFragmentById(R.id.content);
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }
}
