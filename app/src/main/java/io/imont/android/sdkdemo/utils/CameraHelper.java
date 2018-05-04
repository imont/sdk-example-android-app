/*
 * Copyright (C) 2018 IMONT Technologies Limited
 *
 */
package io.imont.android.sdkdemo.utils;

import io.imont.mole.MoleClient;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;
import io.imont.mole.client.GlobalEntityId;

import static io.imont.cairo.events.Hardware.DEVICE_ADDED_EVENT;
import static io.imont.cairo.events.Hardware.DEVICE_ADDED_IP_ADDRESS_META;
import static io.imont.cairo.events.IPInformation.IP_ADDRESS_META;
import static io.imont.cairo.events.IPInformation.IP_LOCATED_EVENT;

public final class CameraHelper {

    private CameraHelper() {
        // don't construct me
    }

    public static String getIPAddress(final GlobalEntityId id, final MoleClient mc) throws MoleException {
        final Event devicedAdded = mc.getState(id, DEVICE_ADDED_EVENT.getFQEventKey());
        final Event ipLocatedEvent = mc.getState(id, IP_LOCATED_EVENT.getFQEventKey());

        if (ipLocatedEvent != null) {
            return ipLocatedEvent.getMetadata().get(IP_ADDRESS_META.getMetaKey());
        } else {
            // TODO Old event, should not be used any more
            return devicedAdded.getMetadata().get(DEVICE_ADDED_IP_ADDRESS_META.getMetaKey());
        }
    }
}
