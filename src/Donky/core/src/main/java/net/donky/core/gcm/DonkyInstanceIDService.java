package net.donky.core.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import net.donky.core.model.DonkyDataController;

/**
 * Created by Marcin Swierczek
 * 16/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyInstanceIDService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        //Delete current GCM token from local Donky configuration.
        DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);
        //Fetch updated Instance ID token.
        Intent intent = new Intent(this, DonkyGCMRegistrationService.class);
        startService(intent);
    }

}