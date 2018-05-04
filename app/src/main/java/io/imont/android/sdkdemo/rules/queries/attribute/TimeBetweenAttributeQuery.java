/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules.queries.attribute;

import io.imont.android.sdkdemo.rules.AttributeQuery;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TimeBetweenAttributeQuery extends AttributeQuery {

    public static final String KEY = "TIME_IS_BETWEEN";

    public TimeBetweenAttributeQuery(final Condition.Operator operator, final Object value) {
        super(operator, value);
    }

    public TimeBetweenAttributeQuery(final RuleConditionV1 ruleConditionV1) {
        super(ruleConditionV1);
        Object from = null;
        Object to = null;
        for (RuleConditionV1 subCondition : ruleConditionV1.getAllOf()) {
            if (Objects.equals(subCondition.getCondition().getOperator(), Condition.Operator.GTE)) {
                from = subCondition.getCondition().getValue();
            } else if (Objects.equals(subCondition.getCondition().getOperator(), Condition.Operator.LT)) {
                to = subCondition.getCondition().getValue();
            }
        }
        setValue(Arrays.asList(from, to));
        setOperator(Condition.Operator.EQ); // irrelevant
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RuleConditionV1 getCondition() {
        Object value = getValue();
        List param;
        if (value instanceof List) {
            param = (List<Integer>) getValue();
            return new RuleConditionV1().withName(getKey()).withAllOf(
                    Arrays.asList(
                            new RuleConditionV1().withCondition(
                                    new Condition().withObject("new Date().getHours()").withOperator(Condition.Operator.GTE).withValue(param.get(0))
                            ),
                            new RuleConditionV1().withCondition(
                                    new Condition().withObject("new Date().getHours()").withOperator(Condition.Operator.LT).withValue(param.get(1))
                            )
                    )
            );
        } else {
            throw new IllegalArgumentException("Params must be a list");
        }
    }
}
