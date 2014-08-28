package me.outi.whispr.skene_v4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.Comparator;


public class Conversation extends Activity {

    private EditText mMessage;
    private Skene mCurSkene;
    private Skene mParentSkene;
    private SkenesAdapter adapter;
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mMessage = (EditText) findViewById(R.id.message);

        ListView listView = (ListView) findViewById(R.id.conversations);

        // Construct the data source
        ArrayList<Skene> arrayOfSkenes = new ArrayList<Skene>();
        // Create the adapter to convert the array to views
        this.adapter = new SkenesAdapter(this, arrayOfSkenes);

        // Attach the adapter to a ListView
        listView.setAdapter(this.adapter);

        Intent intent = getIntent();

        mLatLng = new LatLng(intent.getDoubleExtra("Latitude", 0.0), intent.getDoubleExtra("Longitude", 0.0));

        Boolean isNew = intent.getBooleanExtra("New", true);

        if(!isNew) {
            mParentSkene = (Skene) intent.getSerializableExtra("Skene");
            this.adapter.add(mParentSkene);
            loadChildrenSkenes(mParentSkene.id);
        }
    }

    public void sendMessage(View view) {
        Skene skene;
        JSONObject skeneJSON;
        Toast.makeText(this, "Send message: " + mMessage.getText().toString(), Toast.LENGTH_SHORT).show();

        if(mLatLng != null) {
            long delay = 0;
            skene = new Skene(mLatLng.latitude, mLatLng.longitude, mMessage.getText().toString(), delay);
            mCurSkene = skene;

            if(mParentSkene != null) mCurSkene.parent_id = mParentSkene.id;

            skeneJSON = skene.getJSON();

            if(isOnline()) {
                new PostJSONTask().execute(skeneJSON);
            }
        }
    }

    public Boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void loadChildrenSkenes(Long parent_id) {
        String count = "50";
        String radius = Integer.toString(Skene.radius);

        if(isOnline() && mLatLng != null) {
            String stringUrl = "http://whispr.outi.me/api/get?count="
                    + count
                    + "&lat="
                    + Double.toString(mLatLng.latitude)
                    + "&long="
                    + Double.toString(mLatLng.longitude)
                    + "&radius="
                    + radius
                    + "&parent_id="
                    + parent_id;

            new DownloadUrlTask().execute(stringUrl);
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
            mMessage.setText("");
            // Update messages
            try {
                mCurSkene.id = Long.parseLong(result.trim(), 10);
                adapter.add(mCurSkene);

                mParentSkene = mCurSkene;
            } catch (Exception e) {
                Toast.makeText(Conversation.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }


        }
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

                adapter.sort(new Comparator<Skene>() {
                    @Override
                    public int compare(Skene skene, Skene skene2) {
                        return (int) (skene.pubTime - skene2.pubTime);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
