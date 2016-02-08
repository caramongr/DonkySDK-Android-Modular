package net.donky.core.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import net.donky.core.DonkyCore;
import net.donky.core.events.ApplicationStartEvent;
import net.donky.core.events.ApplicationStopEvent;
import net.donky.core.model.LifeCycleDAO;
import net.donky.core.network.DonkyNetworkController;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Observer of Activity lifecycle. Its puprpose its to inform other donky module about application started/stopped events.
 *
 * Created by Marcin Swierczek
 * 08/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LifeCycleObserver {

    /**
     * Key for intent Extras to keep information about origin of an intent.
     */
    public static final String EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION = "isOpenedFromNotification";

    /**
     * When activities was paused but not resumed during MAX_ACTIVITY_TRANSITION_TIME_MS we treat that as closing app event.
     */
    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 5000;

    /**
     * Data Base Object to store data related to application lifecycle.
     */
    private LifeCycleDAO lifeCycleDAO;

    /**
     * Timer for handling app closed/open events
     */
    private Timer activityTransitionTimer;

    /**
     * TimerTask for handling app closed/open events
     */
    private TimerTask activityTransitionTimerTask;

    /**
     * Flag to determine if app was in background (MAX_ACTIVITY_TRANSITION_TIME_MS passed between onPasue and onResume for any Activity in app.
     */
    public boolean wasInBackground = true;

    /**
     * Last Intent opening any Activity in the application.
     */
    private Intent currentIntent = null;

    /**
     * Shared lock to synchronise threads.
     */
    private Object sharedLock = new Object();

    private AtomicBoolean isApplicationForegrounded;

    /**
     *
     */
    private SyncTimerHelper syncTimerHelper;

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private LifeCycleObserver() {
        isApplicationForegrounded = new AtomicBoolean(false);
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final LifeCycleObserver INSTANCE = new LifeCycleObserver();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static LifeCycleObserver getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void createApplicationLifecycleCallbacksListener() {

        activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                handleOnCreate(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                handleOnResume();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                handleOnPause();
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };
    }

    private void handleOnCreate(Activity activity) {

        Bundle extras;

        synchronized (sharedLock) {
            currentIntent = activity.getIntent();
            extras = currentIntent.getExtras();
            sharedLock.notifyAll();
        }

        if (extras != null) {

            if (extras.containsKey(EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION)) {

                boolean isStartedFromNotification = extras.getBoolean(EXTRA_KEY_IS_APP_OPENED_FROM_NOTIFICATION);

                if (isStartedFromNotification && !isApplicationForegrounded()) {

                    LifeCycleObserver.getInstance().setIsAppOpenedFromNotificationBanner(true);

                } else {

                    LifeCycleObserver.getInstance().setIsAppOpenedFromNotificationBanner(false);

                }

            } else {

                LifeCycleObserver.getInstance().setIsAppOpenedFromNotificationBanner(false);

            }

        }

    }

    private void handleOnPause() {
        LifeCycleObserver.getInstance().startActivityTransitionTimer();
    }

    private void handleOnResume() {

        if (LifeCycleObserver.getInstance().wasInBackground()) {

            DonkyNetworkController.getInstance().synchronise();

            syncTimerHelper.startSynchronisationTimer();

            final long startTime = System.currentTimeMillis();

            lifeCycleDAO.saveAppstartTimestamp(startTime);

            isApplicationForegrounded.set(true);

            DonkyCore.publishLocalEvent(new ApplicationStartEvent(currentIntent,  startTime, isAppOpenedFromNotificationBanner()));

        }

        LifeCycleObserver.getInstance().stopActivityTransitionTimer();

    }

    public void init(final Application application) {

        lifeCycleDAO = new LifeCycleDAO(application.getApplicationContext());

        syncTimerHelper  = new SyncTimerHelper(application);

        createApplicationLifecycleCallbacksListener();

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

    }

    public boolean wasInBackground() {
        return wasInBackground;
    }

    /**
     * Set true if app was opened from notification centre.
     *
     * @param isFromNotification True if app was opened from notification centre.
     */
    void setIsAppOpenedFromNotificationBanner(boolean isFromNotification) {

        lifeCycleDAO.setIsAppOpenedFromNotificationBanner(isFromNotification);

    }

    /**
     * True if app was opened from notification centre.
     *
     * @return True if app was opened from notification.
     */
    public boolean isAppOpenedFromNotificationBanner() {

        return lifeCycleDAO.isAppOpenedFromNotificationBanner();

    }

    /**
     * Start timer to measure time between onPasue and onResume to determine if app was backgrounded/closed
     */
    void startActivityTransitionTimer() {

        activityTransitionTimer = new Timer();

        activityTransitionTimerTask = new TimerTask() {

            public void run() {

                wasInBackground = true;

                syncTimerHelper.stopSynchronisationTimer();

                final long stopTime = System.currentTimeMillis();

                final long startTime = lifeCycleDAO.getAppStartTimestamp();

                lifeCycleDAO.resetTimestamps();

                isApplicationForegrounded.set(false);
                DonkyCore.publishLocalEvent(new ApplicationStopEvent(startTime, stopTime, isAppOpenedFromNotificationBanner()));

                setIsAppOpenedFromNotificationBanner(false);

            }
        };

        this.activityTransitionTimer.schedule(activityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);

    }

    /**
     * Stop timer measuring time between onPasue and onResume to determine if app was backgrounded/closed
     */
    void stopActivityTransitionTimer() {

        if (activityTransitionTimerTask != null) {

            activityTransitionTimerTask.cancel();

        }

        wasInBackground = false;

    }

    /**
     * Setup the maximum time in minutes between notification synchronisations when app is opened.
     *
     * @param maxMinutesWithoutNotificationExchangeSetting The setting string for maximum time in minutes between notification synchronisations when app is opened.
     */
    public void setMaxMinutesWithoutNotificationExchange(String maxMinutesWithoutNotificationExchangeSetting) {

        Integer delay = Integer.parseInt(maxMinutesWithoutNotificationExchangeSetting);

        if (delay != null) {
            syncTimerHelper.setDelayInMilliseconds(delay * 60 * 1000);
        }

    }

    /**
     * Return the current global state of Application.
     *
     * @return True if application is in the foreground.
     */
    public boolean isApplicationForegrounded() {
        return isApplicationForegrounded.get();
    }
}
