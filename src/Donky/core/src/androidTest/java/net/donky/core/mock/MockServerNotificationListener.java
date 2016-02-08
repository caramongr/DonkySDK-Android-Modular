package net.donky.core.mock;

import net.donky.core.NotificationListener;
import net.donky.core.network.ServerNotification;

/**
 * Created by Marcin Swierczek
 * 29/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockServerNotificationListener implements NotificationListener<ServerNotification> {

    ServerNotification notification;

    @Override
    public void onNotification(ServerNotification notification) {
        this.notification = notification;

        synchronized (this) {
            notifyAll(  );
        }
    }

    public ServerNotification getNotification() {
        return notification;
    }
}
