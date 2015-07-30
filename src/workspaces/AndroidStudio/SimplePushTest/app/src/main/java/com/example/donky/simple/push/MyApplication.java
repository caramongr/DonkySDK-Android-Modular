package com.example.donky.simple.push;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.UserDetails;
import net.donky.core.messaging.push.ui.DonkyPushUI;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 30/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DonkyPushUI.initialiseDonkyPush(this,
                new DonkyListener() {

                    @Override
                    public void success() {

                    }

                    @Override
                    public void error(DonkyException e, Map<String, String> map) {

                    }
                });

        // Core Module initialisation needs to go last
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("John253").setUserDisplayName("John");

        DonkyCore.initialiseDonkySDK(this,
                ">>ENTER API KEY HERE<<",
                userDetails,
                null,
                "1.0.0.0",
                new DonkyListener() {

                    @Override
                    public void success() {

                    }

                    @Override
                    public void error(DonkyException e, Map<String, String> map) {

                    }
                });
    }
}
