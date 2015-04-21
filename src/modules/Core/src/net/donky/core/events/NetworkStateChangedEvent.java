package net.donky.core.events;

import net.donky.core.network.DonkyNetworkController;

/**
 * Represent Event raised by Donky Core library or another Donky Module when network connectivity changed.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NetworkStateChangedEvent extends LocalEvent {

    private boolean isConnected;

    private DonkyNetworkController.ConnectionType connectionType;

    /**
     * Local Donky event delivered to subscribers when the connectivity state changed.
     *
     * @param isConnected True if device is connected to the internet.
     * @param connectionType Type of internet connection.
     */
    public NetworkStateChangedEvent(boolean isConnected, DonkyNetworkController.ConnectionType connectionType) {
        super();
        this.isConnected = isConnected;
        this.connectionType = connectionType;
    }

    /**
     * Is device is connected to the internet.
     *
     * @return True if device is connected to the internet.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Get type of internet connection.
     *
     * @return Type of internet connection.
     */
    public DonkyNetworkController.ConnectionType getConnectionType() {
        return connectionType;
    }
}
