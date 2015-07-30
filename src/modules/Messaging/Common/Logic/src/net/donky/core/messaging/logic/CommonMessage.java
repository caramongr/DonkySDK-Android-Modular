package net.donky.core.messaging.logic;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Parent class for all Messages.
 *
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class CommonMessage implements Serializable {

    @SerializedName("avatarAssetId")
    private String avatarAssetId;

    @SerializedName("body")
    private String body;

    @SerializedName("contextItems")
    private Map<String, String> contextItems;

    @SerializedName("expiryTimeStamp")
    private String expiryTimestamp;

    @SerializedName("messageId")
    private String messageId;

    @SerializedName("messageScope")
    private String messageScope;

    @SerializedName("senderDisplayName")
    private String senderDisplayName;

    @SerializedName("senderInternalUserId")
    private String senderInternalUserId;

    @SerializedName("senderMessageId")
    private String senderMessageId;

    @SerializedName("sentTimestamp")
    private String sentTimestamp;

    @SerializedName("messageType")
    private String messageType;

    @SerializedName("description")
    private String description;

    private boolean messageRead;

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public String getBody() {
        return body;
    }

    public String getMessageScope() {
        return messageScope;
    }

    public String getSenderInternalUserId() {
        return senderInternalUserId;
    }

    public String getSenderMessageId() {
        return senderMessageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public Map<String, String> getContextItems() {
        return contextItems;
    }

    public String getAvatarAssetId() {
        return avatarAssetId;
    }

    public String getSentTimestamp() {
        return sentTimestamp;
    }

    public String getExpiryTimeStamp() {
        return expiryTimestamp;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setMessageScope(String messageScope) {
        this.messageScope = messageScope;
    }

    public void setSenderInternalUserId(String senderInternalUserId) {
        this.senderInternalUserId = senderInternalUserId;
    }

    public void setSenderMessageId(String senderMessageId) {
        this.senderMessageId = senderMessageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setContextItems(Map<String, String> contextItems) {
        this.contextItems = contextItems;
    }

    public void setAvatarAssetId(String avatarAssetId) {
        this.avatarAssetId = avatarAssetId;
    }

    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public void setExpiryTimeStamp(String expiryTimeStamp) {
        this.expiryTimestamp = expiryTimeStamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }
}
