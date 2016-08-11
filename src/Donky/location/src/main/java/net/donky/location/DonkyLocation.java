package net.donky.location;

import android.app.Application;
import android.content.Context;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationBatchListener;
import net.donky.core.Subscription;
import net.donky.core.events.ApplicationStartEvent;
import net.donky.core.events.ApplicationStopEvent;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.model.AbstractLastLocation;
import net.donky.core.network.ServerNotification;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Marcin Swierczek
 * 19/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyLocation {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.0";

    public static final String TAG = DonkyLocation.class.getSimpleName();

    private Context context;

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyLocation() {

    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyLocation INSTANCE = new DonkyLocation();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyLocation getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Automation Module.
     *
     * @param application Application instance
     * @param donkyListener The callback to invoke when the Module is initialised.
     */
    public static void initialiseDonkyLocation(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application.getApplicationContext(), donkyListener);

    }

    private void init(Context applicationContext, final DonkyListener donkyListener) {

        context = applicationContext;

        if (!initialised.get()) {

            try {

                DonkyLocationController.getInstance().init(context);

                DonkyCore.getInstance().registerService(AbstractLastLocation.SERVICE_CATEGORY_LOCATION, DonkyLocationController.getInstance());

                DonkyCore.registerModule(new ModuleDefinition(DonkyLocation.class.getSimpleName(), version));

                initialised.set(true);

                if (donkyListener != null) {
                    donkyListener.success();
                }

                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<ApplicationStopEvent>(ApplicationStopEvent.class) {

                    @Override
                    public void onDonkyEvent(ApplicationStopEvent event) {
                        DonkyLocationController.getInstance().stopAutomaticLocationUpdatesTimer();
                        DonkyLocationController.getInstance().appStopped();
                    }

                });

                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<ApplicationStartEvent>(ApplicationStartEvent.class) {

                    @Override
                    public void onDonkyEvent(ApplicationStartEvent event) {
                        DonkyLocationController.getInstance().startAutomaticLocationUpdatesTimer();
                        DonkyLocationController.getInstance().sendLocationUpdate(null);
                    }

                });

                List<Subscription<ServerNotification>> subscriptions = new LinkedList<>();

                subscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_LOCATION_REQUEST,
                        new NotificationBatchListener<ServerNotification>() {

                            @Override
                            public void onNotification(ServerNotification notification) {
                            }

                            @Override
                            public void onNotification(List<ServerNotification> notifications) {
                                new NotificationHandler().handleLocationRequest(notifications);
                            }

                        }));

                subscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_USER_LOCATION,
                        new NotificationBatchListener<ServerNotification>() {

                            @Override
                            public void onNotification(ServerNotification notification) {
                            }

                            @Override
                            public void onNotification(List<ServerNotification> notifications) {
                                new NotificationHandler().handleUserLocation(notifications);
                            }

                        }));

                DonkyCore.subscribeToDonkyNotifications(
                        new ModuleDefinition(DonkyLocation.class.getSimpleName(), version),
                        subscriptions,
                        true);

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising DonkyLocation Module");
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

    public Context getContext() {
        return context;
    }

}
