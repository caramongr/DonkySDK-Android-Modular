package net.donky.core;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Broadcast receiver for internal Donky Intent broadcasts.
 * <p/>
 * Created by Marcin Swierczek
 * 26/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyBroadcastReceiver extends WakefulBroadcastReceiver {

    /**
     * Intent action type to trigger notifications synchronisation
     */
    public static final String ACTION_SYNCHRONISE_DONKY_SDK = "net.donky.core.ACTION_SYNCHRONISE";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_SYNCHRONISE_DONKY_SDK.equals(intent.getAction())) {

            Intent service = new Intent(context, DonkyIntentService.class);

            service.setAction(intent.getAction());

            startWakefulService(context, service);

        }
    }
}
