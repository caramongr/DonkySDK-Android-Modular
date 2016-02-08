package net.donky.core.messaging.rich.inbox.ui.components;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import net.donky.core.messaging.rich.inbox.ui.R;
import net.donky.core.messaging.ui.components.DonkyActivity;

/**
 * Activity containing Donky Rich Messaging Inbox fragment and rich message fragment in split view mode.
 *
 * Created by Marcin Swierczek
 * 07/06/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichInboxAndMessageActivityNoToolbar extends DonkyActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dk_activity_rich_inbox_no_toolbar);

        if (savedInstanceState == null) {
            RichInboxAndMessageFragment richInboxAndMessageFragment = new RichInboxAndMessageFragment();
            richInboxAndMessageFragment.setRichMessageActivityClass(RichMessageForInboxActivityNoToolbar.class);
            richInboxAndMessageFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.dk_rich_inbox_fragment_container, richInboxAndMessageFragment).commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}