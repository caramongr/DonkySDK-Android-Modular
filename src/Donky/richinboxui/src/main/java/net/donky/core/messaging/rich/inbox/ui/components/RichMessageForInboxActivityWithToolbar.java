package net.donky.core.messaging.rich.inbox.ui.components;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.inbox.ui.R;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.ui.components.DonkyActivity;
import net.donky.core.messaging.ui.components.generic.GenericSplitFragment;

/**
 * Activity displaying Rich Message for Rich Messages Inbox. In contrast to {@link RichMessageForInboxActivityNoToolbar} class this one will work with themes that have Toolbar instead of ActionBar.
 *
 * Created by Marcin Swierczek
 * 21/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageForInboxActivityWithToolbar extends DonkyActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        super.onCreate(savedInstanceState);

        Boolean isDisplayModeMixed = getIntent().getExtras().getBoolean(GenericSplitFragment.KEY_DISPLAY_MODE_MIXED);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isDisplayModeMixed) {
            finish();
            return;
        }

        setContentView(R.layout.dk_rich_message_activity);

        RichMessageFragment richMessageDetailFragment;

        richMessageDetailFragment = (RichMessageFragment) getSupportFragmentManager().findFragmentById(R.id.rich_message_fragment);

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE)) {
            new DLog("RichMessageActivity").error("Intent must contain the rich message ID.");
            finish();
            return;
        }

        RichMessage richMessage = (RichMessage) extras.get(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE);

        if (richMessage == null) {
            new DLog("RichMessageActivity").error("Intent must contain the rich message.");
            finish();
            return;
        }

        richMessageDetailFragment.setRichMessage(richMessage);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitle(richMessage.getSenderDisplayName());
            setupUI(toolbar);
            setSupportActionBar(toolbar);
        }
    }
}
