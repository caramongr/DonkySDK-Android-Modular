package net.donky.core.messaging.rich.inbox.ui.components;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import net.donky.core.messaging.rich.inbox.ui.R;
import net.donky.core.messaging.ui.components.DonkyActivity;

/**
 * Activity containing Donky Rich Messaging Inbox fragment and rich message fragment in split view mode.
 *
 * Created by Marcin Swierczek
 * 07/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichInboxAndMessageActivityWithToolbar extends DonkyActivity {

    protected Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dk_activity_rich_inbox_with_toolbar);

        if (savedInstanceState == null) {
            RichInboxAndMessageFragment richInboxAndMessageFragment = new RichInboxAndMessageFragment();
            richInboxAndMessageFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.dk_rich_inbox_fragment_container, richInboxAndMessageFragment).commit();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setupUI(toolbar);
            toolbar.setTitle(R.string.dk_inbox_fragment_title);
        }

    }

}