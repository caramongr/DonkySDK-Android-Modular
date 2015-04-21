package net.donky.core.network.restapi.secured;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.TagDescription;
import net.donky.core.network.restapi.RestClient;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateTags extends GenericSecuredServiceRequest<Void> {

    List<TagDescription> tagDescriptions;

    NetworkResultListener<Void> listener;

    public UpdateTags(List<TagDescription> tagDescriptions) {
        super();
        this.tagDescriptions = tagDescriptions;

    }

    @Override
    protected void doStartListenForConnectionRestored() {

        if (DonkyAccountController.getInstance().isRegistered()) {
            startUniqueListener();
        }

    }

    @Override
    protected Void doSynchronousCall(String authorisation) throws RetrofitError {
        return RestClient.getAPI().updateTags(authorisation, tagDescriptions);
    }

    @Override
    protected void doAsynchronousCall(final String authorisation,final NetworkResultListener<Void> listener) {

        RestClient.getAPI().updateTags(authorisation, tagDescriptions, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {
                listener.success(null);
            }

            @Override
            public void failure(RetrofitError error) {
                listener.onFailure(error);

                UpdateTags.this.listener = listener;
            }
        });

    }

    @Override
    protected void onConnected() {

        synchronized (sharedLock) {
            stopUniqueListener();
            sharedLock.notifyAll();
        }

        DonkyNetworkController.getInstance().updateTags(tagDescriptions, new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Tags updated after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when updating tags after connection restored.");

            }
        });
    }
}
