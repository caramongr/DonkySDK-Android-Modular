package net.donky.core.messaging.push.logic.events;

import net.donky.core.events.LocalEvent;
import net.donky.core.messaging.push.logic.SimplePushData;

/**
 * Created by Marcin Swierczek
 * 10/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SimplePushMessageEvent extends LocalEvent {

    private SimplePushData simplePushData;

    private boolean receivedExpired;

    public SimplePushMessageEvent(SimplePushData simplePushData, boolean receivedExpired) {
        super();
        this.simplePushData = simplePushData;
        this.receivedExpired = receivedExpired;
    }

    public SimplePushData getSimplePushData() {
        return simplePushData;
    }

    public boolean isReceivedExpired() {
        return receivedExpired;
    }
}
