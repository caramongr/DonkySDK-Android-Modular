package net.donky.core.messaging.logic;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationBatchListener;
import net.donky.core.Subscription;
import net.donky.core.network.ServerNotification;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of Common Messaging Module.
 *
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyMessaging {

    public static final String KEY_INTENT_BUNDLE_RICH_MESSAGE = "richMessage";

    public static final String KEY_INTENT_BUNDLE_CONVERSATION = "conversation";

    public static final String KEY_INTENT_BUNDLE_CONTACT = "contact";

    public static final String KEY_INTENT_BUNDLE_CONTACT_LIST = "contactList";

    public static final String KEY_INTENT_BUNDLE_PARTICIPANTS = "participants";

    public static final String KEY_INTENT_CONTACT_CHANGED = "contactChanged";

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.1.0.0";

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyMessaging() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyMessaging INSTANCE = new DonkyMessaging();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyMessaging getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Messaging Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkyMessaging(final Application application, final DonkyListener donkyListener) {

        getInstance().init(donkyListener);

    }

    private void init(final DonkyListener donkyListener) {

        if (!initialised.get()) {

            try {

                DonkyCore.registerModule(new ModuleDefinition(DonkyMessaging.class.getSimpleName(), version));

                List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();

                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TYPE_SyncMsgDeleted,
                        new NotificationBatchListener<ServerNotification>() {

                            @Override
                            public void onNotification(ServerNotification notification) {

                            }

                            @Override
                            public void onNotification(List<ServerNotification> notifications) {
                                new NotificationHandler().handleMessageDeletedNotification(notifications);
                            }

                        }));

                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TYPE_SyncMsgRead,
                        new NotificationBatchListener<ServerNotification>() {

                            @Override
                            public void onNotification(ServerNotification notification) {

                            }

                            @Override
                            public void onNotification(List<ServerNotification> notifications) {
                                new NotificationHandler().handleMessageReadNotification(notifications);
                            }

                        }));

                DonkyCore.subscribeToDonkyNotifications(
                        new ModuleDefinition(DonkyMessaging.class.getSimpleName(), version),
                        serverNotificationSubscriptions,
                        false);

                initialised.set(true);

                if (donkyListener != null) {
                    donkyListener.success();
                }

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising Donky Common Messaging Module.");
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

}
