package me.outi.whispr.skene_v4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zaynetro on 10.8.2014.
 */
//@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Skene implements Serializable {
    public long id;
    public double latitude;
    public double longitude;
    public String text;
    public long pubTime;
    public long pubDelay;
    public long parent_id;

    public static final int radius = 500;

    public Skene(JSONObject object) {
        try {
            this.id = object.getLong("id");
            this.latitude = object.getDouble("latitude");
            this.longitude = object.getDouble("longitude");
            this.text = object.getString("text");
            this.pubTime = object.getLong("pubTime");
            this.parent_id = object.getLong("parent_id");
            this.pubDelay = object.getLong("pubDelay");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Skene(double latitude, double longitude, String text, long delay) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
        this.pubTime = new Date().getTime() / 1000; // Servers saves in seconds

        this.pubDelay = delay;
        this.parent_id = 0;
    }

    public static ArrayList<Skene> fromJSON(JSONArray jsonObjects) {
        ArrayList<Skene> skenes = new ArrayList<Skene>();

        for(int i = 0; i < jsonObjects.length(); i++) {
            try {
                skenes.add(new Skene(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return skenes;
    }

    public String readableTime() {
        Date date = new Date(this.pubTime * 1000); // pubTime from the server is in seconds
        return DateFormat.getDateTimeInstance().format(date);
    }

    public JSONObject getJSON() {
        JSONObject skene = new JSONObject();
        try {
            skene.put("latitude", this.latitude);
            skene.put("longitude", this.longitude);
            skene.put("text", this.text);
            skene.put("pubTime", this.pubTime);
            skene.put("parent_id", this.parent_id);
            skene.put("pubDelay", this.pubDelay);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return skene;
    }
}