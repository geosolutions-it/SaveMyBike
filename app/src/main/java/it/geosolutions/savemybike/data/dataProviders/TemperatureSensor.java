package it.geosolutions.savemybike.data.dataProviders;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Locale;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.data.service.SaveMyBikeService;


/**
 * Created by Robert Oehler on 08.06.17.
 *
 * A class to measure the temperature from the internal sensor
 * If available, the device's ambient temperature sensor is measured, if not the default (battery) temperature
 */

public class TemperatureSensor implements SensorEventListener, IDataProvider {

    private final static String TAG = "TemperatureSensor";

    private SaveMyBikeService service;
    private SensorManager sensorManager;
    private Sensor temperatureSensor;

    private long lastDataTime = 0;
    private boolean isRegistered = false;

    public TemperatureSensor(final SaveMyBikeService saveMyBikeService){

        this.service = saveMyBikeService;

        //temperature sensor available
        sensorManager = (SensorManager) saveMyBikeService.getSystemService(Context.SENSOR_SERVICE);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if(temperatureSensor != null){
            if(BuildConfig.DEBUG) {
                Log.i(TAG, "ambient temperature sensor available");
            }

        }else{
            Log.i(TAG, "NO temperature sensor available, registering battery temperature events");
        }

    }

    public boolean hasTemperatureSensor(){
        return temperatureSensor != null;
    }

    @Override
    public void start() {

        if(!isRegistered && temperatureSensor != null) {
            sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isRegistered = true;
        }
    }

    @Override
    public void stop() {
        if(isRegistered && temperatureSensor != null) {
            sensorManager.unregisterListener(this);
            isRegistered = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(System.currentTimeMillis() - lastDataTime >= DATA_INTERVAL) {

            //values[0]: ambient (room) temperature in degree Celsius.
            float temperature = event.values[0];

            if (BuildConfig.DEBUG) {
                Log.i(TAG, String.format(Locale.US, "Temperature changed to %.2f Celsius", temperature));
            }

            if (service != null && service.getSession() != null) {
                service.getSession().getCurrentDataPoint().temperature = temperature;
            }

            lastDataTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public String getName() {
        return TAG;
    }
}
