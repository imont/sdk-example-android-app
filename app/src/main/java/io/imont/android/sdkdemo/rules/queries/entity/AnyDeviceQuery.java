/*
 * Copyright 2017 IMONT Technologies
 * Created by romanas on 26/04/2017.
 */
package io.imont.android.sdkdemo.rules.queries.entity;

import io.imont.android.sdkdemo.rules.EntityQuery;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;


public class AnyDeviceQuery extends EntityQuery {

    public static final String KEY = "ANY_DEVICE";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withCondition(
                new Condition().withObject("1").withOperator(Condition.Operator.EQ).withValue("1")
        );
    }
}
