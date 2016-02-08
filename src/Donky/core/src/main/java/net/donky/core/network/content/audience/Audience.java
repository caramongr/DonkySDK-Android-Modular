package net.donky.core.network.content.audience;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for audience of content notifications.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Audience {

    @SerializedName("type")
    private final String type;

    /**
     * The audience to target with the content.
     *
     * @param type Type of audience.
     */
    protected Audience(String type) {
        this.type = type;
    }

    /**
     * @return Audience type.
     */
    public String getType() {
        return type;
    }

}
