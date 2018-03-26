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
            TextView giro = (TextView) v.findViewById(R.id.giroTxtView);
            TextView mov = (TextView) v.findViewById(R.id.movTxtView);
            TextView time = (TextView) v.findViewById(R.id.timeTxtView);
            if (pos != null) {
                pos.setText(p.getPos());
            }

            if (giro != null) {
                giro.setText(p.getGiro());
            }

            if (mov != null) {
                mov.setText(p.getMov());
            }
            if (time != null) {
                time.setText(p.getTime());
            }

        }

        return v;
    }

}