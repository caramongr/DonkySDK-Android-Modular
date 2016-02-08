package net.donky.location.internal;

import android.content.Context;
import android.os.CountDownTimer;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.logging.DLog;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.location.DonkyLocationController;

import java.util.Map;

/**
 * Timer manager to schedule automatic location updates when application is foregrounded.
 *
 * Created by Marcin Swierczek
 * 08/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LocationUpdateTimer {

    private final int defaultInterval = 5 * 60 * 1000;

    /**
     * Application Context.
     */
    Context context;

    /**
     * Delay between synchronisation attempts.
     */
    long delayInMilliseconds;

    /**
     * Logging helper.
     */
    DLog log;

    CountDownTimer countDownTimer;

    private long MILLIS_IN_FUTURE = Long.MAX_VALUE;

    public LocationUpdateTimer(Context context) {
        log = new DLog("LocationUpdateTimer");
        this.context = context;
    }

    /**
     * Start automatic location updates send to Donky Network.
     */
    public void startTimer() {

        try {

            String interval = DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().get(ConfigurationDAO.KEY_CONFIGURATION_LocationUpdateIntervalSeconds);

            if (interval != null) {

                Integer delay = Integer.parseInt(interval);

                if (delay != null && delay > 0) {
                    delayInMilliseconds = delay * 1000;
                } else {
                    delayInMilliseconds = defaultInterval;
                }

            } else {
                delayInMilliseconds = defaultInterval;
            }

        } catch (Exception e) {
            delayInMilliseconds = defaultInterval;
            log.error("Error setting delay for location updates.", e);
        }

        if (countDownTimer == null) {

            countDownTimer = new CountDownTimer(MILLIS_IN_FUTURE, delayInMilliseconds) {

                @Override
                public void onTick(long millisUntilFinished) {
                    performTickAction();
                }

                @Override
                public void onFinish() {
                    log.debug("CountDownTimer finished.");
                }
            };

        }

        countDownTimer.start();
    }

    /**
     * Stop automatic location updates send to Donky Network.
     */
    public void stopTimer() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

    }

    /**
     * Sets the delay in milliseconds between
     * @param delayInMilliseconds
     */
    public void setDelayInMilliseconds(long delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    private void performTickAction() {

        DonkyLocationController.getInstance().sendLocationUpdate(new DonkyListener() {
            @Override
            public void success() {
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                log.warning("Couldn't send automatic location update.");
            }
        });

    }
}
