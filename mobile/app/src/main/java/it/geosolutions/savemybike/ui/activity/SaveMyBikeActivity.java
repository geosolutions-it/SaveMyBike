package it.geosolutions.savemybike.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.data.Constants;
import it.geosolutions.savemybike.data.service.SaveMyBikeService;
import it.geosolutions.savemybike.model.Configuration;
import it.geosolutions.savemybike.model.Session;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.fragment.BikeListFragment;
import it.geosolutions.savemybike.ui.fragment.RecordFragment;
import it.geosolutions.savemybike.ui.fragment.StatsFragment;

public class SaveMyBikeActivity extends AppCompatActivity {

    private final static String TAG = "SaveMyBikeActivity";

    private final static int UI_UPDATE_INTERVAL = 1000;

    private SaveMyBikeService mService;

    private Configuration configuration;
    private Vehicle currentVehicle;
    private boolean applyServiceVehicle = false;

    protected static final byte PERMISSION_REQUEST = 122;
    private Handler handler;
    private MReceiver mReceiver;

    public enum PermissionIntent
    {
        LOCATION,
        SD_CARD
    }
    protected PermissionIntent mPermissionIntent;

    private boolean simulate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        changeFragment(0);

        currentVehicle = getCurrentVehicleFromConfig();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final boolean isServiceRunning = isServiceRunning(getBaseContext(), Constants.SERVICE_NAME);
        if(isServiceRunning){
            bindToService(new Intent(this, SaveMyBikeService.class));
            applyServiceVehicle = true;
        }
        //start updating the UI
        getHandler().removeCallbacks(mUpdateUITask);
        getHandler().postDelayed(mUpdateUITask, 10);

        registerReceiver(getmReceiver(), new IntentFilter(Constants.INTENT_STOP_FROM_SERVICE));
        registerReceiver(getmReceiver(), new IntentFilter(Constants.INTENT_VEHICLE_UPDATE));

        //when not having an ongoing session, invalidate with the local vehicle
        if(!applyServiceVehicle) {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment != null && currentFragment instanceof RecordFragment) {
                ((RecordFragment) currentFragment).invalidateUI(currentVehicle);
            }
        }else{
            //otherwise the UI update is done when re-binding to the service in @link onServiceConnected()
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mService != null){
            unbindService(mServiceConnection);
        }
        //start updating the UI
        getHandler().removeCallbacks(mUpdateUITask);

        unregisterReceiver(getmReceiver());
    }

    /**
     * starts the recording of a session by launching the recording service and binding to it
     */
    public void startRecording() {

        //check location permission
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionNecessary(Manifest.permission.ACCESS_FINE_LOCATION, PermissionIntent.LOCATION))) {

            //start service using bindService
            Intent serviceIntent = new Intent(this, SaveMyBikeService.class);

            //TODO configure params
            long continueId = -1;

            serviceIntent.putExtra(SaveMyBikeService.PARAM_SIMULATE, simulate);
            serviceIntent.putExtra(SaveMyBikeService.PARAM_CONTINUE_ID, continueId);
            serviceIntent.putExtra(SaveMyBikeService.PARAM_VEHICLE, currentVehicle);
            serviceIntent.putExtra(SaveMyBikeService.PARAM_CONFIG, getConfiguration());

            startService(serviceIntent);

            bindToService(serviceIntent);
        }
    }

    /**
     * stops recording a session
     */
    public void stopRecording() {

        if(mService != null){
            mService.stopSession();
        }

        unbindService(mServiceConnection);
        //onServiceDisconnected is only called when service crashes, hence nullify service here
        mService = null;

        invalidateOptionsMenu();
    }

    /**
     * bind or rebind to a running service
     * @param serviceIntent the intent for the service to bind to
     */
    private void bindToService(Intent serviceIntent){

        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * the current connection to the recording service
     */
    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            if(binder instanceof SaveMyBikeService.SaveMyBikeBinder) {
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "onServiceConnected");
                }
                mService = ((SaveMyBikeService.SaveMyBikeBinder)binder).getService();
                if(applyServiceVehicle){

                    Fragment currentFragment = getCurrentFragment();
                    if(currentFragment != null && currentFragment instanceof RecordFragment && mService.getSessionLogic() != null && mService.getSessionLogic().getVehicle() != null){
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "rebound to service, applying vehicle "+ mService.getSessionLogic().getVehicle().toString());
                        }
                        ((RecordFragment) currentFragment).invalidateUI(mService.getSessionLogic().getVehicle());
                    }

                    applyServiceVehicle = false;
                }
                invalidateOptionsMenu();
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

    /**
     * load fragment for index @param position
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

    /**
     * changes the current vehicle in the configuration and updates the UI if a record fragment is currently visible
     * @param vehicleType the new vehicle type
     */
    public void changeVehicle(Vehicle.VehicleType vehicleType, boolean setInService) {

        for(Vehicle vehicle : getConfiguration().getVehicles()){
            if(vehicle.getType() == vehicleType){
                vehicle.setSelected(true);
                Fragment currentFragment = getCurrentFragment();

                if(currentFragment != null && currentFragment instanceof RecordFragment){
                    ((RecordFragment) currentFragment).selectVehicle(vehicle);
                }
                currentVehicle = vehicle;

                if (setInService && mService != null) {
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
                        Toast.makeText(getBaseContext(), R.string.permission_location_required, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(getCurrentSession() == null) {

            getMenuInflater().inflate(R.menu.menu_record, menu);

            MenuItem followItem = menu.findItem(R.id.menu_simulate);
            followItem.setChecked(simulate);
            followItem.setIcon(simulate ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);

            return true;
        }else{
            return  super.onCreateOptionsMenu(menu);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_simulate:

                simulate = !simulate;
                Fragment currentFragment = getCurrentFragment();
                if(currentFragment != null && currentFragment instanceof RecordFragment){
                    ((RecordFragment)currentFragment).applySimulate(simulate);
                }
                invalidateOptionsMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private Runnable mUpdateUITask = new Runnable() {

        public void run() {

            Fragment currentFragment = getCurrentFragment();
            if(currentFragment != null && currentFragment instanceof RecordFragment){
                Session session = getCurrentSession();
                ((RecordFragment)currentFragment).invalidate(session);
            }

            getHandler().postDelayed(this, UI_UPDATE_INTERVAL);
        }
    };

        public class MReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals(Constants.INTENT_STOP_FROM_SERVICE)){

                    unbindService(mServiceConnection);
                    //onServiceDisconnected is only called when service crashes, hence nullify service here
                    mService = null;

                    invalidateOptionsMenu();

                    Fragment currentFragment = getCurrentFragment();
                    if (currentFragment != null && currentFragment instanceof RecordFragment) {
                        ((RecordFragment) currentFragment).applySessionState(Session.SessionState.STOPPED);
                    }
                }else if(intent.getAction().equals(Constants.INTENT_VEHICLE_UPDATE)){

                    if(mService != null){
                        Vehicle newVehicle = mService.getCurrentVehicle();
                        if(newVehicle != null){
                            changeVehicle(newVehicle.getType(), false);
                        }
                    }
                }
            }
        }
    /**
     * checks if a service is running in the system
     * @param context a context
     * @param serviceName the package name of the service
     * @return true if running
     */
    public boolean isServiceRunning(@NonNull Context context,@NonNull final String serviceName) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the current session when available - null otherwise
     */
    public Session getCurrentSession(){

        if(mService != null && mService.getSessionLogic() != null && mService.getSessionLogic().getSession() != null){

            return mService.getSessionLogic().getSession();
        }
        return null;
    }

    /**
     * gets the configuration - if it is null it is loaded
     * @return the configuration
     */
    public Configuration getConfiguration() {
        if(configuration == null){
            configuration = Configuration.loadConfiguration(getBaseContext());
        }
        return configuration;
    }

    /**
     * gets the currently selected vehicle from the configuration
     * @return the vehicle
     */
    public Vehicle getCurrentVehicleFromConfig(){

        for(Vehicle vehicle : getConfiguration().getVehicles()){
            if(vehicle.isSelected()){
                return vehicle;
            }
        }

        return null;
    }

    /**
     * @return the currently visible fragment
     */
    private Fragment getCurrentFragment(){

        return getFragmentManager().findFragmentById(R.id.content);
    }

    /**
     * @return the currently used vehicle
     */
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    private Handler getHandler() {
        if(handler == null){
            handler = new Handler();
        }
        return handler;
    }

    public MReceiver getmReceiver() {

        if(mReceiver == null){
            mReceiver = new MReceiver();
        }

        return mReceiver;
    }
}
