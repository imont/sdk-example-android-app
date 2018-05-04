/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class Json {

    private static ObjectMapper om = new ObjectMapper();

    public static String encode(final Object object) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T decode(final String json, final Class<T> clazz) {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
