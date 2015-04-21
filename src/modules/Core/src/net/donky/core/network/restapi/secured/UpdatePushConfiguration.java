package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to update push configuration.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdatePushConfiguration extends GenericSecuredServiceRequest<Void> {

    private static final String TYPE = "Gcm";

    @SerializedName("type")
    private final String type;

    @SerializedName("registrationId")
    private final String registrationId;

    public UpdatePushConfiguration(String registrationId) {
        super();
        this.registrationId = registrationId;
        this.type = TYPE;
    }

    /**
     * @return Type of push chanel. Should be Gcm.
     */
    public String getType() {
        return type;
    }

    /**
     * @return GCM registration id.
     */
    public String getRegistrationId() {
        return registrationId;
    }

    @Override
    protected Void doSynchronousCall(String apiKey) {
        return RestClient.getAPI().updatePush(apiKey, this);
    }

    @Override
    protected void doAsynchronousCall(String authorization, final NetworkResultListener<Void> listener) {

        RestClient.getAPI().updatePush(authorization, this, new Callback<Void>() {

            @Override
            public void success(Void loginResponse, retrofit.client.Response response) {
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

        UpdatePushConfiguration updatePushConfiguration = new UpdatePushConfiguration(registrationId);

        DonkyNetworkController.getInstance().updatePushConfigurationOnNetwork(updatePushConfiguration, new DonkyListener() {
            @Override
            public void success() {

                new DLog("onConnected").info("Push configuration updated after connection restored.");



            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when updating push configuration after connection restored.");

            }
        });
    }
}
