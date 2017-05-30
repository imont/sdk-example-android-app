package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.cairo.EventKeyRegistry;
import io.imont.cairo.EventMetaKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventAttributesAdapter extends BaseAdapter {

    private final List metadata = new ArrayList();

    public EventAttributesAdapter(Map<EventMetaKey, String> metadata) {
        this.metadata.addAll(metadata.entrySet());
    }

    @Override
    public int getCount() {
        return metadata.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position) {
        return (Map.Entry) metadata.get(position);
    }

    @Override
    public long getItemId(int position) {
        //TODO
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Map.Entry<String, String> item = getItem(position);
        EventMetaKey metaKey = EventKeyRegistry.getEventMetaKey(item.getKey());

        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_attributes_in_list_view, parent, false);
        TextView key = (TextView) rowView.findViewById(R.id.attribute_key);
        TextView date = (TextView) rowView.findViewById(R.id.attribute_date);
        TextView valueTV = (TextView) rowView.findViewById(R.id.attribute_value);

        date.setText("");
        key.setText(item.getKey().toString());
        String value = item.getValue();

        if (metaKey.getUnitOfMeasure() != null) {
            value += " " + metaKey.getUnitOfMeasure();
        }
        valueTV.setText(value);

        return rowView;
    }
}
