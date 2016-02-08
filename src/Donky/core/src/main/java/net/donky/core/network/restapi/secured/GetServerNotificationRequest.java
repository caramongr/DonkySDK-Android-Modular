package net.donky.core.network.restapi.secured;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.restapi.RestClient;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to download specific notification.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GetServerNotificationRequest extends GenericSecuredServiceRequest<ServerNotification> {

    private final String id;

    public GetServerNotificationRequest(String id){
        super();
        this.id = id;
    }

    @Override
    protected ServerNotification doSynchronousCall(String authorization) {
        return RestClient.getAPI().getNotification(authorization, id);
    }

    @Override
    protected void doAsynchronousCall(final String authorization, final NetworkResultListener<ServerNotification> listener) {

        RestClient.getAPI().getNotification(authorization, id, new Callback<ServerNotification>() {

            @Override
            public void success(ServerNotification serverNotification, retrofit.client.Response response) {
                listener.success(serverNotification);
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
    public void onConnected() {

    }
}
