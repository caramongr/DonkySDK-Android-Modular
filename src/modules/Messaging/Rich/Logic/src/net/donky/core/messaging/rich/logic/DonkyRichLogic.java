package net.donky.core.messaging.rich.logic;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationBatchListener;
import net.donky.core.Subscription;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.RegistrationChangedEvent;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.messaging.rich.logic.model.RichMessagingSQLiteHelper;
import net.donky.core.model.AbstractDonkySQLiteHelper;
import net.donky.core.network.ServerNotification;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of Donky Rich Messages Logic Module.
 *
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyRichLogic {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.1";

    public final static String PLATFORM = "Mobile";

    private static String RICH_MESSAGES_SQLITE_HELPER = "RichMessagesSQLiteHelper";

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyRichLogic() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyRichLogic INSTANCE = new DonkyRichLogic();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyRichLogic getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Push Logic Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkyRich(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener);

    }

    private void init(final Application application, final DonkyListener donkyListener) {

        if (!initialised.get()) {

            try {

                DonkyMessaging.initialiseDonkyMessaging(application, new DonkyListener() {

                    @Override
                    public void success() {

                        DonkyCore.getInstance().registerService(RICH_MESSAGES_SQLITE_HELPER, AbstractDonkySQLiteHelper.SERVICE_CATEGORY_SQLITE_HELPER, new RichMessagingSQLiteHelper());

                        List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();

                        serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TYPE_RichMessage,
                                new NotificationBatchListener<ServerNotification>() {

                                    @Override
                                    public void onNotification(ServerNotification notification) {

                                    }

                                    @Override
                                    public void onNotification(List<ServerNotification> notifications) {
                                        new NotificationHandler().handleRichMessageNotification(notifications);
                                    }

                                }));

                        DonkyCore.subscribeToDonkyNotifications(
                                new ModuleDefinition(DonkyRichLogic.class.getSimpleName(), version),
                                serverNotificationSubscriptions,
                                false);

                        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<CoreInitialisedSuccessfullyEvent>(CoreInitialisedSuccessfullyEvent.class) {
                            @Override
                            public void onDonkyEvent(CoreInitialisedSuccessfullyEvent event) {

                                RichMessageDataController.getInstance().getRichMessagesDAO().removeMessagesThatExceededTheAvailabilityPeriod();

                            }
                        });

                        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<RegistrationChangedEvent>(RegistrationChangedEvent.class) {
                            @Override
                            public void onDonkyEvent(RegistrationChangedEvent event) {
                                if (event.isReplaceRegistration()) {
                                    RichMessageDataController.getInstance().getRichMessagesDAO().removeAllRichMessages();
                                }
                            }
                        });

                        initialised.set(true);

                        if (donkyListener != null) {
                            donkyListener.success();
                        }

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        if (donkyListener != null) {
                            donkyListener.error(donkyException, null);
                        }

                    }
                });

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising Rich Logic Module");
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

}
