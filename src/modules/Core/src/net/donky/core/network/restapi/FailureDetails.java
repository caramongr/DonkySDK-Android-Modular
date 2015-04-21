package net.donky.core.network.restapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Marcin Swierczek
 * 03/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class FailureDetails {

    public static final String USER_ID_ALREADY_TAKEN = "UserIdAlreadyTaken";

    @SerializedName("property")
    private String property;

    @SerializedName("details")
    private String details;

    @SerializedName("failureKey")
    private String failureKey;

    public String getProperty() {
        return property;
    }

    public String getDetails() {
        return details;
    }

    public String getFailureKey() {
        return failureKey;
    }
}
