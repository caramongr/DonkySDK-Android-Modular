package net.donky.core.messaging.push;

import android.app.Application;
import android.content.Context;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationBatchListener;
import net.donky.core.Subscription;
import net.donky.core.assets.DonkyAssets;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.push.logic.PushLogicController;
import net.donky.core.messaging.push.logic.SimplePushHandler;
import net.donky.core.messaging.push.ui.SimplePushUIConfiguration;
import net.donky.core.network.ServerNotification;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of Donky Push Messages Logic Module.
 *
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyPush {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.1";

    public final static String PLATFORM = "Mobile";

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    private Context context;

    /**
     * UI configuration for simple and interactive push notifications.
     */
    private SimplePushUIConfiguration simplePushUIConfiguration;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyPush() {

    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyPush INSTANCE = new DonkyPush();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyPush getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Push Logic Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkyPush(final Application application, final boolean shouldDisplayRemoteNotifications, final DonkyListener donkyListener) {

        getInstance().init(application, shouldDisplayRemoteNotifications, donkyListener);

    }

    private void init(final Application application, final boolean shouldDisplayRemoteNotifications, final DonkyListener donkyListener) {

        if (!initialised.get()) {

            this.context = application.getApplicationContext();

            try {

                DonkyCore.registerModule(new ModuleDefinition(DonkyPush.class.getSimpleName(), version));

                DonkyMessaging.initialiseDonkyMessaging(application, new DonkyListener() {

                    @Override
                    public void success() {

                        DonkyAssets.initialiseDonkyAssets(application, new DonkyListener() {

                            @Override
                            public void success() {

                                PushLogicController.getInstance().init(application);

                                if (shouldDisplayRemoteNotifications) {
                                    simplePushUIConfiguration = new SimplePushUIConfiguration(context);
                                }

                                List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();

                                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TYPE_SimplePushMessage,
                                        new NotificationBatchListener<ServerNotification>() {

                                            @Override
                                            public void onNotification(ServerNotification notification) {

                                            }

                                            @Override
                                            public void onNotification(List<ServerNotification> notifications) {
                                                new SimplePushHandler().handleSimplePushMessage(context, shouldDisplayRemoteNotifications, simplePushUIConfiguration, notifications);
                                            }

                                        }));

                                DonkyCore.subscribeToDonkyNotifications(
                                        new ModuleDefinition(DonkyPush.class.getSimpleName(), version),
                                        serverNotificationSubscriptions,
                                        false);

                                initialised.set(true);

                                if (donkyListener != null) {
                                    donkyListener.success();
                                }
                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                if (donkyListener != null) {
                                    donkyListener.error(donkyException, validationErrors);
                                }
                            }
                        });
                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        if (donkyListener != null) {
                            donkyListener.error(donkyException, null);
                        }

                    }
                });

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising Automation Module");
                donkyException.initCause(e);

                if (donkyListener != null) {
                    donkyListener.error(donkyException, null);
                }

            }

        } else {

            if (donkyListener != null) {
                donkyListener.success();
            }
        }
    }

    /**
     * Check if Push Logic Module is successfully initialised.
     *
     * @return True if Push Logic Module is successfully initialised.
     */
    public static boolean isInitialised() {
        return initialised.get();
    }

    /**
     * Get UI configuration for simple and interactive push notifications.
     *
     * @return UI configuration for simple and interactive push notifications.
     */
    public SimplePushUIConfiguration getSimplePushUIConfiguration() {

        return simplePushUIConfiguration;

    }

    /**
     * Override UI configuration for simple and interactive push notifications.
     *
     * @param simplePushUIConfiguration UI configuration for simple and interactive push notifications.
     */
    public void setSimplePushUIConfiguration(SimplePushUIConfiguration simplePushUIConfiguration) {

        this.simplePushUIConfiguration = simplePushUIConfiguration;

    }
}
