/*
 * Copyright 2017 IMONT Technologies Limited
 * Created by romanas on 26/04/2017.
 */
package io.imont.android.sdkdemo.rules.queries.attribute;

import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.cairo.events.PushButton;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

public class ButtonPushedAttributeQuery extends AttributeQuery {

    public static final String KEY = "BUTTON_PUSHED";

    public ButtonPushedAttributeQuery(final Condition.Operator operator, final Object value) {
        super(operator, value);
    }

    public ButtonPushedAttributeQuery(final RuleConditionV1 ruleConditionV1) {
        super(ruleConditionV1);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public RuleConditionV1 getCondition() {
        return new RuleConditionV1().withName(getKey()).withCondition(
                new Condition().withObject("event.key")
                    .withOperator(Condition.Operator.EQ)
                    .withValue(PushButton.PUSHED_EVENT.getFQEventKey())
        );
    }
}
