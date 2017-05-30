/**
 * Copyright 2016 IMONT Technologies
 * Created by romanas on 25/08/2016.
 */
package io.imont.android.sdkdemo.utils;

import io.imont.lion.api.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Objects;

public class MoleDeviceListComparator implements Comparator<Device> {

    private static final Logger logger = LoggerFactory.getLogger(MoleDeviceListComparator.class);

    private final String localPeerId;

    public MoleDeviceListComparator(final String localPeerId) {
        this.localPeerId = localPeerId;
    }

    /**
     * Note that this sorts in REVERSE order so that more important devices come first
     *
     *
     */
    @Override
    public int compare(final Device lhs, final Device rhs) {
        try {
            String molePeerId1 = lhs.getId().getPeerId();
            String molePeerId2 = rhs.getId().getPeerId();

            // Local peer always first
            if (localPeerId.equals(molePeerId1)) {
                return -1;
            } else if (localPeerId.equals(molePeerId2)) {
                return 1;
            }

            // then order by peerId, but make sure that bridged devices are after the bridging device

            if (Objects.equals(molePeerId1, molePeerId2)) {
                if (lhs.getType() == Device.Type.PEER) {
                    return -1;
                } else {
                    return 1;
                }
            }

            return molePeerId1.compareTo(molePeerId2);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return 0;
    }

}
