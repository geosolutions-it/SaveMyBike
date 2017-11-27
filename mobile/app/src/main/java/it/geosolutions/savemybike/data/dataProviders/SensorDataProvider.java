package it.geosolutions.savemybike.data.dataProviders;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.data.service.SaveMyBikeService;


/**
 * Created by Robert Oehler on 08.11.17.
 *
 * Class to read sensor data of the device
 *
 * */

public class SensorDataProvider implements SensorEventListener, IDataProvider {

    private final static String TAG = "SensorDataProvider";

    private SaveMyBikeService service;
    private SensorManager sensorManager;

    private long lastPressureDataTime = 0;
    private long lastTemperatureDataTime = 0;
    private long lastAccelerationDataTime = 0;
    private long lastHumidityDataTime = 0;
    private long lastProximityDataTime = 0;
    private long lastLightDataTime = 0;

    private boolean isRegistered = false;

    public SensorDataProvider(final SaveMyBikeService saveMyBikeService){

        this.service = saveMyBikeService;

        sensorManager = (SensorManager) service.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void start() {

        startSensorListening();
    }

    @Override
    public void stop() {

        stopSensorListening();
    }

    /**
     * starts sensor listening with frequency SensorManager.SENSOR_DELAY_NORMAL
     * which should be a frequency of 200000 ns or 200 ms
     * source : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.3_r1/android/hardware/SensorManager.java
     */
    private void startSensorListening(){


        if(!isRegistered) {
            ArrayList<Boolean> sensorRegistrationResults = new ArrayList<>();

            //1.pressure
            sensorRegistrationResults.add(registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), "pressure"));

            //2.temperature
            boolean hasAmbientTemperature = registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), "ambient temperature");
            if(hasAmbientTemperature){
                sensorRegistrationResults.add(hasAmbientTemperature);
            }else{
                sensorRegistrationResults.add(registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE), "non ambient temperature"));
            }

            //3.accelerometer
            sensorRegistrationResults.add( registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), "accelerometer"));

            //4.humidity
            sensorRegistrationResults.add(registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY), "humidity"));

            //5.proximity
            sensorRegistrationResults.add(registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), "proximity"));

            //6.light
            sensorRegistrationResults.add(registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), "light"));

            //if only one registration result was true remember that we have to unregister
            for(Boolean result : sensorRegistrationResults){
                if(result){
                    isRegistered = true;
                    break;
                }
            }
        }
    }

    /**
     * registers if available a sensor and returns if this was possible
     * @param sensor the sensor to register or null if not available on the device
     * @param name the name for logging
     * @return true if the sensor was registered, false otherwise
     */
    private boolean registerSensor(final Sensor sensor, String name){

        if(sensor != null){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            if(BuildConfig.DEBUG) {
                Log.i(TAG, name + " registered");
            }
            return true;
        }else{
            if(BuildConfig.DEBUG) {
                Log.i(TAG, name + " not available");
            }
            return false;
        }
    }
    private void stopSensorListening(){

        if(isRegistered) {
            sensorManager.unregisterListener(this);
            isRegistered = false;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            case Sensor.TYPE_PRESSURE:

                if (System.currentTimeMillis() - lastPressureDataTime >= DATA_INTERVAL) {
                    final float pressure = event.values[0];

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Barometer sensor event new pressure " + pressure);
                    }


                    if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
                        service.getSessionLogic().getSession().getCurrentDataPoint().pressure = pressure;
                    }
                    lastPressureDataTime = System.currentTimeMillis();
                }
            break;
            case  Sensor.TYPE_AMBIENT_TEMPERATURE:
            case  Sensor.TYPE_TEMPERATURE:

                if(System.currentTimeMillis() - lastTemperatureDataTime >= DATA_INTERVAL) {

                    //values[0]: ambient (room) temperature in degree Celsius.
                    float temperature = event.values[0];

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format(Locale.US, "Temperature changed to %.2f Celsius", temperature));
                    }

                    if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
                        service.getSessionLogic().getSession().getCurrentDataPoint().temperature = temperature;
                    }

                    lastTemperatureDataTime = System.currentTimeMillis();
                }
                break;
            case Sensor.TYPE_ACCELEROMETER:
                //https://developer.android.com/reference/android/hardware/Sensor.html#TYPE_ACCELEROMETER
                if (System.currentTimeMillis() - lastAccelerationDataTime >= DATA_INTERVAL) {

                    float[] values = event.values;

                    float x = values[0];//Acceleration minus Gx on the x-axis
                    float y = values[1];//Acceleration minus Gy on the y-axis
                    float z = values[2];//Acceleration minus Gz on the z-axis

                    float accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "current acc " + accelerationSquareRoot);
                    }

                    if (accelerationSquareRoot >= 2){
                        lastAccelerationDataTime = event.timestamp;
                        Toast.makeText(service.getBaseContext(), "Device was shuffled", Toast.LENGTH_SHORT).show();
                    }

                    if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
                        service.getSessionLogic().getSession().getCurrentDataPoint().acceleration = accelerationSquareRoot;
                    }

                    lastAccelerationDataTime = System.currentTimeMillis();

                }
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:

                if(System.currentTimeMillis() - lastHumidityDataTime >= DATA_INTERVAL) {

                    //values[0]: Relative ambient air humidity in percent
                    float humidity = event.values[0];

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format(Locale.US, "Humidity changed to %.2f percent", humidity));
                    }

                    //TODO register humidity ?
//                    if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
//                        service.getSessionLogic().getSession().getCurrentDataPoint().temperature = humidity;
//                    }

                    lastHumidityDataTime = System.currentTimeMillis();
                }
                break;
            case Sensor.TYPE_PROXIMITY:

                if(System.currentTimeMillis() - lastProximityDataTime >= DATA_INTERVAL) {

                    //values[0]: Proximity sensor distance measured in centimeters
                    float proximity = event.values[0];

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format(Locale.US, "proximity changed to %.2f", proximity));
                    }

                    //TODO register proximity ?
//                    if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
//                        service.getSessionLogic().getSession().getCurrentDataPoint().temperature = humidity;
//                    }

                    lastProximityDataTime = System.currentTimeMillis();
                }
                break;
            case Sensor.TYPE_LIGHT:

                if(System.currentTimeMillis() - lastLightDataTime >= DATA_INTERVAL) {

                    //values[0]: Ambient light level in SI lux units
                    float light = event.values[0];

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format(Locale.US, "light changed to %.2f lux", light));
                    }

                    //TODO register light?
//                    if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
//                        service.getSessionLogic().getSession().getCurrentDataPoint().temperature = humidity;
//                    }

                    lastLightDataTime = System.currentTimeMillis();
                }
                break;
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "accuracy changed to " + accuracy);
        }
    }

    @Override
    public String getName() {
        return TAG;
    }
}
