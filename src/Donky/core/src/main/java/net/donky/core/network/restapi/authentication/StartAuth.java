package net.donky.core.network.restapi.authentication;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by Marcin Swierczek
 * 17/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class StartAuth extends GenericAuthenticationServiceRequest<StartAuthResponse> {

    @Override
    protected StartAuthResponse doSynchronousCall(String apiKey) {
        return RestClient.getAuthAPI().startAuth(apiKey);
    }

    @Override
    protected void doAsynchronousCall(String apiKey, final NetworkResultListener<StartAuthResponse> listener) {

        RestClient.getAuthAPI().startAuth(apiKey, new Callback<StartAuthResponse>() {

            @Override
            public void success(StartAuthResponse loginResponse, retrofit.client.Response response) {
                listener.success(loginResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                listener.onFailure(error);
            }
        });
    }

    @Override
    protected void doStartListenForConnectionRestored() {

//        if (DonkyAccountController.getInstance().isRegistered()) {
//            startUniqueListener();
//        }

    }

    @Override
    public void onConnected() {
//        synchronized (sharedLock) {
//            stopUniqueListener();
//            sharedLock.notifyAll();
//        }
    }

    @Override
    public String toString() {
        return "StartAuth";
    }

}
