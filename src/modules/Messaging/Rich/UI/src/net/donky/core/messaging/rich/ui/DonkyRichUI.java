package net.donky.core.messaging.rich.ui;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.events.ApplicationStartEvent;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.messaging.rich.logic.DonkyRichLogic;
import net.donky.core.messaging.rich.logic.RichMessageEvent;
import net.donky.core.messaging.ui.DonkyMessagingUI;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class for Donky Rich Messages popup UI.
 * <p/>
 * Created by Marcin Swierczek
 * 14/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyRichUI {

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
    private DonkyRichUI() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyRichUI INSTANCE = new DonkyRichUI();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyRichUI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Push UI Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkyRich(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener);

    }

    private void init(final Application application, final DonkyListener donkyListener) {

        if (!initialised.get()) {

            try {

                RichUIController.getInstance().init(application.getApplicationContext());

                DonkyRichLogic.initialiseDonkyRich(application, new DonkyListener() {

                    @Override
                    public void success() {

                        DonkyMessagingUI.initialiseDonkyMessaging(application, new DonkyListener() {

                                    @Override
                                    public void success() {

                                        DonkyCore.registerModule(new ModuleDefinition(DonkyRichUI.class.getSimpleName(), version));

                                        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<ApplicationStartEvent>(ApplicationStartEvent.class) {

                                            @Override
                                            public void onDonkyEvent(ApplicationStartEvent event) {

                                                new EventHandler(application.getApplicationContext()).handleApplicationStartEvent(event);

                                            }

                                        });

                                        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<RichMessageEvent>(RichMessageEvent.class) {

                                            @Override
                                            public void onDonkyEvent(RichMessageEvent event) {

                                                new EventHandler(application.getApplicationContext()).handleRichMessageEvent(event);

                                            }

                                        });

                                        initialised.set(true);

                                        if (donkyListener != null) {
                                            donkyListener.success();
                                        }

                                    }

                                    @Override
                                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                                    }
                                }
                        );

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        if (donkyListener != null) {
                            donkyListener.error(donkyException, null);
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
