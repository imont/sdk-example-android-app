/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules.queries.attribute;

import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.cairo.events.Temperature;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

import java.util.Arrays;
import java.util.Objects;

import static io.imont.lion.rules.schema.Condition.Operator.*;

public class TemperatureAttributeQuery extends AttributeQuery {

    public static final String KEY = "DEVICE_TEMPERATURE";

    public TemperatureAttributeQuery(final Condition.Operator operator, final Object value) {
        super(operator, value);
    }

    public TemperatureAttributeQuery(final RuleConditionV1 ruleConditionV1) {
        super(ruleConditionV1);
        for (RuleConditionV1 subCondition : ruleConditionV1.getAllOf()) {
            if (Objects.equals(subCondition.getCondition().getObject(), "parseFloat(event.value)")) {
                setValue(subCondition.getCondition().getValue());
                setOperator(subCondition.getCondition().getOperator());
                return;
            }
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        RuleConditionV1 historyRule;
        int ruleValue = Integer.valueOf(getValue().toString());
        if (getOperator() == LT || getOperator() == LTE) {
            historyRule = new RuleConditionV1().withCondition(
                    new Condition().withObject("parseFloat(util.previousValue(event))").withOperator(GT).withValue(ruleValue)
            );
        } else {
            historyRule = new RuleConditionV1().withCondition(
                    new Condition().withObject("parseFloat(util.previousValue(event))").withOperator(LT).withValue(ruleValue)
            );
        }
        return new RuleConditionV1().withName(getKey()).withAllOf(
                Arrays.asList(
                        new RuleConditionV1().withCondition(
                                new Condition().withObject("event.key").withOperator(EQ).withValue(Temperature.AMBIENT_TEMPERATURE_EVENT.getFQEventKey())
                        ),
                        new RuleConditionV1().withCondition(
                                new Condition().withObject("parseFloat(event.value)").withOperator(getOperator()).withValue(ruleValue)
                        ),
                        historyRule
                )
        );
    }
}
