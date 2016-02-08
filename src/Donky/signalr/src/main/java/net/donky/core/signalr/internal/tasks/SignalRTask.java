package net.donky.core.signalr.internal.tasks;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.signalr.internal.HubConnectionFactory;

import java.util.Map;

/**
 * Current task performed by SignalR Module.
 *
 * Created by Marcin Swierczek
 * 23/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class SignalRTask<T> {

    DonkyResultListener<T> resultListener;

    DonkyListener connectionCallbackListener;

    DonkyResultListener<T> invokeNetworkHubResultListener;

    protected final static Object sharedLock = new Object();

    protected final HubConnectionFactory hubConnectionFactory;

    public SignalRTask(final HubConnectionFactory hubConnectionFactory, final DonkyResultListener<T> resultListener) {

        this.resultListener = resultListener;
        this.hubConnectionFactory = hubConnectionFactory;

        this.connectionCallbackListener = new DonkyListener() {

            @Override
            public void success() {}

            @Override
            public void error(final DonkyException donkyException, final Map<String, String> validationErrors) {

                if (resultListener != null) {
                    resultListener.error(donkyException, validationErrors);
                }
            }
        };

        this.invokeNetworkHubResultListener = new DonkyResultListener<T>() {

            @Override
            public void success(final T result) {

                if (resultListener != null) {
                    resultListener.success(result);
                }
            }

            @Override
            public void error(final DonkyException donkyException, final Map<String, String> validationErrors) {

                if (resultListener != null) {
                    resultListener.error(donkyException, validationErrors);
                }
            }
        };
    }

    public void notifyConnectionError(final DonkyException donkyException, final Map<String, String> validationErrors) {
        synchronized (sharedLock) {
            connectionCallbackListener.error(donkyException, validationErrors);
            sharedLock.notifyAll();
        }
    }

    public void notifyTaskFailed(final DonkyException donkyException, final Map<String, String> validationErrors) {
        synchronized (sharedLock) {
            invokeNetworkHubResultListener.error(donkyException, validationErrors);
            sharedLock.notifyAll();
        }
    }

    public void notifyTaskSuccessful(T result) {
        synchronized (sharedLock) {
            invokeNetworkHubResultListener.success(result);
            sharedLock.notifyAll();
        }
    }

    public abstract void performTask();

}
