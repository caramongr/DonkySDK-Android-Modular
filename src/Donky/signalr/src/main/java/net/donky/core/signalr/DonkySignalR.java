package net.donky.core.signalr;

import android.app.Application;
import android.content.Context;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.events.ApplicationStartEvent;
import net.donky.core.events.ApplicationStopEvent;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.network.signalr.SignalRController;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to be used for Module initialisation in onCreate method of Application class. This initialisation need to be performed before initialisation of Core module.
 *
 * Created by Marcin Swierczek
 * 11/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkySignalR {


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

    private Context context;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkySignalR() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkySignalR INSTANCE = new DonkySignalR();
    }

    /**
     * Get instance of DonkySignalR singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkySignalR getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise DonkySignalR Module. SignalR channel will be used only when the app is foregrounded. The REST APIs will be used when app is backgrounded or if SDK fails to connect through the socket.
     *
     * @param donkyListener The callback to invoke when the module is initialised.
     */
    public static void initialiseDonkySignalR(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener);

    }

    private void init(final Application application, final DonkyListener donkyListener) {

        this.context = application.getApplicationContext();

        if (!initialised.get()) {

            try {

                DonkySignalRController.getInstance().init(application);

                DonkyCore.registerModule(new ModuleDefinition(SignalRController.SERVICE_NAME, version));

                DonkyCore.getInstance().registerService(SignalRController.SERVICE_NAME, DonkySignalRController.getInstance());

                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<ApplicationStartEvent>(ApplicationStartEvent.class) {
                    @Override
                    public void onDonkyEvent(ApplicationStartEvent event) {
                        DonkySignalRController.getInstance().startSignalR();
                    }
                });

                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<ApplicationStopEvent>(ApplicationStopEvent.class) {
                    @Override
                    public void onDonkyEvent(ApplicationStopEvent event) {
                        DonkySignalRController.getInstance().stopSignalR();
                    }
                });

                initialised.set(true);

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising DonkySignalR");
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
     * Check if DonkySignalR is successfully initialised.
     *
     * @return True if DonkySignalR is successfully initialised.
     */
    public static boolean isInitialised() {
        return initialised.get();
    }
}
