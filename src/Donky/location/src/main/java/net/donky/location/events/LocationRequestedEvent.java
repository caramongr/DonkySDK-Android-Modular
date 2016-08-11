package net.donky.location.events;

import net.donky.core.DonkyListener;
import net.donky.core.events.LocalEvent;
import net.donky.core.network.location.UserLocationRequest;

import java.util.List;

/**
 * Event notifying about some users on the Donky Network requesting the location of these device. You can respond to that using net.donky.location.DonkyLocationController#sendLocationUpdate(DonkyListener)
 *
 * Created by Marcin Swierczek
 * 27/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LocationRequestedEvent extends LocalEvent {

    private List<UserLocationRequest> requests;

    public LocationRequestedEvent(List<UserLocationRequest> requests) {
        super();
        this.requests = requests;
    }

    /**
     * Gets the requests to get you current location
     */
    public List<UserLocationRequest> getRequests() {
        return requests;
    }
}