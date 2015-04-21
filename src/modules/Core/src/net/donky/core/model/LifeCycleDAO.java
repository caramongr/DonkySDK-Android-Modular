package net.donky.core.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LifeCycleDAO extends SharedPreferencesBaseDAO {

    /**
     * File name for device details storage.
     */
    private static final String SHARED_PREFERENCES_FILENAME_ANALYTICS = "DonkyPreferencesAnalytics";

    private static final String KEY_IS_STARTED_FROM_NOTIFICATION = "isFromNotification";

    private static final String DONKY_START_TIME = "startTime";

    private static final String DONKY_STOP_TIME = "stopTime";

    Context context;

    public LifeCycleDAO(Context context) {
        super(context, SHARED_PREFERENCES_FILENAME_ANALYTICS);
        this.context = context;

    }

    /**
     * Set global flag to determine if app was opened from notification.
     */
    public boolean setIsAppOpenedFromNotificationBanner(boolean isFromNotification) {

        return setBoolean(KEY_IS_STARTED_FROM_NOTIFICATION, isFromNotification);

    }

    /**
     * Get global flag to determine if app was opened from notification.
     */
    public boolean isAppOpenedFromNotificationBanner() {

        return getBoolean(KEY_IS_STARTED_FROM_NOTIFICATION, false);

    }

    public boolean saveAppstartTimestamp(long startTime) {

        return setLong(DONKY_START_TIME, startTime);

    }

    public long getAppStartTimestamp() {

        return getLong(DONKY_START_TIME, 0);

    }

    public boolean resetTimestamps() {

        SharedPreferences settings = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME_ANALYTICS, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        editor.putLong(DONKY_START_TIME, 0);

        editor.putLong(DONKY_STOP_TIME, 0);

        return editor.commit();
    }
}
