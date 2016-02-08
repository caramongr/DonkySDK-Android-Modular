package net.donky.core.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyCore;
import net.donky.core.events.RegistrationChangedEvent;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.ServerNotification;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Handler for user update details from another device.
 *
 * Created by Marcin Swierczek
 * 26/01/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserUpdatedHandler {

    /**
     * Looks for most recent server notification and saves user details locally.
     * @param notifications List of server notifications of type {@link ServerNotification#NOTIFICATION_TYPE_UserUpdated)
     */
    public void handleUserUpdatedNotifications(List<ServerNotification> notifications) {

        if (notifications != null && !notifications.isEmpty()) {

            ServerNotification lastNotification = null;
            long lastNotificationCreatedOn = 0;

            for (ServerNotification notification : notifications) {

                long createdOn = DateAndTimeHelper.parseUTCStringToUTCLong(notification != null ? notification.getCreatedOn() : null);

                if (lastNotification == null || createdOn > lastNotificationCreatedOn) {
                    lastNotification = notification;
                    lastNotificationCreatedOn = createdOn;
                }
            }

            processNotification(lastNotification, lastNotificationCreatedOn);
        }

    }

    /**
     * Saves the update and publish local event.
     */
    private void processNotification(ServerNotification notification, long createdOn) {

        Gson gson = new GsonBuilder().create();

        UserUpdated userUpdated = gson.fromJson(notification.getData(), UserUpdated.class);

        if (userUpdated != null) {

            UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

            if (userDetails.getLastUpdated() < createdOn) {

                userDetails.setUserDisplayName(userUpdated.displayName);
                userDetails.setUserEmailAddress(userUpdated.emailAddress);
                userDetails.setUserAdditionalProperties(userUpdated.additionalProperties);
                userDetails.setUserId(userUpdated.externalUserId);
                userDetails.setUserAvatarId(userUpdated.avatarAssetId);
                userDetails.setUserMobileNumber(userUpdated.phoneNumber);
                userDetails.setUserFirstName(userUpdated.firstName);
                userDetails.setUserLastName(userUpdated.lastName);
                userDetails.setCountryCode(userUpdated.countryIsoCode);
                userDetails.setLastUpdated(System.currentTimeMillis());
                DonkyDataController.getInstance().getUserDAO().setUserDetails(userDetails);

                DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, DonkyAccountController.getInstance().getDeviceDetails(), false));
            }
        }
    }

    /**
     * Represents the user updated details
     */
    class UserUpdated {

        @SerializedName("networkProfileId")
        private String networkProfileId;

        @SerializedName("externalUserId")
        private String externalUserId;

        @SerializedName("displayName")
        private String displayName;

        @SerializedName("emailAddress")
        private String emailAddress;

        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        @SerializedName("countryIsoCode")
        private String countryIsoCode;

        @SerializedName("phoneNumber")
        private String phoneNumber;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        @SerializedName("avatarAssetId")
        private String avatarAssetId;

        @SerializedName("operatingSystems")
        private List<String> operatingSystems;

        @SerializedName("utcOffsetMins")
        private long utcOffsetMins;

        @SerializedName("registeredOn")
        private String registeredOn;

        @SerializedName("additionalProperties")
        private TreeMap<String, String> additionalProperties;

        @SerializedName("selectedTags")
        private Set<String> selectedTags;

        @SerializedName("billingStatus")
        private String billingStatus;

        @SerializedName("isAnonymous")
        private boolean isAnonymous;

    }

}
