package net.donky.core.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Wakeful Broadcast Receiver for GCM messages.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Deprecated
public class DonkyGcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        /**
         * The old GCM APIs have been deprecated. They are delivered to {@link DonkyGcmIntentService} directly.
         */

//        new DLog("GcmBroadcastReceiver").info("GCM message delivered.");
//
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//
//        // Explicitly specify that GcmIntentService will handle the intent.
//        ComponentName comp = new ComponentName(context.getPackageName(),
//                DonkyGcmIntentService.class.getName());
//
//        // Start the service, keeping the device awake while it is launching.
//        startWakefulService(context, (intent.setComponent(comp)));
//        setResultCode(Activity.RESULT_OK);
    }
}