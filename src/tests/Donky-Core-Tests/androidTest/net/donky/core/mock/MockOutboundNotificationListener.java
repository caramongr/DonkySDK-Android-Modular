package net.donky.core.mock;

import net.donky.core.NotificationListener;
import net.donky.core.OutboundNotification;

/**
 * Created by Marcin Swierczek
 * 01/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockOutboundNotificationListener implements NotificationListener<OutboundNotification> {

    OutboundNotification notification;

    public MockOutboundNotificationListener() {

    }

    @Override
    public void onNotification(OutboundNotification notification) {


        this.notification = notification;


        synchronized (this) {
            notifyAll();
        }

    }

    public OutboundNotification getNotification() {
        return notification;
    }
}
