package net.donky.core.messaging.ui.components;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import net.donky.core.DonkyCore;
import net.donky.core.events.OnCreateEvent;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;

/**
 * Base Activity for Donky Messaging Module.
 *
 * Created by Marcin Swierczek
 * 15/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyActivity extends AppCompatActivity {

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

    @Override
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        for (Fragment frag : fm.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }
        super.onBackPressed();
    }
}
