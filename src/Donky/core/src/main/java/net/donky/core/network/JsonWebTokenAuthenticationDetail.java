package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Marcin Swierczek
 * 17/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class JsonWebTokenAuthenticationDetail extends AuthenticationDetail {

    @SerializedName("token")
    protected String token;

    public JsonWebTokenAuthenticationDetail(String authenticationId, String provider, String token) {
        super(authenticationId, provider);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
