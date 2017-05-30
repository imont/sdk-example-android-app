/**
 * Copyright 2017 IMONT Technologies
 * Created by romanas on 16/01/2017.
 */
package io.imont.android.sdkdemo.utils;

import io.imont.mole.MoleClient;
import io.imont.mole.MoleException;
import io.imont.mole.client.Event;

import static io.imont.cairo.events.Hardware.DEVICE_ADDED_EVENT;
import static io.imont.cairo.events.Hardware.DEVICE_ADDED_IP_ADDRESS_META;
import static io.imont.cairo.events.IPInformation.IP_ADDRESS_META;
import static io.imont.cairo.events.IPInformation.IP_LOCATED_EVENT;

public final class CameraHelper {

    private CameraHelper() {
        // don't construct me
    }

    public static String getIPAddress(final String id, final MoleClient mc) throws MoleException {
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
