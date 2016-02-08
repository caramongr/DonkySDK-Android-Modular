package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

/**
 * Response from the network that allow to determine if user with particular external user id has been registered on the network.
 *
 * Created by Marcin Swierczek
 * 14/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class IsValidPlatformUserResponse {

    @SerializedName("userId")
    private String userId;

    @SerializedName("networkProfileId")
    private String networkProfileId;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("mobileNumber")
    private String mobileNumber;

    @SerializedName("country")
    private String country;

    @SerializedName("emailAddress")
    private String emailAddress;

    @SerializedName("avatarAssetId")
    private String avatarAssetId;

    @SerializedName("validUser")
    private boolean validUser;

    public String getUserId() {
        return userId;
    }

    public String getNetworkProfileId() {
        return networkProfileId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getAvatarAssetId() {
        return avatarAssetId;
    }

    public boolean isValidUser() {
        return validUser;
    }
}
