/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.rules;

import java.io.Serializable;
import java.util.Arrays;

public final class RuleAction {
    private String action;
    private Serializable[] parameters;

    public RuleAction(final String action, final Serializable[] parameters) {
        this.action = action;
        this.parameters = parameters;
    }

    public String getAction() {
        return action;
    }

    public Serializable[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "RuleAction{" +
                "action='" + action + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
