package com.conecta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.conecta.models.TrackerPos;
import com.conecta.restserver.R;

import java.util.List;


public class TrackerAdapter extends ArrayAdapter<TrackerPos> {

    public TrackerAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public TrackerAdapter(Context context, int resource, List<TrackerPos> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_layout_list_tracker, null);
        }

        TrackerPos p = getItem(position);

        if (p != null) {
            TextView pos = (TextView) v.findViewById(R.id.posTxtViewTracker);
            TextView time = (TextView) v.findViewById(R.id.timeTxtViewTracker);
            if (pos != null) {
                pos.setText(p.getPos());
            }

            if (time != null) {
                time.setText(p.getTime());
            }

        }

        return v;
    }

}