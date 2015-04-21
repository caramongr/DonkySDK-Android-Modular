package net.donky.core.messaging.logic;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of Common Messaging Module.
 *
 * Created by Marcin Swierczek
 * 09/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyMessaging {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.0";

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
