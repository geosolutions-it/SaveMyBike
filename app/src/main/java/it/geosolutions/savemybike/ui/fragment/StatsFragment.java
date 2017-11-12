package it.geosolutions.savemybike.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.geosolutions.savemybike.R;
import it.geosolutions.savemybike.data.Util;
import it.geosolutions.savemybike.data.db.SMBDatabase;
import it.geosolutions.savemybike.model.Session;

/**
 * Created by Robert Oehler on 25.10.17.
 *
 */

public class StatsFragment extends Fragment {

    private SessionAdapter adapter;

    private TextView overallDistanceTV;
    private TextView overallTimeTV;
    private TextView overallElevTV;

    private LinearLayout progress;
    private LinearLayout content;

    /**
     * inflate and setup the view of this fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stats, container,false);

        content  = view.findViewById(R.id.content_layout);
        progress = view.findViewById(R.id.progress_layout);

        overallDistanceTV = view.findViewById(R.id.distance_overall);
        overallTimeTV     = view.findViewById(R.id.time_overall);
        overallElevTV     = view.findViewById(R.id.elev_overall);

        final ListView listView = view.findViewById(R.id.sessions_list);
        adapter = new SessionAdapter(getActivity(), R.layout.item_session, new ArrayList<Session>());
        listView.setAdapter(adapter);

        invalidateSessions();

        return view;
    }

    /**
     * loads the locally available sessions from the database and invalidates the UI
     */
    private void invalidateSessions() {

        new AsyncTask<Void,Void,ArrayList<Session>>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                showProgress(true);
            }

            @Override
            protected ArrayList<Session> doInBackground(Void... voids) {

                ArrayList<Session> sessions = null;
                final SMBDatabase database = new SMBDatabase(getActivity());
                try{

                    database.open();
                    sessions = database.getAllSessions();

                }finally {
                    database.close();
                }

                return sessions;
            }

            @Override
            protected void onPostExecute(ArrayList<Session> sessions) {
                super.onPostExecute(sessions);

                showProgress(false);

                double dist = 0, elev = 0;
                long time = 0;

                for(Session session : sessions){
                    dist += session.getDistance();
                    time += session.getOverallTime();
                    elev += session.getOverallElevation();
                }

                overallDistanceTV.setText(String.format(Locale.US,"%.1f km",dist / 1000f ));
                overallTimeTV.setText(Util.longToTimeString(time));
                overallElevTV.setText(String.format(Locale.US,"%.0f m", elev));

                adapter.addAll(sessions);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    /**
     * Switches the UI of this screen to show either the progress UI or the content
     * @param show if true shows the progress UI and hides content, if false the other way around
     */
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        progress.setVisibility(View.VISIBLE);
        progress.animate().setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progress.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });

        content.setVisibility(View.VISIBLE);
        content.animate().setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        content.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

    }

    /**
     * adapter for sessions
     */
    private class SessionAdapter extends ArrayAdapter<Session> {

        private	int resource;

        SessionAdapter(final Context context, int textViewResourceId, List<Session> sessions){
            super(context, textViewResourceId, sessions);

            resource = textViewResourceId;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            RelativeLayout view;

            if(convertView == null){
                view = new RelativeLayout(getContext());
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(resource, view,true);
            }else{
                view = (RelativeLayout) convertView;
            }

            final Session session = getItem(position);

            final TextView nameTV = view.findViewById(R.id.name_value);
            final TextView distanceTV = view.findViewById(R.id.dist_value);
            final TextView dataTV = view.findViewById(R.id.data_value);

            if(session.getName() != null) {
                nameTV.setText(session.getName());
            }else{
                nameTV.setText("session wo name");
            }
            //TODO always metric ?
            distanceTV.setText(String.format(Locale.US,"%.1f km", (session.getDistance() / 1000f)));
            dataTV.setText(String.format(Locale.US,"%d", session.getDataPoints().size()));

            return view;
        }
    }
}
