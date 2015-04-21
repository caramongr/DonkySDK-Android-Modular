package net.donky.core.network;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is handling network connection issues.
 *
 * Created by Marcin Swierczek
 * 25/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class OnConnectionListener {

    private static final ConcurrentHashMap<String, OnConnectionListener> pendingTasks = new ConcurrentHashMap<>();

    protected static final Object sharedLock = new Object();

    private String key;

    /**
     * Callback to be invoked when internet connection has been restored.
     */
    protected abstract void onConnected();

    /**
     * Add this listener as network pending task without any restrictions of number of similar tasks.
     */
    protected void startUniqueListener() {

        synchronized (sharedLock) {
            key = this.getClass().getSimpleName();
            pendingTasks.put(key, this);
            sharedLock.notifyAll();
        }
    }

    /**
     *  Remove this listener from the list of connectivity change observers.
     */
    protected void stopUniqueListener() {

        synchronized (sharedLock) {
            pendingTasks.remove(this.getClass().getSimpleName());
            sharedLock.notifyAll();
        }

    }

    /**
     * @return True if there is internet connection available.
     */
    protected boolean isConnectionAvailable() {

        return DonkyNetworkController.getInstance().isInternetConnectionAvailable();
    }

    /**
     * All connection listeners will be notified about restored internet connection. This method is for Donky Core internal use.
     */
    public static void notifyAllConnectionListeners() {

        synchronized (sharedLock) {
            for (String key : pendingTasks.keySet()) {
                pendingTasks.get(key).onConnected();
            }
            sharedLock.notifyAll();
        }
    }
}
