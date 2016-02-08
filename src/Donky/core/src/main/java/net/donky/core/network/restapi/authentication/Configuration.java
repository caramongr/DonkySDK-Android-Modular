package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

import net.donky.core.network.ConfigurationSets;

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

    @SerializedName("configurationSets")
    private ConfigurationSets configurationSets;

    /**
     * @return Dictionary of configuration settings set on the network.
     */
    public Map<String, String> getConfigurationItems() {
        return configurationItems;
    }

    /**
     * Gets ConfigurationSets with standard contact details.
     * @return ConfigurationSets with standard contact details.
     */
    public ConfigurationSets getConfigurationSets() {
        return configurationSets;
    }
}
