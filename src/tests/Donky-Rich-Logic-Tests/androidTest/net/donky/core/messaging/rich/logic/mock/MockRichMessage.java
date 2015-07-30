package net.donky.core.messaging.rich.logic.mock;

import net.donky.core.helpers.IdHelper;
import net.donky.core.messaging.rich.logic.model.RichMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 15/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockRichMessage extends RichMessage {

    public MockRichMessage(boolean isAvailabilityPeriodExceeded, boolean expired, boolean receivedExpired, boolean hasExpiredBody, boolean canShare, boolean canForward, boolean canReply) {

        super();

        if (expired) {
            setExpiryTimeStamp("2000-01-01T10:00:00.000Z");
        } else {
            setExpiryTimeStamp("2050-01-01T10:00:00.000Z");
        }

        if (!isAvailabilityPeriodExceeded) {
            setSentTimestamp("2015-07-19T10:00:00.000Z");
        } else {
            setSentTimestamp("2000-07-19T10:00:00.000Z");
        }

        setReceivedExpired(receivedExpired);

        setAvatarAssetId(null);
        setBody("body");
        setCanForward(canForward);
        setCanReply(canReply);
        setCanShare(canShare);

        Map<String, String> contextItems = new HashMap<>();
        contextItems.put("contextKey", "contextValue");
        setContextItems(contextItems);

        setConversationId(IdHelper.generateId());
        setDescription("description");

        if (hasExpiredBody) {
            setExpiredBody("expired body");
        }

        setExternalRef(IdHelper.generateId());
        setForwardedBy("forwarded by");
        setForwardingOverlayMessage("overlay message");
        setInternalId(IdHelper.generateId());
        setMessageId(IdHelper.generateId());

        setSenderAccountType("account type");
        setSenderDisplayName("display name");
        setSenderExternalUserId(IdHelper.generateId());
        setSenderInternalUserId(IdHelper.generateId());
        setSenderMessageId(IdHelper.generateId());
        setSilentNotification(false);
        setUrlToShare("url to share");
        setMessageType("A2P");
        setMessageRead(false);
        setConversationId(IdHelper.generateId());
    }

    public boolean equals(RichMessage richMessage) {

        return (compareStrings(getConversationId(), richMessage.getConversationId())) &&
                (compareStrings(getExpiredBody(), richMessage.getExpiredBody())) &&
                (compareStrings(getExternalRef(), richMessage.getExternalRef())) &&
                (compareStrings(getForwardedBy(), richMessage.getForwardedBy())) &&
                (compareStrings(getForwardingOverlayMessage(), richMessage.getForwardingOverlayMessage())) &&
                (compareStrings(getInternalId(), richMessage.getInternalId())) &&
                (compareStrings(getConversationId(), richMessage.getConversationId())) &&
                (compareStrings(getMsgSentTimeStamp(), richMessage.getMsgSentTimeStamp())) &&
                (compareStrings(getSenderAccountType(), richMessage.getSenderAccountType())) &&
                (compareStrings(getSenderExternalUserId(), richMessage.getSenderExternalUserId())) &&
                (compareStrings(getURLEncodedBody(), richMessage.getURLEncodedBody())) &&
                (compareStrings(getURLEncodedExpiredBody(), richMessage.getURLEncodedExpiredBody())) &&
                (compareStrings(getUrlToShare(), richMessage.getUrlToShare())) &&
                (compareStrings(getAvatarAssetId(), richMessage.getAvatarAssetId())) &&
                (compareStrings(getBody(), richMessage.getBody())) &&
                (compareStrings(getDescription(), richMessage.getDescription())) &&
                (compareStrings(getExpiryTimeStamp(), richMessage.getExpiryTimeStamp())) &&
                (compareStrings(getMessageId(), richMessage.getMessageId())) &&
                (compareStrings(getMessageScope(), richMessage.getMessageScope())) &&
                (compareStrings(getMessageType(), richMessage.getMessageType())) &&
                (compareStrings(getSenderDisplayName(), richMessage.getSenderDisplayName())) &&
                (compareStrings(getSenderInternalUserId(), richMessage.getSenderInternalUserId())) &&
                (compareStrings(getSenderMessageId(), richMessage.getSenderMessageId())) &&
                (compareStrings(getSentTimestamp(), richMessage.getSentTimestamp())) &&
                isSilentNotification() == richMessage.isSilentNotification() &&
                isReceivedExpired() == richMessage.isReceivedExpired() &&
                isMessageRead() == richMessage.isMessageRead() &&
                isCanShare() == richMessage.isCanShare() &&
                isCanForward() == richMessage.isCanForward() &&
                isCanReply() == richMessage.isCanReply();
    }

    public boolean compareStrings(String one, String two) {

        if ((one != null && one.equals(two)) || (one == null && two == null)) {
            return true;
        } else {
            return false;
        }
    }
}
