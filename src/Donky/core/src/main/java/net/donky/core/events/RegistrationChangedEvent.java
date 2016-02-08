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

    private final boolean isReplaceRegistration;

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
        this.isReplaceRegistration = false;
    }

    /**
     * Local Donky event delivered to subscribers when the registration details change.
     *
     * @param userDetails New user details fro registration update.
     * @param deviceDetails New device details fro registration update.
     * @param isReplaceRegistration Is the change a complete replacing of old registration. Be aware that setting this value to true may cause some modules to wipe out user specific data.
     */
    public RegistrationChangedEvent(UserDetails userDetails, DeviceDetails deviceDetails, boolean isReplaceRegistration) {
        super();
        this.userDetails = userDetails;
        this.deviceDetails = deviceDetails;
        this.isReplaceRegistration = isReplaceRegistration;
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

    /**
     * Was the update the complete replacing of registered user.
     *
     * @return True if the update was a complete replacing of registered user.
     */
    public boolean isReplaceRegistration() {
        return isReplaceRegistration;
    }
}
