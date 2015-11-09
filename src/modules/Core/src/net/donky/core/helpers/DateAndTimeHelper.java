package net.donky.core.helpers;

import android.text.TextUtils;

import net.donky.core.logging.DLog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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

    /**
     * Check if message expired or passed its availability period.
     *
     * @param sentTime Date when message was sent.
     * @param expiryTime Expiry date.
     * @param currentTime Current time in milliseconds.
     * @param availabilityDays Number of days any message should be available.
     * @return True if message can be seen.
     */
    public static boolean isExpired(Date sentTime, Date expiryTime, Date currentTime, Integer availabilityDays) {

        if (isExceededMaxAvailabilityDays(sentTime, currentTime, availabilityDays)) {
            return true;
        }

        if (currentTime != null && expiryTime != null) {
            if (currentTime.after(expiryTime)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if message exceeded max availability days.
     *
     * @param sentTime Date when message was sent.
     * @param currentTime Current time in milliseconds.
     * @param availabilityDays Number of days any message should be available.
     * @return True if message can be seen.
     */
    public static boolean isExceededMaxAvailabilityDays(Date sentTime, Date currentTime, Integer availabilityDays) {

        if (sentTime != null && currentTime != null && availabilityDays != null) {
            if (currentTime.getTime() - sentTime.getTime() > TimeUnit.DAYS.toMillis(availabilityDays)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return a timestamp for a json date() string value.
     *
     * @param val The date() value.
     * @return The timestamp.
     */
    public static Long parseJsonTimestamp(String val) {

        if (TextUtils.isEmpty(val))
            return null;

        int start = val.indexOf("(") + 1;
        if (start == -1)
            return null;
        int end = val.indexOf(")", start);
        if (end == -1)
            return null;

        String timestamp = val.substring(start, end);

        int zonePos = timestamp.indexOf("+");
        if (zonePos == -1)
            zonePos = timestamp.indexOf("-");
        if (zonePos > -1)
            timestamp = timestamp.substring(0, zonePos);

        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    public static long getUTCFromGMT(long gmtTimeMilliseconds) {

        Calendar cal= Calendar.getInstance();
        int offset = cal.get(Calendar.ZONE_OFFSET)
                + cal.get(Calendar.DST_OFFSET);

        return gmtTimeMilliseconds - offset;
    }

    public static long parseUTCStringToUTCLong(String dateStr) {

        Date date = parseUtcDate(dateStr);
        if (date != null) {
            return getUTCFromGMT(date.getTime());
        }

        return 0;
    }
}
