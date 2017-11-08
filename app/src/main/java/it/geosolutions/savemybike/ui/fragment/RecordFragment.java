package it.geosolutions.savemybike.ui.fragment;

import android.app.Fragment;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.model.Session;
import it.geosolutions.savemybike.model.Vehicle;
import it.geosolutions.savemybike.ui.activity.SaveMyBikeActivity;

/**
 * Created by Robert Oehler on 25.10.17.
 *
 */

public class RecordFragment extends Fragment {

    private final static String TAG = "RecordFragment";

    private ArrayList<View> modeViews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_record, container,false);

        getModeViews().add(view.findViewById(R.id.mode_foot));
        getModeViews().add(view.findViewById(R.id.mode_bike));
        getModeViews().add(view.findViewById(R.id.mode_bus));
        getModeViews().add(view.findViewById(R.id.mode_car));

        for(View iv : modeViews){

            iv.setOnClickListener(modeClickListener);
        }
        view.findViewById(R.id.record_button).setOnClickListener(modeClickListener);

        selectVehicle(((SaveMyBikeActivity)getActivity()).getCurrentVehicle());

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Log.i(TAG, "onHiddenChanged, hidden : "+ hidden);

        if(!hidden){
            selectVehicle(((SaveMyBikeActivity)getActivity()).getCurrentVehicle());
        }
    }

    public void selectVehicle(Vehicle vehicle){

        Log.i(TAG, "selecting "+vehicle.getType().toString());

        for(int i = 0; i < getModeViews().size(); i++){

            if(vehicle.getType().ordinal() == i){

                //select
                Drawable mWrappedDrawable = DrawableCompat.wrap(((ImageView)getModeViews().get(i)).getDrawable().mutate());
                DrawableCompat.setTint(mWrappedDrawable, ContextCompat.getColor(getActivity(), android.R.color.white));
                DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);
                ((ImageView)getModeViews().get(i)).setImageDrawable(mWrappedDrawable);

                getModeViews().get(i).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.mode_selected));
            }else{

                //unselect
                Drawable mWrappedDrawable = DrawableCompat.wrap(((ImageView)getModeViews().get(i)).getDrawable().mutate());
                DrawableCompat.setTint(mWrappedDrawable, ContextCompat.getColor(getActivity(), android.R.color.black));
                DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);
                ((ImageView)getModeViews().get(i)).setImageDrawable(mWrappedDrawable);

                getModeViews().get(i).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.mode_bordered));
            }
        }
    }

    private View.OnClickListener modeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.mode_foot:
                    ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.FOOT);
                    break;
                case R.id.mode_bike:
                    ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.BIKE);
                    break;
                case R.id.mode_bus:
                    ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.BUS);
                    break;
                case R.id.mode_car:
                    ((SaveMyBikeActivity)getActivity()).changeVehicle(Vehicle.VehicleType.CAR);
                    break;
                case R.id.record_button:

                    //TODO detect if we are currently recording or not
                    boolean activeSession = false;
                    Session currentSession = null;

                    if(((SaveMyBikeActivity)getActivity()).getService() != null &&
                            ((SaveMyBikeActivity)getActivity()).getService().getSession() != null){
                        currentSession = ((SaveMyBikeActivity)getActivity()).getService().getSession();
                        activeSession = currentSession.getState() == Session.SessionState.ACTIVE;
                    }

                    if(activeSession){

                        //stop service
                        if(((SaveMyBikeActivity)getActivity()).getService() != null){
                            ((SaveMyBikeActivity)getActivity()).getService().stopSession();
                        }

                        getActivity().unbindService(((SaveMyBikeActivity) getActivity()).getServiceConnection());

                        //switch to "Record" UI
                    } else {

                        ((SaveMyBikeActivity)getActivity()).startRecording();

                        //switch to "Pause" UI
                        ((ImageView) view).setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause));
                    }
                    break;
            }
        }
    };

    public ArrayList<View> getModeViews() {

        if(modeViews == null){
            modeViews = new ArrayList<>();
        }

        return modeViews;
    }
}
