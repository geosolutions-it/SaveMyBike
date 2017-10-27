package it.geosolutions.savemybike.ui.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.model.Configuration;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.fragment.BikeListFragment;
import it.geosolutions.savemybike.ui.fragment.RecordFragment;
import it.geosolutions.savemybike.ui.fragment.StatsFragment;

public class SaveMyBikeActivity extends AppCompatActivity {

    private final static String TAG = "SaveMyBikeActivity";

    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        changeFragment(0);

        //TODO load configuration

        final ArrayList<Vehicle> vehicles = new ArrayList<>();

        vehicles.add(new Vehicle(Vehicle.VehicleType.FOOT, 1000, 0));
        vehicles.add(new Vehicle(Vehicle.VehicleType.BIKE, 1000, 0));
        vehicles.add(new Vehicle(Vehicle.VehicleType.BUS, 1000, 0));
        vehicles.add(new Vehicle(Vehicle.VehicleType.CAR, 1000, 0));

        vehicles.get(1).setSelected(true);

        configuration = new Configuration(vehicles);
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

    public Vehicle getCurrentVehicle(){

        for(Vehicle vehicle : configuration.getVehicles()){
            if(vehicle.isSelected()){
                return vehicle;
            }
        }

        return null;
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
                    ((RecordFragment) currentFragment).selectMode(vehicle);
                }
            }else{
                vehicle.setSelected(false);
            }
        }
    }

    private Fragment getCurrentFragment(){

        return getFragmentManager().findFragmentById(R.id.content);
    }
}
