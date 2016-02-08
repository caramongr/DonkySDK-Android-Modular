package net.donky.core.network.assets;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Marcin Swierczek
 * 18/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AssetMetaData {

    @SerializedName("MimeType")
    private String mimeType;

    @SerializedName("Type")
    private String type;

    public AssetMetaData(AssetType type, String mimeType) {
        this.mimeType = mimeType;
        this.type = type.toString();
    }
}
