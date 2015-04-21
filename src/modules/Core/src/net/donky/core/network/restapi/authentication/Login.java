package net.donky.core.network.restapi.authentication;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DeviceDetails;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;
import net.donky.core.settings.AppSettings;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to authenticate the account.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Login extends GenericAuthenticationServiceRequest<LoginResponse> {

    @SerializedName("networkId")
    private final String networkId;

    @SerializedName("deviceSecret")
    private final String deviceSecret;

    @SerializedName("operatingSystem")
    private final String operatingSystem;

    @SerializedName("sdkVersion")
    private final String sdkVersion;

    public Login() {

        this.deviceSecret = DonkyDataController.getInstance().getDeviceDAO().getDeviceSecret();
        this.operatingSystem = DeviceDetails.getOSName();
        this.sdkVersion = AppSettings.getVersion();
        this.networkId = DonkyDataController.getInstance().getUserDAO().getUserNetworkId();
    }

    @Override
    protected LoginResponse doSynchronousCall(String apiKey) {
        return RestClient.getAuthAPI().login(apiKey, this);
    }

    @Override
    protected void doAsynchronousCall(String apiKey, final NetworkResultListener<LoginResponse> listener) {

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

        return "LOGIN: " + " networkId: " + networkId + divider + " deviceSecret : " + deviceSecret + divider + " operatingSystem : " + operatingSystem + divider + " sdkVersion : " + sdkVersion;
    }
}
