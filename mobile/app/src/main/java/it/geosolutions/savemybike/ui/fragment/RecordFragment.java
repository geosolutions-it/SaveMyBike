package it.geosolutions.savemybike.ui.fragment;

import android.app.Fragment;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.data.Constants;
import it.geosolutions.savemybike.data.Util;
import it.geosolutions.savemybike.model.Session;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.activity.SaveMyBikeActivity;

/**
 * Created by Robert Oehler on 25.10.17.
 *
 * A fragment containing the UI to switch between vehicles, start/stop a session and to show some session stats
 */

public class RecordFragment extends Fragment {

    private final static String TAG = "RecordFragment";

    @BindViews({
            R.id.mode_foot,
            R.id.mode_bike,
            R.id.mode_bus,
            R.id.mode_car,
            R.id.mode_moped,
            R.id.mode_train
            })
    List<View> modeViews;

    @BindView(R.id.record_button) ImageView recordButton;
    @BindView(R.id.simulate_tv) TextView simulateTV;
    @BindView(R.id.stats_dist) TextView distTV;
    @BindView(R.id.stats_time) TextView timeTV;
    @BindView(R.id.stats_row) LinearLayout statsRow;

    private boolean statsHidden = true;

    /**
     * inflates the view of this fragment and initializes it
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_record, container,false);
        ButterKnife.bind(this, view);

        invalidateUI(((SaveMyBikeActivity)getActivity()).getCurrentVehicle());

        return view;
    }

    /**
     * invalidates the UI of this fragment by
     * 1. applying the current session state (active or stopped)
     * 2. selecting the current vehicle @param vehicle
     * 3. invalidating session stats if possible/necessary
     * @param vehicle the current vehicle
     */
    public void invalidateUI(Vehicle vehicle){

        Session session = ((SaveMyBikeActivity) getActivity()).getCurrentSession();
        if(session == null){
            applySessionState(Session.SessionState.STOPPED);
        }else{
            applySessionState(session.getState());
        }
        selectVehicle(vehicle);
        invalidateSessionStats(session);
    }

    /**
     * applies the session state by changing the icon of the record button according to @param state
     * @param state the current state
     */
    public void applySessionState(final Session.SessionState state){

        switch (state){

            case ACTIVE:
                //switch to "Pause" UI
                recordButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause));
                break;
            case STOPPED:
                //switch to "Record" UI
                recordButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_record));
                break;
        }
    }

    /***
     * changes the icon of the vehicle @param vehicle to "selected" (white on black)
     * and the icons of all other vehicles to "unselected" (black on white)
     * @param vehicle the selected vehicle
     */
    public void selectVehicle(Vehicle vehicle){

        for(int i = 0; i < modeViews.size(); i++){

            if(vehicle.getType().ordinal() == i){

                //select
                Drawable mWrappedDrawable = DrawableCompat.wrap(((ImageView)modeViews.get(i)).getDrawable().mutate());
                DrawableCompat.setTint(mWrappedDrawable, ContextCompat.getColor(getActivity(), android.R.color.white));
                DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);
                ((ImageView)modeViews.get(i)).setImageDrawable(mWrappedDrawable);

                modeViews.get(i).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.mode_selected));
            }else{

                //unselect
                Drawable mWrappedDrawable = DrawableCompat.wrap(((ImageView)modeViews.get(i)).getDrawable().mutate());
                DrawableCompat.setTint(mWrappedDrawable, ContextCompat.getColor(getActivity(), android.R.color.black));
                DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);
                ((ImageView)modeViews.get(i)).setImageDrawable(mWrappedDrawable);

                modeViews.get(i).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.mode_bordered));
            }
        }
    }

    /**
     * invalidates the session stats UI of this fragment with @param session
     * @param session the session containing the data to invalidate
     */
    public void invalidateSessionStats(final Session session){

        if(session != null){

            if(statsHidden){
                statsRow.setVisibility(View.VISIBLE);
                statsHidden = false;
            }

            final double dist = session.getDistance();
            final long time = session.getOverallTime();

            if(((SaveMyBikeActivity)getActivity()).getConfiguration().metric){

                distTV.setText(String.format(Locale.US,"%.2f %s", dist / 1000f, Constants.UNIT_KM));
            }else{
                distTV.setText(String.format(Locale.US,"%.2f %s", dist / 1000f * Constants.KM_TO_MILES, Constants.UNIT_MI));
            }
            timeTV.setText(Util.longToTimeString(time));

        }else{
            if(!statsHidden){
                statsRow.setVisibility(View.INVISIBLE);
                statsHidden = true;
            }
        }
    }

    /**
     * click listener for vehicles and record button
     * a click on a vehicle changes the vehicle
     * a click on the record button starts/stops a session
     */
    @OnClick({
            R.id.mode_foot,
            R.id.mode_bike,
            R.id.mode_bus,
            R.id.mode_car,
            R.id.mode_moped,
            R.id.mode_train,
            R.id.record_button})
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.mode_foot:
                ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.FOOT, true);
                break;
            case R.id.mode_bike:
                ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.BIKE, true);
                break;
            case R.id.mode_bus:
                ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.BUS, true);
                break;
            case R.id.mode_car:
                ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.CAR, true);
                break;
            case R.id.mode_moped:
                ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.MOPED, true);
                break;
            case R.id.mode_train:
                ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.TRAIN, true);
                break;
            case R.id.record_button:

                //detect if we are currently recording or not
                Session currentSession = null;

                if(((SaveMyBikeActivity)getActivity()).getCurrentSession() != null){
                    currentSession = ((SaveMyBikeActivity)getActivity()).getCurrentSession();
                }

                if(currentSession != null && currentSession.getState() == Session.SessionState.ACTIVE){

                    //stop service
                    ((SaveMyBikeActivity)getActivity()).stopRecording();

                    applySessionState(Session.SessionState.STOPPED);
                } else {

                    ((SaveMyBikeActivity)getActivity()).startRecording();

                    applySessionState(Session.SessionState.ACTIVE);
                }
            break;
        }
    }

    /**
     * shows or hides the simulation view
     * @param simulate
     */
    public void applySimulate(boolean simulate){

        simulateTV.setVisibility(simulate ? View.VISIBLE : View.INVISIBLE);
    }

}
