package net.donky.core.network.restapi.authentication;


import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyCore;
import net.donky.core.account.AuthenticationChallengeDetails;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.network.AuthenticationDetail;
import net.donky.core.network.JsonWebTokenAuthenticationDetail;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to register the account. This call will require a valid Auth token from auth provider to succeed.
 *
 * Created by Marcin Swierczek
 * 10/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RegisterAuth extends Register {

    @SerializedName("authenticationDetail")
    protected JsonWebTokenAuthenticationDetail authenticationDetail;

    public RegisterAuth(final String apiKey,  final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, boolean overrideRegistration, final AuthenticationChallengeDetails details) {
        super(apiKey, userDetails, deviceDetails, appVersion, overrideRegistration);
        authenticationDetail = new JsonWebTokenAuthenticationDetail(details.getCorrelationId(), AuthenticationDetail.JSON_WEB_TOKEN_PROVIDER, details.getToken());
    }

    @Override
    protected RegisterResponse doSynchronousCall(String apiKey) {
        return RestClient.getAuthAPI().register(apiKey, this);
    }

    @Override
    protected void doAsynchronousCall(String apiKey, final NetworkResultListener<RegisterResponse> listener) {

        RestClient.getAuthAPI().register(apiKey, this, new Callback<RegisterResponse>() {

            @Override
            public void success(RegisterResponse loginResponse, retrofit.client.Response response) {
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
        startUniqueListener();
    }

    @Override
    public void onConnected() {

        synchronized (sharedLock) {
            stopUniqueListener();
            sharedLock.notifyAll();
        }

        DonkyCore.getInstance().processInBackground(new Runnable() {
            @Override
            public void run() {
                DonkyAccountController.getInstance().registerAuthenticated(userDetails, deviceDetails, appVersion, null);
            }
        });
    }

    @Override
    public String toString() {

        String divider = "\n";

        StringBuilder sb = new StringBuilder();
        sb.append("AUTH DETAILS: ");
        sb.append(divider);
        sb.append("token = ");
        sb.append(authenticationDetail.getToken());
        sb.append(divider);
        sb.append("correlationId = ");
        sb.append(authenticationDetail.getAuthenticationId());
        sb.append(divider);

        return super.toString()+" "+sb.toString();
    }
}
