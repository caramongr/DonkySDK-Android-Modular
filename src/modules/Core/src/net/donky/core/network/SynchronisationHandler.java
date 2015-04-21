package net.donky.core.network;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.observables.SubscriptionController;
import net.donky.core.observables.SubscriptionInternal;

import java.util.LinkedList;
import java.util.List;

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

        for (final ServerNotification serverNotification : serverNotifications) {

            boolean isCategoryCustom = serverNotification.getType().equals(ServerNotification.NOTIFICATION_CATEGORY_CUSTOM);

            String category;

            String type = null;

            boolean shouldNotifyInMainThread = true;

            if (isCategoryCustom) {

                category = ServerNotification.NOTIFICATION_CATEGORY_CUSTOM;

                try {

                    type = serverNotification.getData().get("customType").getAsString();

                } catch (Exception e) {

                    log.error("Error parsing custom server notification type", e);

                }

            } else {

                category = ServerNotification.NOTIFICATION_CATEGORY_DONKY;

                shouldNotifyInMainThread = false;

                type = serverNotification.getType();

            }

            serverNotification.setBaseNotificationType(type);

            List<SubscriptionInternal<ServerNotification>> subscriptions = SubscriptionController.getInstance().getSubscriptionsForServerNotification(category, type);

            acknowledgeNotification(serverNotification, subscriptions, type);

            notifySubscribers(serverNotification, subscriptions, shouldNotifyInMainThread);

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
     * @param serverNotification Received Server Notification.
     * @param subscriptions      Server Notification subscriptions to be notified on the main thread.
     */
    private void notifySubscribers(final ServerNotification serverNotification, final List<SubscriptionInternal<ServerNotification>> subscriptions, boolean shouldNotifyInMainThread) {

        Handler handler = new Handler(Looper.getMainLooper());

        for (final SubscriptionInternal<ServerNotification> subscription : subscriptions) {

            if (serverNotification.getBaseNotificationType() != null && serverNotification.getBaseNotificationType().equals(subscription.getNotificationType())) {

                //log.debug("module " + subscription.getModuleDefinition().getName() + " notified about " + serverNotification.getType() + " id " + serverNotification.getId());


                if (shouldNotifyInMainThread) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            subscription.getListener().onNotification(serverNotification);
                        }
                    });

                } else {

                    subscription.getListener().onNotification(serverNotification);

                }
            }
        }
    }
}
