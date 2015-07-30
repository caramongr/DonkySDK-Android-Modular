package net.donky.core.messaging.rich.logic.model;

import com.google.gson.annotations.SerializedName;

import net.donky.core.messaging.logic.CommonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Description of Rich Message contract with Donky Network.
 *
 * Created by Marcin Swierczek
 * 14/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessage extends CommonMessage {

    private String internalId;

    private boolean receivedExpired;

    @SerializedName("senderExternalUserId")
    private String senderExternalUserId;

    @SerializedName("externalRef")
    private String externalRef;

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

    @SerializedName("senderAccountType")
    private String senderAccountType;

    public String getSenderExternalUserId() {
        return senderExternalUserId;
    }

    public String getExternalRef() {
        return externalRef;
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

    public void setSenderExternalUserId(String senderExternalUserId) {
        this.senderExternalUserId = senderExternalUserId;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
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

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public boolean isReceivedExpired() {
        return receivedExpired;
    }

    public void setReceivedExpired(boolean receivedExpired) {
        this.receivedExpired = receivedExpired;
    }

    /**
     * Return the rich message's body, URL encoded.
     * This is mostly useful for showing in a {@link android.webkit.WebView}, where Android 2.3 has a bug: https://code.google.com/p/android/issues/detail?id=4401
     *
     * @return The rich message's body, URL encoded.
     */
    public String getURLEncodedBody() {

        try {

            return URLEncoder.encode(getBody(), "utf-8").replaceAll("\\+", " ");

        } catch (UnsupportedEncodingException e) {

            return getBody();

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
