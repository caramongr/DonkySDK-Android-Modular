package net.donky.core;

/**
 * Listener for notification being sent or received. The generic type is the type of notification that the listener will be registered to receive.
 * @deprecated Please use NotificationBatchListener class instead for better performance
 *
 * Created by Marcin Swierczek
 * 02/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Deprecated
public interface NotificationListener<T extends Notification> {

    /**
     * Callback to be invoked when notification of a given type has been sent or received.
     * @deprecated please use NotificationBatchListener class instead
     * @param notification Notification of expected type was received or send.
     */
    @Deprecated
    public void onNotification(T notification);
}
