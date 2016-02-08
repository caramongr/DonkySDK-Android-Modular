package net.donky.core.network.assets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import net.donky.core.logging.DLog;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Controller class for Asset related functionality.
 * @deprecated Please use net.donky.core.assets.DonkyAssetController from Assets Module instead.
 *
 * Created by Marcin Swierczek
 * 13/04/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Deprecated
public class DonkyAssetController {

    private static final String ASSET_URL_ID_REPLACEMENT = "{0}";

    private static final int SUCCESSFUL_DOWNLOAD = 200;

    DLog log = new DLog("AssetController");

    OkHttpClient okHttpClient;

    private Context context;

    // Private constructor. Prevents instantiation from other classes.
    private DonkyAssetController() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyAssetController INSTANCE = new DonkyAssetController();
    }

    /**
     * @return Instance of Asset Controller singleton.
     */
    public static DonkyAssetController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(Context context, OkHttpClient okHttpClient) {

        this.okHttpClient = okHttpClient;

        this.context = context;

    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * Download Asset asynchronously.
     *
     * @param assetId Asset id.
     * @param listener Callback to be invoked when completed.
     */
    public void downloadAvatar(String assetId, final NotificationImageLoader listener) {

        if (!TextUtils.isEmpty(assetId) && isInitialised()) {

            try {

                String path = getAssetUrl(assetId);

                Request request = new Request.Builder().url(path).build();

                okHttpClient.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Request request, IOException e) {

                        if (listener != null) {
                            listener.failure(e);
                        }

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {

                        if (response != null && response.code() == SUCCESSFUL_DOWNLOAD) {

                            ResponseBody responseBody = response.body();

                            if (responseBody != null) {

                                InputStream inputStream = responseBody.byteStream();

                                if (listener != null) {
                                    listener.downloadCompleted(BitmapFactory.decodeStream(inputStream));
                                }

                            } else {

                                if (listener != null) {
                                    listener.failure(new Exception("Null response body"));
                                }

                            }

                        } else {

                            if (listener != null) {
                                listener.failure(new Exception("Null response"));
                            }

                        }

                    }

                });
            } catch (Exception exception) {

                log.error("Error downloading avatar",exception);

                if (listener != null) {
                    listener.failure(exception);
                }

            }
        } else {

            if (listener != null) {
                listener.failure(new Exception("Empty asset id or network client not initialised."));
            }

        }
    }

    /**
     * Download Asset synchronously. Cannot be performed in the main thread.
     *
     * @param assetId Asset id.
     * @return Image asset.
     */
    public Bitmap downloadAvatar(String assetId) {

        if (!TextUtils.isEmpty(assetId) && isInitialised()) {

            try {

                String path = getAssetUrl(assetId);

                Request request = new Request.Builder().url(path).build();

                Response response = okHttpClient.newCall(request).execute();

                if (response != null) {

                    ResponseBody responseBody = response.body();

                    InputStream inputStream = responseBody.byteStream();

                    Resources resources = context.getResources();

                    int size = ImageHelper.getPixelsFromDP(resources, 128);

                    return ImageHelper.resizeBitmap(BitmapFactory.decodeStream(inputStream), size, size, true);

                }


            } catch (Exception exception) {
                log.error("Error downloading avatar", exception);
            }
        }

        return null;

    }

    /**
     * Construct full asset url for given assetId.
     *
     * @param assetId The assetId to get the URL for.
     * @return The full URL to the requested asset.
     */
    public String getAssetUrl(String assetId) {

        if (!TextUtils.isEmpty(assetId)) {

            String query;

            try {

                query = URLEncoder.encode(assetId, "utf-8");

            } catch (UnsupportedEncodingException e) {

                log.error("Error URL encoding the asset Id.", e);

                query = assetId;
            }

            String urlFormat = DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().get(ConfigurationDAO.KEY_CONFIGURATION_AssetDownloadUrlFormat);

            if (!TextUtils.isEmpty(urlFormat) && !TextUtils.isEmpty(query)) {
                return urlFormat.replace(ASSET_URL_ID_REPLACEMENT, query);
            }

        }

        return null;
    }

    public boolean isInitialised() {

        return okHttpClient != null;

    }

}
