package net.donky.core.mock;

import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.LocalEvent;

/**
 * Created by Marcin Swierczek
 * 28/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockDonkyEventListener<T extends LocalEvent> extends DonkyEventListener<T> {

    T event;

    public MockDonkyEventListener(Class<T> type) {
        super(type);
    }

    @Override
    public void onDonkyEvent(T event) {

        this.event = event;

        synchronized (this) {
            notifyAll(  );
        }
    }

    public T getEvent() {
        return event;
    }
}
