package net.donky.core.network;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import net.donky.core.Notification;
import net.donky.core.helpers.IdHelper;

/**
 * Notification received form server in synchronisation call.
 *
 * Created by Marcin Swierczek
 * 24/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ServerNotification extends Notification {

    /**
     * Type of notification that allows content (messages, custom notifications etc.) to be sent via the network.
     */
    public static final String NOTIFICATION_CATEGORY_CUSTOM = "Custom";

    /**
     * A piece of feedback from a device to the Donky Network. These are internal to Donky only and will not be formally exposed to the public
     */
    public static final String NOTIFICATION_CATEGORY_DONKY = "Donky";

    /**
     * Notification triggering transmitting debug logs to the network.
     */
    public static final String NOTIFICATION_TYPE_TransmitDebugLog = "TransmitDebugLog";

    /**
     * Notification that new device was registered against the user account.
     */
    public static final String NOTIFICATION_TYPE_NewDeviceAddedToUser = "NewDeviceAddedToUser";

    /**
     * Notification triggering transmitting debug logs to the network.
     */
    public static final String NOTIFICATION_TYPE_SimplePushMessage = "SimplePushMessage";

    public static String NOTIFICATION_TYPE_RichMessage = "RichMessage";

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private String id;

    @SerializedName("data")
    private JsonObject data;

    @SerializedName("createdOn")
    private String createdOn;

    private String category;

    protected ServerNotification() {
        super(null, IdHelper.generateId());
    }

    /**
     * @return Type of Notification
     */
    public String getType() {
        return type;
    }

    /**
     * @return Unique ID for this notification
     */
    public String getId() {
        return id;
    }

    /**
     * @return JSON serialised data
     */
    public JsonObject getData() {
        return data;
    }

    /**
     * @return Timestamp for the notification
     */
    public String getCreatedOn() {
        return createdOn;
    }

    /**
     * @return Category of the notification
     */
    public String getCategory() {
        return category;
    }

    /**
     * Category of the notification {@link ServerNotification#NOTIFICATION_CATEGORY_CUSTOM} or {@link ServerNotification#NOTIFICATION_CATEGORY_DONKY}
     * @param category Category of the notification.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {

        String divider = " | ";

        return "ServerNotification: " + " type: " + type + divider + " serverNotificationId : " + id + divider + " data : " + data.toString() + divider + " createdOn : " + createdOn;
    }

    /*
    Access methods for testes.
     */

    protected void setMockData(String type, String id, JsonObject data, String createdOn) {
        this.type = type;
        this.id = id;
        this.data = data;
        this.createdOn = createdOn;
    }
}