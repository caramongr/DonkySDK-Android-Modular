package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.logging.DLog;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to update user registration.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateUser extends GenericSecuredServiceRequest<Void> {

    @SerializedName("id")
    private final String id;

    @SerializedName("displayName")
    private final String displayName;

    @SerializedName("firstName")
    private final String firstName;

    @SerializedName("lastName")
    private final String lastName;

    @SerializedName("emailAddress")
    private final String emailAddress;

    @SerializedName("countryCode")
    private final String countryCode;

    @SerializedName("mobileNumber")
    private final String mobileNumber;

    @SerializedName("avatarAssetId")
    private final String avatarAssetId;

    @SerializedName("additionalProperties")
    private Map<String, String> additionalProperties;

    private UserDetails userDetails;

    public UpdateUser(UserDetails user) {
        super();
        this.id = user.getUserId();
        this.displayName = user.getUserDisplayName();
        this.firstName = user.getUserFirstName();
        this.lastName = user.getUserLastName();
        this.emailAddress = user.getUserEmailAddress();
        this.countryCode = user.getCountryCode();
        this.mobileNumber = user.getUserMobileNumber();
        this.avatarAssetId = user.getUserAvatarId();
        Map<String, String> additionalProperties = user.getUserAdditionalProperties();
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            this.additionalProperties = additionalProperties;
        }

        this.userDetails = user;
    }

    @Override
    public String toString() {

        String divider = " | ";

        StringBuilder sb = new StringBuilder();

        try {
            sb.append("USER: ");
            sb.append(divider);
            sb.append(" id: ").append(id);
            sb.append(divider);
            sb.append(" displayName : ").append(displayName);
            sb.append(divider);
            sb.append(" firstName : ").append(firstName);
            sb.append(divider);
            sb.append(" lastName : ").append(lastName);
            sb.append(divider);
            sb.append(" emailAddress : ").append(emailAddress);
            sb.append(divider);
            sb.append(" countryCode : ").append(countryCode);

            sb.append(" mobileNumber: ").append(mobileNumber);
            sb.append(divider);
            sb.append(" avatarAssetId : ").append(avatarAssetId);
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
        return RestClient.getAPI().updateUser(apiKey, this);
    }

    @Override
    protected void doAsynchronousCall(String authorization, final NetworkResultListener<Void> listener) {

        RestClient.getAPI().updateUser(authorization, this, new Callback<Void>() {

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

        DonkyAccountController.getInstance().updateUserDetails(userDetails, new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("User details updated after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when updating user details after connection restored.");

            }
        });
    }
}
