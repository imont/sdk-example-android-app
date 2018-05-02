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
import io.imont.ferret.client.mesh.Candidate;

import java.util.List;

public class FoundCandidatesAdapter extends ArrayAdapter<Candidate> {

    private List<Candidate> candidates;

    public FoundCandidatesAdapter(final Context context, final List<Candidate> candidates) {
        super(context, -1, candidates);
        this.candidates = candidates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.found_device_in_list_view, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.found_device_name);
        TextView description = (TextView) rowView.findViewById(R.id.found_device_description);
        name.setText(candidates.get(position).getFriendlyName());
        description.setText(candidates.get(position).getType());

        return rowView;
    }
}
