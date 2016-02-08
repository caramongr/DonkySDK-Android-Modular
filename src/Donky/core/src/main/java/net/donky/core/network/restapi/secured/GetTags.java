package net.donky.core.network.restapi.secured;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.TagDescription;
import net.donky.core.network.restapi.RestClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GetTags extends GenericSecuredServiceRequest<List<TagDescription>> {

    NetworkResultListener<List<TagDescription>> listener;

    @Override
    protected void doStartListenForConnectionRestored() {
        startUniqueListener();
    }

    @Override
    protected List<TagDescription> doSynchronousCall(String apiKey) throws RetrofitError {
        return RestClient.getAPI().getTags(apiKey);
    }

    @Override
    protected void doAsynchronousCall(final String authorisation, final NetworkResultListener<List<TagDescription>> listener) {

        RestClient.getAPI().getTags(authorisation, new Callback<List<TagDescription>>() {

            @Override
            public void success(List<TagDescription> tags, retrofit.client.Response response) {
                listener.success(tags);
            }

            @Override
            public void failure(RetrofitError error) {

                listener.onFailure(error);

                GetTags.this.listener = listener;
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
