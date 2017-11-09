package it.geosolutions.savemybike.data.dataProviders;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import it.geosolutions.savemybike.BuildConfig;
import it.geosolutions.savemybike.data.service.SaveMyBikeService;


/**
 * Created by Robert Oehler on 08.11.17.
 *
 * Class to measure the atmospheric pressure provided by the barometric sensor of the device (if available)
 */

public class BarometricSensor implements SensorEventListener, IDataProvider {

    private final static String TAG = "BarometricSensor";

    /**
     * min pressure change
     */
    private static final float MIN_DIFF_THRESHOLD = 0.1f;

    private SaveMyBikeService service;
    private SensorManager sensorManager;

    private long lastDataTime = 0;
    private boolean isRegistered = false;

    public BarometricSensor(final SaveMyBikeService saveMyBikeService){

        this.service = saveMyBikeService;
    }

    @Override
    public void start() {

        startSensorListening();
    }

    @Override
    public void stop() {

        stopSensorListening();
    }

    private void startSensorListening(){

        sensorManager = (SensorManager) service.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(sensor != null) {
            if(!isRegistered) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                isRegistered = true;
            }
        }else{
            Log.w(TAG, "no baro sensor available");
        }
    }
    private void stopSensorListening(){

        if(isRegistered) {
            sensorManager.unregisterListener(this);
            isRegistered = false;
        }
    }

    @Override
    public String toString() {
        return "Barometer";
    }



    @Override
    public void onSensorChanged(SensorEvent event) {

        final float pressure = event.values[0];

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Barometer sensor event new pressure " + pressure);
        }

        evaluateNewPressure(pressure);
    }

    private void evaluateNewPressure(float pressure){

        if(System.currentTimeMillis() - lastDataTime >= DATA_INTERVAL){

            if (service != null && service.getSessionLogic() != null && service.getSessionLogic().getSession() != null) {
                service.getSessionLogic().getSession().getCurrentDataPoint().pressure = pressure;
            }
            lastDataTime = System.currentTimeMillis();
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
