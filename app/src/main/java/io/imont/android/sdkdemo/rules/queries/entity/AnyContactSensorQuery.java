/*
 * Copyright 2017 IMONT Technologies
 * Created by romanas on 26/04/2017.
 */
package io.imont.android.sdkdemo.rules.queries.entity;

import io.imont.cairo.events.OpenClosed;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

public class AnyContactSensorQuery extends AnyDeviceQuery {

    public static final String KEY = "ANY_CONTACT_SENSOR";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withCondition(
                new Condition().withObject("util.deviceType(event.entityId)")
                        .withOperator(Condition.Operator.EQ)
                        .withValue(OpenClosed.OPEN_CLOSED_FEATURE.getFQEventKey())
        );
    }
}
