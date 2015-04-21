package net.donky.core.network.restapi.secured;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.DeviceDetails;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to update device registration.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateDevice extends GenericSecuredServiceRequest<Void> {

    @SerializedName("id")
    private final String id;

    @SerializedName("secret")
    private final String secret;

    @SerializedName("operatingSystem")
    private final String operatingSystem;

    @SerializedName("operatingSystemVersion")
    private final String operatingSystemVersion;

    @SerializedName("type")
    private String type;

    @SerializedName("model")
    private final String model;

    @SerializedName("pushConfiguration")
    private final UpdatePushConfiguration updatePushConfiguration;

    @SerializedName("name")
    private String name;

    @SerializedName("additionalProperties")
    private Map<String, String> additionalProperties;

    public UpdateDevice(DeviceDetails deviceDetails) {

        super();

        this.id = DonkyDataController.getInstance().getDeviceDAO().getDeviceId();
        this.secret = DonkyDataController.getInstance().getDeviceDAO().getDeviceSecret();

        if (deviceDetails != null) {
            this.type = deviceDetails.getDeviceType();
            this.name = deviceDetails.getDeviceName();
            this.additionalProperties = deviceDetails.getAdditionalProperties();
        }

        this.operatingSystem =  DeviceDetails.getOSName();
        this.operatingSystemVersion = DeviceDetails.getOSVersion();
        this.model = DeviceDetails.getDeviceModel();

        String gcmRegistrationId = DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationId();
        if (!TextUtils.isEmpty(gcmRegistrationId)) {
            this.updatePushConfiguration = new UpdatePushConfiguration(gcmRegistrationId);
        } else {
            this.updatePushConfiguration = null;
        }
    }

    @Override
    public String toString() {

        String divider = " | ";

        StringBuilder sb = new StringBuilder();

        try {
            sb.append("DEVICE: ");
            sb.append(" id: ").append(id);
            sb.append(divider);
            sb.append(" secret : ").append(secret);
            sb.append(divider);
            sb.append(" operatingSystem : ").append(operatingSystem);
            sb.append(divider);
            sb.append(" operatingSystemVersion : ").append(operatingSystemVersion);
            sb.append(divider);
            sb.append(" type : ").append(type);
            sb.append(divider);
            sb.append(" model : ").append(model);
            sb.append(divider);
            sb.append(" name: ").append(name);
            if (updatePushConfiguration != null) {
                sb.append(divider);
                sb.append(" pushConfiguration.Type : ").append(updatePushConfiguration.getType());
                sb.append(divider);
                sb.append(" pushConfiguration.RegistrationId : ").append(updatePushConfiguration.getRegistrationId());
            }

            sb.append(divider);
            if (additionalProperties != null) {
                for (String key : additionalProperties.keySet()) {
                    sb.append(key);
                    sb.append(" : ");
                    sb.append(additionalProperties.get(key));
                    sb.append(divider);
                }
            }
        } catch (Exception e) {
            sb.append("Error building log string");
        }
        return sb.toString();
    }

    @Override
    protected Void doSynchronousCall(String apiKey) {
        return RestClient.getAPI().updateDevice(apiKey, this);
    }

    @Override
    protected void doAsynchronousCall(String authorization, final NetworkResultListener<Void> listener) {

        RestClient.getAPI().updateDevice(authorization, this, new Callback<Void>() {

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

        DonkyAccountController.getInstance().updateDeviceDetails(new DeviceDetails(UpdateDevice.this.name, UpdateDevice.this.type, UpdateDevice.this.additionalProperties), new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Device details updated after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when updating device details after connection restored.");

            }
        });
    }
}
