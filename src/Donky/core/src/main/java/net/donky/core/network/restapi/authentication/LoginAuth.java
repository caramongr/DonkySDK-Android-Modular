package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.AuthenticationChallengeDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.AuthenticationDetail;
import net.donky.core.network.JsonWebTokenAuthenticationDetail;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to authenticate the account. This call will require a valid Auth token from auth provider to succeed.
 *
 * Created by Marcin Swierczek
 * 11/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LoginAuth extends Login {

    @SerializedName("authenticationDetail")
    protected JsonWebTokenAuthenticationDetail authenticationDetail;

    public LoginAuth(AuthenticationChallengeDetails details) {
        super();
        authenticationDetail = new JsonWebTokenAuthenticationDetail(details.getCorrelationId(), AuthenticationDetail.JSON_WEB_TOKEN_PROVIDER, details.getToken());
    }

    @Override
    protected LoginResponse doSynchronousCall(String apiKey) {
        loadAuthDetails();
        return RestClient.getAuthAPI().login(apiKey, this);
    }

    @Override
    protected void doAsynchronousCall(String apiKey, final NetworkResultListener<LoginResponse> listener) {
        loadAuthDetails();
        RestClient.getAuthAPI().login(apiKey, this, new Callback<LoginResponse>() {

            @Override
            public void success(LoginResponse loginResponse, retrofit.client.Response response) {
                listener.success(loginResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                listener.onFailure(error);
            }
        });
    }

    private void loadAuthDetails() {
        deviceSecret = DonkyDataController.getInstance().getDeviceDAO().getDeviceSecret();
        networkId = DonkyDataController.getInstance().getUserDAO().getUserNetworkId();
    }

    @Override
    protected void doStartListenForConnectionRestored() {

        if (DonkyAccountController.getInstance().isRegistered()) {
            startUniqueListener();
        }

    }

    @Override
    public void onConnected() {

        synchronized (sharedLock) {
            stopUniqueListener();
            sharedLock.notifyAll();
        }

        DonkyAccountController.getInstance().authenticate(new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Logged in after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when logging in after connection restored.");

            }

        });

    }

    @Override
    public String toString() {

        String divider = " | ";

        StringBuilder sb = new StringBuilder();
        sb.append("AUTH DETAILS: ");
        sb.append(divider);
        sb.append("token = ");
        sb.append(authenticationDetail.getToken());
        sb.append(divider);
        sb.append("correlationId = ");
        sb.append(authenticationDetail.getAuthenticationId());
        sb.append(divider);

        return sb.toString();
    }

}
