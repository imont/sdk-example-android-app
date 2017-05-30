/*
 * Copyright 2017 IMONT Technologies
 * Created by romanas on 26/04/2017.
 */
package io.imont.android.sdkdemo.rules;

import io.imont.lion.rules.schema.RuleConditionV1;

public abstract class EntityQuery implements RuleEngineQuery {
    public abstract RuleConditionV1 getCondition();
}
