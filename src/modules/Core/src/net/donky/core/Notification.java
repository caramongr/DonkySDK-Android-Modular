package net.donky.core;

import net.donky.core.helpers.IdHelper;

/**
 * Base type for {@link net.donky.core.network.ServerNotification}, {@link net.donky.core.OutboundNotification}, {@link net.donky.core.network.ClientNotification} and {@link net.donky.core.network.content.ContentNotification} Notification
 *
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Notification {

    /**
     * The type of notification.
     */
    private String baseNotificationType;

    private String notificationId;

    /**
     * Base type of notifications.
     *
     * @param baseNotificationType Type of notification should be populated by a child class.
     */
    protected Notification(String baseNotificationType, String id) {

        this.baseNotificationType = baseNotificationType;

        this.notificationId = id;

    }

    /**
     * Get the type of notification.
     *
     * @return The type of notification.
     */
    public String getBaseNotificationType() {
        return baseNotificationType;
    }

    public void setBaseNotificationType(String baseNotificationType) {
        this.baseNotificationType = baseNotificationType;
    }

    public String getId() {
        return notificationId;
    }
}
