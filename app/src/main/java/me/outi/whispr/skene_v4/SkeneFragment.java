package me.outi.whispr.skene_v4;

/**
 * Created by zaynetro on 10.8.2014.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SkeneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SkeneFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SkeneFragment extends android.support.v4.app.Fragment {
    private OnFragmentInteractionListener mListener;

    /**
     * Variables
     */
    private SkenesAdapter adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SkeneFragment.
     */
    public static SkeneFragment newInstance() {
        SkeneFragment fragment = new SkeneFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public SkeneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed, container, false);

        ListView listView = (ListView) view.findViewById(R.id.conversations);

        // Construct the data source
        ArrayList<Skene> arrayOfSkenes = new ArrayList<Skene>();
        // Create the adapter to convert the array to views
        this.adapter = new SkenesAdapter(getActivity(), arrayOfSkenes);

        // Attach the adapter to a ListView
        listView.setAdapter(this.adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Skene skene = (Skene) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), Conversation.class);
                intent.putExtra("Skene", skene);
                intent.putExtra("Answerable", true);
                intent.putExtra("New", false);

                Location location = mListener.getLocation();
                intent.putExtra("Latitude", location.getLatitude());
                intent.putExtra("Longitude", location.getLongitude());

                startActivity(intent);

            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public Boolean isOnline();
        public Location getLocation();
    }

    /**
     * #######################################################################
     * Methods
     */

    public void loadSkenes(Location location) {
        int count = 50;
        String radius = Integer.toString(Skene.radius);
        JSONObject params;

        if(mListener.isOnline()) {
            params = new JSONObject();
            try {
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());
                params.put("radius", radius);
                params.put("count", count);
                new LoadSkenesTask().execute(params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Task to download json and put to FeedFragment
     */
    private class LoadSkenesTask extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... params) {
            // params comes from the execute() call: params[0] is the url.
            return new Loader().loadSkenes(params[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray jsonArray = json.getJSONArray("result");
                adapter.clear();
                adapter.addAll(Skene.fromJSON(jsonArray));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
