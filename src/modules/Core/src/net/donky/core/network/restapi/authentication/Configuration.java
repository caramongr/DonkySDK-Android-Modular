package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Account configuration received within authentication responses.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class Configuration {

    @SerializedName("configurationItems")
    private Map<String ,String> configurationItems;

    /**
     * @return Dictionary of configuration settings set on the network.
     */
    public Map<String, String> getConfigurationItems() {
        return configurationItems;
    }
}
