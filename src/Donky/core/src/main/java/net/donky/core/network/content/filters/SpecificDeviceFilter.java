package net.donky.core.network.content.filters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Filter to specify to which devices should the {@link net.donky.core.network.content.ContentNotification} be send.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SpecificDeviceFilter  extends Filter {

    @SerializedName("deviceIds")
    private final List<String> deviceIds;

    public SpecificDeviceFilter(List<String> deviceIds) {
        super("SpecificDevice");
        this.deviceIds = deviceIds;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }
}