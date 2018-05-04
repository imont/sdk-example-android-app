/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules;

import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;

public abstract class AttributeQuery implements RuleEngineQuery {

    private Condition.Operator operator;
    private Object value;

    public AttributeQuery(final Condition.Operator operator, final Object value) {
        this.operator = operator;
        this.value = value;
    }

    public AttributeQuery(final RuleConditionV1 ruleConditionV1) {
        // Implemented in subclasses that need it
    }

    public abstract String getKey();
    public abstract RuleConditionV1 getCondition();

    public Condition.Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    protected void setOperator(final Condition.Operator operator) {
        this.operator = operator;
    }

    protected void setValue(final Object value) {
        this.value = value;
    }
}
