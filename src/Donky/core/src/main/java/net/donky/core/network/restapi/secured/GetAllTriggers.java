package net.donky.core.network.restapi.secured;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.location.Trigger;
import net.donky.core.network.restapi.RestClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by Igor Bykov
 * 02/11/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GetAllTriggers extends GenericSecuredServiceRequest<List<Trigger>> {

    NetworkResultListener<List<Trigger>> listener;

    @Override
    protected void doStartListenForConnectionRestored() {
        startUniqueListener();
    }

    @Override
    protected List<Trigger> doSynchronousCall(String apiKey) throws RetrofitError {
        return RestClient.getAPI().getAllTriggers(apiKey);
    }

    @Override
    protected void doAsynchronousCall(final String authorisation, final NetworkResultListener<List<Trigger>> listener) {

        RestClient.getAPI().getAllTriggers(authorisation, new Callback<List<Trigger>>() {

            @Override
            public void success(List<Trigger> tags, retrofit.client.Response response) {
                listener.success(tags);
            }

            @Override
            public void failure(RetrofitError error) {

                listener.onFailure(error);

                GetAllTriggers.this.listener = listener;
            }

        });

    }

    @Override
    protected void onConnected() {

        synchronized (sharedLock) {
            stopUniqueListener();
            sharedLock.notifyAll();
        }

    }
}

