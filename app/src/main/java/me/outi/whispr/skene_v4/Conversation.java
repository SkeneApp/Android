package me.outi.whispr.skene_v4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
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

    public Boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void sendMessage(View view) {
        Skene skene;
        JSONObject skeneJSON;
        Toast.makeText(this, "Sending message...", Toast.LENGTH_SHORT).show();

        if(isOnline() && mLatLng != null) {
            long delay = 0;
            skene = new Skene(mLatLng.latitude, mLatLng.longitude, mMessage.getText().toString(), delay);
            mCurSkene = skene;

            if(mParentSkene != null) mCurSkene.parentId = mParentSkene.id;

            skeneJSON = skene.getJSON();
            new PostJSONTask().execute(skeneJSON);
        }
    }

    public void loadChildrenSkenes(String parentId) {
        int count = 50;
        JSONObject params;

        if(isOnline() && mLatLng != null) {
            params = new JSONObject();
            try {
                params.put("count", count);
                params.put("parentId", parentId);
                new LoadSkenesTask().execute(params);
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
            return new Loader().addSkene(skene[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            mMessage.setText("");
            // Update messages
            try {
                JSONObject json = new JSONObject(result);
                if(json.has("error")) {
                    Toast.makeText(Conversation.this, "Error occurred", Toast.LENGTH_SHORT).show();
                } else {
                    mCurSkene.id = json.getString("objectId");
                    mCurSkene.createdAt = json.getString("createdAt");
                    adapter.add(mCurSkene);
                }

                mParentSkene = mCurSkene;
            } catch (JSONException e) {
                Toast.makeText(Conversation.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }


        }
    }

    /**
     * Task to download json and put to FeedFragment
     */
    private class LoadSkenesTask extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... params) {
            return new Loader().loadSkenes(params[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray jsonArray = json.getJSONArray("result");
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
