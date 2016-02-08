package net.donky.core.network.restapi.secured;


import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;
import net.donky.core.settings.AppSettings;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to update client information.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateClient extends GenericSecuredServiceRequest<Void> {

    @SerializedName("sdkVersion")
    private final String sdkVersion;

    @SerializedName("moduleVersions")
    private final Map<String, String> moduleVersions;

    @SerializedName("appVersion")
    private final String appVersion;

    @SerializedName("currentLocalTime")
    private final String currentLocalTime;

    public UpdateClient() {
        super();
        this.sdkVersion = AppSettings.getVersion();
        this.moduleVersions = DonkyDataController.getInstance().getConfigurationDAO().getModules();
        this.appVersion = DonkyDataController.getInstance().getConfigurationDAO().getAppVersion();
        this.currentLocalTime = DateAndTimeHelper.getCurrentLocalTime();
    }

    public UpdateClient(String appVersion) {
        this.sdkVersion = AppSettings.getVersion();
        this.moduleVersions = DonkyDataController.getInstance().getConfigurationDAO().getModules();
        this.appVersion = appVersion;
        this.currentLocalTime = DateAndTimeHelper.getCurrentLocalTime();
    }

    @Override
    protected Void doSynchronousCall(String authorization) {
        return RestClient.getAPI().updateClient(authorization, this);
    }

    @Override
    protected void doAsynchronousCall(String authorization, final NetworkResultListener<Void> listener) {

        RestClient.getAPI().updateClient(authorization, this, new Callback<Void>() {

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

        DonkyAccountController.getInstance().updateClient(new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Client details updated after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when updating client details after connection restored.");

            }
        });

    }

    @Override
    public String toString() {

        String divider = " | ";

        StringBuilder sb = new StringBuilder();

        try {

            sb.append("CLIENT: ");
            sb.append(divider);
            sb.append(" sdkVersion: ").append(sdkVersion);
            sb.append(divider);
            sb.append(" appVersion : ").append(appVersion);
            sb.append(divider);
            if (moduleVersions != null) {
                for (String key : moduleVersions.keySet()) {
                    sb.append(key);
                    sb.append(" : ");
                    sb.append(moduleVersions.get(key));
                    sb.append(divider);
                }
            }
        } catch (Exception e) {
            sb.append("Error building log string");
        }
        return sb.toString();
    }
}
