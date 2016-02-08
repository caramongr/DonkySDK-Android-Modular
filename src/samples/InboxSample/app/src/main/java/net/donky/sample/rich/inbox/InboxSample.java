package net.donky.sample.rich.inbox;

import android.app.Application;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.messaging.rich.logic.DonkyRichLogic;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 28/01/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class InboxSample extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DonkyRichLogic.initialiseDonkyRich(this,
                new DonkyListener() {

                    @Override
                    public void success() {

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    }
                });

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
