package net.donky.core.mock;

import net.donky.core.NotificationBatchListener;
import net.donky.core.OutboundNotification;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcin Swierczek
 * 01/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockOutboundBatchNotificationListener implements NotificationBatchListener<OutboundNotification> {

    List<OutboundNotification> notification;

    public MockOutboundBatchNotificationListener() {

    }

    @Override
    public void onNotification(OutboundNotification notification) {

        if (notification != null) {
            this.notification = new LinkedList<>();
            this.notification.add(notification);
        }

        synchronized (this) {
            notifyAll();
        }

    }

    public List<OutboundNotification> getNotifications() {
        return notification;
    }

    @Override
    public void onNotification(List<OutboundNotification> notification) {
        this.notification = notification;

        synchronized (this) {
            notifyAll();
        }
    }
}
