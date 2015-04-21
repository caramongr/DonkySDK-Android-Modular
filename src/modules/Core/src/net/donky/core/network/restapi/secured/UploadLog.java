package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.logging.DonkyLoggingController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.restapi.RestClient;

import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Network request to upload logs.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UploadLog extends GenericSecuredServiceRequest<UploadLogResponse> {

    /**
     * The audience type for the content.
     */
    public enum SubmissionReason {

        NotDefined,
        AutomaticByDevice,
        ManualRequest;

        public boolean equals(String type) {
            return this.toString().equals(type);
        }
    }

    /**
     * Log to be uploaded to Donky Network.
     */
    @SerializedName("data")
    private final String data;

    /**
     * The reason that the log is being submitted.
     */
    @SerializedName("submissionReason")
    private String submissionReason;

    private SubmissionReason reason;

    public UploadLog(String data, SubmissionReason submissionReason) {
        super();
        this.data = data;
        if (submissionReason != null){
            this.submissionReason = submissionReason.toString();
        }
    }

    @Override
    protected UploadLogResponse doSynchronousCall(final String authorization) {
        return RestClient.getAPI().uploadLog(authorization, this);
    }

    @Override
    protected void doAsynchronousCall(final String authorization,final NetworkResultListener<UploadLogResponse> listener) {

        RestClient.getAPI().uploadLog(authorization, this, new Callback<UploadLogResponse>() {

            @Override
            public void success(UploadLogResponse uploadLogResponse, retrofit.client.Response response) {
                listener.success(uploadLogResponse);
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

        DonkyLoggingController.getInstance().submitLog(reason, new DonkyListener() {

            @Override
            public void success() {

                new DLog("onConnected").info("Log uploaded after connection restored.");

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                new DLog("onConnected").error("Error when uploading logs after connection restored.");

            }
        });

    }
}
