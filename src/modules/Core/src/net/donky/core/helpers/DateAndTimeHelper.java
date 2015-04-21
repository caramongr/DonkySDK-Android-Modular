package net.donky.core.helpers;

import net.donky.core.logging.DLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper for Date and Time formats.
 *
 * Created by Marcin Swierczek
 * 24/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DateAndTimeHelper {

    /**
     * Convert date string to Date object.
     * @param dateStr String with date in expected format.
     * @return The Date representing provided String.
     */
    public static Date parseUtcDate(String dateStr) {

        if (dateStr != null) {

            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                DLog log = new DLog("DateAndTimeHelper");
                log.error("Error parsing date " + dateStr, e);
            }

            return date;

        } else {

            return null;

        }
    }

    /**
     * String representation of current local time in format expected by Donky Network.
     *
     * @return String representation of current local time in format expected by Donky Network.
     */
    public static String getCurrentLocalTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US);
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * String representation of current UTC time in format expected by Donky Network.
     *
     * @return String representation of current UTC time in format expected by Donky Network.
     */
    public static String getCurrentUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * String representation of UTC time in format expected by Donky Network.
     *
     * @return String representation of UTC time in format expected by Donky Network.
     */
    public static String getUTCTimeFormated(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(time);
    }
}
