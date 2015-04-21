package net.donky.core.network;

import retrofit.RetrofitError;

/**
 * Listener used for internal network callbacks.
 *
 * Created by Marcin Swierczek
 * 13/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class NetworkResultListener<T> {

    /**
     * Callback to be invoked when operation was successful.
     *
     * @param result Result of successful operation.
     */
    public abstract void success(T result);

    /**
     * Callback to be invoked when operation was successful.
     *
     * @param cause Error from Retrofit library
     */
    public abstract void onFailure(RetrofitError cause);

}
