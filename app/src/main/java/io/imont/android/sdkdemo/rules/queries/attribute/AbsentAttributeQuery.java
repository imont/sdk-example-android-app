/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules.queries.attribute;

import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.cairo.events.Hardware;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

import java.util.Arrays;

import static io.imont.lion.rules.schema.Condition.Operator.EQ;

public class AbsentAttributeQuery extends AttributeQuery {

    public static final String KEY = "DEVICE_ABSENT";

    public AbsentAttributeQuery(final Condition.Operator operator, final Object value) {
        super(operator, value);
    }

    public AbsentAttributeQuery(final RuleConditionV1 ruleConditionV1) {
        super(ruleConditionV1);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withAllOf(
                Arrays.asList(
                        new RuleConditionV1().withCondition(
                                new Condition().withObject("event.key").withOperator(EQ).withValue(Hardware.DEVICE_PRESENCE_EVENT.getFQEventKey())
                        ),
                        new RuleConditionV1().withCondition(
                                new Condition().withObject("event.value").withOperator(EQ).withValue(Hardware.Presence.ABSENT.toString())
                        )
                )
        );
    }
}
