/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 27/06/2016.
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
import io.imont.cairo.events.OnOff;
import io.imont.mole.client.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeviceAttributeAdapter extends ArrayAdapter<Event> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceAttributeAdapter.class);

    private final List<Event> attributes;

    public DeviceAttributeAdapter(final Context context, final List<Event> attributes) {
        super(context, -1, attributes);
        this.attributes = attributes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_attributes_in_list_view, parent, false);
        TextView key = (TextView) rowView.findViewById(R.id.attribute_key);
        TextView date = (TextView) rowView.findViewById(R.id.attribute_date);
        TextView valueTV = (TextView) rowView.findViewById(R.id.attribute_value);

        final Event ev = attributes.get(position);
        if (ev == null) {
            throw new IllegalStateException("No item at position: " + position + ", list is: " + attributes);
        }
        final EventKey eventKey = EventKeyRegistry.getEventKey(ev.getKey());

        key.setText(eventKey.getFQEventKey());
        date.setText(ev.getReportedDate().toString());
        String value = ev.getValue();
        if (eventKey.equals(OnOff.ON_OFF_EVENT)) {
            if (ev.getValue().equals("1")) {
                value = "ON";
                rowView.setBackground(getContext().getResources().getDrawable(R.drawable.switch_on_bg, null));
            } else {
                value = "OFF";
                rowView.setBackground(getContext().getResources().getDrawable(R.drawable.switch_off_bg, null));
            }
        }

        if (eventKey.getUnitOfMeasure() != null) {
            value += " " + eventKey.getUnitOfMeasure();
        }
        valueTV.setText(value);
        return rowView;
    }
}
