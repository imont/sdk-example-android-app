/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules;

import io.imont.lion.rules.schema.RuleConditionV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RuleCondition {

    private static final Logger logger = LoggerFactory.getLogger(RuleCondition.class);

    private EntityQuery entityQuery;
    private AttributeQuery attributeQuery;

    // TODO Assumes 1 or 2 conditions
    public static RuleCondition lookup(final List<RuleConditionV1> ruleConditions) {
        EntityQuery eq = null;
        AttributeQuery aq = null;
        try {
            for (RuleConditionV1 rc : ruleConditions) {
                EntityQuery entity = RuleMapper.toEntityQuery(rc);
                if (entity != null) {
                    eq = entity;
                    continue;
                }
                AttributeQuery attributeQuery = RuleMapper.toAttributeQuery(rc);
                if (attributeQuery != null) {
                    aq = attributeQuery;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to look up rule condition", e);
        }
        return new RuleCondition(eq, aq);
    }

    public RuleCondition(final EntityQuery entityQuery, final AttributeQuery attributeQuery) {
        this.entityQuery = entityQuery;
        this.attributeQuery = attributeQuery;
    }

    public EntityQuery getEntityQuery() {
        return entityQuery;
    }

    public AttributeQuery getAttributeQuery() {
        return attributeQuery;
    }

    public RuleConditionV1 getAttributeQueryCondition() {
        return attributeQuery.getCondition();
    }

    @Override
    public String toString() {
        return "RuleCondition{"
                + "entityQuery=" + entityQuery
                + ", attributeQuery=" + attributeQuery
                + '}';
    }
}
