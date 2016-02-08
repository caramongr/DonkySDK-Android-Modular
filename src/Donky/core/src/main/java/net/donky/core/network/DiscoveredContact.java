package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Marcin Swierczek
 * 14/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DiscoveredContact {

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

    public String getExternalUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNetworkProfileId() {
        return networkProfileId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getAvatarAssetId() {
        return avatarAssetId;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCountry() {
        return country;
    }

}
