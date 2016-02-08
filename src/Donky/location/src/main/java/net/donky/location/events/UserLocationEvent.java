package net.donky.location.events;

import net.donky.core.DonkyListener;
import net.donky.core.events.LocalEvent;
import net.donky.core.network.location.UserLocation;
import net.donky.location.TargetUser;

import java.util.List;

/**
 * Event notifying about location sent to you from other devices on the network e.g. in response to {@link net.donky.location.DonkyLocationController#requestUserLocation(TargetUser, DonkyListener)}}
 *
 * Created by Marcin Swierczek
 * 27/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserLocationEvent extends LocalEvent {

    private List<UserLocation> locations;

    public UserLocationEvent(List<UserLocation> locations) {
        super();
        this.locations = locations;
    }

    /**
     * Gets the users location data from these event send to you by these users
     */
    public List<UserLocation> getLocations() {
        return locations;
    }
}