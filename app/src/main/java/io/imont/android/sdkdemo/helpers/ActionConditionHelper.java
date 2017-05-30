/*
 * Copyright 2017 IMONT Technologies Limited
 */
package io.imont.android.sdkdemo.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import io.imont.android.sdkdemo.R;
import io.imont.android.sdkdemo.rules.queries.attribute.TemperatureAttributeQuery;
import io.imont.android.sdkdemo.rules.queries.attribute.TimeBetweenAttributeQuery;
import io.imont.lion.rules.schema.Condition;

import java.util.Arrays;
import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ActionConditionHelper {

    public abstract static class ParamConfigHolder {
        public final View view;

        public ParamConfigHolder(final View view) {
            this.view = view;
        }

        public abstract Condition.Operator getOperator();
        public abstract Object getValue();

    }

    public static ParamConfigHolder getParamConfigView(final String conditionKey, final Context context) {
        if (Objects.equals(conditionKey, TemperatureAttributeQuery.KEY)) {
            return handleTemperature(context);
        } else if (Objects.equals(conditionKey, TimeBetweenAttributeQuery.KEY)) {
            return handleTimeBetween(context);
        }
        return null;
    }


    private static ParamConfigHolder handleTemperature(final Context context) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.rule_condition_params_numeric_comparison, null);
        final Spinner condition = (Spinner) view.findViewById(R.id.numeric_condition);
        final EditText value = (EditText) view.findViewById(R.id.numeric_condition_value);

        return new ParamConfigHolder(view) {
            @Override
            public Condition.Operator getOperator() {
                String conditionValue = (String) condition.getSelectedItem();
                switch (conditionValue) {
                    case "more than":
                        return Condition.Operator.GTE;
                    case "less than":
                        return Condition.Operator.LTE;
                    case "equals":
                        return Condition.Operator.EQ;
                    default:
                        return Condition.Operator.EQ;
                }
            }

            @Override
            public Object getValue() {
                return value.getText().toString();
            }
        };
    }

    private static ParamConfigHolder handleTimeBetween(final Context context) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.rule_condition_params_between_numbers, null);
        final EditText value1 = (EditText) view.findViewById(R.id.numeric_between_one);
        final EditText value2 = (EditText) view.findViewById(R.id.numeric_between_two);

        return new ParamConfigHolder(view) {
            @Override
            public Condition.Operator getOperator() {
                return null;
            }

            @Override
            public Object getValue() {
                return Arrays.asList(value1.getText().toString(), value2.getText().toString());
            }
        };
    }

}
