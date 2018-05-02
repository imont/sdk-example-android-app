/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules.queries.entity;

public class SpecificButtonQuery extends SpecificDeviceQuery {

    public static final String KEY = "SPECIFIC_BUTTON";

    public SpecificButtonQuery(final String entityId) {
        super(entityId);
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
