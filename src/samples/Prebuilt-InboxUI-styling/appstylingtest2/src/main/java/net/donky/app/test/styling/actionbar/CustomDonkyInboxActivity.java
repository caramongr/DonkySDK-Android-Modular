package net.donky.app.test.styling.actionbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.donky.core.messaging.rich.inbox.ui.components.RichInboxAndMessageFragment;
import net.donky.core.messaging.rich.inbox.ui.components.RichMessageForInboxActivityNoToolbar;

/**
 * Custom Activity to display RichInboxAndMessageFragment with a theme that uses ActionBar instead of Toolbar.
 *
 * Created by Marcin Swierczek
 * 16/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class CustomDonkyInboxActivity extends AppCompatActivity {

    private final static String TAG = "RichInboxAndMessageFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rich_inbox);

        if (savedInstanceState == null) {
            RichInboxAndMessageFragment richInboxAndMessageFragment = new RichInboxAndMessageFragment();
            //For an application theme with action bar we need to set Rich Message Activity class that has no Toolbar to avoid conflicts.
            //For themes with no action bar we can use default setting.
            richInboxAndMessageFragment.setRichMessageActivityClass(RichMessageForInboxActivityNoToolbar.class);
            richInboxAndMessageFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rich_inbox_fragment_container, richInboxAndMessageFragment, TAG).commit();
        } else {
            RichInboxAndMessageFragment richInboxAndMessageFragment = (RichInboxAndMessageFragment) getSupportFragmentManager().findFragmentByTag(TAG);
            //For an application theme with action bar we need to set Rich Message Activity class that has no Toolbar to avoid conflicts.
            //For themes with no action bar we can use default setting.
            richInboxAndMessageFragment.setRichMessageActivityClass(RichMessageForInboxActivityNoToolbar.class);
        }
    }

}
