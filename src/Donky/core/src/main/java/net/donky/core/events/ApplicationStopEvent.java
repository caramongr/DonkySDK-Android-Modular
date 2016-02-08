package net.donky.core.events;

/**
 * Created by Marcin Swierczek
 * 08/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ApplicationStopEvent extends LocalEvent {

    private final long startTime;
    private final long stopTime;
    private final boolean appOpenedFromNotificationBanner;

    public ApplicationStopEvent(long startTime, long stopTime, boolean appOpenedFromNotificationBanner) {
        super();

        this.startTime = startTime;
        this.stopTime = stopTime;
        this.appOpenedFromNotificationBanner = appOpenedFromNotificationBanner;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public boolean isAppOpenedFromNotificationBanner() {
        return appOpenedFromNotificationBanner;
    }
}
