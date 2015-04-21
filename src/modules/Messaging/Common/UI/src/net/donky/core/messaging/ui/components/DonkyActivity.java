package net.donky.core.messaging.ui.components;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import net.donky.core.DonkyCore;
import net.donky.core.events.OnCreateEvent;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;
import net.donky.core.messaging.ui.UIConfigurationSettings;

/**
 * Base Activity for Donky Messaging Module.
 *
 * Created by Marcin Swierczek
 * 15/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DonkyCore.publishLocalEvent(new OnCreateEvent(getIntent()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        DonkyCore.publishLocalEvent(new OnResumeEvent());
    }

    @Override
    protected void onPause() {
        super.onPause();

        DonkyCore.publishLocalEvent(new OnPauseEvent());
    }

    protected void setupUI(Toolbar toolbar) {

        int backgroundColor = UIConfigurationSettings.getInstance().getToolbarBackgroundColor();

        if (backgroundColor != 0) {
            toolbar.setBackgroundColor(backgroundColor);
        }
    }
}
