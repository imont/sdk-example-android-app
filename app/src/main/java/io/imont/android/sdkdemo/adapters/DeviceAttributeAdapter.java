/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.cairo.EventKey;
import io.imont.cairo.EventKeyRegistry;
import io.imont.cairo.events.Lock;
import io.imont.cairo.events.OnOff;
import io.imont.mole.client.Event;

import java.text.SimpleDateFormat;
import java.util.*;

import static io.imont.cairo.events.Lock.LockValue.LOCKED;

public class DeviceAttributeAdapter extends ArrayAdapter<Event> {

    public DeviceAttributeAdapter(final Context context, final List<Event> attributes) {
        super(context, -1, attributes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.device_attributes_in_list_view, parent, false);
        TextView key = (TextView) rowView.findViewById(R.id.attribute_key);
        TextView date = (TextView) rowView.findViewById(R.id.attribute_date);
        final TextView valueTV = (TextView) rowView.findViewById(R.id.attribute_value);

        final Event ev = getItem(position);
        if (ev == null) {
            throw new IllegalStateException("No item at position: " + position);
        }
        final EventKey eventType = EventKeyRegistry.getEventKey(ev.getType() != null ? ev.getType() : ev.getKey());

        key.setText(ev.getKey());
        String dateStr = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH).format(ev.getReportedDate());
        date.setText(dateStr);
        String value = ev.getValue();
        if (value != null && value.length() > 30) {
            String prefix = value.substring(0, 12);  // Some values such as the LION_DRIVER are huge, we should trim them
            String suffix = value.substring(value.length() -3);
            value = prefix + "..." + suffix;
        }
        Drawable rowBackground = null;
        if (eventType.equals(OnOff.ON_OFF_EVENT)) {
            if (ev.getValue().equals("1")) {
                value = "ON";
                rowBackground = getContext().getResources().getDrawable(R.drawable.switch_on_bg, null);
            } else {
                value = "OFF";
                rowBackground = getContext().getResources().getDrawable(R.drawable.switch_off_bg, null);
            }
        } else if (eventType.equals(Lock.LOCK_EVENT)) {
            if (ev.getValue().equals(LOCKED.name())) {
                rowBackground = getContext().getResources().getDrawable(R.drawable.switch_on_bg, null);
            } else {
                rowBackground = getContext().getResources().getDrawable(R.drawable.switch_off_bg, null);
            }
        }

        if (rowBackground != null) {
            rowView.setBackground(rowBackground);
        }

        if (eventType.getUnitOfMeasure() != null) {
            value += " " + eventType.getUnitOfMeasure();
        }
        valueTV.setText(value);
        return rowView;
    }
}
