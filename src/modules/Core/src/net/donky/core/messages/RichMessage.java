package net.donky.core.messages;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Description of Rich Message contract with Donky Network.
 *
 * Created by Marcin Swierczek
 * 14/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessage implements Serializable {

    private String internalId;

    private boolean messageRead;


    @SerializedName("messageType")
    private String messageType;

    @SerializedName("senderExternalUserId")
    private String senderExternalUserId;

    @SerializedName("externalRef")
    private String externalRef;

    @SerializedName("description")
    private String description;

    @SerializedName("expiredBody")
    private String expiredBody;

    @SerializedName("canReply")
    private boolean canReply;

    @SerializedName("canForward")
    private boolean canForward;

    @SerializedName("canShare")
    private boolean canShare;

    @SerializedName("urlToShare")
    private String urlToShare;

    @SerializedName("silentNotification")
    private boolean silentNotification;

    @SerializedName("msgSentTimeStamp")
    private String msgSentTimeStamp;

    @SerializedName("forwardedBy")
    private String forwardedBy;

    @SerializedName("forwardingOverlayMessage")
    private String forwardingOverlayMessage;

    @SerializedName("conversationId")
    private String conversationId;

    //@SerializedName("assets")
    //private List<Asset> assets;

    @SerializedName("senderAccountType")
    private String senderAccountType;

    @SerializedName("senderDisplayName")
    private String senderDisplayName;

    @SerializedName("body")
    private String body;

    @SerializedName("messageScope")
    private String messageScope;

    @SerializedName("senderInternalUserId")
    private String senderInternalUserId;

    @SerializedName("senderMessageId")
    private String senderMessageId;

    @SerializedName("messageId")
    private String messageId;

    @SerializedName("contextItems")
    private Map<String, String> contextItems;

    @SerializedName("avatarAssetId")
    private String avatarAssetId;

    @SerializedName("sentTimestamp")
    private String sentTimestamp;

    @SerializedName("expiryTimeStamp")
    private String expiryTimeStamp;

    public String getMessageType() {
        return messageType;
    }

    public String getSenderExternalUserId() {
        return senderExternalUserId;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public String getDescription() {
        return description;
    }

    public String getExpiredBody() {
        return expiredBody;
    }

    public boolean isCanReply() {
        return canReply;
    }

    public boolean isCanForward() {
        return canForward;
    }

    public boolean isCanShare() {
        return canShare;
    }

    public String getUrlToShare() {
        return urlToShare;
    }

    public boolean isSilentNotification() {
        return silentNotification;
    }

    public String getMsgSentTimeStamp() {
        return msgSentTimeStamp;
    }

    public String getForwardedBy() {
        return forwardedBy;
    }

    public String getForwardingOverlayMessage() {
        return forwardingOverlayMessage;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getSenderAccountType() {
        return senderAccountType;
    }

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
        return expiryTimeStamp;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setSenderExternalUserId(String senderExternalUserId) {
        this.senderExternalUserId = senderExternalUserId;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExpiredBody(String expiredBody) {
        this.expiredBody = expiredBody;
    }

    public void setCanReply(boolean canReply) {
        this.canReply = canReply;
    }

    public void setCanForward(boolean canForward) {
        this.canForward = canForward;
    }

    public void setCanShare(boolean canShare) {
        this.canShare = canShare;
    }

    public void setUrlToShare(String urlToShare) {
        this.urlToShare = urlToShare;
    }

    public void setSilentNotification(boolean silentNotification) {
        this.silentNotification = silentNotification;
    }

    public void setMsgSentTimeStamp(String msgSentTimeStamp) {
        this.msgSentTimeStamp = msgSentTimeStamp;
    }

    public void setForwardedBy(String forwardedBy) {
        this.forwardedBy = forwardedBy;
    }

    public void setForwardingOverlayMessage(String forwardingOverlayMessage) {
        this.forwardingOverlayMessage = forwardingOverlayMessage;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setSenderAccountType(String senderAccountType) {
        this.senderAccountType = senderAccountType;
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
        this.expiryTimeStamp = expiryTimeStamp;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    /**
     * Return the rich message's body, URL encoded.
     * This is mostly useful for showing in a {@link android.webkit.WebView}, where Android 2.3 has a bug: https://code.google.com/p/android/issues/detail?id=4401
     *
     * @return The rich message's body, URL encoded.
     */
    public String getURLEncodedBody() {

        try {

            return URLEncoder.encode(body, "utf-8").replaceAll("\\+", " ");

        } catch (UnsupportedEncodingException e) {

            return body;

        }
    }

    /**
     * Return the rich message's body, URL encoded.
     * This is mostly useful for showing in a {@link android.webkit.WebView}, where Android 2.3 has a bug: https://code.google.com/p/android/issues/detail?id=4401
     *
     * @return The rich message's body, URL encoded.
     */
    public String getURLEncodedExpiredBody() {

        if (expiredBody != null) {

            try {

                return URLEncoder.encode(expiredBody, "utf-8").replaceAll("\\+", " ");

            } catch (UnsupportedEncodingException e) {

                return expiredBody;

            }

        }

        return null;
    }
}
