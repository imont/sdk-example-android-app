/*
 * Copyright 2017 IMONT Technologies Limited
 */
package io.imont.android.sdkdemo.rules;

import io.imont.android.sdkdemo.rules.queries.attribute.*;
import io.imont.android.sdkdemo.rules.queries.entity.*;
import io.imont.lion.rules.schema.Condition;
import io.imont.lion.rules.schema.RuleConditionV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RuleMapper {

    private static final Logger logger = LoggerFactory.getLogger(RuleMapper.class);

    private static final Map<String, List<String>> QUERIES = new HashMap<>();

    private static final Map<String, Class<? extends EntityQuery>> ENTITY_QUERIES = new HashMap<>();
    private static final Map<String, Class<? extends AttributeQuery>> ATTRIBUTE_QUERIES = new HashMap<>();

    static {
        List<String> globalQueries = Arrays.asList(
                AbsentAttributeQuery.KEY,
                PresentAttributeQuery.KEY,
                TemperatureAttributeQuery.KEY
        );

        QUERIES.put(AnyButtonQuery.KEY,
                makeQueryList(globalQueries, ButtonPushedAttributeQuery.KEY));
        QUERIES.put(AnyContactSensorQuery.KEY,
                makeQueryList(globalQueries, ContactSensorClosedAttributeQuery.KEY, ContactSensorOpenedAttributeQuery.KEY));
        QUERIES.put(AnyDeviceQuery.KEY,
                globalQueries);
        QUERIES.put(AnyMotionSensorQuery.KEY,
                makeQueryList(globalQueries, MotionDetectedAttributeQuery.KEY));
        QUERIES.put(TimeRangeQuery.KEY, Collections.singletonList(TimeBetweenAttributeQuery.KEY));
//        QUERIES.put(SpecificButtonQuery.KEY,
//                makeQueryList(globalQueries, ButtonPushedAttributeQuery.KEY));
//        QUERIES.put(SpecificDeviceQuery.KEY,
//                globalQueries);

        ENTITY_QUERIES.put(AnyButtonQuery.KEY, AnyButtonQuery.class);
        ENTITY_QUERIES.put(AnyContactSensorQuery.KEY, AnyContactSensorQuery.class);
        ENTITY_QUERIES.put(AnyDeviceQuery.KEY, AnyDeviceQuery.class);
        ENTITY_QUERIES.put(AnyMotionSensorQuery.KEY, AnyMotionSensorQuery.class);
        ENTITY_QUERIES.put(SpecificButtonQuery.KEY, SpecificButtonQuery.class);
        ENTITY_QUERIES.put(SpecificDeviceQuery.KEY, SpecificDeviceQuery.class);
        ENTITY_QUERIES.put(TimeRangeQuery.KEY, TimeRangeQuery.class);

        ATTRIBUTE_QUERIES.put(AbsentAttributeQuery.KEY, AbsentAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(ButtonPushedAttributeQuery.KEY, ButtonPushedAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(ContactSensorClosedAttributeQuery.KEY, ContactSensorClosedAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(ContactSensorOpenedAttributeQuery.KEY, ContactSensorOpenedAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(MotionDetectedAttributeQuery.KEY, MotionDetectedAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(PresentAttributeQuery.KEY, PresentAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(TemperatureAttributeQuery.KEY, TemperatureAttributeQuery.class);
        ATTRIBUTE_QUERIES.put(TimeBetweenAttributeQuery.KEY, TimeBetweenAttributeQuery.class);
    }

    public static List<String> getTopLevelQueries() {
        return new ArrayList<>(QUERIES.keySet());
    }

    public static List<String> getSubQueries(final String topLevelQueryKey) {
        return QUERIES.get(topLevelQueryKey);
    }

    public static EntityQuery toEntityQuery(final String key) {
        Class<? extends EntityQuery> clazz = ENTITY_QUERIES.get(key);
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.newInstance();
        } catch(Exception e) {
            logger.warn("Unable to resolve entity query {}", key, e);
        }
        return null;
    }

    public static EntityQuery toEntityQuery(final RuleConditionV1 ruleConditionV1) {
        return toEntityQuery(ruleConditionV1.getName());
    }

    public static AttributeQuery toAttributeQuery(final String key, final Condition.Operator operator, final Object value) {
        try {
            return ATTRIBUTE_QUERIES.get(key).getConstructor(Condition.Operator.class, Object.class).newInstance(operator, value);
        } catch(Exception e) {
            logger.warn("Unable to resolve attribute query {}", key, e);
        }
        return null;
    }


    public static AttributeQuery toAttributeQuery(final RuleConditionV1 ruleConditionV1) {
        Class<? extends AttributeQuery> aq = ATTRIBUTE_QUERIES.get(ruleConditionV1.getName());
        if (aq == null) {
            return null;
        }
        try {
            return aq.getConstructor(RuleConditionV1.class).newInstance(ruleConditionV1);
        } catch (Exception e) {
            logger.warn("Unable to resolve attribute query {}", ruleConditionV1.getName(), e);
        }
        return null;
    }


    private static List<String> makeQueryList(final List<String> globals, final String... query) {
        List<String> res = new ArrayList<>();
        res.addAll(globals);
        if (query != null) {
            for (String q : query) {
                res.add(q);
            }
        }
        return res;
    }

}
