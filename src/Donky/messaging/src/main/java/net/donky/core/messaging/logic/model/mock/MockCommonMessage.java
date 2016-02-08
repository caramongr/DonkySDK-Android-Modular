package net.donky.core.messaging.logic.model.mock;

import net.donky.core.helpers.IdHelper;
import net.donky.core.messaging.logic.model.CommonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 21/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockCommonMessage extends CommonMessage {

    public MockCommonMessage(boolean isAvailabilityPeriodExceeded, boolean expired) {

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

        setAvatarAssetId(null);
        setBody("body");


        Map<String, String> contextItems = new HashMap<>();
        contextItems.put("contextKey", "contextValue");
        setContextItems(contextItems);

        setDescription("description");

        setMessageId(IdHelper.generateId());

        setSenderDisplayName("display name");
        setSenderInternalUserId(IdHelper.generateId());
        setSenderMessageId(IdHelper.generateId());
        setMessageType("A2P");
        setMessageRead(false);
    }

    public boolean equals(CommonMessage richMessage) {

        return ((compareStrings(getAvatarAssetId(), richMessage.getAvatarAssetId())) &&
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
                isMessageRead() == richMessage.isMessageRead());
    }

    public boolean compareStrings(String one, String two) {

        if ((one != null && one.equals(two)) || (one == null && two == null)) {
            return true;
        } else {
            return false;
        }
    }
}
