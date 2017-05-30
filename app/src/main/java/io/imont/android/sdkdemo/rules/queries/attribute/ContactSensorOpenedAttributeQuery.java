/*
 * Copyright 2017 IMONT Technologies Limited
 * Created by romanas on 27/04/2017.
 */
package io.imont.android.sdkdemo.rules.queries.attribute;

import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.cairo.events.OpenClosed;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

import java.util.Arrays;

public class ContactSensorOpenedAttributeQuery extends AttributeQuery {

    public static final String KEY = "CONTACT_SENSOR_OPENED";

    public ContactSensorOpenedAttributeQuery(final Condition.Operator operator, final Object value) {
        super(operator, value);
    }

    public ContactSensorOpenedAttributeQuery(final RuleConditionV1 ruleConditionV1) {
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
                                .withValue(OpenClosed.OpenClosedValues.OPEN.toString())

        )));
    }
}