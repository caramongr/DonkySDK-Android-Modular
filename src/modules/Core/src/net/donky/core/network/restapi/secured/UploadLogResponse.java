package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

/**
 * Network response for uploading logs.
 *
 * Created by Marcin Swierczek
 * 27/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UploadLogResponse {

    @SerializedName("alwaysSubmitErrors")
    private boolean alwaysSubmitErrors;

    /**
     * Should the Core SDK submit logs automatically when new error message was logged.
     * @return True if SDK should submit logs automatically.
     */
    public boolean isAlwaysSubmitErrors() {
        return alwaysSubmitErrors;
    }
}
