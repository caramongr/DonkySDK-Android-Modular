package net.donky.core;

import net.donky.core.network.DonkyNetworkController;

import java.util.Map;

/**
 * Listener for asynchronous API calls with result to deliver with callback.
 *
 * Created by Marcin Swierczek
 * 05/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class DonkyResultListener<T> {

    /**
     * Operation performed successfully.
     *
     * @param result Result delivered with successful call.
     */
    public abstract void success(T result);

    /**
     * Operation failed.
     *
     * @param donkyException Exception describing the problem.
     * @param validationErrors Map of validation failures describing the problem.
     */
    public abstract void error(DonkyException donkyException, Map<String, String> validationErrors);

    /**
     * Callback invoked when user was suspended on the network.
     */
    public void userSuspended() {

        error(new DonkyException("user suspended"), null);

    }
}
