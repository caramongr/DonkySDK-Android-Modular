package net.donky.core.messaging.rich.ui.components;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import net.donky.core.messaging.rich.ui.RichUIController;
import net.donky.core.messaging.ui.components.rich.RichMessageActivity;
import net.donky.core.model.DonkyDataController;

/**
 * RichMessage popup Activity.
 *
 * Created by Marcin Swierczek
 * 15/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessagePopUpActivity extends RichMessageActivity {

    public static final String ACTIVITY_IN_FOREGROUND = "ACTIVITY_IN_FOREGROUND";

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTIVITY_IN_FOREGROUND)) {
                this.setResultCode(Activity.RESULT_OK);
            }
        }

    };

    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();

        intentFilter.addAction(ACTIVITY_IN_FOREGROUND);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RichUIController.getInstance().checkActivityForegroundAndDisplayRichMessage();

    }

    @Override
    protected void onResume() {

        registerReceiver(intentReceiver, intentFilter);

        DonkyDataController.getInstance().getRichMessagesDAO().removeRichMessage(richMessageDetailFragment.getRichMessage());

        super.onResume();
    }

    @Override
    protected void onPause() {

        unregisterReceiver(intentReceiver);

        super.onPause();
    }

}
