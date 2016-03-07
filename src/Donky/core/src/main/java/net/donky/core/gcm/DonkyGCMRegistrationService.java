package net.donky.core.gcm;

import android.app.IntentService;
import android.content.Intent;

import net.donky.core.DonkyException;
import net.donky.core.logging.DLog;

/**
 * Intent service to perform GCM registration.
 *
 * Created by Marcin Swierczek
 * 16/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGCMRegistrationService extends IntentService {

    public DonkyGCMRegistrationService() {
        super("DonkyGCMRegistrationService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        try {
            DonkyGcmController.getInstance().registerPush();
        } catch (DonkyException e) {
            new DLog("DonkyGCMRegistrationService").error("Error registering GCM push configuration.");
        }
    }

}