package net.donky.core.messaging.rich.logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.donky.core.DonkyCore;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.helpers.IdHelper;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.logic.model.MessageReceivedDetails;
import net.donky.core.messaging.logic.MessagingInternalController;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.network.AcknowledgementDetail;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Handler for received server notification.
 *
 * Created by Marcin Swierczek
 * 14/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NotificationHandler {

    /**
     * Handler for received server notification.
     *
     * @param serverNotifications Received server notifications.
     */
    public void handleRichMessageNotification(List<ServerNotification> serverNotifications) {

        List<RichMessage> richMessages = new LinkedList<>();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_TYPE_RichMessage.equals(serverNotification.getType())) {

                JsonObject data = serverNotification.getData();

                Gson gson = new Gson();

                final RichMessage richMessage = gson.fromJson(data.toString(), RichMessage.class);

                if (RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessageWithMessageId(richMessage.getMessageId()) != null) {
                    continue;
                }

                MessageReceivedDetails messageReceivedDetails = new MessageReceivedDetails();

                messageReceivedDetails.setMessageType(richMessage.getMessageType());
                messageReceivedDetails.setMessageId(richMessage.getMessageId());
                messageReceivedDetails.setMessageScope(MessageReceivedDetails.MessageScope.A2P.toString());
                messageReceivedDetails.setContextItems(richMessage.getContextItems());
                messageReceivedDetails.setSenderInternalUserId(richMessage.getSenderInternalUserId());
                messageReceivedDetails.setSenderMessageId(richMessage.getMessageId());
                messageReceivedDetails.setSentTimestamp(richMessage.getSentTimestamp());

                Date expiredTime = DateAndTimeHelper.parseUtcDate(richMessage.getExpiryTimeStamp());

                boolean receivedExpired = false;

                if (expiredTime != null) {
                    receivedExpired = new Date().after(expiredTime);
                    messageReceivedDetails.setReceivedExpired(receivedExpired);
                }

                AcknowledgementDetail acknowledgementDetail = new AcknowledgementDetail();
                acknowledgementDetail.setCustomNotificationType(null);
                acknowledgementDetail.setType(serverNotification.getType());
                acknowledgementDetail.setResult(AcknowledgementDetail.Result.Delivered.toString());
                acknowledgementDetail.setSentTime(serverNotification.getCreatedOn());
                acknowledgementDetail.setServerNotificationId(serverNotification.getId());
                messageReceivedDetails.setAcknowledgementDetail(acknowledgementDetail);

                MessagingInternalController.getInstance().queueMessageReceivedNotification(messageReceivedDetails);

                richMessage.setInternalId(IdHelper.generateId());

                if (!receivedExpired) {

                    RichMessageDataController.getInstance().getRichMessagesDAO().saveRichMessage(richMessage);

                }

                richMessage.setReceivedExpired(receivedExpired);
                richMessages.add(richMessage);

            }
        }

        DonkyNetworkController.getInstance().synchronise();

        DonkyCore.publishLocalEvent(new RichMessageEvent(richMessages));

    }
}
