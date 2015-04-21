package net.donky.core.account;

/**
 * Wrapper class for all registration related data.
 *
 * Created by Marcin Swierczek
 * 19/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RegistrationDetails {

    private final UserDetails userDetails;

    private final DeviceDetails deviceDetails;

    /**
     * Registration details.
     *
     * @param userDetails User registration details.
     * @param deviceDetails Device registration details.
     */
    RegistrationDetails(UserDetails userDetails, DeviceDetails deviceDetails) {
        this.userDetails = userDetails;
        this.deviceDetails = deviceDetails;
    }

    /**
     * User registration details.
     *
     * @return User registration details.
     */
    public UserDetails getUserDetails() {
        return userDetails;
    }

    /**
     * Device registration details.
     *
     * @return Device registration details.
     */
    public DeviceDetails getDeviceDetails() {
        return deviceDetails;
    }
}
