package net.donky.core;

import java.util.List;

/**
 * Listener for notification being sent or received. The generic type is the type of notification that the listener will be registered to receive.
 *
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface NotificationBatchListener<T extends Notification> extends NotificationListener<T> {

    /**
     * Callback to be invoked when notification of a given type has been sent or received.
     *
     * @param notification Notification of expected type was received or send.
     */
    public void onNotification(List<T> notification);
}
