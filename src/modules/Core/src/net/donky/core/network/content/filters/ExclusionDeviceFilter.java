package net.donky.core.network.content.filters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Audience for content message will be all users except the one listed here.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ExclusionDeviceFilter extends Filter {

    @SerializedName("deviceIds")
    private final List<String> deviceIds;

    public ExclusionDeviceFilter(List<String> deviceIds) {
        super("DeviceExclusion");
        this.deviceIds = deviceIds;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }
}
