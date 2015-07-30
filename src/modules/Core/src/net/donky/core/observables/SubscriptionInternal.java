package net.donky.core.observables;

import net.donky.core.ModuleDefinition;
import net.donky.core.Notification;
import net.donky.core.NotificationBatchListener;
import net.donky.core.NotificationListener;
import net.donky.core.Subscription;

/**
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SubscriptionInternal<T extends Notification> {

    /**
     * The module definition for the module that is subscribing for notifications.
     */
    private final ModuleDefinition moduleDefinition;

    /**
     * Callback that will be invoked when the notification is was received or send.
     */
    private final NotificationListener<T> listener;

    /**
     * Callback that will be invoked when the notification is was received or send.
     */
    private final NotificationBatchListener<T> batchListener;

    /**
     * Type of the notification that when it is being send or received the Callback should be invoked.
     */
    private final String notificationType;

    /**
     * Category of notification {@link net.donky.core.network.ServerNotification#NOTIFICATION_CATEGORY_CUSTOM} or {@link net.donky.core.network.ServerNotification#NOTIFICATION_CATEGORY_DONKY}
     */
    private String notificationCategory;

    /**
     * True if this notification should e acknowledged by Core SDK automatically.
     */
    private boolean autoAcknowledge;

    /**
     * Subscription to receive server notification receive callbacks. Internal wrapper used by observable.
     *
     * @param moduleDefinition Details of subscribing Module.
     * @param notificationSubscription Subscription details.
     * @param notificationCategory Category of notification. Used to distinguish between Donky and Custom notifications. {@link net.donky.core.network.ServerNotification#NOTIFICATION_CATEGORY_CUSTOM} or {@link net.donky.core.network.ServerNotification#NOTIFICATION_CATEGORY_DONKY}
     * @param getAutoAcknowledge Should the module auto acknowledge the notification.
     */
    public SubscriptionInternal(ModuleDefinition moduleDefinition, Subscription<T> notificationSubscription, String notificationCategory, boolean getAutoAcknowledge) {
        this.moduleDefinition = moduleDefinition;
        this.listener = notificationSubscription.getNotificationListener();
        this.batchListener = notificationSubscription.getNotificationBatchListener();
        this.notificationType = notificationSubscription.getNotificationType();
        this.notificationCategory = notificationCategory;
        this.autoAcknowledge = getAutoAcknowledge;
    }

    /**
     * Subscription to receive server notification receive callbacks. Internal wrapper used by observable.
     *
     * @param moduleDefinition Details of subscribing Module.
     * @param notificationSubscription Subscription details.
     */
    public SubscriptionInternal(ModuleDefinition moduleDefinition, Subscription<T> notificationSubscription) {
        this.moduleDefinition = moduleDefinition;
        this.listener = notificationSubscription.getNotificationListener();
        this.batchListener = notificationSubscription.getNotificationBatchListener();
        this.notificationType = notificationSubscription.getNotificationType();
    }

    /**
     * @return Details of subscribing Module.
     */
    public ModuleDefinition getModuleDefinition() {
        return moduleDefinition;
    }

    /**
     * @return Listener for server notifications.
     */
    public NotificationListener<T> getListener() {
        return listener;
    }

    /**
     * @return Listener for server notifications.
     */
    public NotificationBatchListener<T> getBatchListener() {
        return batchListener;
    }

    /**
     * @return Type of server notification to which the subscription apply.
     */
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * @return Category of notification.  Used to distinguish between Donky and Custom notifications.
     */
    public String getNotificationCategory() {
        return notificationCategory;
    }

    /**
     * @return Should the Donky Core Module acknowledge the notification.
     */
    public boolean isAutoAcknowledge() {
        return autoAcknowledge;
    }
}
