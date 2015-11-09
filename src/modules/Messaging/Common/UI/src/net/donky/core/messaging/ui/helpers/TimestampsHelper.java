package net.donky.core.messaging.ui.helpers;

import android.content.Context;

import net.donky.core.messaging.ui.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to generate timestamps for the UI
 * <p/>
 * Created by Marcin Swierczek
 * 09/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class TimestampsHelper {

    private static String justNow = null;
    private static String numberOfMinutes;
    private static String numberOfHoursSingular;
    private static String numberOfHoursPlural;
    private static String yesterday;
    private static String today;

    /**
     * Get formatted timestamp date for Rich Inbox UI
     *
     * @param timestampMillis   Delivery time in milliseconds
     * @param currentTimeMillis Current time in milliseconds
     * @return Timestamp description
     */
    public static String getShortTimestampForRichMessage(Context context, long timestampMillis, long currentTimeMillis) {
        return getShortTimestampForListView(context, timestampMillis, currentTimeMillis);
    }

    /**
     * Get formatted timestamp date for list views
     *
     * @param timestampMillis   Delivery time in milliseconds
     * @param currentTimeMillis Current time in milliseconds
     * @return Timestamp description
     */
    public static String getShortTimestampForListView(Context context, long timestampMillis, long currentTimeMillis) {

        if (justNow == null) {
            justNow = context.getResources().getString(R.string.dk_just_now);
            numberOfMinutes = context.getResources().getString(R.string.dk_number_of_minutes);
            numberOfHoursSingular = context.getResources().getString(R.string.dk_number_of_hours_singular);
            numberOfHoursPlural = context.getResources().getString(R.string.dk_number_of_hours_plural);
            yesterday = context.getResources().getString(R.string.dk_yesterday);
        }

        long diff = currentTimeMillis - timestampMillis;

        if (diff < TimeUnit.MINUTES.toMillis(5)) {

            return justNow;

        } else if (diff < TimeUnit.HOURS.toMillis(1)) {

            return "" + TimeUnit.MILLISECONDS.toMinutes(diff) + " " + numberOfMinutes;

        } else if (diff == TimeUnit.HOURS.toMillis(2)) {

            return "" + TimeUnit.MILLISECONDS.toHours(diff) + " " + numberOfHoursSingular;

        } else if (diff < TimeUnit.DAYS.toMillis(1)) {

            if (new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(timestampMillis)).equals(new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(currentTimeMillis)))) {
                return "" + TimeUnit.MILLISECONDS.toHours(diff) + " " + numberOfHoursPlural;
            } else {
                return yesterday;
            }

        } else if (diff < TimeUnit.DAYS.toMillis(7)) {

            return new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(timestampMillis));

        } else {

            return new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(new Date(timestampMillis));

        }

    }

    /**
     * Get formatted timestamp date for Chat UI
     *
     * @param timestampMillis   Delivery time in milliseconds
     * @return Timestamp description
     */
    public static String getShortTimestampForChatMessage(long timestampMillis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestampMillis));
    }

    /**
     * Create chat header timestamp
     *
     * @param context Application context
     * @param previousRowTime Timestamp for previous row
     * @param currentRowTime Timestamp for current row
     * @return Chat header timestamp
     */
    public static String getChatHeaderDate(Context context, long previousRowTime, long currentRowTime) {

        String currentRowDay = new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(currentRowTime));

        if ( new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(previousRowTime)).equals(currentRowDay)) {
            return null;
        }

        long currentTime = System.currentTimeMillis();

        long diff = currentTime - currentRowTime;

        if (diff < TimeUnit.DAYS.toMillis(7) && new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(currentTime)).equals(currentRowDay)) {

            if (today == null) {
                today = context.getResources().getString(R.string.dk_today);
            }
            return today;
        }

        return new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date(currentRowTime)).toUpperCase();

    }
}
