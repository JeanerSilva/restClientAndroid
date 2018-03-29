package com.conecta.restserver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class AlertAdapter extends ArrayAdapter<Alert> {

    public AlertAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public AlertAdapter(Context context, int resource, List<Alert> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_layout_list_alert, null);
        }

        Alert p = getItem(position);

        if (p != null) {
            TextView pos = (TextView) v.findViewById(R.id.posTxtView);
            TextView font = (TextView) v.findViewById(R.id.fontTxtView);
            TextView time = (TextView) v.findViewById(R.id.timeTxtView);
            if (pos != null) {
                pos.setText(p.getPos());
            }

            if (font != null) {
                font.setText(p.getFont());
            }

            if (time != null) {
                time.setText(p.getTime());
            }

        }

        return v;
    }

}