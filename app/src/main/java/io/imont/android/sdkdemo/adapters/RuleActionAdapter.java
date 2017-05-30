/*
 * Copyright 2017 IMONT Technologies Limited
 * Created by romanas on 26/04/2017.
 */
package io.imont.android.sdkdemo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.android.sdkdemo.rules.RuleAction;
import io.imont.android.sdkdemo.utils.Resources;

import java.util.Iterator;
import java.util.List;

public class RuleActionAdapter extends ArrayAdapter<RuleAction> {

    private final AdapterView.OnClickListener addListener;
    private final AdapterView.OnClickListener removeListener;

    private List<RuleAction> actions;

    public RuleActionAdapter(@NonNull final Context context, final List<RuleAction> actions,
                             final AdapterView.OnClickListener addListener,
                             final AdapterView.OnClickListener removeListener) {
        super(context, -1, actions);
        this.addListener = addListener;
        this.removeListener = removeListener;
        this.actions = actions;
    }

    @Override
    public void notifyDataSetChanged() {
        if (actions.size() == 0) {
            actions.add(new RuleAction(null, null)); // add a dummy one
        } else {
            Iterator<RuleAction> iterator = actions.iterator();
            while (iterator.hasNext()) {
                RuleAction c = iterator.next();
                if (c.getAction() == null) {
                    iterator.remove();
                    break;
                }
            }
        }
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rule_action_in_list_view, parent, false);

        View actionPlaceholder = rowView.findViewById(R.id.action_placeholder);
        View actionContent = rowView.findViewById(R.id.action_content);

        TextView name = (TextView) rowView.findViewById(R.id.action_name);
        Button addButton = (Button) rowView.findViewById(R.id.add_action_button);
        Button removeButton = (Button) rowView.findViewById(R.id.remove_action_button);

        addButton.setOnClickListener(addListener);

        // The tag is important - it's used to determine the index of the item to remove
        removeButton.setTag(position);
        removeButton.setOnClickListener(removeListener);

        RuleAction action = getItem(position);
        if (action.getAction() == null) {
            actionContent.setVisibility(View.GONE);
            actionPlaceholder.setVisibility(View.VISIBLE);
            removeButton.setVisibility(View.INVISIBLE);
        } else {
            actionContent.setVisibility(View.VISIBLE);
            actionPlaceholder.setVisibility(View.GONE);
            name.setText(Resources.lookupStringResource(getContext(), String.format("action_%s", action.getAction().toLowerCase())));
        }

        return rowView;
    }

}
