package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

/**
 * Server notification with request to get location
 *
 * Created by Marcin Swierczek
 * 26/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserLocationRequest {

    @SerializedName("sendToNetworkProfileId")
    private String sendToNetworkProfileId;

    /**
     * Gets profile id of the user that requested your current location
     */
    public String getSendToNetworkProfileId() {
        return sendToNetworkProfileId;
    }
}