package net.donky.core;

import java.util.Set;

/**
 * Subscription for inbound and outbound notifications.
 *
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Subscription<T extends Notification> {

    /**
     * Type of notification that the subscriber is interested in.
     */
    private final String notificationType;

    private final Set<String> notificationTypes;

    /**
     * Callback to be invoked when the notification is received or send.
     */
    private final NotificationListener<T> notificationListener;

    /**
     * Callback to be invoked when the notification is received or send.
     */
    private final NotificationBatchListener<T> notificationBatchListener;

    /**
     * Subscription for inbound and outbound notifications.
     *
     * @param notificationType     Type of notification that the subscriber is interested in.
     * @param notificationListener Callback to be invoked when the notification is received or send.
     * @deprecated Please use Subscription#Subscription(String, NotificationBatchListener) instead.
     */
    @Deprecated
    public Subscription(String notificationType, NotificationListener<T> notificationListener) {

        this.notificationType = notificationType;

        this.notificationTypes = null;

        this.notificationListener = notificationListener;

        this.notificationBatchListener = null;
    }

    /**
     * Subscription for inbound and outbound notifications.
     *
     * @param notificationType          Type of notification that the subscriber is interested in.
     * @param notificationBatchListener Callback to be invoked when the notification is received or send.
     */
    public Subscription(String notificationType, NotificationBatchListener<T> notificationBatchListener) {

        this.notificationType = notificationType;

        this.notificationTypes = null;

        this.notificationBatchListener = notificationBatchListener;

        this.notificationListener = null;
    }

    /**
     * Subscription for inbound and outbound notifications.
     *
     * @param notificationTypes         Type of notification that the subscriber is interested in.
     * @param notificationBatchListener Callback to be invoked when the notification is received or send.
     */
    public Subscription(Set<String> notificationTypes, NotificationBatchListener<T> notificationBatchListener) {

        this.notificationTypes = notificationTypes;

        this.notificationType = null;

        this.notificationBatchListener = notificationBatchListener;

        this.notificationListener = null;
    }

    /**
     * Get type of notification that the subscriber is interested in.
     *
     * @return Type of notification that the subscriber is interested in.
     */
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * Get callback to be invoked when the notification is received or send.
     *
     * @return Callback to be invoked when the notification is received or send.
     * @deprecated Please use Subscription#getNotificationBatchListener method instead.
     */
    @Deprecated
    public NotificationListener<T> getNotificationListener() {
        return notificationListener;
    }

    /**
     * Get callback to be invoked when the notification is received or send.
     *
     * @return Callback to be invoked when the notification is received or send.
     */
    public NotificationBatchListener<T> getNotificationBatchListener() {
        return notificationBatchListener;
    }

    public Set<String> getNotificationTypes() {
        return notificationTypes;
    }
}
