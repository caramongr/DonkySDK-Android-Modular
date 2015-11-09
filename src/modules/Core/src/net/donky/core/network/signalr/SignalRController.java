package net.donky.core.network.signalr;

import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.restapi.secured.SynchroniseResponse;

import java.util.List;

/**
 * Interface respected by 'DonkySignalRService' service that SignalR module is registering in Core module when initialising.
 *
 * Created by Marcin Swierczek
 * 12/09/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class SignalRController {

    public static String SERVICE_NAME = "MobileSignalR";

    /**
     * Configure and start signalR connection.
     */
    abstract public void startSignalR();

    /**
     * Stop signalR Connection.
     */
    abstract public void stopSignalR();

    /**
     * Perform network synchronisation.
     * @param resultListener
     */
    abstract public void synchronise(final List<ClientNotification> clientNotificationsToSend, final DonkyResultListener<SynchroniseResponse> resultListener);

}
