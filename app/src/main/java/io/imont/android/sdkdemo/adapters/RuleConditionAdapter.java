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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.android.sdkdemo.rules.RuleCondition;
import io.imont.android.sdkdemo.utils.Resources;

import java.util.Iterator;
import java.util.List;

public class RuleConditionAdapter extends ArrayAdapter<RuleCondition> {

    private final AdapterView.OnClickListener addListener;
    private final AdapterView.OnClickListener removeListener;

    private List<RuleCondition> conditions;

    public RuleConditionAdapter(@NonNull final Context context, final List<RuleCondition> conditions,
                                final AdapterView.OnClickListener addListener,
                                final AdapterView.OnClickListener removeListener) {
        super(context, -1, conditions);
        this.addListener = addListener;
        this.removeListener = removeListener;
        this.conditions = conditions;
    }

    // This actively adds or removes the empty condition which is used as a placeholder
    @Override
    public void notifyDataSetChanged() {
        if (conditions.size() == 0) {
            conditions.add(new RuleCondition(null, null)); // add a dummy one
        } else {
            Iterator<RuleCondition> iterator = conditions.iterator();
            while (iterator.hasNext()) {
                RuleCondition c = iterator.next();
                if (c.getEntityQuery() == null) {
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
        View rowView = inflater.inflate(R.layout.rule_condition_in_list_view, parent, false);

        View conditionPlaceholder = rowView.findViewById(R.id.condition_placeholder);
        View conditionContent = rowView.findViewById(R.id.condition_content);

        TextView entityName = (TextView) rowView.findViewById(R.id.entity_name);
        TextView eventName = (TextView) rowView.findViewById(R.id.event_name);
        Button addButton = (Button) rowView.findViewById(R.id.add_condition_button);
        Button removeButton = (Button) rowView.findViewById(R.id.remove_condition_button);

        addButton.setOnClickListener(addListener);

        // The tag is important - it's used to determine the index of the item to remove
        removeButton.setTag(position);
        removeButton.setOnClickListener(removeListener);

        RuleCondition cond = getItem(position);
        if (cond.getEntityQuery() == null) {
            conditionContent.setVisibility(View.GONE);
            conditionPlaceholder.setVisibility(View.VISIBLE);
            removeButton.setVisibility(View.INVISIBLE);
        } else {
            conditionPlaceholder.setVisibility(View.GONE);
            conditionContent.setVisibility(View.VISIBLE);
            entityName.setText(Resources.lookupStringResource(getContext(), String.format("cond_%s", cond.getEntityQuery().getKey().toLowerCase())));
            eventName.setText(Resources.lookupStringResource(getContext(), String.format("cond_%s", cond.getAttributeQuery().getKey().toLowerCase())));
        }
        return rowView;
    }


}
