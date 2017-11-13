package it.geosolutions.savemybike.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import it.geosolutions.savemybike.data.Constants;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Configuration implements Serializable {

    public String id;
    public Integer version;

    public ArrayList<Vehicle> vehicles;

    public ArrayList<Bike> bikes;

    @SerializedName("persistanceInterval")
    public int persistanceInterval;
    @SerializedName("dataReadInterval")
    public int dataReadInterval;
    @SerializedName("metric")
    public boolean metric;


    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public static Configuration loadConfiguration(final Context context){

        Gson gson = new Gson();
        final String jsonConf = loadJSONFromAsset(context);

        Configuration configuration = gson.fromJson(jsonConf, Configuration.class);

        if(configuration != null){
            for(Vehicle  vehicle : configuration.getVehicles()){
                Log.i("Config","vehicle "+ vehicle.toString());
            }
        }

        return configuration;

    }

    private static String loadJSONFromAsset(final Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open(Constants.DEFAULT_CONFIGURATION_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.e("Configuration", "error reading conf json from assets", e);
            return null;
        }
        return json;
    }

}
