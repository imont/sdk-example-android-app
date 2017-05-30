/*
 * Copyright 2017 IMONT Technologies Limited
 * Created by romanas on 26/04/2017.
 */
package io.imont.android.sdkdemo.rules.queries.entity;

import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

public class SpecificDeviceQuery extends AnyDeviceQuery {

    public static final String KEY = "SPECIFIC_DEVICE";

    private final String entityId;

    public SpecificDeviceQuery(final String entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withCondition(
                new Condition().withObject("event.entityId")
                        .withOperator(Condition.Operator.EQ)
                        .withValue(entityId)
        );
    }

}
