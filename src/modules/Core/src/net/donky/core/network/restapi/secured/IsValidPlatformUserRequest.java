package net.donky.core.network.restapi.secured;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Marcin Swierczek
 * 14/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class IsValidPlatformUserRequest extends GenericSecuredServiceRequest<IsValidPlatformUserResponse> {

    String externalUSerId;

    public IsValidPlatformUserRequest(String externalUSerId) {
        this.externalUSerId = externalUSerId;
    }

    @Override
    protected IsValidPlatformUserResponse doSynchronousCall(String authorization) {
        return RestClient.getAPI().isValidPlatformUser(authorization, externalUSerId);
    }

    @Override
    protected void doAsynchronousCall(final String authorization, final NetworkResultListener<IsValidPlatformUserResponse> listener) {
        RestClient.getAPI().isValidPlatformUser(authorization, externalUSerId, new Callback<IsValidPlatformUserResponse>() {
            @Override
            public void success(IsValidPlatformUserResponse isValidPlatformUserResponse, Response response) {
                listener.success(isValidPlatformUserResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                listener.onFailure(error);
            }
        });
    }

    @Override
    protected void doStartListenForConnectionRestored() {
    }

    @Override
    protected void onConnected() {
    }

    @Override
    public String toString() {

        String divider = "\n";

        StringBuilder sb = new StringBuilder();
        sb.append("IsValidPlatformUserRequest: ");
        sb.append(divider);
        sb.append(externalUSerId);

        return sb.toString();
    }
}
