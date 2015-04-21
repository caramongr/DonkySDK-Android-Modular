package net.donky.core.events;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;

/**
 * Represent Event raised by Donky Core library or another Donky Module when registration data change.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RegistrationChangedEvent extends LocalEvent {

    private final UserDetails userDetails;

    private final DeviceDetails deviceDetails;

    /**
     * Local Donky event delivered to subscribers when the registration details change.
     *
     * @param userDetails New user details fro registration update.
     * @param deviceDetails New device details fro registration update.
     */
    public RegistrationChangedEvent(UserDetails userDetails, DeviceDetails deviceDetails) {
        super();
        this.userDetails = userDetails;
        this.deviceDetails = deviceDetails;
    }

    /**
     * New user details fro registration update.
     *
     * @return New user details fro registration update.
     */
    public UserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * New device details from registration update.
     *
     * @return New device details from registration update.
     */
    public DeviceDetails getDeviceDetails() {
        return deviceDetails;
    }
}
