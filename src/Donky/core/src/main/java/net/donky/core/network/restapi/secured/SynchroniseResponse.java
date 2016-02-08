package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

import net.donky.core.network.ServerNotification;

import java.util.List;

/**
 * Network response for notifications synchronisation request.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SynchroniseResponse {

    @SerializedName("serverNotifications")
    private List<ServerNotification> serverNotifications;

    @SerializedName("failedClientNotifications")
    private List<FailedClientNotification> failedClientNotifications;

    @SerializedName("moreNotificationsAvailable")
    private boolean moreNotificationsAvailable;

    /**
     * @return Server notifications to be processed by the modules.
     */
    public List<ServerNotification> getServerNotifications() {
        return serverNotifications;
    }

    /**
     * @return Details of notifications sent by the client that couldn't be processed.
     */
    public List<FailedClientNotification> getFailedClientNotifications() {
        return failedClientNotifications;
    }

    /**
     * @return True if there more notifications available on the server to download.
     */
    public boolean isMoreNotificationsAvailable() {
        return moreNotificationsAvailable;
    }

    /**
     * Represents notifications sent by the client that couldn't be processed by the server.
     */
    public class FailedClientNotification {

        @SerializedName("notification")
        private Notification notification;

        @SerializedName("failureReason")
        private String failureReason;

        @SerializedName("validationFailures")
        private List<ValidationFailure> validationFailures;

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();

            for (ValidationFailure validationFailure : validationFailures) {
                sb.append(validationFailure.toString());
            }

            return "Notification "+notification+"; Reason "+failureReason+"; Validation Failures: "+sb.toString();
        }

        public Notification getNotification() {
            return notification;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public List<ValidationFailure> getValidationFailures() {
            return validationFailures;
        }
    }

    /**
     * Represents details for notification sent by the client that couldn't be processed.
     */
    private class Notification {

        @SerializedName("type")
        private String type;

        @Override
        public String toString() {
            return "type: "+type;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * Represents details for failure reasons for notification sent by the client that couldn't be processed.
     */
    public class ValidationFailure {

        @SerializedName("property")
        private String property;

        @SerializedName("details")
        private String details;

        @SerializedName("failureKey")
        private String failureKey;

        @Override
        public String toString() {
            return "property: "+property+" details "+details+" failure key: "+failureKey+"; ";
        }

        public String getProperty() {
            return property;
        }

        public String getDetails() {
            return details;
        }

        public String getFailureKey() {
            return failureKey;
        }
    }
}
