package net.donky.core.network.content.content;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

/**
 * Custom content for {@link net.donky.core.network.content.ContentNotification}
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NotificationContent {

    private final static String CONTENT_NOTIFICATION_TYPE = "Custom";

    @SerializedName("type")
    private final String type = CONTENT_NOTIFICATION_TYPE;

    @SerializedName("customType")
    private final String customType;

    @SerializedName("data")
    private final JSONObject data;

    /**
     * Content wrapper constructor for custom notification.
     *
     * @param customType Type of content notification.
     * @param data JSON serialised data {@link JSONObject} to be sent with content notification.
     */
    public NotificationContent(String customType, JSONObject data) {
        this.customType = customType;
        this.data = data;
    }

    /**
     * @return Type of content notification.
     */
    public String getType() {
        return type;
    }

    /**
     * @return Type of custom notification.
     */
    public String getCustomType() {
        return customType;
    }

    /**
     * @return JSON serialised data
     */
    public JSONObject getData() {
        return data;
    }
}
