/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules;

import io.imont.lion.rules.schema.RuleConditionV1;

public abstract class EntityQuery implements RuleEngineQuery {
    public abstract RuleConditionV1 getCondition();
}
