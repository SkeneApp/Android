package me.outi.whispr.skene_v4;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zaynetro on 10.8.2014.
 */
/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SkeneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SkeneFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MapFragment extends android.support.v4.app.Fragment {
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
    private GoogleMap mMap;
    private SkenesAdapter adapter;

    private Location selectedLoc;
    private Circle curCircle;

    private ArrayList<Circle> curCircles = new ArrayList<Circle>();

    private ListView listView;

    private long mTimePast;
    private static final long timeInterval = 2000; // milliseconds

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SkeneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, number);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public MapFragment() {
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

        View view = inflater.inflate(R.layout.map, container, false);
        setUpMapIfNeeded();
        listView = (ListView) view.findViewById(R.id.conversations);
        // Construct the data source
        ArrayList<Skene> arrayOfSkenes = new ArrayList<Skene>();
        // Create the adapter to convert the array to views
        this.adapter = new SkenesAdapter(getActivity(), arrayOfSkenes);

        // Attach the adapter to a ListView
        listView.setAdapter(this.adapter);

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
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call  once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(mListener.getLocation());

                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));

                /**
                 * When map is clicked set up new location circle
                 */
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Location location = new Location("Map click");
                        location.setLatitude(latLng.latitude);
                        location.setLongitude(latLng.longitude);

                        setUpMap(location);
                        // TODO update feed
                    }
                });

                /**
                 * When map is moved load more map data
                 */
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {
                        if(new Date().getTime() > (mTimePast + timeInterval)) {
                            // If timeInterval past then update
                            updatePlaces();
                        }
                    }
                });

            }
        }
    }


    public void setUpMap(Location location) {
        if(location != null) {
            CircleOptions circle = new CircleOptions();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            circle.center(latLng);
            circle.radius(Skene.radius);
            circle.strokeColor(Color.argb(255, 52, 173, 125));
            circle.strokeWidth(4);
            circle.fillColor(Color.argb(80, 52, 173, 125));
            if(curCircle != null) {
                curCircle.remove();
            }
            curCircle = mMap.addCircle(circle);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            loadSkenes(location);
        }
    }

    /**
     * Draw skene circle
     * @param latLng
     */
    public void drawCircle(LatLng latLng) {
        int radius = 50;
        CircleOptions circle = new CircleOptions();
        circle.center(latLng);
        circle.radius(radius);
        circle.strokeColor(Color.argb(150, 204, 33, 33));
        circle.strokeWidth(10);
        circle.fillColor(Color.argb(100, 240, 96, 96));

        curCircles.add(mMap.addCircle(circle));
    }

    /**
     * Clear all circles on map
     */
    public void clearCircles() {
        for(Circle circle : curCircles) {
            circle.remove();
        }

        curCircles.clear();
    }

    public void appendSkenes(ArrayList<Skene> skenes) {
        adapter.addAll(skenes);

        for(Skene skene : skenes) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(skene.latitude, skene.longitude));
            mMap.addMarker(marker);
        }
    }

    public void updatePlaces() {
        Projection projection = mMap.getProjection();
        VisibleRegion visibleRegion = projection.getVisibleRegion();

        LatLng northEast = visibleRegion.latLngBounds.northeast;
        LatLng southWest = visibleRegion.latLngBounds.southwest;

        String count = "25";

        String url = "http://whispr.outi.me/api/get_map_data?count="
            + count
            + "&min_lat="
            + southWest.latitude
            + "&min_lon="
            + southWest.longitude
            + "&max_lat="
            + northEast.latitude
            + "&max_long="
            + northEast.longitude;

        if(mListener.isOnline()) {
            new GetMapDataTask().execute(url);
        }

        mTimePast = new Date().getTime();
    }

    /**
     * Task to get map data json array
     */
    private class GetMapDataTask extends AsyncTask<String, Void, String> {
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
                clearCircles();

                JSONArray jsonArray = new JSONArray(result);

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject point = jsonArray.getJSONObject(i);
                    drawCircle(new LatLng(point.getDouble("latitude"), point.getDouble("longitude")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load skenes for the chosen location
     * @param location
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

            new LoadSkenesTask().execute(stringUrl);
        }
    }

    /**
     * Task to download json and add to adapter
     */
    private class LoadSkenesTask extends AsyncTask<String, Void, String> {
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
            adapter.clear();

            try {
                JSONArray jsonArray = new JSONArray(result);
                adapter.addAll(Skene.fromJSON(jsonArray));

                listView.setSelectionAfterHeaderView();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
