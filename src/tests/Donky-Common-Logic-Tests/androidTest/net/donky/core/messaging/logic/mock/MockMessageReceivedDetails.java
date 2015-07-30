package net.donky.core.messaging.logic.mock;

import net.donky.core.helpers.IdHelper;
import net.donky.core.messaging.logic.MessageReceivedDetails;
import net.donky.core.network.AcknowledgementDetail;
import net.donky.core.network.ServerNotification;

import java.util.HashMap;

/**
 * Created by Marcin Swierczek
 * 20/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockMessageReceivedDetails extends MessageReceivedDetails {

    public MockMessageReceivedDetails(boolean isReceivedExpired, ServerNotification serverNotification) {

        setMessageType("MockMessage");
        setMessageId(IdHelper.generateId());
        setMessageScope(MessageReceivedDetails.MessageScope.A2P.toString());
        setContextItems(new HashMap<String, String>());
        setSenderInternalUserId(IdHelper.generateId());
        setSenderMessageId(IdHelper.generateId());
        setSentTimestamp("2015-07-21T10:00:00.000Z");

        setReceivedExpired(isReceivedExpired);

        AcknowledgementDetail acknowledgementDetail = new AcknowledgementDetail();
        acknowledgementDetail.setCustomNotificationType(null);
        acknowledgementDetail.setType(serverNotification.getType());
        acknowledgementDetail.setResult(AcknowledgementDetail.Result.Delivered.toString());
        acknowledgementDetail.setSentTime(serverNotification.getCreatedOn());
        acknowledgementDetail.setServerNotificationId(serverNotification.getId());
        setAcknowledgementDetail(acknowledgementDetail);

    }

}
