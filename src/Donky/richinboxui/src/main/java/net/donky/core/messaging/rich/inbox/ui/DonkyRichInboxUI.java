package net.donky.core.messaging.rich.inbox.ui;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.assets.DonkyAssets;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.rich.logic.DonkyRichLogic;
import net.donky.core.messaging.rich.logic.SyncRichMessageEvent;
import net.donky.core.messaging.rich.logic.RichMessageEvent;
import net.donky.core.messaging.ui.DonkyMessagingUI;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class with which you can initialise the Rich Messaging Inbox SDK.
 *
 * Created by Marcin Swierczek
 * 04/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyRichInboxUI {


    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.1";

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyRichInboxUI() {

    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyRichInboxUI INSTANCE = new DonkyRichInboxUI();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyRichInboxUI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Push UI Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkyRich(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener, null);

    }

    /**
     * Initialise Donky Push UI Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     * @param pushConfiguration Configuration with Activities classes that should be launched when user taps remote notifications
     */
    public static void initialiseDonkyRich(final Application application, final DonkyListener donkyListener, final PushConfiguration pushConfiguration) {

        getInstance().init(application, donkyListener, pushConfiguration);

    }

    private void init(final Application application, final DonkyListener donkyListener, PushConfiguration pushConfiguration) {

        if (!initialised.get()) {

            try {

                RichInboxUIController.getInstance().init(application.getApplicationContext(), pushConfiguration);

                DonkyAssets.initialiseDonkyAssets(application, new DonkyListener() {

                    @Override
                    public void success() {

                        DonkyRichLogic.initialiseDonkyRich(application, new DonkyListener() {

                            @Override
                            public void success() {

                                DonkyMessagingUI.initialiseDonkyMessaging(application, new DonkyListener() {

                                            @Override
                                            public void success() {

                                                DonkyCore.registerModule(new ModuleDefinition(DonkyRichInboxUI.class.getSimpleName(), version));

                                                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<RichMessageEvent>(RichMessageEvent.class) {

                                                    @Override
                                                    public void onDonkyEvent(RichMessageEvent event) {

                                                        if (event != null) {
                                                            RichInboxUIController.getInstance().notifyListeners(event.getRichMessages());
                                                        } else {
                                                            new DLog("handleRichMessageEvent").info("RichMessage received expired.");
                                                        }
                                                    }
                                                });

                                                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<SyncRichMessageEvent>(SyncRichMessageEvent.class) {

                                                    @Override
                                                    public void onDonkyEvent(SyncRichMessageEvent event) {
                                                        RichInboxUIController.getInstance().notifyListeners(null);
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
                                                    donkyListener.error(donkyException, validationErrors);
                                                }
                                            }
                                        }
                                );

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
                            donkyListener.error(donkyException, validationErrors);
                        }
                    }
                });

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising DonkyRichUI Module");
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
