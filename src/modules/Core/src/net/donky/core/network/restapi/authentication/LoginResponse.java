package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Network response for the account authentication.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LoginResponse {

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("tokenType")
    private String tokenType;

    @SerializedName("expiresInSeconds")
    private int expiresInSeconds;

    @SerializedName("expiresOn")
    private String expiresOn;

    @SerializedName("secureServiceRootUrl")
    private String secureServiceRootUrl;

    @SerializedName("signalRUrl")
    private String signalRUrl;

    @SerializedName("configuration")
    private Configuration configuration;

    /**
     * @return Authorisation token that need to be added to secured network calls.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return Type of authorisation token that need to be added to secured network calls.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * @return Number of seconds before authorisation token expiry.
     */
    public Integer getExpiresInSeconds() {
        return expiresInSeconds;
    }

    /**
     * @return The date of authorisation token expiry.
     */
    public String getExpiresOn() {
        return expiresOn;
    }

    /**
     * @return URL for secured network service.
     */
    public String getSecureServiceRootUrl() {
        return secureServiceRootUrl;
    }

    /**
     * @return URL for signal R.
     */
    public String getSignalRUrl() {
        return signalRUrl;
    }

    /**
     * @return Dictionary of configuration settings set on the network.
     */
    public Map<String ,String> getConfigurationItems() {

        if (configuration != null) {
            return configuration.getConfigurationItems();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {

        String divider = " | ";

        return "LOGIN RESPONSE: " + " accessToken: " + accessToken + divider + " tokenType : " + tokenType + divider + " expiresInSeconds : " + expiresInSeconds + divider + " expiresOn : " + expiresOn + divider + " secureServiceRootUrl : " + secureServiceRootUrl + divider + " signalRUrl : " + signalRUrl;
    }


}
