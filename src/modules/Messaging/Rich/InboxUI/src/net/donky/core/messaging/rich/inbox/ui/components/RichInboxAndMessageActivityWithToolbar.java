package net.donky.core.messaging.rich.inbox.ui.components;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
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
        setupUI(toolbar);

    }

    /**
     * Setup toolbar widget.
     *
     * @param toolbar Toolbar widget to set.
     */
    protected void setupUI(Toolbar toolbar) {

        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[] { R.attr.dk_ic_arrow_back_24dp };
        TypedArray array = obtainStyledAttributes(typedValue.resourceId, attribute);
        int attributeResourceId = array.getResourceId(0, -1);

        Drawable drawable = getResources().getDrawable(attributeResourceId);
        array.recycle();

        toolbar.setNavigationIcon(drawable);

        setTitle(R.string.dk_inbox_fragment_title);

        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
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