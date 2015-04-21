package net.donky.core.observables;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationListener;
import net.donky.core.OutboundNotification;
import net.donky.core.Subscription;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.ServerNotification;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Controller for subscriptions for notifications being send or received.
 * <p/>
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SubscriptionController {

    /**
     * Observable for outbound notifications.
     */
    private final NotificationObservable<OutboundNotification> outboundNotificationObservable;

    /**
     * Observable for inbound Donky notifications.
     */
    private final NotificationObservable<ServerNotification> inboundDonkyNotificationObservable;

    /**
     * Observable for inbound content/custom notifications.
     */
    private final NotificationObservable<ServerNotification> inboundContentNotificationObservable;

    private SubscriptionController() {

        inboundContentNotificationObservable = new NotificationObservable<>();
        inboundDonkyNotificationObservable = new NotificationObservable<>();
        outboundNotificationObservable = new NotificationObservable<>();

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final SubscriptionController INSTANCE = new SubscriptionController();
    }

    /**
     * @return Static instance of Donky Core singleton.
     */
    public static SubscriptionController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Adds a subscription for specific types of Content/Custom notification.
     *
     * @param moduleDefinition                 The module details.
     * @param contentNotificationSubscriptions The subscriptions to register for this module.
     */
    public static void subscribeToContentNotifications(ModuleDefinition moduleDefinition, List<Subscription<ServerNotification>> contentNotificationSubscriptions) {

        getInstance().inboundContentNotificationObservable.subscribeToNotifications(moduleDefinition, contentNotificationSubscriptions, ServerNotification.NOTIFICATION_CATEGORY_CUSTOM, true);

    }

    /**
     * Removes a subscription for specific types of custom/Content notification.
     *
     * @param moduleDefinition              The module details.
     * @param donkyNotificationSubscription The subscriptions to remove.
     */
    public static void unsubscribeFromContentNotification(ModuleDefinition moduleDefinition, Subscription<ServerNotification> donkyNotificationSubscription) {

        getInstance().inboundContentNotificationObservable.unsubscribeFromNotifications(moduleDefinition, donkyNotificationSubscription);

    }

    /**
     * API for Donky module usage only. Adds a subscription for specific types of Donky notification.
     *
     * @param moduleDefinition              The module details.
     * @param donkyNotificationSubscription The subscriptions to register for this module.
     */
    public static void subscribeToDonkyNotifications(ModuleDefinition moduleDefinition, List<Subscription<ServerNotification>> donkyNotificationSubscription, boolean autoAcknowledge) {

        getInstance().inboundDonkyNotificationObservable.subscribeToNotifications(moduleDefinition, donkyNotificationSubscription, ServerNotification.NOTIFICATION_CATEGORY_DONKY, autoAcknowledge);
    }

    /**
     * Removes a subscription for specific types of Donky notification.
     *
     * @param moduleDefinition              The module details.
     * @param donkyNotificationSubscription The subscriptions to remove.
     */
    public static void unsubscribeFromDonkyNotification(ModuleDefinition moduleDefinition, Subscription<ServerNotification> donkyNotificationSubscription) {

        getInstance().inboundDonkyNotificationObservable.unsubscribeFromNotifications(moduleDefinition, donkyNotificationSubscription);

    }

    /**
     * Subscribes to outbound notifications. Callbacks are made during the Synchronise flow.
     *
     * @param moduleDefinition                  The module details.
     * @param outboundNotificationSubscriptions The subscriptions to register for this module.
     */
    public static void subscribeToOutboundNotifications(ModuleDefinition moduleDefinition, List<Subscription<OutboundNotification>> outboundNotificationSubscriptions) {

        getInstance().outboundNotificationObservable.subscribeToNotifications(moduleDefinition, outboundNotificationSubscriptions, "Outbound", false);

    }

    /**
     * Removes a subscription for specific types of outbound notification.
     *
     * @param moduleDefinition                 The module details.
     * @param outboundNotificationSubscription The subscriptions to remove.
     */
    public static void unsubscribeFromOutboundNotification(ModuleDefinition moduleDefinition, Subscription<OutboundNotification> outboundNotificationSubscription) {

        getInstance().outboundNotificationObservable.unsubscribeFromNotifications(moduleDefinition, outboundNotificationSubscription);

    }

    /**
     * Get all subscriptions registered for server notifications.
     *
     * @param category Category of server notification - {@link net.donky.core.network.ServerNotification#NOTIFICATION_CATEGORY_DONKY} or {@link net.donky.core.network.ServerNotification#NOTIFICATION_CATEGORY_CUSTOM}
     * @param type     Type of notification that the subscriber is registered to listen for.
     * @return Subscriptions for given notification type.
     */
    public List<SubscriptionInternal<ServerNotification>> getSubscriptionsForServerNotification(String category, String type) {


        List<SubscriptionInternal<ServerNotification>> triggeredInboundNotificationSubscriptions = new LinkedList<>();

        NotificationObservable<ServerNotification> observable = null;

        if (ServerNotification.NOTIFICATION_CATEGORY_CUSTOM.equals(category)) {

            observable = inboundContentNotificationObservable;

        } else if (ServerNotification.NOTIFICATION_CATEGORY_DONKY.equals(category)) {

            observable = inboundDonkyNotificationObservable;

        }

        if (type != null && observable != null) {

            for (final SubscriptionInternal<ServerNotification> inboundNotificationSubscription : observable.getSubscriptions()) {

                if (inboundNotificationSubscription.getNotificationType() != null && inboundNotificationSubscription.getNotificationType().equals(type)) {

                    triggeredInboundNotificationSubscriptions.add(inboundNotificationSubscription);

                }

            }

        }

        return triggeredInboundNotificationSubscriptions;
    }

    /**
     * Notify subscribers about outbound Client notifications.
     *
     * @param clientNotification Client notification to notify subscribers about.
     */
    public void notifyAboutOutboundClientNotification(final ClientNotification clientNotification) {

        for (final SubscriptionInternal<OutboundNotification> outboundNotificationSubscriptionInternal : outboundNotificationObservable.getSubscriptions()) {

            if (outboundNotificationSubscriptionInternal.getNotificationType() != null && outboundNotificationSubscriptionInternal.getNotificationType().equals(clientNotification.getBaseNotificationType())) {

                final NotificationListener<OutboundNotification> listener = outboundNotificationSubscriptionInternal.getListener();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onNotification(new OutboundNotification(clientNotification));
                    }
                });

            }

        }

    }
}
