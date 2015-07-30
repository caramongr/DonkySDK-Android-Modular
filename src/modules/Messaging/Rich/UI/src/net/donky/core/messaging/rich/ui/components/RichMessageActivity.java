package net.donky.core.messaging.rich.ui.components;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.ui.components.DonkyActivity;

import net.donky.core.messaging.rich.ui.R;

/**
 * Created by Marcin Swierczek
 * 15/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageActivity extends DonkyActivity {

    protected RichMessageFragmentForPopup richMessageDetailFragment;

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.dk_rich_message_activity_for_popup);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);

        richMessageDetailFragment = (RichMessageFragmentForPopup) getSupportFragmentManager().findFragmentById(R.id.rich_message_fragment_for_popup);

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE)) {

            new DLog("RichMessageActivity").error("Intent must contain the rich message ID.");

            finish();

        }

        RichMessage richMessage = (RichMessage) extras.get(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE);

        if (richMessage == null) {

            new DLog("RichMessageActivity").error("Intent must contain the rich message.");

            finish();

        }

        richMessageDetailFragment.setRichMessage(richMessage);

        toolbar.setTitle(richMessage.getDescription());
    }

}