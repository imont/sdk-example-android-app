/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;

import java.util.List;

public class VersionsAdapter extends ArrayAdapter<VersionsAdapter.AvailableVersion> {

    public static class AvailableVersion {
        private String version;
        private String filename;

        public AvailableVersion(final String version, final String filename) {
            this.version = version;
            this.filename = filename;
        }

        public String getVersion() {
            return version;
        }

        public String getFilename() {
            return filename;
        }
    }

    public VersionsAdapter(@NonNull final Context context, List<AvailableVersion> items) {
        super(context, -1, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.versions_in_list_view, parent, false);

        TextView version = (TextView) rowView.findViewById(R.id.upgrade_version);
        TextView filename = (TextView) rowView.findViewById(R.id.upgrade_file_name);

        AvailableVersion ver = getItem(position);
        if (ver == null) {
            throw new IllegalArgumentException("No such item at position " + position);
        }

        version.setText(ver.getVersion());
        filename.setText(ver.getFilename());

        return rowView;
    }

}
