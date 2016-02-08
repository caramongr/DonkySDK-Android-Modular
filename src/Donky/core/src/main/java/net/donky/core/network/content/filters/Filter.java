package net.donky.core.network.content.filters;

import com.google.gson.annotations.SerializedName;

/**
 * Filter
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Filter {

    @SerializedName("type")
    private final String type;

    Filter(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
