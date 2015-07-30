package net.donky.core.messaging.push.logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.donky.core.DonkyCore;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.MessageReceivedDetails;
import net.donky.core.messaging.logic.MessagingInternalController;
import net.donky.core.messaging.push.logic.events.SimplePushMessageEvent;
import net.donky.core.network.AcknowledgementDetail;
import net.donky.core.network.ServerNotification;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Class responsible for translating Server notification with Simple/Interactive notification into Local Event.
 *
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SimplePushHandler {

    /**
     * Logging helper
     */
    private DLog log;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    SimplePushHandler() {

        log = new DLog("PushLogicController");

    }

    /**
     * Translating Server notification with Simple/Interactive notification into Local Event and fire this event.
     *
     * @param serverNotifications Server notifications with Simpe/Interactive Push message.
     */
    void handleSimplePushMessage(List<ServerNotification> serverNotifications) {

        List<SimplePushData> simplePushDataList = new LinkedList<>();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_TYPE_SimplePushMessage.equals(serverNotification.getType())) {

                JsonObject data = serverNotification.getData();

                Gson gson = new Gson();

                SimplePushData simplePushData = gson.fromJson(data.toString(), SimplePushData.class);

                MessageReceivedDetails messageReceivedDetails = new MessageReceivedDetails();

                messageReceivedDetails.setMessageType(simplePushData.getMessageType());
                messageReceivedDetails.setMessageId(simplePushData.getMessageId());
                messageReceivedDetails.setMessageScope(MessageReceivedDetails.MessageScope.A2P.toString());
                messageReceivedDetails.setContextItems(simplePushData.getContextItems());
                messageReceivedDetails.setSenderInternalUserId(simplePushData.getSenderInternalUserId());
                messageReceivedDetails.setSenderMessageId(simplePushData.getSenderMessageId());
                messageReceivedDetails.setSentTimestamp(simplePushData.getSentTimestamp());

                Date expiredTime = DateAndTimeHelper.parseUtcDate(simplePushData.getExpiryTimeStamp());

                boolean receivedExpired = false;

                if (expiredTime != null) {

                    receivedExpired = expiredTime.getTime() > System.currentTimeMillis();

                    if (receivedExpired) {

                        messageReceivedDetails.setReceivedExpired(true);

                    } else {

                        messageReceivedDetails.setReceivedExpired(false);

                    }
                }

                AcknowledgementDetail acknowledgementDetail = new AcknowledgementDetail();
                acknowledgementDetail.setCustomNotificationType(null);
                acknowledgementDetail.setType(serverNotification.getType());
                acknowledgementDetail.setResult(AcknowledgementDetail.Result.Delivered.toString());
                acknowledgementDetail.setSentTime(serverNotification.getCreatedOn());
                acknowledgementDetail.setServerNotificationId(serverNotification.getId());
                messageReceivedDetails.setAcknowledgementDetail(acknowledgementDetail);

                MessagingInternalController.getInstance().queueMessageReceivedNotification(messageReceivedDetails);

                simplePushData.setReceivedExpired(receivedExpired);

                simplePushDataList.add(simplePushData);

            }
        }

        DonkyCore.publishLocalEvent(new SimplePushMessageEvent(simplePushDataList));
    }

}
