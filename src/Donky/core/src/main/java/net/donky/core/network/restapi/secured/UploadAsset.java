package net.donky.core.network.restapi.secured;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.assets.AssetMetaData;
import net.donky.core.network.assets.AssetType;
import net.donky.core.network.restapi.RestClient;

import java.io.File;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;

/**
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UploadAsset extends GenericSecuredServiceRequest<UploadAssetResponse> {

    private final AssetMetaData metadata;

    private final String metadataStr;

    private final TypedFile typedFile;

    private final TypedInput typedInput;

    NetworkResultListener<UploadAssetResponse> listener;

    String authorisation;

    public UploadAsset(AssetType assetType, String mimeType, File file) {
        super();
        this.metadata = new AssetMetaData(assetType, mimeType);
        Gson gson = new GsonBuilder().create();
        this.metadataStr = gson.toJson(metadata);
        typedFile = new TypedFile(mimeType, file);
        typedInput = null;
    }

    public UploadAsset(AssetType assetType, String mimeType, byte[] byteArray) {
        super();
        this.metadata = new AssetMetaData(assetType, mimeType);
        Gson gson = new GsonBuilder().create();
        this.metadataStr = gson.toJson(metadata);
        this.typedInput = new TypedByteArray(mimeType,  byteArray);
        typedFile = null;
    }

    @Override
    protected void doStartListenForConnectionRestored() {
        startUniqueListener();
    }

    @Override
    protected UploadAssetResponse doSynchronousCall(String authorisation) throws RetrofitError {

        this.authorisation = authorisation;

        if (!TextUtils.isEmpty(metadataStr)) {
            if (typedInput != null) {
                return RestClient.getAPI().uploadAsset(authorisation, metadataStr, typedInput);
            } else if (typedFile != null) {
                return RestClient.getAPI().uploadAsset(authorisation, metadataStr, typedFile);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void doAsynchronousCall(final String authorisation, final NetworkResultListener<UploadAssetResponse> listener) {

        UploadAsset.this.listener = listener;
        this.authorisation = authorisation;

        if (!TextUtils.isEmpty(metadataStr)) {
            if (typedInput != null) {
                RestClient.getAPI().uploadAsset(authorisation, metadataStr, typedInput, new Callback<UploadAssetResponse>() {

                    @Override
                    public void success(UploadAssetResponse uploadAssetResponse, retrofit.client.Response response) {
                        listener.success(uploadAssetResponse);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        listener.onFailure(error);
                    }

                });
            } else if (typedFile != null) {
                RestClient.getAPI().uploadAsset(authorisation, metadataStr, typedFile, new Callback<UploadAssetResponse>() {

                    @Override
                    public void success(UploadAssetResponse uploadAssetResponse, retrofit.client.Response response) {
                        listener.success(uploadAssetResponse);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        listener.onFailure(error);
                    }

                });
            } else {
                listener.onFailure(null);
            }
        } else {
            listener.onFailure(null);
        }
    }

    @Override
    protected void onConnected() {

        synchronized (sharedLock) {
            stopUniqueListener();
            sharedLock.notifyAll();
        }

        doAsynchronousCall(authorisation, listener);
    }
}
