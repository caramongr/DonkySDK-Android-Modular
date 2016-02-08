package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.logging.DLog;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to update registration details.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateRegistration extends GenericSecuredServiceRequest<Void> {

    @SerializedName("device")
    private final UpdateDevice device;

    @SerializedName("client")
    private final UpdateClient client;

    @SerializedName("user")
    private final UpdateUser user;

    private UserDetails userDetails;

    private DeviceDetails deviceDetails;

    public UpdateRegistration(UserDetails userDetails, DeviceDetails deviceDetails) {
        super();
        user = new UpdateUser(userDetails);
        device = new UpdateDevice(deviceDetails);
        client = new UpdateClient();

        this.userDetails = userDetails;

        this.deviceDetails = deviceDetails;

    }

    @Override
    public String toString() {

        String divider = "\n";

        StringBuilder sb = new StringBuilder();

        try {
            sb.append("REGISTER: ");
            sb.append(divider);
            sb.append(device.toString());
            sb.append(divider);
            sb.append(client.toString());
            if (user != null) {
                sb.append(divider);
                sb.append(user.toString());
            }
        } catch (Exception e) {
            sb.append("Error building log string");
        }
        return sb.toString();
    }

    @Override
    protected Void doSynchronousCall(String authorization) {
        return RestClient.getAPI().updateRegistration(authorization, this);
    }

    @Override
    protected void doAsynchronousCall(String authorization, final NetworkResultListener<Void> listener) {

        RestClient.getAPI().updateRegistration(authorization, this, new Callback<Void>() {

            @Override
            public void success(Void updateResponse, retrofit.client.Response response) {
                listener.success(null);
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

        DonkyAccountController.getInstance().updateRegistrationDetails(userDetails, deviceDetails, new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Registration details updated after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when updating registration details after connection restored.");

            }
        });
    }
}
