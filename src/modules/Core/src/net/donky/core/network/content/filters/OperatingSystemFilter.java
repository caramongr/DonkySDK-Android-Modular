package net.donky.core.network.content.filters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Filter to specify Operation System of the phones to which the {@link net.donky.core.network.content.ContentNotification} should be delivered.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class OperatingSystemFilter extends Filter {


    @SerializedName("operatingSystems")
    private final List<String> operatingSystems;

    public OperatingSystemFilter(List<String> operatingSystems) {
        super("OperatingSystem");
        this.operatingSystems = operatingSystems;
    }

    public List<String> getOperatingSystems() {
        return operatingSystems;
    }
}
