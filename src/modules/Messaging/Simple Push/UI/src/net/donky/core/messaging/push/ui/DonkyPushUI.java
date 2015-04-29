package net.donky.core.messaging.push.ui;

import android.app.Application;
import android.content.Context;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.messaging.push.logic.DonkyPushLogic;
import net.donky.core.messaging.push.logic.events.SimplePushMessageEvent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class for Donky Push UI class.
 * <p/>
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyPushUI {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.2";

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
    private DonkyPushUI() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyPushUI INSTANCE = new DonkyPushUI();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyPushUI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Push UI Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkyPush(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener);

    }

    private void init(final Application application, final DonkyListener donkyListener) {

        if (!initialised.get()) {

            this.context = application.getApplicationContext();

            this.simplePushUIConfiguration = new SimplePushUIConfiguration();

            try {

                DonkyPushLogic.initialiseDonkyPush(application, new DonkyListener() {

                    @Override
                    public void success() {

                        DonkyCore.registerModule(new ModuleDefinition(DonkyPushUI.class.getSimpleName(), version));

                        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<SimplePushMessageEvent>(SimplePushMessageEvent.class) {

                            @Override
                            public void onDonkyEvent(SimplePushMessageEvent event) {

                                new EventHandler().handleSimplePushEvent(context, event, simplePushUIConfiguration);

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
