package me.outi.whispr.skene_v4;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.JarOutputStream;

/**
 * Created by zaynetro on 10.8.2014.
 */
public class Loader {
    Context context;
    private static final String DEBUG_TAG = "Loader";

    private static String PARSE_APP_ID;
    private static String PARSE_KEY;

    public Loader(Context context) {
        this.context = context;
    }
    public Loader() {}

    public static void setupKeys(String app_id, String key) {
        Loader.PARSE_APP_ID = app_id;
        Loader.PARSE_KEY = key;
    }

    public String mapData(JSONObject params) {
        String url = "https://api.parse.com/1/functions/map_data";
        return postJSON(url, params);
    }

    public String loadSkenes(JSONObject params) {
        String url = "https://api.parse.com/1/functions/get";
        return postJSON(url, params);
    }

    public String addSkene(JSONObject json) {
        String url = "https://api.parse.com/1/classes/message";
        return postJSON(url, json);
    }

    /**
     * General method to POST json's
     */
    private String postJSON(String url, JSONObject json) {
        String resultString = "";

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPostRequest = new HttpPost(url);

            StringEntity se;
            se = new StringEntity(json.toString());

            // Set HTTP parameters
            httpPostRequest.setEntity(se);
            httpPostRequest.setHeader("X-Parse-Application-Id", Loader.PARSE_APP_ID);
            httpPostRequest.setHeader("X-Parse-REST-API-Key", Loader.PARSE_KEY);
            httpPostRequest.setHeader("Content-Type", "application/json");

            long t = System.currentTimeMillis();
            HttpResponse response = (HttpResponse) httpClient.execute(httpPostRequest);
            Log.i(DEBUG_TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
                InputStream inStream = entity.getContent();

                // convert content stream to a String
                resultString = convertStreamToString(inStream);
                inStream.close();

                Log.i(DEBUG_TAG,"Result: " + resultString);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return resultString;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
