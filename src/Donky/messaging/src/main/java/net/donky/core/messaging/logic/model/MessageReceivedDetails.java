package net.donky.core.messaging.logic.model;

import net.donky.core.network.AcknowledgementDetail;

import java.util.Map;

/**
 * Description of Donky Network message.
 *
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessageReceivedDetails {

    /**
     * A2P for Application to person
     * P2P for Person to Person
     */
    public enum MessageScope {

        NotDefined,
        A2P,
        P2P

    }

    private String senderInternalUserId;

    private String messageId;

    private String senderMessageId;

    private boolean receivedExpired;

    private String messageType;

    private String messageScope;

    private String sentTimestamp;

    private Map<String,String> contextItems;

    private AcknowledgementDetail acknowledgementDetail;

    public String getSenderInternalUserId() {
        return senderInternalUserId;
    }

    public void setSenderInternalUserId(String senderInternalUserId) {
        this.senderInternalUserId = senderInternalUserId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderMessageId() {
        return senderMessageId;
    }

    public void setSenderMessageId(String senderMessageId) {
        this.senderMessageId = senderMessageId;
    }

    public boolean isReceivedExpired() {
        return receivedExpired;
    }

    public void setReceivedExpired(boolean receivedExpired) {
        this.receivedExpired = receivedExpired;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageScope() {
        return messageScope;
    }

    public void setMessageScope(String messageScope) {
        this.messageScope = messageScope;
    }

    public String getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public Map<String, String> getContextItems() {
        return contextItems;
    }

    public void setContextItems(Map<String, String> contextItems) {
        this.contextItems = contextItems;
    }

    public AcknowledgementDetail getAcknowledgementDetail() {
        return acknowledgementDetail;
    }

    public void setAcknowledgementDetail(AcknowledgementDetail acknowledgementDetail) {
        this.acknowledgementDetail = acknowledgementDetail;
    }
}
