package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

import net.donky.core.network.DiscoveredContact;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by Marcin Swierczek
 * 14/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GetPlatformUsersRequest extends GenericSecuredServiceRequest<List<DiscoveredContact>> {

    @SerializedName("EmailList")
    private List<String> emailList;

    @SerializedName("PhoneNumbers")
    private List<String> phoneNumbers;

    public GetPlatformUsersRequest(List<String> phoneNumbers, List<String> emails) {

        if (phoneNumbers != null) {
            this.phoneNumbers = phoneNumbers;
        } else {
            this.phoneNumbers = new ArrayList<>();
        }

        if (emails != null) {
            this.emailList = emails;
        } else {
            this.emailList = new ArrayList<>();
        }
    }

    @Override
    protected List<DiscoveredContact> doSynchronousCall(String authorization) {
        return RestClient.getAPI().getPlatformUsers(authorization, this);
    }

    @Override
    protected void doAsynchronousCall(final String authorization, final NetworkResultListener<List<DiscoveredContact>> listener) {
        RestClient.getAPI().getPlatformUsers(authorization, this, new Callback<List<DiscoveredContact>>() {

            @Override
            public void success(List<DiscoveredContact> platformUsersResponse, retrofit.client.Response response) {
                listener.success(platformUsersResponse);
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
        sb.append("GetPlatformUsersRequest: ");
        sb.append(divider);
        sb.append(phoneNumbers.toString());
        sb.append(divider);
        sb.append(emailList.toString());

        return sb.toString();
    }
}
