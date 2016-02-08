package net.donky.core.events;

/**
 * Listener for Donky Local Events. Can be subscribed to listen for events of type provided in template argument.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class DonkyEventListener<T extends LocalEvent> {

    private String type;

    public DonkyEventListener(Class<T> type) {
        this.type = type.getSimpleName();
    }

    /**
     * This callback is invoked when local event of defined type is delivered to observers.
     *
     * @param event Event to deliver.
     */
    public abstract void onDonkyEvent(T event);

    /**
     * Event for which this listener is registered.
     *
     * @return Event for which this listener is registered.
     */
    public String getEventType() {
        return type;
    }

}
