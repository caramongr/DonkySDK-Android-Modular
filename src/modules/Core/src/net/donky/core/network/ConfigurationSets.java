package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * Details from register and login response about standard contacts.
 *
 * Created by Marcin Swierczek
 * 14/10/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConfigurationSets {

    @SerializedName("StandardContacts")
    private StandardContacts standardContacts;

    /**
     * @return details about standard contacts.
     */
    public StandardContacts getStandardContacts() {
        return standardContacts;
    }
}
