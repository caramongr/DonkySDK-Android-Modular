package net.donky.core;

import android.app.IntentService;
import android.content.Intent;

import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;

/**
 * Service to handle all Donky Core internally scheduled long operation requests.
 *
 * Created by Marcin Swierczek
 * 26/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyIntentService extends IntentService {

    private DLog log = new DLog("DonkyIntentService");

    public DonkyIntentService() {
        super("DonkyIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        if(DonkyBroadcastReceiver.ACTION_SYNCHRONISE_DONKY_SDK.equals(intent.getAction())) {

            try {

                DonkyNetworkController.getInstance().synchroniseSynchronously();

            } catch (Exception e) {

                if (DonkyCore.isInitialised()) {
                    log.error("Error synchronising with network triggered by the intent.", e);
                }

            }

            DonkyBroadcastReceiver.completeWakefulIntent(intent);

        }

    }
}
