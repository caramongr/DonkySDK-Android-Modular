package net.donky.core.analytics;

import net.donky.core.events.ApplicationStartEvent;
import net.donky.core.events.ApplicationStopEvent;
import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;

/**
 * Created by Marcin Swierczek
 * 06/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AnalyticsInternalController {

    /**
     * Trigger type for application start
     */
    enum Trigger {

        None(1),
        Notification(2);

        private int value;

        private Trigger(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Logging helper.
     */
    private DLog log;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    AnalyticsInternalController() {

        log = new DLog("AnalyticsController");

    }

    /**
     * Queue client notification to tell network that application was stopped.
     */
    void notifyAppStopped(ApplicationStopEvent applicationStopEvent) {

        if (applicationStopEvent.getStartTime() == 0) {

            log.warning("Incorrect start time error.");

            return;
        }

        Trigger trigger = Trigger.None;

        if (applicationStopEvent.isAppOpenedFromNotificationBanner()) {

            trigger = Trigger.Notification;

        }

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createAppStopNotification(applicationStopEvent.getStartTime(), applicationStopEvent.getStartTime(), trigger));

        log.info("A stopped notification queued.");
    }

    /**
     * Queue client notification to tell network that application was started.
     */
    void notifyAppStarted(ApplicationStartEvent applicationStartEvent) {

        Trigger trigger = Trigger.None;

        if (applicationStartEvent.isOpenedFromNotificationBanner()) {

            trigger = Trigger.Notification;

        }

        DonkyNetworkController.getInstance().queueClientNotification(ClientNotification.createAppStartNotification(applicationStartEvent.getStartTime(), trigger));

        log.info("A started notification queued.");

    }
}
