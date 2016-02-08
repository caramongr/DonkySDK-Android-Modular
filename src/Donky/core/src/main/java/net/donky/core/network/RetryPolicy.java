package net.donky.core.network;

import android.text.TextUtils;

import net.donky.core.logging.DLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Retry policy class storing information used to retry network calls if they fail.
 *
 * Created by Marcin Swierczek
 * 09/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RetryPolicy {

    private static final String DEFAULT_DEVICE_CONNECTION_RETRY_SCHEDULE = "5,2|30,2|60,1|120,1|300,9|600,6|900,*";

    private static String DEVICE_CONNECTION_RETRY_SCHEDULE = "5,2|30,2|60,1|120,1|300,9|600,6|900,*";

    private long delayBeforeNextRetry;

    private int currentRetryDescriptionCount;

    private int retryCountForCurrentRetryDescription;

    private int retryMaxValueForCurrentRetryDescription;

    private List<RetryDescription> retryDescriptions;

    private boolean wasRetriedAlready = false;

    public RetryPolicy() {

        // Parse String describing retries to list of RetryDescription objects
        if (retryDescriptions == null) {
            try {
                retryDescriptions = parseRetries();
            } catch (Exception e) {
                retryDescriptions = null;
            }
        }

        currentRetryDescriptionCount = 0;
        retryCountForCurrentRetryDescription = 0;

        if (retryDescriptions != null && !retryDescriptions.isEmpty()) {

            retryMaxValueForCurrentRetryDescription = retryDescriptions.get(currentRetryDescriptionCount).attempts;
            delayBeforeNextRetry = retryDescriptions.get(currentRetryDescriptionCount).delayMS;

        } else {

            // If parsing was unsuccessful do not retry.
            retryMaxValueForCurrentRetryDescription = 0;
            delayBeforeNextRetry = Long.MAX_VALUE;

        }
    }

    /**
     * @return True if operation should be retried again.
     */
    public boolean retry() {

        wasRetriedAlready = true;

        retryCountForCurrentRetryDescription += 1;

        if (retryCountForCurrentRetryDescription > retryMaxValueForCurrentRetryDescription) {

            currentRetryDescriptionCount += 1;
            retryCountForCurrentRetryDescription = 0;

            if (retryDescriptions.size() > currentRetryDescriptionCount) {

                retryMaxValueForCurrentRetryDescription = retryDescriptions.get(currentRetryDescriptionCount).attempts;
                delayBeforeNextRetry = retryDescriptions.get(currentRetryDescriptionCount).delayMS;

            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * @return Return delay in milliseconds before next retry.
     */
    public long getDelayBeforeNextRetry() {

        return delayBeforeNextRetry;

    }

    /**
     * Check if for particular network response status code the call should be retried.
     *
     * @param status Network call status code.
     * @return True if operation should be retried again.
     */
    public boolean shouldRetryForStatusCode(int status) {
        return (status != 400 && status != 401 && status != 403 && status != 404);
    }

    /**
     * Set the retry strategy scheme.
     * @param schedule The retry scheme.
     */
    public static void setConnectionRetrySchedule(String schedule) {
        if (!TextUtils.isEmpty(schedule)) {
            DEVICE_CONNECTION_RETRY_SCHEDULE = schedule;
        }
    }

    /**
     * Parse the retry strategy scheme string.
     * @return List of Retry descriptions consisting of number of retries and delay between them.
     */
    private List<RetryDescription> parseRetries() {

        List<RetryDescription> retries;

        try {
            retries = parseRetries(DEVICE_CONNECTION_RETRY_SCHEDULE);
            if (retries.size() == 0) {
                return null;
            } else {
                return retries;
            }
        } catch (Exception e) {
            retries = parseRetries(DEFAULT_DEVICE_CONNECTION_RETRY_SCHEDULE);
            if (retries.size() == 0) {
                return null;
            } else {
                return retries;
            }
        }
    }

    /**
     * Parse the retry strategy scheme string.
     * @param retryScheme The retry scheme.
     * @return List of Retry descriptions consisting of number of retries and delay between them.
     */
    private List<RetryDescription> parseRetries(String retryScheme) {
        String[] schemeRetries = retryScheme.split("\\|");
        List<RetryDescription> retries = new ArrayList<>(schemeRetries.length);
        for (String schemeRetry : schemeRetries) {
            try {
                RetryDescription d = new RetryDescription();
                d.setRetryDescription(schemeRetry);
                retries.add(d);
            } catch (Exception e) {
                DLog log = new DLog("RetryScheme");
                log.error("Error parsing retry scheme "+retryScheme,e);
                return null;
            }
        }
        return retries;
    }

    /**
     * Description of Donky retry policies.
     */
    class RetryDescription {

        private int attempts;
        private long delayMS;

        private void setRetryDescription(String schemeRetry) throws Exception {
            if (TextUtils.isEmpty(schemeRetry)) {
                throw new Exception("schemeRetry cannot be empty");
            }
            String[] vals = schemeRetry.split(",");
            if (vals.length < 2 || vals[0] == null || vals[1] == null) {
                throw new Exception("schemeRetry must have 2 values");
            }
            float seconds;
            try {
                seconds = Float.parseFloat(vals[0]);
            } catch (NumberFormatException e) {
                throw new Exception("First schemeRetry value must be a float", e);
            }
            if ("*".equals(vals[1].trim())) {
                attempts = -1;
            } else {
                try {
                    attempts = Integer.parseInt(vals[1]);
                } catch (NumberFormatException e) {
                    throw new Exception("Second schemeRetry value must be an int", e);
                }
            }
            delayMS = (long) (seconds * 1000);
        }
    }

    public boolean isWasRetriedAlready() {
        return wasRetriedAlready;
    }
}
