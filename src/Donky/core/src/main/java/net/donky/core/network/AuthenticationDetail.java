package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * Encapsulates internal authentication process details.
 *
 * Created by Marcin Swierczek
 * 17/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AuthenticationDetail {

    public static final String JSON_WEB_TOKEN_PROVIDER = "JsonWebToken";

    @SerializedName("authenticationId")
    protected String authenticationId;

    @SerializedName("provider")
    protected int provider;

    public AuthenticationDetail(String authenticationId, String provider) {
        this.authenticationId = authenticationId;
        if (JSON_WEB_TOKEN_PROVIDER.equals(provider)) {
            this.provider = 1;
        } else {
            this.provider = 0;
        }
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public int getProvider() {
        return provider;
    }
}
