package net.donky.core.messaging.ui.helpers;

import android.content.Context;

import net.donky.core.messaging.ui.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to generate timestamps for the UI
 *
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

    /**
     * Get formated timestamp date for UI
     *
     * @param timestampMillis Delivery time in milliseconds
     * @param currentTimeMillis Current time in milliseconds
     * @return Timestamp description
     */
    public static String getShortTimestamp(Context context, long timestampMillis, long currentTimeMillis) {

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

        } else if ( diff < TimeUnit.HOURS.toMillis(1)) {

            return ""+TimeUnit.MILLISECONDS.toMinutes(diff)+" "+numberOfMinutes;

        }  else if ( diff == TimeUnit.HOURS.toMillis(2)) {

            return ""+TimeUnit.MILLISECONDS.toHours(diff)+" "+numberOfHoursSingular;

        } else if ( diff < TimeUnit.DAYS.toMillis(1)) {

            if ( new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(timestampMillis)).equals(new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(currentTimeMillis)))) {
                return ""+TimeUnit.MILLISECONDS.toHours(diff)+" "+numberOfHoursPlural;
            } else {
                return yesterday;
            }

        } else if ( diff < TimeUnit.DAYS.toMillis(7)) {

            return new SimpleDateFormat("EE", Locale.getDefault()).format(new Date(timestampMillis));

        } else {

            return new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(new Date(timestampMillis));

        }

    }

}
