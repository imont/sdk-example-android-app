/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules.queries.entity;

import io.imont.cairo.events.Motion;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

public class AnyMotionSensorQuery extends AnyDeviceQuery {

    public static final String KEY = "ANY_MOTION_SENSOR";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withCondition(
                new Condition().withObject("util.deviceType(event.entityId)")
                        .withOperator(Condition.Operator.EQ)
                        .withValue(Motion.MOTION_FEATURE.getFQEventKey())
        );
    }
}
