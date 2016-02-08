package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

/**
 * Server notification with user location details
 *
 * Created by Marcin Swierczek
 * 26/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserLocation {

    @SerializedName("senderNetworkProfileId")
    private String senderNetworkProfileId;

    @SerializedName("senderDeviceId")
    private String senderDeviceId;

    @SerializedName("location")
    private LocationPoint location;

    @SerializedName("timestamp")
    private String timestamp;

    /**
     * Gets profile id of user that send you location of his device
     */
    public String getSenderNetworkProfileId() {
        return senderNetworkProfileId;
    }

    /**
     * Gets device id of user that send you location of his device
     */
    public String getSenderDeviceId() {
        return senderDeviceId;
    }

    /**
     * Gets location details
     */
    public LocationPoint getLocation() {
        return location;
    }

    /**
     * When the location details where sent
     */
    public String getTimestamp() {
        return timestamp;
    }
}
