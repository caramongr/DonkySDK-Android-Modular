package net.donky.core.network.restapi.secured;

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
 * Network request to delete push configuration.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DeletePushConfigurationRequest extends GenericSecuredServiceRequest<Void> {

    @Override
    protected Void doSynchronousCall(String authorization) {
        return RestClient.getAPI().deletePush(authorization);
    }

    @Override
    protected void doAsynchronousCall(final String authorization, final NetworkResultListener<Void> listener) {

        RestClient.getAPI().deletePush(authorization, new Callback<Void>() {

            @Override
            public void success(Void responseDeletePush, retrofit.client.Response response) {
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

        DonkyNetworkController.getInstance().deletePushConfigurationOnNetwork(new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Push configuration deleted after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when deleting push configuration after connection restored.");

            }
        });
    }
}