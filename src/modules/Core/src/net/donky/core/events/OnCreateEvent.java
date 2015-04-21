package net.donky.core.events;

import android.content.Intent;

import net.donky.core.events.LocalEvent;

/**
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class OnCreateEvent extends LocalEvent {

    Intent intent;

    /**
     * Base class for all local Donky events. Parent class should call this constructor with it's own Class object.
     *
     */
    public OnCreateEvent(Intent intent) {
        super();
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }
}
