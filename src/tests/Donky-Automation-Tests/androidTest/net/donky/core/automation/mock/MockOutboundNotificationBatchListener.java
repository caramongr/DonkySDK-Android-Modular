package net.donky.core.automation.mock;

import net.donky.core.NotificationBatchListener;
import net.donky.core.OutboundNotification;

import java.util.List;

/**
 * Created by Marcin Swierczek
 * 01/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockOutboundNotificationBatchListener implements NotificationBatchListener<OutboundNotification> {

    List<OutboundNotification> notifications;

    public MockOutboundNotificationBatchListener() {

    }

    @Override
    public void onNotification(OutboundNotification notification) {

        this.notifications = null;

        synchronized (this) {
            notifyAll();
        }

    }

    public List<OutboundNotification> getNotifications() {
        return notifications;
    }

    @Override
    public void onNotification(List<OutboundNotification> notifications) {

        this.notifications = notifications;

        synchronized (this) {
            notifyAll();
        }

    }
}
