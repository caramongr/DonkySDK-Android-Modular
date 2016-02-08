package net.donky.location;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.donky.core.DonkyCore;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.location.UserLocation;
import net.donky.core.network.location.UserLocationRequest;
import net.donky.location.events.LocationRequestedEvent;
import net.donky.location.events.UserLocationEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * Class responsible for translating server notifications with location notification into Local Events.
 *
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NotificationHandler {

    NotificationHandler() {
    }

    /**
     * Translating server notifications with location requests into Local Event and fire this event.
     *
     * @param serverNotifications Server notifications with location requests from another users
     */
    void handleLocationRequest(List<ServerNotification> serverNotifications) {

        List<UserLocationRequest> requests = new LinkedList<>();

        boolean shouldAutomaticallySendLocation = DonkyLocationController.getInstance().isShouldAutomaticallySendLocation();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_LOCATION_REQUEST.equals(serverNotification.getType())) {

                JsonObject data = serverNotification.getData();

                Gson gson = new Gson();

                UserLocationRequest request = gson.fromJson(data.toString(), UserLocationRequest.class);

                if (request != null) {
                    requests.add(request);

                    if (shouldAutomaticallySendLocation) {
                        DonkyLocationController.getInstance().sendLocationUpdateToUser(TargetUser.getTargetUserByProfileId(request.getSendToNetworkProfileId()), null);
                    }
                }

            }
        }

        DonkyCore.publishLocalEvent(new LocationRequestedEvent(requests));
    }

    /**
     * Translating server notifications with location details into Local Event and fire this event.
     *
     * @param serverNotifications Server notifications with location details from another users
     */
    void handleUserLocation(List<ServerNotification> serverNotifications) {

        List<UserLocation> locations = new LinkedList<>();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_USER_LOCATION.equals(serverNotification.getType())) {

                JsonObject data = serverNotification.getData();

                Gson gson = new Gson();

                UserLocation location = gson.fromJson(data.toString(), UserLocation.class);

                if (location != null) {
                    locations.add(location);
                }

            }
        }

        DonkyCore.publishLocalEvent(new UserLocationEvent(locations));
    }

}
