package net.donky.core.observables;

import net.donky.core.ModuleDefinition;
import net.donky.core.Notification;
import net.donky.core.Subscription;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observable for subscriptions for notifications being send or received. The generic type is the type of notification that this observable can store subscriptions for.
 *
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class NotificationObservable<T extends Notification> {

    /**
     * Stores listeners registered by Donky Modules that want to be notified about server notifications.
     */
    private final CopyOnWriteArrayList<SubscriptionInternal<T>> observersForNotifications = new CopyOnWriteArrayList<>();

    /**
     * Subscribes to notifications. Callbacks are made during the Synchronise flow.
     *
     * @param moduleDefinition The module details.
     * @param subscriptions    The subscriptions to register for this module.
     */
    public void subscribeToNotifications(ModuleDefinition moduleDefinition, List<Subscription<T>> subscriptions) {

        List<SubscriptionInternal<T>> moduleSubscriptionsToAdd = new LinkedList<>();

        for (Subscription<T> notificationSubscription : subscriptions) {

            SubscriptionInternal<T> moduleSubscription = new SubscriptionInternal<>(moduleDefinition, notificationSubscription);
            moduleSubscriptionsToAdd.add(moduleSubscription);

        }

        observersForNotifications.addAll(moduleSubscriptionsToAdd);
    }

    /**
     * Subscribes to notifications. Callbacks are made during the Synchronise flow.
     *
     * @param moduleDefinition The module details.
     * @param subscriptions    The subscriptions to register for this module.
     */
    public void subscribeToNotifications(ModuleDefinition moduleDefinition, List<Subscription<T>> subscriptions, String category, boolean autoAcknowledge) {

        List<SubscriptionInternal<T>> moduleSubscriptionsToAdd = new LinkedList<>();

        for (Subscription<T> notificationSubscription : subscriptions) {

            SubscriptionInternal<T> moduleSubscription = new SubscriptionInternal<>(moduleDefinition, notificationSubscription, category, autoAcknowledge);
            moduleSubscriptionsToAdd.add(moduleSubscription);

        }

        observersForNotifications.addAll(moduleSubscriptionsToAdd);
    }

    /**
     * Unsubscribe from notifications. Callbacks are made during the Synchronise flow.
     *
     * @param moduleDefinition The module details.
     * @param subscription     Subscriptions to remove.
     */
    public void unsubscribeFromNotifications(ModuleDefinition moduleDefinition, Subscription<T> subscription) {

        if (subscription != null) {

            List<SubscriptionInternal<T>> moduleSubscriptionsToRemove = new LinkedList<>();

            for (SubscriptionInternal<T> notificationSubscription : observersForNotifications) {

                if (notificationSubscription.getNotificationType() != null &&
                        notificationSubscription.getNotificationType().equals(subscription.getNotificationType()) &&
                        (
                                (notificationSubscription.getListener() != null &&
                                        notificationSubscription.getListener().equals(subscription.getNotificationListener())) ||

                                        (notificationSubscription.getBatchListener() != null &&
                                                notificationSubscription.getBatchListener().equals(subscription.getNotificationBatchListener()))
                        )
                        ) {

                    moduleSubscriptionsToRemove.add(notificationSubscription);

                }
            }

            observersForNotifications.removeAll(moduleSubscriptionsToRemove);

        }
    }

    public CopyOnWriteArrayList<SubscriptionInternal<T>> getSubscriptions() {

        return observersForNotifications;

    }

}
