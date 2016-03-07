package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Marcin Swierczek
 * 17/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class StartAuthResponse {

    public static final int JSON_WEB_TOKEN_PROVIDER = 1;

    @SerializedName("authenticationId")
    private String authenticationId;

    @SerializedName("provider")
    private String provider;

    @SerializedName("nonceRequired")
    private boolean nonceRequired;

    @SerializedName("nonce")
    private String nonce;

    public String getAuthenticationId() {
        return authenticationId;
    }

    public String getProvider() {
        return provider;
    }

    public boolean isNonceRequired() {
        return nonceRequired;
    }

    public String getNonce() {
        return nonce;
    }
}
