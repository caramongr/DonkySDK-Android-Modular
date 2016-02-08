package net.donky.core.network.restapi.secured;

import net.donky.core.network.location.GeoFence;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by Igor Bykov
 * 02/11/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GetAllGeoFence extends GenericSecuredServiceRequest<List<GeoFence>> {

    NetworkResultListener<List<GeoFence>> listener;

    @Override
    protected void doStartListenForConnectionRestored() {
        startUniqueListener();
    }

    @Override
    protected List<GeoFence> doSynchronousCall(String apiKey) throws RetrofitError {
        return RestClient.getAPI().getAllGeoFences(apiKey);
    }

    @Override
    protected void doAsynchronousCall(final String authorisation, final NetworkResultListener<List<GeoFence>> listener) {

        RestClient.getAPI().getAllGeoFences(authorisation, new Callback<List<GeoFence>>() {

            @Override
            public void success(List<GeoFence> tags, retrofit.client.Response response) {
                listener.success(tags);
            }

            @Override
            public void failure(RetrofitError error) {

                listener.onFailure(error);

                GetAllGeoFence.this.listener = listener;
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
