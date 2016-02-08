package net.donky.core.network.restapi.secured;

import com.google.gson.JsonObject;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 *
 *
 * Created by Marcin Swierczek
 * 14/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GetConversationsHistory extends GenericSecuredServiceRequest<List<JsonObject>> {

    String query;

    public GetConversationsHistory(String query) {
        this.query = query;
    }

    @Override
    protected List<JsonObject> doSynchronousCall(String authorization) {
        return RestClient.getAPI().getConversationsHistory(authorization, query);
    }

    @Override
    protected void doAsynchronousCall(final String authorization, final NetworkResultListener<List<JsonObject>> listener) {
        RestClient.getAPI().getConversationsHistory(authorization, query, new Callback<List<JsonObject>>() {
            @Override
            public void success(List<JsonObject> result, Response response) {
                listener.success(result);
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
        sb.append("GetMessageHistory: ");
        sb.append(divider);
        sb.append(query);

        return sb.toString();
    }
}
