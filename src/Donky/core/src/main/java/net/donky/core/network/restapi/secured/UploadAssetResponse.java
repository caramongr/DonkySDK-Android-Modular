package net.donky.core.network.restapi.secured;

import com.google.gson.annotations.SerializedName;

/**
 * Response for asset upload request.
 *
 * Created by Marcin Swierczek
 * 23/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UploadAssetResponse {

    @SerializedName("assetId")
    private String assetId;

    public String getAssetId() {
        return assetId;
    }
}