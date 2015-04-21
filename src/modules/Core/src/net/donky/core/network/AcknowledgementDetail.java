package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

/**
 * Part of Client Notifications describing the context of the notification.
 *
 * Created by Marcin Swierczek
 * 20/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AcknowledgementDetail {

    /**
     * The id of the server notification being acknowledged
     */
    @SerializedName("serverNotificationId")
    private String serverNotificationId;

    /**
     * Indicates the result of the notifications delivery
     */
    @SerializedName("result")
    private String result;

    /**
     * The sent time of the original notification
     */
    @SerializedName("sentTime")
    private String sentTime;

    /**
     * The type of the original notification
     */
    @SerializedName("type")
    private String type;

    /**
     * The custom notification type of the original notification.
     */
    @SerializedName("customNotificationType")
    private String customNotificationType;

    /**
     * @return The id of the server notification being acknowledged
     */
    public String getServerNotificationId() {
        return serverNotificationId;
    }

    /**
     * @param serverNotificationId The id of the server notification being acknowledged
     */
    public void setServerNotificationId(String serverNotificationId) {
        this.serverNotificationId = serverNotificationId;
    }

    /**
     * @return Indicates the result of the notifications delivery
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result Indicates the result of the notifications delivery
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return The sent time of the original notification
     */
    public String getSentTime() {
        return sentTime;
    }

    /**
     * @param sentTime The sent time of the original notification
     */
    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }

    /**
     * @return The type of the original notification
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type of the original notification
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The custom notification type of the original notification.
     */
    public String getCustomNotificationType() {
        return customNotificationType;
    }

    /**
     * @param customNotificationType The custom notification type of the original notification.
     */
    public void setCustomNotificationType(String customNotificationType) {
        this.customNotificationType = customNotificationType;
    }

    /**
     * Client Notifications result.
     */
    public enum Result {

        NoResult,
        Delivered,
        DeliveredNoSubscription,
        Failed,
        Rejected;

        public boolean equals(String type) {
            return this.toString().equals(type);
        }
    }
}
