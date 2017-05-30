/*
 * Copyright 2017 IMONT Technologies Limited
 */
package io.imont.android.sdkdemo.rules.queries.entity;

import io.imont.android.sdkdemo.rules.EntityQuery;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

public class TimeRangeQuery extends EntityQuery {

    public static final String KEY = "TIME_RANGE";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        // Just a stub query here, actual comparison performed in attribute query
        return new RuleConditionV1().withName(getKey()).withCondition(
                new Condition().withObject("1").withOperator(Condition.Operator.EQ).withValue("1")
        );
    }
}
