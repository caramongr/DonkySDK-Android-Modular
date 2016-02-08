package net.donky.core.lifecycle;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import net.donky.core.DonkyBroadcastReceiver;
import net.donky.core.logging.DLog;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;

/**
 * Created by Marcin Swierczek
 * 08/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SyncTimerHelper {

    /**
     * ID for Alarm that will start synchronisation
     */
    private static final int SYNCHRONISE_ALARM_ID = 934;

    /**
     * Synchronisation pending intent.
     */
    private PendingIntent pendingIntent;

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

    SyncTimerHelper(Context context) {

        log = new DLog("SyncTimerHelper");

        this.context = context;

        try {

            String maxMinutes = DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().get(ConfigurationDAO.KEY_CONFIGURATION_MaxMinutesWithoutNotificationExchange);

            Integer delay = null;

            try {

                delay = Integer.parseInt(maxMinutes);

            } catch (Exception e) {

                // Configuration not available yet

            }

            if (delay != null && delay > 0) {

                delayInMilliseconds = delay * 60 * 1000;

            } else {

                delayInMilliseconds = 5 * 60 * 1000;

            }

        } catch (Exception e) {

            log.error("Error setting delay between synchronisation attempts.", e);

        }
    }

    /**
     * Start timer to Synchronise notifications with the Donky Network.
     * This will setup the exact repeating timer which can drain the battery. This is to allow intervals smaller that 15 min.
     */
    public void startSynchronisationTimer() {

        try {

            Intent intent = new Intent(DonkyBroadcastReceiver.ACTION_SYNCHRONISE_DONKY_SDK);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (pendingIntent == null) {
                pendingIntent = PendingIntent.getBroadcast(context, SYNCHRONISE_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            long timeNow = System.currentTimeMillis();
            long lastSynchronisation = DonkyNetworkController.getInstance().getLastSynchronisationTimestamp();
            long nextSynchronisationDelay;

            if (timeNow - lastSynchronisation < delayInMilliseconds) {
                nextSynchronisationDelay = delayInMilliseconds - (timeNow - lastSynchronisation);
            } else {
                nextSynchronisationDelay = delayInMilliseconds;
            }

            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                    + nextSynchronisationDelay, delayInMilliseconds, pendingIntent);

        } catch (Exception e) {

            log.error("Error starting synchronisation timer.", e);

        }
    }

    /**
     * Stop timer to Synchronise notifications with the Donky Network.
     */
    public void stopSynchronisationTimer() {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public void setDelayInMilliseconds(long delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }
}
