package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

import net.donky.core.network.ConfigurationSets;
import net.donky.core.network.StandardContacts;

import java.util.Map;

/**
 * Network response for the account registration.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RegisterResponse {

    @SerializedName("networkId")
    private String networkId;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("accessDetails")
    private AccessDetails accessDetails;

    public class AccessDetails {

        @SerializedName("accessToken")
        private String accessToken;

        @SerializedName("expiresOn")
        private String expiresOn;

        @SerializedName("tokenType")
        private String tokenType;

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

        public String getTokenType() {
            return tokenType;
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

        /**
         * @return Details about Standard Contacts.
         */
        public StandardContacts getStandardContacts() {
            if (configuration != null && configuration.getConfigurationSets() != null) {
                return configuration.getConfigurationSets().getStandardContacts();
            } else {
                return null;
            }
        }

    }

    /**
     * @return Unique user identifier.
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * @return Unique device identifier.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return Unique user identifier.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return Secured services access details.
     */
    public AccessDetails getAccessDetails() {
        return accessDetails;
    }

    @Override
    public String toString() {

        String divider = " | ";

        return "REGISTER RESPONSE: " + " networkId: " + networkId + divider + " deviceId : " + deviceId + divider + " userId : " + userId + divider + " accessDetails : " + accessDetails;
    }

}
