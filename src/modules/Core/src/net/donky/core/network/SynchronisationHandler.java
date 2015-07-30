package net.donky.core.network;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.observables.SubscriptionController;
import net.donky.core.observables.SubscriptionInternal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class to handle received server notifications.
 * <p/>
 * Created by Marcin Swierczek
 * 10/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SynchronisationHandler {

    private final List<ServerNotification> serverNotifications;

    private final DLog log;

    /**
     * Create handler to process server messages received from Donky Network from synchronisation.
     *
     * @param serverNotifications List of {@link ServerNotification}'s received from Donky Network from synchronisation.
     */
    public SynchronisationHandler(final List<ServerNotification> serverNotifications) {
        this.serverNotifications = serverNotifications;
        log = new DLog("SynchronisationHandler");
    }

    /**
     * Create handler to process server messages received from Donky Network from synchronisation.
     *
     * @param serverNotification {@link ServerNotification} received from Donky Network from synchronisation.
     */
    public SynchronisationHandler(final ServerNotification serverNotification) {
        this.serverNotifications = new LinkedList<>();
        this.serverNotifications.add(serverNotification);
        log = new DLog("SynchronisationHandler");
    }

    /**
     * Send Notifications to subscribers and send acknowledge messages back to Donky Network.
     */
    public void processServerNotifications() {

        Map<String, List<ServerNotification>> customNotificationsByType = new HashMap<>();

        Map<String, List<ServerNotification>> donkyNotificationsByType = new HashMap<>();

        for (final ServerNotification serverNotification : serverNotifications) {

            boolean isCategoryCustom = serverNotification.getType().equals(ServerNotification.NOTIFICATION_CATEGORY_CUSTOM);

            String type = null;

            if (isCategoryCustom) {

                serverNotification.setCategory(ServerNotification.NOTIFICATION_CATEGORY_CUSTOM);

                try {

                    type = serverNotification.getData().get("customType").getAsString();

                } catch (Exception e) {

                    log.error("Error parsing custom server notification type", e);

                }

                serverNotification.setBaseNotificationType(type);

                addNotificationToTheMap(type, serverNotification, customNotificationsByType);

            } else {

                serverNotification.setCategory(ServerNotification.NOTIFICATION_CATEGORY_DONKY);

                type = serverNotification.getType();

                serverNotification.setBaseNotificationType(type);

                addNotificationToTheMap(type, serverNotification, donkyNotificationsByType);

            }
        }

        processNotificationOfGivenCategoryAndType(ServerNotification.NOTIFICATION_CATEGORY_DONKY, donkyNotificationsByType, false);

        processNotificationOfGivenCategoryAndType(ServerNotification.NOTIFICATION_CATEGORY_CUSTOM, customNotificationsByType, true);

    }

    private void addNotificationToTheMap(String type, ServerNotification serverNotification, Map<String, List<ServerNotification>> notificationsByType) {

        if (notificationsByType.containsKey(type)) {
            notificationsByType.get(type).add(serverNotification);
        } else {
            List<ServerNotification> notifications = new LinkedList<>();
            notifications.add(serverNotification);
            notificationsByType.put(type, notifications);
        }

    }

    private void processNotificationOfGivenCategoryAndType(String category, Map<String, List<ServerNotification>> notificationsByType, boolean shouldNotifyInMainThread) {

        for (String type : notificationsByType.keySet()) {

            List<ServerNotification> notifications = notificationsByType.get(type);

            List<SubscriptionInternal<ServerNotification>> subscriptions = SubscriptionController.getInstance().getSubscriptionsForServerNotification(category, type);

            for (ServerNotification serverNotification : notifications) {
                acknowledgeNotification(serverNotification, subscriptions, type);
            }

            notifySubscribers(type, notifications, subscriptions, shouldNotifyInMainThread);

        }

    }

    /**
     * Acknowledge - Assuming that if any Donky module don't want notification to be auto acknowledge there is only one such module [to avoid conflicts].
     *
     * @param serverNotification Received Server Notification.
     * @param subscriptions      Server Notification subscriptions registered for incoming notification type.
     * @param customType         Type of custom notification.
     */
    private void acknowledgeNotification(final ServerNotification serverNotification, final List<SubscriptionInternal<ServerNotification>> subscriptions, final String customType) {

        boolean shouldSdkAcknowledgeNotification = true;

        if (subscriptions.size() == 1 && !subscriptions.get(0).isAutoAcknowledge()) {
            shouldSdkAcknowledgeNotification = false;
        }

        if (shouldSdkAcknowledgeNotification) {

            ClientNotification clientNotification = ClientNotification.createAcknowledgment(serverNotification, customType, !subscriptions.isEmpty());

            DonkyDataController.getInstance().getNotificationDAO().addNotification(clientNotification);

        }
    }

    /**
     * Notify subscribers to Donky Notification types about received Server Notification.
     *
     * @param serverNotifications Received Server Notifications.
     * @param subscriptions      Server Notification subscriptions to be notified on the main thread.
     */
    private void notifySubscribers(final String type, final List<ServerNotification> serverNotifications, final List<SubscriptionInternal<ServerNotification>> subscriptions, boolean shouldNotifyInMainThread) {

        Handler handler = new Handler(Looper.getMainLooper());

        for (final SubscriptionInternal<ServerNotification> subscription : subscriptions) {

            if (type != null && type.equals(subscription.getNotificationType())) {

                if (shouldNotifyInMainThread) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (subscription.getBatchListener() != null) {
                                subscription.getBatchListener().onNotification(serverNotifications);
                            } else if (subscription.getListener() != null) {
                                for (ServerNotification serverNotification : serverNotifications) {
                                    subscription.getListener().onNotification(serverNotification);
                                }
                            }

                        }
                    });

                } else {

                    if (subscription.getBatchListener() != null) {
                        subscription.getBatchListener().onNotification(serverNotifications);
                    } else if (subscription.getListener() != null) {
                        for (ServerNotification serverNotification : serverNotifications) {
                            subscription.getListener().onNotification(serverNotification);
                        }
                    }

                }
            }
        }
    }

}
