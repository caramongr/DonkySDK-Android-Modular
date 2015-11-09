package net.donky.app.test.styling.actionbar;

import android.app.Application;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.analytics.DonkyAnalytics;
import net.donky.core.messaging.rich.inbox.ui.DonkyRichInboxUI;
import net.donky.core.messaging.rich.inbox.ui.PushConfiguration;
import net.donky.core.messaging.rich.inbox.ui.components.RichMessageActivityNoToolbar;

import java.util.Map;

/**
 * Application class
 *
 * Created by Marcin Swierczek
 * 07/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Donky extends Application {

    private static final String TAG = "DonkyTestApp2";

    @Override
    public void onCreate()
    {
        super.onCreate();

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

        // Initialise Donky Rich UI Module
        DonkyRichInboxUI.initialiseDonkyRich(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Rich Messaging initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Rich Messaging error", donkyException);
            }

        }, new PushConfiguration(CustomDonkyInboxActivity.class, RichMessageActivityNoToolbar.class));

        initialiseDonkyCoreModule();
    }

    private void initialiseDonkyCoreModule() {

        // User details
        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId("john-smith-test-2").
                setUserFirstName("John").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John");

        // Device description
        DeviceDetails deviceDetails = new DeviceDetails("John's phone", "Smartphone", null);

        // Put your Donky API key here
        String apiKey =">>PUT_YOUR_API_KEY_HERE<<";

        // Initialise Donky Core SDK
        DonkyCore.initialiseDonkySDK(this, apiKey, userDetails, deviceDetails, "v2", new DonkyListener() {

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