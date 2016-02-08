package net.donky.sample.rich.push;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.messaging.rich.logic.DonkyRichLogic;
import net.donky.core.messaging.rich.logic.RichMessageEvent;

import java.util.Map;

/**
 * Application class for sample app. Need to be declared in Android Manifest file.
 *
 * Created by Marcin Swierczek
 * 29/01/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichPushSample extends Application {

    PushHandler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup the app to handle incoming Rich Messages
        handler  = new PushHandler();
        DonkyCore.subscribeToLocalEvent(new DonkyEventListener<RichMessageEvent>(RichMessageEvent.class) {

            @Override
            public void onDonkyEvent(RichMessageEvent event) {
                handler.handleRichMessageEvent(getApplicationContext(), event);
            }
        });

        // Initialise Donky Rich Messaging Module.
        DonkyRichLogic.initialiseDonkyRich(this,
                new DonkyListener() {

                    @Override
                    public void success() {

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    }
                });

        // Initialise Donky Core Messaging Module. This need to go after initialising Rich Messaging Module.
        DonkyCore.initialiseDonkySDK(this, "PUT_YOUR_API_KEY_HERE",
                new DonkyListener() {

                    @Override
                    public void success() {

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    }
                });

    }
}
