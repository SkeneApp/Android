package me.outi.whispr.skene_v4;

/**
 * Created by zaynetro on 10.8.2014.
 */

import android.app.Activity;
import android.app.Fragment;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Variables
     */
    private SkenesAdapter adapter;
    private TextView message;
    private Skene curSkene;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SkeneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SkeneFragment newInstance() {
        SkeneFragment fragment = new SkeneFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, number);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SkeneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed, container, false);

        ListView listView = (ListView) view.findViewById(R.id.conversations);
        message = (TextView) view.findViewById(R.id.message);

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
                Toast.makeText(getActivity(), "Clicked: " + skene.text, Toast.LENGTH_SHORT).show();
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
        public MapFragment getMapFragment();
    }

    /**
     * #######################################################################
     * Methods
     */

    public void loadSkenes(Location location) {
        String count = "50";
        String radius = Integer.toString(Skene.radius);

        if(mListener.isOnline()) {
            String stringUrl = "http://whispr.outi.me/api/get?count="
                    + count
                    + "&lat="
                    + Double.toString(location.getLatitude())
                    + "&long="
                    + Double.toString(location.getLongitude())
                    +"&radius="
                    + radius;

            new DownloadUrlTask().execute(stringUrl);
        }
    }

    public void sendMessage(View view, Location location) {
        Skene skene;
        JSONObject skeneJSON;
        Toast.makeText(getActivity(), "Send message: " + message.getText().toString(), Toast.LENGTH_SHORT).show();

        if(location != null) {
            long delay = 0;
            skene = new Skene(location.getLatitude(), location.getLongitude(), message.getText().toString(), delay);
            this.curSkene = skene;
            skeneJSON = skene.getJSON();

            if(mListener.isOnline()) {
                new PostJSONTask().execute(skeneJSON);
            }
        }
    }

    public ArrayList<Skene> getSkenes() {
        ArrayList<Skene> skenes = new ArrayList<Skene>();
        int i;

        for(i = 0; i < adapter.getCount(); i++) {
            skenes.add(adapter.getItem(i));
        }

        return skenes;
    }

    /**
     * Task to download json and put to FeedFragment
     */
    private class DownloadUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return new Loader().downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                adapter.addAll(Skene.fromJSON(jsonArray));

                //mListener.getMapFragment().appendSkenes(getSkenes());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Task to send skene
     */
    private class PostJSONTask extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... skene) {

            // params comes from the execute() call: params[0] is the url.
            return new Loader().postJSON(skene[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            message.setText("");
            // Update messages
            try {
                curSkene.id = Long.parseLong(result.trim(), 10);
                adapter.insert(curSkene, 0);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }


        }
    }
}
