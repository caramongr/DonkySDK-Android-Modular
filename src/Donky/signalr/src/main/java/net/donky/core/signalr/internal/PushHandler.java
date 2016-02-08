package net.donky.core.signalr.internal;

/**
 * Push Handler to get the messages from the Network by signalR
 *
 * Created by Marcin Swierczek
 * 20/09/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface PushHandler {

    /**
     * Handle push message sent from Network by the signalR connection.
     * @param obj Received data from Network
     */
    void handlePush(Object obj);

}
