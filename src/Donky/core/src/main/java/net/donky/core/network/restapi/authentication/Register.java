package net.donky.core.network.restapi.authentication;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.logging.DLog;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;
import net.donky.core.network.restapi.secured.UpdateClient;
import net.donky.core.network.restapi.secured.UpdateDevice;
import net.donky.core.network.restapi.secured.UpdateUser;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to register the account.
 * <p/>
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Register extends GenericAuthenticationServiceRequest<RegisterResponse> {

    @SerializedName("device")
    private final UpdateDevice device;

    @SerializedName("client")
    private final UpdateClient client;

    @SerializedName("user")
    private UpdateUser user;

    protected UserDetails userDetails;

    protected DeviceDetails deviceDetails;

    protected String appVersion;

    protected String apiKey;

    protected boolean overrideRegistration;

    public Register(final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, boolean overrideRegistration) {
        device = new UpdateDevice(deviceDetails);
        client = new UpdateClient(appVersion);
        if (userDetails != null && !TextUtils.isEmpty(userDetails.getUserId())) {
            user = new UpdateUser(userDetails);
        }

        this.userDetails = userDetails;

        this.deviceDetails = deviceDetails;

        this.appVersion = appVersion;

        this.apiKey = apiKey;

        this.overrideRegistration = overrideRegistration;

    }

    public void replaceDeviceSecret(String secret) {
        device.replaceDeviceSecret(secret);
    }

    @Override
    public String toString() {

        String divider = "\n";

        StringBuilder sb = new StringBuilder();
        sb.append("REGISTER: ");
        sb.append(divider);
        sb.append(device.toString());
        sb.append(divider);
        sb.append(client.toString());
        if (user != null) {
            sb.append(divider);
            sb.append(user.toString());
        }

        return sb.toString();
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

        new Thread() {

            @Override
            public void run() {

                try {

                    DonkyAccountController.getInstance().register(apiKey, userDetails, deviceDetails, appVersion, overrideRegistration);

                } catch (DonkyException e) {

                    new DLog("onConnected").error("Error registering account after connection was restored.");

                }
            }
        }.start();

    }
}
