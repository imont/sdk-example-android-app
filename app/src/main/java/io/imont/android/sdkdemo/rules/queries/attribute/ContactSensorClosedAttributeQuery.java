/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules.queries.attribute;


import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.cairo.events.OpenClosed;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

import java.util.Arrays;

public class ContactSensorClosedAttributeQuery extends AttributeQuery {

    public static final String KEY = "CONTACT_SENSOR_CLOSED";

    public ContactSensorClosedAttributeQuery(final Condition.Operator operator, final Object value) {
        super(operator, value);
    }

    public ContactSensorClosedAttributeQuery(final RuleConditionV1 ruleConditionV1) {
        super(ruleConditionV1);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withAllOf(Arrays.asList(
                new RuleConditionV1().withCondition(
                        new Condition().withObject("event.key")
                                .withOperator(Condition.Operator.EQ)
                                .withValue(OpenClosed.OPEN_CLOSED_EVENT.getFQEventKey())),
                new RuleConditionV1().withCondition(
                        new Condition().withObject("event.value")
                                .withOperator(Condition.Operator.EQ)
                                .withValue(OpenClosed.OpenClosedValue.CLOSED.toString())

                )));
    }
}
