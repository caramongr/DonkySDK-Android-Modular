package net.donky.core.messaging.logic;

import net.donky.core.DonkyListener;
import net.donky.core.messaging.logic.model.CommonMessage;
import net.donky.core.messaging.logic.model.MessageReceivedDetails;
import net.donky.core.network.DonkyNetworkController;

/**
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MessagingInternalController {

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private MessagingInternalController() {
    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final MessagingInternalController INSTANCE = new MessagingInternalController();
    }

    /**
     * @return Static instance of Messaging Controller singleton.
     */
    public static MessagingInternalController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Send 'Message Read' client notification.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void sendMessageReadNotification(CommonMessage commonMessage, DonkyListener listener) {

        DonkyNetworkController.getInstance().sendClientNotification(ClientNotification.createMessageReadNotification(commonMessage), listener);

    }

    /**
     * Queue 'Message Read' client notification.
     */
    public void queueMessageReadNotification(CommonMessage commonMessage) {

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createMessageReadNotification(commonMessage));

    }

    /**
     * Queue 'Message Deleted' client notification.
     */
    public void queueMessageDeletedNotification(CommonMessage commonMessage) {

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createMessageDeletedNotification(commonMessage));

    }

    /**
     * Queue 'Message Deleted' client notification.
     */
    public void sendMessageDeletedNotification(CommonMessage commonMessage, DonkyListener listener) {

        DonkyNetworkController.getInstance().sendClientNotification(ClientNotification.createMessageDeletedNotification(commonMessage), null);

    }

    /**
     * Queue 'Message Received' client notification.
     *
     * @param messageReceivedDetails Description of received message.
     */
    public void queueMessageReceivedNotification(MessageReceivedDetails messageReceivedDetails) {

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createMessageReceivedNotification(messageReceivedDetails));

    }

    /**
     * Send 'Message Shared' client notification.
     *
     * @param commonMessage Shared rich message.
     * @param sharedTo App name that the message was shared with.
     */
    public void queueMessageSharedNotification(CommonMessage commonMessage, String sharedTo) {

        DonkyNetworkController.getInstance().sendClientNotification(ClientNotification.createMessageSharedNotification(commonMessage, sharedTo), null);

    }

}
