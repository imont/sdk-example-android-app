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
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.imont.android.sdkdemo.R;
import io.imont.android.sdkdemo.rules.RuleCondition;
import io.imont.android.sdkdemo.rules.RuleEngineQuery;
import io.imont.android.sdkdemo.utils.Resources;
import io.imont.lion.rules.Rule;
import io.imont.lion.rules.schema.RuleConditionV1;

import java.util.Iterator;
import java.util.List;

public class RulesAdapter extends ArrayAdapter<Rule> {

    private final List<Rule> rules;

    public RulesAdapter(@NonNull final Context context, final List<Rule> rules) {
        super(context, -1, rules);
        this.rules = rules;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rule_in_list_view, parent, false);

        Rule rule = getItem(position);

        TextView name = (TextView) rowView.findViewById(R.id.rule_name);
        TextView conditions = (TextView) rowView.findViewById(R.id.rule_conditions);
        TextView actions = (TextView) rowView.findViewById(R.id.rule_actions);

        name.setText(rule.getName());
        conditions.setText(getCondition(rule.getCondition()));
        actions.setText(Resources.lookupStringResource(getContext(), String.format("action_%s", rule.getActionKey().toLowerCase())));

        return rowView;
    }

    private String getCondition(final RuleConditionV1 cond) {
        StringBuilder b = new StringBuilder("When ");
        if (cond.getAllOf().size() > 0) {
            b.append(getCondition(cond.getAllOf(), "AND"));
        } else if (cond.getAnyOf().size() > 0) {
            b.append(getCondition(cond.getAnyOf(), "OR"));
        }
        return b.toString();
    }

    private String getCondition(final List<RuleConditionV1> cond, final String separator) {
        StringBuilder b = new StringBuilder();
        Iterator<RuleConditionV1> iterator = cond.iterator();
        while (iterator.hasNext()) {
            RuleConditionV1 topLevel = iterator.next();
            RuleCondition condition = RuleCondition.lookup(topLevel.getAllOf());
            b.append(Resources.lookupStringResource(getContext(), String.format("cond_%s", getQueryKey(condition.getEntityQuery()))));
            b.append(" ");
            if (condition.getAttributeQuery() != null) {
                b.append(Resources.lookupStringResource(getContext(), String.format("cond_%s", getQueryKey(condition.getAttributeQuery()))));
                if (condition.getAttributeQuery().getOperator() != null) {
                    b.append(" ").append(condition.getAttributeQuery().getOperator())
                            .append(" ")
                            .append(condition.getAttributeQuery().getValue());
                }
            }
            if (iterator.hasNext()) {
                b.append(" ").append(separator).append(" ");
            }
        }
        return b.toString();
    }

    private String getQueryKey(final RuleEngineQuery query) {
        if (query != null) {
            return query.getKey().toLowerCase();
        }
        return "";
    }

}
