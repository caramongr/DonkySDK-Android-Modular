package net.donky.core.messaging.logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyCore;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.events.SyncMessageDeletedEvent;
import net.donky.core.messaging.logic.events.SyncMessageReadEvent;
import net.donky.core.network.ServerNotification;

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
     * Handle change of message status to 'Read'
     *
     * @param serverNotifications List of server notifications to be processed.
     */
    public void handleMessageReadNotification(List<ServerNotification> serverNotifications) {

        List<String> ids = new LinkedList<>();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_TYPE_SyncMsgRead.equals(serverNotification.getType())) {

                try {

                    JsonObject data = serverNotification.getData();

                    Gson gson = new Gson();

                    final MessageReadNotification stateNotification = gson.fromJson(data.toString(), MessageReadNotification.class);

                    if (stateNotification != null) {
                        ids.add(stateNotification.messageId);
                    }
                } catch (Exception e) {
                    new DLog("").error("Error parsing sync msg read.", e);
                }
            }

        }

        if (!ids.isEmpty()) {
            DonkyCore.publishLocalEvent(new SyncMessageReadEvent(ids));
        }

    }

    /**
     * Handle change of message status to 'Read'
     *
     * @param serverNotifications List of server notifications to be processed.
     */
    public void handleMessageDeletedNotification(List<ServerNotification> serverNotifications) {

        List<String> ids = new LinkedList<>();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_TYPE_SyncMsgDeleted.equals(serverNotification.getType())) {

                try {

                    JsonObject data = serverNotification.getData();

                    Gson gson = new Gson();

                    final MessageDeletedNotification stateNotification = gson.fromJson(data.toString(), MessageDeletedNotification.class);

                    if (stateNotification != null) {
                        ids.add(stateNotification.messageId);
                    }
                } catch (Exception e) {
                    new DLog("").error("Error parsing sync msg deleted.", e);
                }
            }

        }

        if (!ids.isEmpty()) {
            DonkyCore.publishLocalEvent(new SyncMessageDeletedEvent(ids));
        }
    }

    /**
     * Represents a message state update from the data of a server notification.
     */
    private class MessageReadNotification {

        @SerializedName("messageId")
        private String messageId;

    }

    /**
     * Represents a message state update from the data of a server notification.
     */
    private class MessageDeletedNotification {

        @SerializedName("messageId")
        private String messageId;

    }
}
