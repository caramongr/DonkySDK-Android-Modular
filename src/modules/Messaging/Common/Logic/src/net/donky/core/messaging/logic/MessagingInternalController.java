package net.donky.core.messaging.logic;

import net.donky.core.DonkyListener;
import net.donky.core.messages.RichMessage;
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
     * <p/>
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
     * Send 'Message Received' client notification.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void sendMessageReadNotification(RichMessage richMessage, DonkyListener listener) {

        DonkyNetworkController.getInstance().sendClientNotification(ClientNotification.createMessageReadNotification(richMessage), listener);

    }

    /**
     * Send 'Message Received' client notification.
     *
     * @param messageReceivedDetails Description of received message.
     */
    public void queueMessageReceivedNotification(MessageReceivedDetails messageReceivedDetails) {

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createMessageReceivedNotification(messageReceivedDetails));

    }

}
