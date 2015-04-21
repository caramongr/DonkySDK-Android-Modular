package net.donky.core.events;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.logging.DLog;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Access point for saved Donky local event subscriptions.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class EventObservable {

    private final CopyOnWriteArrayList<DonkyEventListener> observers;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private EventObservable() {
        this.observers = new CopyOnWriteArrayList<>();
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final EventObservable INSTANCE = new EventObservable();
    }

    /**
     * Static instance of Event Observable singleton.
     *
     * @return Static instance of Event Observable singleton.
     */
    private static EventObservable getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Register observer for local Donky events.
     *
     * @param observerToAdd Callback to invoke when event is created.
     */
    public static void registerObserver(DonkyEventListener observerToAdd) {
        getInstance().observers.add(observerToAdd);
    }

    /**
     * Remove observer for local Donky events.
     *
     * @param observerToRemove Callback to remove.
     */
    public static void removeObserver(DonkyEventListener observerToRemove) {
        getInstance().observers.remove(observerToRemove);
    }

    /**
     * Notify event observers registered for particular local Donky Event.
     *
     * @param event Local Donky event to be delivered.
     */
    @SuppressWarnings("unchecked")
    public static void notifyObserver(final LocalEvent event) {

        for (final DonkyEventListener observer : getInstance().observers) {
            if (observer.getEventType() != null && observer.getEventType().equals(event.getLocalEventType())) {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.onDonkyEvent(event);
                    }
                });

            }
        }
    }
}
