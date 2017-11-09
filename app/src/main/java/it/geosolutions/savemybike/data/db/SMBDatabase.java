package it.geosolutions.savemybike.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import it.geosolutions.savemybike.model.Bike;
import it.geosolutions.savemybike.model.DataPoint;
import it.geosolutions.savemybike.model.Session;

/**
 * Created by Robert Oehler on 09.11.17.
 *
 */

public class SMBDatabase extends SQLiteOpenHelper {

    private final String TAG = "SMDDatabase";

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "smbDb.db";

    //Tables
    private static final String BIKES_TABLE = "bikes";
    private static final String SESSIONS_TABLE = "sessions";
    private static final String DATA_POINTS_TABLE = "datapoints";

    //Ids
    private static final String ID = "_id";
    private static final String SESSION_ID = "session_id";

    //Session Table
    private static final String START_TIME = "start_time";
    private static final String END_TIME = "end_time";
    private static final String BIKE_ID = "bike_id";
    private static final String NAME = "name";
    private static final String STATE = "state";
    private static final String USER_ID = "user_id";
    private static final String SERVER_ID = "server_id";
    private static final String LAST_UPLOADED_INDEX = "last_upload_id";
    private static final String LAST_PERSISTED_INDEX = "last_persist_id";

    //DataPoints table
    private static final String VEHICLE  = "vehicle";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";
    private static final String TIME = "time";
    private static final String ELEVATION = "elev";
    private static final String TEMPERATURE = "temperature";
    private static final String BEARING  = "bearing";
    private static final String ACCURACY = "accuracy";
    private static final String SPEED = "speed";
    private static final String PRESSURE = "pressure";
    private static final String BATTERY_LEVEL  = "battery_level";
    private static final String BATTERY_CONSUMPTION  = "battery_cons";
    private static final String ACCELERATION  = "acc";
    private static final String ORIENTATION = "orientation";

    //Bikes Table
    private static final String IMAGE_URI = "image_uri";
    private static final String STOLEN = "stolen";
    private static final String SELECTED   = "selected";

    private SQLiteDatabase db;

    private static final String CREATE_SESSIONS_TABLE =
            "CREATE TABLE " + SESSIONS_TABLE +
                    " (" + ID + " integer primary key autoincrement, " +
                    SERVER_ID + " text, "+
                    START_TIME + " long, " +
                    END_TIME + " long, " +
                    BIKE_ID + " integer, " +
                    USER_ID + " text, " +
                    STATE + " integer, " +
                    LAST_UPLOADED_INDEX + " integer, " +
                    LAST_PERSISTED_INDEX + " integer, " +
                    NAME + " text);";

    private static final String CREATE_DATA_POINTS_TABLE =
            "CREATE TABLE " + DATA_POINTS_TABLE +
                    " (" + ID + " integer primary key autoincrement, " +
                    SESSION_ID + " integer, "+
                    VEHICLE + " integer, " +
                    LATITUDE + " float, " +
                    LONGITUDE + " float, " +
                    TIME + " long, " +
                    ELEVATION + " float, " +
                    BEARING + " float, " +
                    ACCURACY + " float, " +
                    SPEED + " float, " +
                    PRESSURE + " float, " +
                    BATTERY_LEVEL + " integer, " +
                    BATTERY_CONSUMPTION + " float, " +
                    ACCELERATION + " float, " +
                    ORIENTATION + " integer, " +
                    TEMPERATURE + " float);";

    private static final String CREATE_BIKES_TABLE =
            "CREATE TABLE " + BIKES_TABLE +
                    " (" + ID + " integer primary key autoincrement, " +
                    IMAGE_URI + " text, " +
                    STOLEN + " integer, " +
                    NAME + " text, " +
                    SELECTED + " integer);";

    /**
     * Constructor using the default file
     * @param context a context
     */
    public SMBDatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Constructor for creating a database using the file of @param name
     * @param context a context
     * @param name file name
     */
    public SMBDatabase(Context context, String name){
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_SESSIONS_TABLE);
        db.execSQL(CREATE_DATA_POINTS_TABLE);
        db.execSQL(CREATE_BIKES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);
    }

    /**
     * tries to open this database, first in writable mode
     * if that fails readable
     * @return true if the database was opened, false otherwise
     */
    public boolean open(){
        try{
            this.db = this.getWritableDatabase();
        } catch (SQLiteException e){
            try{
                this.db = this.getReadableDatabase();
            } catch (Exception ef){
                Log.e(TAG,"error getting even readable DB",ef);
                return false;
            }
        }
        return true;
    }
    public void close(){
        try{
            this.db.close();
        }catch(Exception e){
            Log.e(TAG,"error closing DB",e);
        }
    }

    public long insertSession(final Session session, boolean update){
        ContentValues cv = new ContentValues();

        cv.put(START_TIME, session.getStartTime());
        cv.put(END_TIME, session.getEndTime());
        if(session.getBike() != null) {
            cv.put(BIKE_ID, session.getBike().getId());
        }
        cv.put(STATE, session.getState().ordinal());
        cv.put(NAME, session.getName());
        cv.put(USER_ID, session.getUserId());
        cv.put(SERVER_ID, session.getServerId());
        cv.put(LAST_UPLOADED_INDEX, session.getLastUploadedIndex());
        cv.put(LAST_PERSISTED_INDEX, session.getLastPersistedIndex());

        if(update){
            return db.update(SESSIONS_TABLE, cv,  ID + "=" + session.getId(), null);
        }else{
            return db.insert(SESSIONS_TABLE, null, cv);
        }
    }

    public long insertDataPoint(final DataPoint dataPoint){

        ContentValues cv = new ContentValues();

        //assign values
        cv.put(SESSION_ID, dataPoint.sessionId);
        cv.put(VEHICLE, dataPoint.vehicleMode);
        cv.put(LATITUDE, (float) dataPoint.latitude);
        cv.put(LONGITUDE, (float) dataPoint.longitude);
        cv.put(ELEVATION, dataPoint.elevation);
        cv.put(TIME, dataPoint.timeStamp);
        cv.put(TEMPERATURE,  dataPoint.temperature);
        cv.put(BEARING, dataPoint.bearing);
        cv.put(ACCURACY, dataPoint.accuracy);
        cv.put(SPEED, dataPoint.speed);
        cv.put(PRESSURE, dataPoint.pressure);
        cv.put(ACCELERATION, dataPoint.acceleration);
        cv.put(ORIENTATION, dataPoint.orientation);
        cv.put(BATTERY_LEVEL, dataPoint.batteryLevel);
        cv.put(BATTERY_CONSUMPTION, dataPoint.batConsumptionPerHour);

        //insert the row
        return db.insert(DATA_POINTS_TABLE, null, cv);
    }

    public long insertBike(final Bike bike, boolean update){

        ContentValues cv = new ContentValues();

        //assign values
        cv.put(NAME, bike.getName());
        cv.put(IMAGE_URI, bike.getImagePath());
        cv.put(STOLEN, bike.isStolen());
        cv.put(SELECTED, bike.isSelected() ? 1 : 0);
        //insert the row
        if(update){
            return db.update(BIKES_TABLE, cv, ID + "=" + bike.getId(), null);
        }else {
            return db.insert(BIKES_TABLE, null, cv);
        }
    }

    /**
     * gets the session with the id @param sessionId
     * @param sessionId the id of the queried session
     * @return the session or null if it was not found
     */
    public Session getSession(final long sessionId){

        final ArrayList<Session> sessions = getAllSessions();

        for(Session session : sessions){
            if(session.getId() == sessionId){
                return session;
            }
        }

        return null;
    }

    /**
     * gets all sessions of this database
     * @return a list of sessions
     */
    public ArrayList<Session> getAllSessions(){

        final Cursor cursor = db.query(true, SESSIONS_TABLE,new String[]{ID, SERVER_ID, START_TIME, END_TIME, NAME, BIKE_ID, USER_ID, STATE, LAST_UPLOADED_INDEX, LAST_PERSISTED_INDEX}, null, null, null, null, String.format(Locale.US,"%s DESC", START_TIME), null);

        ArrayList<Session> sessions = new ArrayList<>();

        if(cursor.getCount() > 0 && cursor.moveToFirst()){

            do {

                long id         = cursor.getLong(cursor.getColumnIndex(ID));
                long start      = cursor.getLong(cursor.getColumnIndex(START_TIME));
                long end        = cursor.getLong(cursor.getColumnIndex(END_TIME));
                String userId   = cursor.getString(cursor.getColumnIndex(USER_ID));
                String name     = cursor.getString(cursor.getColumnIndex(NAME));
                String sId      = cursor.getString(cursor.getColumnIndex(SERVER_ID));
                int bikeId      = cursor.getInt(cursor.getColumnIndex(BIKE_ID));
                int state       = cursor.getInt(cursor.getColumnIndex(STATE));
                int lastUpload  = cursor.getInt(cursor.getColumnIndex(LAST_UPLOADED_INDEX));
                int lastPersist = cursor.getInt(cursor.getColumnIndex(LAST_PERSISTED_INDEX));

                Bike bike = getBike(bikeId);

                Session session = new Session(id, bike, start, end, name, userId, sId, state, lastUpload, lastPersist);

                ArrayList<DataPoint> dataPoints = getDataPointsForSession(id);

                session.setDataPoints(dataPoints);
                sessions.add(session);
            }while (cursor.moveToNext());
        }
        cursor.close();

        return sessions;

    }

    /**
     * gets all dataPoints for the session @param sessionId
     * @param sessionId the id of the session
     * @return a list of dataPoints
     */
    private ArrayList<DataPoint> getDataPointsForSession(final long sessionId){

        ArrayList<DataPoint> dataPoints = new ArrayList<>();

        final Cursor cursor = db.query(true,
                DATA_POINTS_TABLE,
                new String[]{ID,
                        VEHICLE,
                        LATITUDE,
                        LONGITUDE,
                        TIME,
                        ELEVATION,
                        BEARING,
                        ACCURACY,
                        SPEED,
                        PRESSURE,
                        BATTERY_LEVEL,
                        BATTERY_CONSUMPTION,
                        ACCELERATION,
                        ORIENTATION,
                        TEMPERATURE},
                SESSION_ID + "=?",
                new String[]{Long.toString(sessionId)},
                null, null, ID, null);

        if(cursor.getCount() > 0 && cursor.moveToFirst()){

            do{
                long  id    = cursor.getLong(cursor.getColumnIndex(ID));
                int vehicle = cursor.getInt(cursor.getColumnIndex(VEHICLE));
                double lat  = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
                double lon  = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
                long time   = cursor.getLong(cursor.getColumnIndex(TIME));
                double elev = cursor.getDouble(cursor.getColumnIndex(ELEVATION));
                float bear  = cursor.getFloat(cursor.getColumnIndex(BEARING));
                float accu  = cursor.getFloat(cursor.getColumnIndex(ACCURACY));
                float spd   = cursor.getFloat(cursor.getColumnIndex(SPEED));
                float press = cursor.getFloat(cursor.getColumnIndex(PRESSURE));
                int bat_l   = cursor.getInt(cursor.getColumnIndex(BATTERY_LEVEL));
                float bat_c = cursor.getFloat(cursor.getColumnIndex(BATTERY_CONSUMPTION));
                float acc   = cursor.getFloat(cursor.getColumnIndex(ACCELERATION));
                int   orie  = cursor.getInt(cursor.getColumnIndex(ORIENTATION));
                float temp  = cursor.getFloat(cursor.getColumnIndex(TEMPERATURE));

                DataPoint dp = new DataPoint(id, vehicle, lat, lon, time, elev, bear, accu, spd, press, bat_l, bat_c, acc, orie, temp);

                dataPoints.add(dp);

            }while (cursor.moveToNext());

            cursor.close();

        }else{
            Log.w(TAG,"ride location cursor for session " + sessionId + " empty");
        }

        return dataPoints;
    }


    /**
     * Gets all currently available bikes from the database
     * if none is available a default bike is created and inserted
     * hence always a list containing at least one bike is returned
     * @return the list of bikes
     */
    public ArrayList<Bike> getAllBikes() {

        final ArrayList<Bike> bikes = new ArrayList<>();

        Cursor cursor = db.query(true, BIKES_TABLE, new String[]{ID, NAME, SELECTED, IMAGE_URI, STOLEN}, null, null, null, null, String.format(Locale.US,"%s ASC",ID), null);

        if(cursor.getCount() > 0 && cursor.moveToFirst()){

            do {

                final int id = cursor.getInt(cursor.getColumnIndex(ID));
                final String name = cursor.getString(cursor.getColumnIndex(NAME));
                final String uri = cursor.getString(cursor.getColumnIndex(IMAGE_URI));
                final boolean selected = cursor.getInt(cursor.getColumnIndex(SELECTED)) > 0;
                final boolean stolen = cursor.getInt(cursor.getColumnIndex(STOLEN)) > 0;

                final Bike bike  = new Bike(id, name, uri, selected, stolen);

                bikes.add(bike);

            } while (cursor.moveToNext());
        }
        cursor.close();

        if(bikes.size() == 0){

            // add here a default bike and persist it
            final Bike firstBike = Bike.createDefaultBike();
            long id = insertBike(firstBike, false);
            firstBike.setId(id);
            bikes.add(firstBike);
        }

        return bikes;
    }

    public Bike getBike(final long bikeId){

        final ArrayList<Bike> bikes = getAllBikes();

        for(Bike bike : bikes){
            if(bike.getId() == bikeId){
                return bike;
            }
        }
        //bike id not found
        Log.w(TAG, "bike id "+bikeId+ " not found");
        return null;
    }

    public Bike getSelectedBike(){

        final ArrayList<Bike> bikes = getAllBikes();

        for(Bike bike : bikes){
            if(bike.isSelected()){
                return bike;
            }
        }
        //nothing selected ???
        Log.w(TAG, "no bike selected");
        if(bikes.size() > 0){
            return bikes.get(0);
        }

        return null;
    }

    public int deleteBike(final long index) {

        return db.delete(BIKES_TABLE,ID + "=?", new String[]{Long.toString(index)});

    }
}
