package net.donky.core.signalr.internal;

import net.donky.core.DonkyException;

import java.util.Map;

/**
 * Listener used to inform current SignalR Task that connection is lost.
 *
 * Created by Marcin Swierczek
 * 23/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface ConnectionListener {

    /**
     * SignalR connection lost
     */
    void notifyConnectionError(final DonkyException donkyException, final Map<String, String> validationErrors);

}
