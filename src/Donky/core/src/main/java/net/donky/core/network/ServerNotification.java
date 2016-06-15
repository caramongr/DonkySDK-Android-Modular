package net.donky.core.network;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import net.donky.core.Notification;
import net.donky.core.helpers.IdHelper;

import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * Notification with Rich Message details.
     */
    public static final String NOTIFICATION_TYPE_RichMessage = "RichMessage";

    /**
     * Notification with Chat Message details.
     */
    public static final String NOTIFICATION_TYPE_ChatMessage = "Message";

    /**
     * Notification with message status
     */
    public static final String NOTIFICATION_TYPE_MsgSent = "MessageSent";

    /**
     * Notification with message status
     */
    public static final String NOTIFICATION_TYPE_MsgDelivered = "MessageDelivered";

    /**
     * Notification with message status
     */
    public static final String NOTIFICATION_TYPE_MsgRead = "MessageRead";

    /**
     * Notification for message read on another device
     */
    public static final String NOTIFICATION_TYPE_SyncMsgRead = "SyncMessageRead";

    /**
     * Notification for message deleted on another device
     */
    public static final String NOTIFICATION_TYPE_SyncMsgDeleted = "SyncMessageDeleted";

    /**
     * Notification with message status
     */
    public static final String NOTIFICATION_TYPE_MsgRejected = "MessageRejected";

    /**
     * Notification with user is typing chat message
     */
    public static final String NOTIFICATION_TYPE_UserIsTyping = "UserIsTyping";

    public static final String NOTIFICATION_START_TRACKING_LOCATION = "StartTrackingLocation";

    public static final String NOTIFICATION_STOP_TRACKING_LOCATION = "StopTrackingLocation";

    public static final String NOTIFICATION_TRIGGER_CONFIGURATION = "TriggerConfiguration";

    public static final String NOTIFICATION_TRIGGER_DELETED = "TriggerDeleted";

    public static final String NOTIFICATION_LOCATION_REQUEST = "LocationRequest";

    public static final String NOTIFICATION_USER_LOCATION = "UserLocation";

    /**
     * Notification with user details update from another device
     */
    public static final String NOTIFICATION_TYPE_UserUpdated = "UserUpdated";

    public static final String DIRECT_MESSAGE_ID = "notificationId";
    public static final String DIRECT_MESSAGE_CREATED_ON = "notificationCreatedOn";
    public static final String DIRECT_MESSAGE_NOTIFICATION_TYPE = "notificationType";
    public static final String DIRECT_MESSAGE_PAYLOAD = "payload";

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
     * Constructor initialised with GCM bundle data.
     *
     * @param bundle GCM bundle data with Donky direct message.
     */
    public ServerNotification(final Bundle bundle) throws JSONException {
        super(null, IdHelper.generateId());

        if (bundle != null) {
            id = bundle.getString(DIRECT_MESSAGE_ID);
            createdOn = bundle.getString(DIRECT_MESSAGE_CREATED_ON);
            type = bundle.getString(DIRECT_MESSAGE_NOTIFICATION_TYPE);
            String payload = bundle.getString(DIRECT_MESSAGE_PAYLOAD);
            if (payload != null) {
                JSONObject jObj = new JSONObject(payload);
                data = new JsonParser().parse(jObj.toString()).getAsJsonObject();
            }
        }
    }

    /**
     * Constructor initialised with json string parsed to tree map.
     *
     * @param notificationTreeMap Json string parsed to tree map.
     */
    public ServerNotification(LinkedTreeMap<?, ?> notificationTreeMap) {
        super(null, IdHelper.generateId());

        if (notificationTreeMap != null) {
            id = (String) notificationTreeMap.get("id");
            createdOn = (String) notificationTreeMap.get("createdOn");
            type = (String) notificationTreeMap.get("type");
            LinkedTreeMap<?, ?> strData = (LinkedTreeMap<?,?>) notificationTreeMap.get("data");
            if (strData != null) {
                JSONObject jObj = new JSONObject(strData);
                if (jObj != null) {
                    data = new JsonParser().parse(jObj.toString()).getAsJsonObject();
                }
            }
        }

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

        return "ServerNotification: " + " type: " + type + divider + " serverNotificationId : " + id + divider + " data : " + data + divider + " createdOn : " + createdOn;
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