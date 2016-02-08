package net.donky;

import android.app.Application;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationBatchListener;
import net.donky.core.Subscription;
import net.donky.core.analytics.DonkyAnalytics;
import net.donky.core.assets.DonkyAssets;
import net.donky.core.automation.DonkyAutomation;
import net.donky.core.messaging.push.DonkyPush;
import net.donky.core.messaging.rich.inbox.ui.DonkyRichInboxUI;
import net.donky.core.network.ServerNotification;
import net.donky.core.signalr.DonkySignalR;
import net.donky.location.DonkyLocation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Application class
 *
 * Created by Marcin Swierczek
 * 07/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Donky extends Application {

    private static final String TAG = "DonkyTestApp";

    @Override
    public void onCreate()
    {
        super.onCreate();

        subscribeForCustomNotifications();

        /* Initialise Donky Modules before Core */

        // Initialise Assets Module
        DonkyAssets.initialiseDonkyAssets(this, new DonkyListener() {
            @Override
            public void success() {
                Log.i(TAG, "Donky Assets initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Assets init error", donkyException);
            }
        });

        // Initialise DonkySignalR Module
        DonkySignalR.initialiseDonkySignalR(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky SignalR initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky SignalR error", donkyException);
            }

        });

        // Initialise Donky Analytics Module
        DonkyAnalytics.initialiseAnalytics(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Analytics initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Analytics error", donkyException);
            }

        });

        // Initialise Donky Automation Module
        DonkyAutomation.initialiseDonkyAutomation(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Automation initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Automation error", donkyException);
            }
        });

        // Initialise Donky Simple Push Module
        DonkyPush.initialiseDonkyPush(this, true, new DonkyListener() {
            @Override
            public void success() {
                Log.i(TAG, "Donky Push initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Push error", donkyException);
            }
        });

        // Initialise Donky Rich UI Module
        DonkyRichInboxUI.initialiseDonkyRich(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Rich Messaging initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Rich Messaging error", donkyException);
            }

        });

        // Initialise Donky location Module
        DonkyLocation.initialiseDonkyLocation(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Location initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Location error", donkyException);
            }

        });

        // Initialise Donky Core SDK
        DonkyCore.initialiseDonkySDK(this, "PUT_YOUR_API_KEY_HERE", new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Core initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Core error", donkyException);
            }

        });

    }

    /**
     * This is for Custom Content Notification test. Subscribing to Content notification of type
     */
    private void subscribeForCustomNotifications() {

        NotificationProcessor.getInstance().init(this.getApplicationContext());

        // Subscribe to receive changeColour Content Notifications
        List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();
        serverNotificationSubscriptions.add(new Subscription<>("changeColour",
                new NotificationBatchListener<ServerNotification>() {

                    @Override
                    public void onNotification(ServerNotification notification) {

                    }

                    @Override
                    public void onNotification(List<ServerNotification> notifications) {
                        for (ServerNotification notification : notifications) {
                            NotificationProcessor.getInstance().processServerNotification(notification);
                        }
                    }

                }));

        DonkyCore.subscribeToContentNotifications(
                new ModuleDefinition("Color Demo", "2.0.0.0"),
                serverNotificationSubscriptions);

    }
}