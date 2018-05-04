/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.ferret.client.status.ConnectivityEvent;

import java.text.SimpleDateFormat;
import java.util.*;

public class ConnEventListAdapter extends ArrayAdapter<ConnectivityEvent> {
    private final List<ConnectivityEvent> events;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);

    public ConnEventListAdapter(final Context context, final List<ConnectivityEvent> events) {
        super(context, -1, events);
        this.events = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.connectivity_events_in_list_view, parent, false);
        TextView time = (TextView) rowView.findViewById(R.id.conn_event_time);
        TextView name = (TextView) rowView.findViewById(R.id.conn_event_name);
        TextView desc = (TextView) rowView.findViewById(R.id.conn_event_description);
        TextView peer = (TextView) rowView.findViewById(R.id.conn_event_peer);

        ConnectivityEvent ev = events.get(position);
        if (ev == null) {
            throw new IllegalStateException("No item at position: " + position + ", list is: " + events);
        }
        time.setText(sdf.format(ev.getDate()));
        name.setText(ev.getType().toString());
        if (ev.getDescription() != null) {
            desc.setText(ev.getDescription());
        } else {
            desc.setVisibility(View.GONE);
        }
        peer.setText(ev.getRemote().getHashname().substring(0, 12) + "...");
        return rowView;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(events, new Comparator<ConnectivityEvent>() {
            @Override
            public int compare(final ConnectivityEvent lhs, final ConnectivityEvent rhs) {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        super.notifyDataSetChanged();
    }
}
