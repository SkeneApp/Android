package me.outi.whispr.skene_v4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by zaynetro on 10.8.2014.
 */
public class SkenesAdapter extends ArrayAdapter<Skene> {

    public SkenesAdapter(Context context, ArrayList<Skene> users) {
        super(context, R.layout.skene, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Skene skene = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.skene, parent, false);
        }
        // Lookup view for data population
        TextView text = (TextView) convertView.findViewById(R.id.text);
        TextView pubTime = (TextView) convertView.findViewById(R.id.pubTime);
        // Populate the data into the template view using the data object
        text.setText(skene.text);
        pubTime.setText(skene.readableTime());
        // Return the completed view to render on screen
        return convertView;
    }
}
