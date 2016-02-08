package net.donky.core.events;

import android.content.Intent;

/**
 * Created by Marcin Swierczek
 * 08/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ApplicationStartEvent extends LocalEvent {

    Intent intent;

    boolean isOpenedFromNotificationBanner;

    long startTime;

    public ApplicationStartEvent(Intent intent,  long startTime, boolean isOpenedFromNotificationBanner) {
        super();
        this.intent = intent;
        this.startTime = startTime;
        this.isOpenedFromNotificationBanner = isOpenedFromNotificationBanner;
    }

    public Intent getIntent() {
        return intent;
    }

    public boolean isOpenedFromNotificationBanner() {
        return isOpenedFromNotificationBanner;
    }

    public long getStartTime() {
        return startTime;
    }

}
