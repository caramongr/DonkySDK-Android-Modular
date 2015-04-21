package net.donky;

import android.app.Application;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationListener;
import net.donky.core.Subscription;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.analytics.DonkyAnalytics;
import net.donky.core.automation.DonkyAutomation;
import net.donky.core.messaging.push.ui.DonkyPushUI;
import net.donky.core.messaging.rich.ui.DonkyRichUI;
import net.donky.core.network.ServerNotification;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        NotificationProcessor.getInstance().init(this.getApplicationContext());

        // Initialise Donky Analytics Module
        DonkyAnalytics.initialiseAnalytics(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG,"Analytics initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG,"Analytics error", donkyException);
            }

        });

        // Initialise Donky Automation Module
        DonkyAutomation.initialiseDonkyAutomation(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG,"Automation initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG,"Automation error", donkyException);
            }
        });

        // Initialise Donky Simple Push UI Module
        DonkyPushUI.initialiseDonkyPush(this, new DonkyListener() {
            @Override
            public void success() {
                Log.i(TAG,"Simple Push initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG,"Simple Push error", donkyException);
            }
        });

        // Initialise Donky Rich UI Module
        DonkyRichUI.initialiseDonkyRich(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG,"Rich Messaging initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG,"Rich Messaging error", donkyException);
            }

        });

        initialiseDonkyCoreModule();
    }

    private void initialiseDonkyCoreModule() {

        // Subscribe to receive changeColour Content Notifications
        List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();
        serverNotificationSubscriptions.add(new Subscription<>("changeColour",
                new NotificationListener<ServerNotification>() {

                    @Override
                    public void onNotification(ServerNotification notification) {
                        NotificationProcessor.getInstance().processServerNotification(notification);
                    }

                }));

        DonkyCore.subscribeToContentNotifications(
                new ModuleDefinition("Color Demo", "2.0.0.0"),
                serverNotificationSubscriptions);

        // Add some tags
        LinkedHashSet<String> selectedTags = new LinkedHashSet<>();
        selectedTags.add("one");
        selectedTags.add("two");
        selectedTags.add("three");

        // Additional properties associated with the account
        TreeMap<String, String> additionalProperties = new TreeMap<>();
        additionalProperties.put("one","1");
        additionalProperties.put("two","2");
        additionalProperties.put("three","3");
        additionalProperties.put("four","4");

        // User details
        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId("john-smith").
                setUserFirstName("John").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John").
                setUserAdditionalProperties(additionalProperties).setSelectedTags(selectedTags);

        // Device description
        DeviceDetails deviceDetails = new DeviceDetails("John's phone", "Smartphone", null);

        // Put your Donky API key here
        String apiKey ="vMBC8SHsILtV1g+UVnozZ0QmMKM4mcpNbNLfwUQnKq8P2z1XPMhhuHThwszJorUv32epCXMSjq3kwq0KM35w";

        // Initialise Donky Core SDK
        DonkyCore.initialiseDonkySDK(this, apiKey, userDetails, deviceDetails, "v1", new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG,"Core initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG,"Core error", donkyException);
            }
        });

    }

}