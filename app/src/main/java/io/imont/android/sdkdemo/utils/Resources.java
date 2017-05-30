/*
 * Copyright 2017 IMONT Technologies Limited
 * Created by romanas on 05/05/2017.
 */

package io.imont.android.sdkdemo.utils;

import android.content.Context;

public class Resources {

    public static final String lookupStringResource(final Context context, final String resource, final Object... params) {
        int resId = context.getResources().getIdentifier(resource, "string", context.getPackageName());
        if (resId == 0) {
            return resource;
        }
        return context.getResources().getString(resId, params);
    }
}
