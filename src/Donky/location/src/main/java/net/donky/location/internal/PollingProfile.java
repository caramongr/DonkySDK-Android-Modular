package net.donky.location.internal;

/**
 * Created by Marcin Swierczek
 * 19/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class PollingProfile {

    public static final long defaultInterval = 300000L;
    public static final long defaultFastestInterval = 300000L;
    public static final int defaultPriority = 100;
    public static final int defaultSmallestDisplacement = 50;

    private long interval;
    private long fastestInterval;
    private int priority;
    private int smallestDisplacement;

    public PollingProfile(long interval, long fastestInterval, int priority, int smallestDisplacement) {
        this.interval = interval;
        this.fastestInterval = fastestInterval;
        this.priority = priority;
        this.smallestDisplacement = smallestDisplacement;
    }

    public PollingProfile() {
        this.interval = defaultInterval;
        this.fastestInterval = defaultFastestInterval;
        this.priority = defaultPriority;
        this.smallestDisplacement = defaultSmallestDisplacement;
    }

    public long getInterval() {
        return interval;
    }

    public long getFastestInterval() {
        return fastestInterval;
    }

    public int getPriority() {
        return priority;
    }

    public int getSmallestDisplacement() {
        return smallestDisplacement;
    }
}
