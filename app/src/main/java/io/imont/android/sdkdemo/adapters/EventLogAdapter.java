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
import io.imont.cairo.EventKey;
import io.imont.cairo.EventKeyRegistry;
import io.imont.mole.client.Event;
import io.imont.mole.client.RequestEvent;

import java.text.SimpleDateFormat;
import java.util.List;

public class EventLogAdapter extends ArrayAdapter<Event> {
    private final List<Event> attributes;

    public EventLogAdapter(final Context context, final List<Event> attributes) {
        super(context, -1, attributes);
        this.attributes = attributes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.event_attributes_in_list_view, parent, false);
        TextView keyTV = (TextView) rowView.findViewById(R.id.event_key);
        TextView valueTV = (TextView) rowView.findViewById(R.id.event_value);
        TextView dateTV = (TextView) rowView.findViewById(R.id.event_datetime);

        Event ev = attributes.get(position);
        if (ev == null) {
            throw new IllegalStateException("No item at position: " + position + ", list is: " + attributes);
        }
        final EventKey eventKey = EventKeyRegistry.getEventKey(ev.getKey());

        String keyText = eventKey.getFQEventKey();
        if (ev instanceof RequestEvent) {
            keyText += " - REQUEST";
        }
        keyTV.setText(keyText);


        if (ev.getValue() != null || ev instanceof RequestEvent) {
            String value = ev.getValue();
            if (eventKey.getUnitOfMeasure() != null) {
                value += " " + eventKey.getUnitOfMeasure();
            }
            valueTV.setText(value);
        } else {
            valueTV.setText("");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MMM-yyyy");
        dateTV.setText(sdf.format(ev.getReportedDate()));

        return rowView;
    }
}
