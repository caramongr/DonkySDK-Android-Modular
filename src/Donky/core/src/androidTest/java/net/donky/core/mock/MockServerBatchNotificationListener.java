package net.donky.core.mock;

import net.donky.core.NotificationBatchListener;
import net.donky.core.network.ServerNotification;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcin Swierczek
 * 01/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockServerBatchNotificationListener implements NotificationBatchListener<ServerNotification> {

    List<ServerNotification> notification;

    public MockServerBatchNotificationListener() {
        this.notification = new LinkedList<>();
    }

    @Override
    public void onNotification(ServerNotification notification) {

        if (notification != null) {
            this.notification.add(notification);
        }

        synchronized (this) {
            notifyAll();
        }

    }

    public List<ServerNotification> getNotifications() {
        return notification;
    }

    @Override
    public void onNotification(List<ServerNotification> notification) {
        this.notification = notification;

        synchronized (this) {
            notifyAll();
        }
    }
}
