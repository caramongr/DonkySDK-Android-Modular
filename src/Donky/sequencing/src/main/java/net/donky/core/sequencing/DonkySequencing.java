package net.donky.core.sequencing;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to initialise the sequencing module. When using this module to do
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkySequencing {


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
    private DonkySequencing() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkySequencing INSTANCE = new DonkySequencing();
    }

    /**
     * Get instance of DonkySequencing singleton.
     *
     * @return Static instance of DonkySequencing singleton.
     */
    public static DonkySequencing getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise DonkySequencing Module.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkySequencing(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener);

    }

    private void init(final Application application, final DonkyListener donkyListener) {

        if (!initialised.get()) {

            try {

                DonkyCore.registerModule(new ModuleDefinition(DonkySequencing.class.getSimpleName(), version));

                DonkySequenceAccountController.getInstance().init();

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising DonkySequencing Module");
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
     * Check if DonkySequencing is successfully initialised.
     *
     * @return True if DonkySequencing is successfully initialised.
     */
    public static boolean isInitialised() {
        return initialised.get();
    }
}
