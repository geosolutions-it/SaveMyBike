package it.geosolutions.savemybike;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import it.geosolutions.savemybike.data.db.SMBDatabase;
import it.geosolutions.savemybike.model.Bike;
import it.geosolutions.savemybike.model.DataPoint;
import it.geosolutions.savemybike.model.Session;
import it.geosolutions.savemybike.model.Vehicle;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Database test inserting a session and a dataPoint
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    public final static String TEST_DATABASE = "smb_test.db";
    public final static double DOUBLE_DELTA = 0.00001d;

    @Test
    public void testDatabase() {

        Context context = InstrumentationRegistry.getTargetContext();
        assertNotNull(context);

        final SMBDatabase database = new SMBDatabase(context, TEST_DATABASE);

        assertNotNull(database);
        assertTrue(database.open());

        //1.create and insert a session
        final Vehicle.VehicleType vehicleType = Vehicle.VehicleType.BIKE;
        final Bike currentBike = database.getSelectedBike();
        assertNotNull(currentBike);
        final Session testSession = new Session(vehicleType);

        String name = "testSession";
        final long startTime = System.currentTimeMillis();

        testSession.setName(name);
        testSession.setState(Session.SessionState.ACTIVE);
        testSession.setBike(currentBike);
        testSession.setLastPersistedIndex(1);

        long testId = database.insertSession(testSession, false);
        testSession.setId(testId);

        //2.create and insert a dataPoint
        final long time = System.currentTimeMillis();
        final double lat = 43.6200172;
        final double lon = 10.3161126;
        final double elev = 333;
        final float temp = 18f;
        final float bearing = 2f;
        final float spd     = 3f;
        final float accuracy = 15f;
        final float pressure = 0.12345f;
        final float acceleration = 3f;
        final int orientation = 1;
        final int bat_l = 99;
        final float bat_c = 15.5f;

        final DataPoint dataPoint = new DataPoint(testId, time, vehicleType.ordinal());
        dataPoint.latitude = lat;
        dataPoint.longitude = lon;
        dataPoint.elevation = elev;
        dataPoint.temperature = temp;
        dataPoint.bearing = bearing;
        dataPoint.accuracy = accuracy;
        dataPoint.speed = spd;
        dataPoint.pressure = pressure;
        dataPoint.batteryLevel = bat_l;
        dataPoint.batConsumptionPerHour = bat_c;
        dataPoint.acceleration = acceleration;
        dataPoint.orientation = orientation;

        database.insertDataPoint(dataPoint);

        //3.read from database
        final Session insertedSession = database.getSession(testId);
        assertNotNull(insertedSession);

        assertNotNull(insertedSession.getBike());
        assertTrue(insertedSession.getBike().getName().equals(currentBike.getName()));
        assertTrue(insertedSession.getBike().getLocalId() == currentBike.getLocalId());

        assertTrue(insertedSession.getName().equals(name));
        assertTrue(insertedSession.getState() == Session.SessionState.ACTIVE);
        assertTrue(insertedSession.getLastPersistedIndex() == 1);
        assertTrue(insertedSession.getLastUploadedIndex() == 0);

        assertNotNull(insertedSession.getDataPoints());
        assertTrue(insertedSession.getDataPoints().size() > 0);
        assertTrue(insertedSession.getDataPoints().size() == 1);
        final DataPoint insertedDataPoint = insertedSession.getDataPoints().get(0);

        assertEquals(insertedDataPoint.sessionId, testId);
        assertEquals(insertedDataPoint.timeStamp, time);
        assertEquals(insertedDataPoint.vehicleMode, vehicleType.ordinal());
        assertEquals(insertedDataPoint.latitude, lat, DOUBLE_DELTA);
        assertEquals(insertedDataPoint.longitude, lon, DOUBLE_DELTA);
        assertEquals(insertedDataPoint.elevation, elev);
        assertEquals(insertedDataPoint.temperature, temp);
        assertEquals(insertedDataPoint.bearing, bearing);
        assertEquals(insertedDataPoint.accuracy, accuracy);
        assertEquals(insertedDataPoint.speed, spd);
        assertEquals(insertedDataPoint.pressure, pressure);
        assertEquals(insertedDataPoint.batteryLevel, bat_l);
        assertEquals(insertedDataPoint.batConsumptionPerHour, bat_c);
        assertEquals(insertedDataPoint.acceleration, acceleration);
        assertEquals(insertedDataPoint.orientation, orientation);

        //done, cleanup

        database.close();

        File file = context.getDatabasePath(TEST_DATABASE);

        if(file.exists()) {
            boolean deleted = file.delete();
            assertTrue(deleted);
        }
    }
}
