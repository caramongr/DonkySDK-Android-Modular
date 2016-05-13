package net.donky.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver for boot completed broadcasts to resume work after device restart.
 *
 * Created by Marcin Swierczek
 * 26/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyBootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Nothing more to do.
    }
}
