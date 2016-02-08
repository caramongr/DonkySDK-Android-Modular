package com.example.donky.simple.push;

import android.app.Application;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.messaging.push.DonkyPush;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 30/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MyApplication extends Application {

    private static final String TAG = "DonkyPushTest";

    @Override
    public void onCreate() {
        super.onCreate();

        DonkyPush.initialiseDonkyPush(this, true,
                new DonkyListener() {

                    @Override
                    public void success() {

                    }

                    @Override
                    public void error(DonkyException e, Map<String, String> map) {

                    }
                });

        // Core Module initialisation needs to go last
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
}
