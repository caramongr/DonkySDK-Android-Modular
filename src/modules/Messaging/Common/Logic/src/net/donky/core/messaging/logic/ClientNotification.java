package net.donky.core.messaging.logic;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;
import net.donky.core.messages.RichMessage;
import net.donky.core.network.AcknowledgementDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

/**
 * Class representing Client Notifications introduced by Messaging Logic Module.
 *
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ClientNotification extends net.donky.core.network.ClientNotification {

    /**
     * Client Notifications result.
     */
    enum Type {

        MessageReceived,
        MessageRead

    }

    /**
     * Client Notification introduced by Messaging Logic Module.
     *
     * @param type Type of Client Notification.
     * @param id Id of client notification.
     */
    protected ClientNotification(String type, String id) {
        super(type, id);
    }

    /**
     * Create 'Message Received' client notification.
     *
     * @param messageReceivedDetails Description of the message.
     * @return 'Message Received' Client Notification
     */
    static net.donky.core.network.ClientNotification createMessageReceivedNotification(MessageReceivedDetails messageReceivedDetails) {

        ClientNotification n = new ClientNotification(Type.MessageReceived.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createMessageReceived(n, messageReceivedDetails)));

        } catch (JSONException e) {

            e.printStackTrace();

        };

        return n;
    }

    /**
     * Create 'Message Read' client notification.
     *
     * @param richMessage Read rich message.
     * @return 'Message Read' Client Notification
     */
    static net.donky.core.network.ClientNotification createMessageReadNotification(RichMessage richMessage) {

        ClientNotification n = new ClientNotification(Type.MessageRead.toString(), IdHelper.generateId());

        Gson gson = new Gson();

        try {

            n.data = new JSONObject(gson.toJson(createMessageRead(n, richMessage)));

        } catch (JSONException e) {

            e.printStackTrace();

        };

        return n;
    }

    /**
     * Create serialized object for message received data.
     */
    private static MessageReceived createMessageReceived(ClientNotification n, MessageReceivedDetails messageReceivedDetails) {

        MessageReceived u = n.new MessageReceived();

        u.type = Type.MessageReceived.toString();
        u.senderInternalUserId = messageReceivedDetails.getSenderInternalUserId();
        u.messageId = messageReceivedDetails.getMessageId();
        u.senderMessageId = messageReceivedDetails.getSenderMessageId();
        u.receivedExpired = messageReceivedDetails.isReceivedExpired();
        u.messageType = messageReceivedDetails.getMessageType();
        u.messageScope = messageReceivedDetails.getMessageScope();
        u.SentTimestamp = messageReceivedDetails.getSentTimestamp();
        u.contextItems = messageReceivedDetails.getContextItems();
        u.acknowledgementDetail = messageReceivedDetails.getAcknowledgementDetail();

        return u;
    }

    /**
     * Create serialized object for message read data.
     */
    private static MessageRead createMessageRead(ClientNotification n, RichMessage richMessage) {

        MessageRead u = n.new MessageRead();

        u.type = Type.MessageRead.toString();
        u.senderInternalUserId = richMessage.getSenderInternalUserId();
        u.messageId = richMessage.getMessageId();
        u.senderMessageId = richMessage.getSenderMessageId();
        u.messageType = richMessage.getMessageType();
        u.messageScope = richMessage.getMessageScope();
        u.sentTimestamp = richMessage.getSentTimestamp();

        if (richMessage.getSentTimestamp() != null) {

            Date sentDate = DateAndTimeHelper.parseUtcDate(richMessage.getSentTimestamp());

            if (sentDate != null) {
                u.timeToReadSeconds = (int) (System.currentTimeMillis() - sentDate.getTime()) / 1000;
            }

        }

        u.contextItems = richMessage.getContextItems();


        return u;
    }

    /**
     * Description of json content of 'Message Received' client notification.
     */
    private class MessageReceived {

        @SerializedName("type")
        private String type;

        @SerializedName("senderInternalUserId")
        private String senderInternalUserId;

        @SerializedName("messageId")
        private String messageId;

        @SerializedName("senderMessageId")
        private String senderMessageId;

        @SerializedName("receivedExpired")
        private boolean receivedExpired;

        @SerializedName("messageType")
        private String messageType;

        @SerializedName("messageScope")
        private String messageScope;

        @SerializedName("sentTimestamp")
        private String SentTimestamp;

        @SerializedName("contextItems")
        private  Map<String,String> contextItems;

        @SerializedName("acknowledgementDetail")
        private AcknowledgementDetail acknowledgementDetail;

    }

    /**
     * Description of json content of 'Message Read' client notification.
     */
    private class MessageRead {

        @SerializedName("type")
        private String type;

        @SerializedName("senderInternalUserId")
        private String senderInternalUserId;

        @SerializedName("messageId")
        private String messageId;

        @SerializedName("senderMessageId")
        private String senderMessageId;

        @SerializedName("messageType")
        private String messageType;

        @SerializedName("messageScope")
        private String messageScope;

        @SerializedName("sentTimestamp")
        private String sentTimestamp;

        @SerializedName("contextItems")
        private  Map<String,String> contextItems;

        @SerializedName("timeToReadSeconds")
        private int timeToReadSeconds;

    }
}
