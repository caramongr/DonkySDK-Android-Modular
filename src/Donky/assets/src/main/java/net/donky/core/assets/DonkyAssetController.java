package net.donky.core.assets;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.assets.utils.ImageUtils;
import net.donky.core.assets.utils.MimeUtils;
import net.donky.core.helpers.IdHelper;
import net.donky.core.helpers.MainThreadHandlerHelper;
import net.donky.core.logging.DLog;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.assets.Asset;
import net.donky.core.network.assets.AssetType;
import net.donky.core.network.restapi.secured.UploadAssetResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Main Controller class for Assets Module
 * <p/>
 * Created by Marcin Swierczek
 * 11/11/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyAssetController {

    public final String downloadsFolder;
    public String applicationDownloadsFolder;

    private static final int MAX_AVATAR_WIDTH = 96;
    private static final int MAX_AVATAR_HEIGHT = 96;

    private static final String ASSET_URL_ID_REPLACEMENT = "{0}";

    private static final int SUCCESSFUL_DOWNLOAD = 200;

    private DLog log;

    private OkHttpClient okHttpClient;

    private Context context;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyAssetController() {
        log = new DLog("DonkyAssetController");
        downloadsFolder = Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOWNLOADS;
        applicationDownloadsFolder = downloadsFolder+"/";
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
     * @return Static instance of Asset Controller singleton.
     */
    public static DonkyAssetController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    void init(Context context, OkHttpClient okHttpClient) {
        this.context = context;
        applicationDownloadsFolder = applicationDownloadsFolder+getFolderName(context)+"/";
        if (okHttpClient != null) {
            this.okHttpClient = okHttpClient;
        } else {
            log.error("Missing okHttpClient instance.");
        }
    }

    /**
     * Upload messaging asset to Donky Network. The mime type and name will be deduced from the file. In response you will get the assetId with which you can reference the asset on the network.
     *
     * @param file                File to upload to donky Network
     * @param donkyResultListener Callback With the details of uploaded assets.
     */
    public void uploadMessageAsset(File file, final DonkyResultListener<Asset> donkyResultListener) {

        if (file != null) {

            String mimeType = MimeUtils.getMimeType(Uri.fromFile(file).toString());

            if (!TextUtils.isEmpty(mimeType)) {

                final Asset assetDetails = new Asset(file.getName(), mimeType, file.length());
                assetDetails.setFilePath(file.getAbsolutePath());

                AssetType assetType = AssetType.MessageAsset;

                final MainThreadHandlerHelper<Asset> handler = new MainThreadHandlerHelper<>();

                DonkyNetworkController.getInstance().uploadAsset(assetType, mimeType, file, new DonkyResultListener<UploadAssetResponse>() {

                    @Override
                    public void success(UploadAssetResponse uploadAssetResponse) {
                        if (uploadAssetResponse != null && !TextUtils.isEmpty(uploadAssetResponse.getAssetId())) {
                            assetDetails.setAssetId(uploadAssetResponse.getAssetId());
                            handler.notifySuccess(donkyResultListener, assetDetails);
                        }
                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                        handler.notifyError(donkyResultListener, donkyException, validationErrors);
                    }
                });
            }
        }
    }

    /**
     * Upload messaging asset to Donky Network. In response you will get the assetId with which you can reference the asset on the network.
     *
     * @param bytesArray          Bytes to upload to donky Network
     * @param mimeType            Mime type of the data
     * @param name                Name to be used as a reference.
     * @param donkyResultListener Callback with the details of uploaded assets.
     */
    public void uploadMessageAsset(final byte[] bytesArray, final String mimeType, @Nullable final String name, final DonkyResultListener<Asset> donkyResultListener) {

        if (bytesArray != null && bytesArray.length > 0 && !TextUtils.isEmpty(mimeType)) {

            AssetType assetType = AssetType.MessageAsset;

            final MainThreadHandlerHelper<Asset> handler = new MainThreadHandlerHelper<>();

            DonkyNetworkController.getInstance().uploadAsset(assetType, mimeType, bytesArray, new DonkyResultListener<UploadAssetResponse>() {

                @Override
                public void success(UploadAssetResponse uploadAssetResponse) {
                    if (uploadAssetResponse != null && !TextUtils.isEmpty(uploadAssetResponse.getAssetId())) {
                        Asset assetDetails = new Asset(name, mimeType, bytesArray.length);
                        assetDetails.setAssetId(uploadAssetResponse.getAssetId());
                        handler.notifySuccess(donkyResultListener, assetDetails);
                    } else {
                        handler.notifyError(donkyResultListener, new DonkyException("No asset id returned from network"), null);
                    }
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    handler.notifyError(donkyResultListener, donkyException, validationErrors);
                }
            });
        }
    }

    /**
     * Upload a new avatar image to the Donky Network.
     *
     * @param bitmap Bitmap to upload
     * @param donkyResultListener Callback when operation completed returning the new avatar asset ID
     */
    public void uploadAccountAvatar(final Bitmap bitmap, final DonkyResultListener<String> donkyResultListener) {

        if (bitmap != null) {

            DonkyCore.getInstance().processInBackground(new Runnable() {

                @Override
                public void run() {

                    byte[] bytes = null;

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmapToSend = ImageUtils.resizeBitmap(bitmap, MAX_AVATAR_WIDTH, MAX_AVATAR_HEIGHT, true);

                    if (bitmapToSend != null) {
                        bitmapToSend.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        bytes = stream.toByteArray();
                    }

                    if (bytes != null && bytes.length > 0) {

                        final MainThreadHandlerHelper<String> handler = new MainThreadHandlerHelper<>();

                        AssetType assetType = AssetType.AccountAvatar;

                        DonkyNetworkController.getInstance().uploadAsset(assetType, MimeUtils.MIME_TYPE_MESSAGE_ASSET_PNG, bytes, new DonkyResultListener<UploadAssetResponse>() {

                            @Override
                            public void success(final UploadAssetResponse uploadAssetResponse) {

                                if (uploadAssetResponse != null && !TextUtils.isEmpty(uploadAssetResponse.getAssetId())) {

                                    UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
                                    if (TextUtils.isEmpty(userDetails.getUserDisplayName())) {
                                        userDetails.setUserDisplayName(userDetails.getUserId());
                                    }
                                    userDetails.setUserAvatarId(uploadAssetResponse.getAssetId());

                                    DonkyAccountController.getInstance().updateUserDetails(userDetails, new DonkyListener() {
                                        @Override
                                        public void success() {
                                            handler.notifySuccess(donkyResultListener, uploadAssetResponse.getAssetId());
                                        }

                                        @Override
                                        public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                            handler.notifyError(donkyResultListener, donkyException, validationErrors);
                                        }
                                    });
                                } else {
                                    handler.notifyError(donkyResultListener, new DonkyException("Empty asset id in response."), null);
                                }
                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                handler.notifyError(donkyResultListener, donkyException, validationErrors);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Download Image Asset asynchronously.
     *
     * @param assetId  Asset id.
     * @param listener Callback to be invoked when completed.
     */
    public void downloadImageAsset(String assetId, final NotificationImageLoader listener) {

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
                                    listener.failure(new Exception("Error downloading image. Null response body"));
                                }

                            }

                        } else {

                            if (listener != null) {
                                listener.failure(new Exception("Error downloading image."));
                            }

                        }

                    }

                });

            } catch (Exception exception) {

                log.error("Error downloading avatar", exception);

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
     * Download Asset asynchronously.
     *
     * @param assetId  ID of asset to download
     * @param listener Callback with the downloaded data
     */
    public void downloadAsset(String assetId, final DonkyResultListener<InputStream> listener) {

        try {

            String path = getAssetUrl(assetId);

            Request request = new Request.Builder().url(path).build();

            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {

                    DonkyException donkyException = new DonkyException("Failed to download file");
                    donkyException.initCause(e);
                    if (listener != null) {
                        listener.error(donkyException, null);
                    }

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    if (response != null && response.code() == SUCCESSFUL_DOWNLOAD) {

                        ResponseBody responseBody = response.body();

                        if (responseBody != null) {

                            InputStream inputStream = responseBody.byteStream();

                            if (listener != null) {
                                if (inputStream != null) {
                                    listener.success(inputStream);
                                } else {
                                    listener.error(new DonkyException("Null response body"), null);
                                }
                            }

                        } else if (listener != null) {
                            listener.error(new DonkyException("Null response body"), null);
                        }

                    } else if (listener != null) {
                        listener.error(new DonkyException("Download failed"), null);
                    }
                }
            });

        } catch (Exception exception) {
            log.error("Error downloading avatar", exception);
            if (listener != null) {
                listener.error(new DonkyException("Download failed"), null);
            }
        }
    }

    /**
     * Download asset file from the Donky Network and save that file into Downloads folder.
     *
     * @param mimeType            Mime type of the data
     * @param name                Name with which the file should be saved
     * @param donkyResultListener Callback delivers you the path where the file was saved
     */
    public void downloadAssetAndSave(final String assetId, final String mimeType, @Nullable final String name,@Nullable final DonkyResultListener<Asset> donkyResultListener) {

        if (!TextUtils.isEmpty(assetId) && !TextUtils.isEmpty(mimeType)) {

            downloadAsset(assetId, new DonkyResultListener<InputStream>() {
                @Override
                public void success(final InputStream stream) {
                    if (stream != null) {
                        saveStream(assetId, mimeType, name, stream, donkyResultListener);
                    }
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    new MainThreadHandlerHelper<>().notifyError(donkyResultListener, donkyException, validationErrors);
                }
            });
        } else {
            new MainThreadHandlerHelper<>().notifyError(donkyResultListener, new DonkyException("Missing asset id or mime type."), null);
        }
    }

    /**
     * Generate full path for file to save
     */
    private String getPathToSaveStream(final String assetId, final String fileName, final String mimeType) {

        String finalName;

        if (!TextUtils.isEmpty(fileName)) {
            finalName = fileName;
        } else {
            if (!TextUtils.isEmpty(assetId)) {
                finalName = assetId;
            } else {
                finalName = IdHelper.generateId();
            }

            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!TextUtils.isEmpty(ext)) {
                finalName = finalName + "." + ext;
            }
        }

        return applicationDownloadsFolder + finalName;
    }

    /**
     * Save stream to a file in Downloads/temp_appname/ folder using given file name if provided
     */
    private void saveStream(final String assetId, final String mimeType, @Nullable final String fileName, final InputStream stream, @Nullable final DonkyResultListener<Asset> donkyResultListener) {

        if (initDownloadFolder()) {

            DonkyCore.getInstance().processInBackground(new Runnable() {

                @Override
                public void run() {

                    String path = getPathToSaveStream(assetId, fileName, mimeType);

                    File file = new File(path);

                    OutputStream outStream = null;

                    final MainThreadHandlerHelper<Asset> handler = new MainThreadHandlerHelper<>();

                    try {
                        try {
                            outStream = new FileOutputStream(file);
                            byte[] buffer = new byte[8 * 1024];
                            int bytesRead;
                            while ((bytesRead = stream.read(buffer)) != -1) {
                                outStream.write(buffer, 0, bytesRead);
                            }
                        } catch (Exception exception) {
                            log.error("Error writing to file", exception);
                            DonkyException donkyException = new DonkyException("Error writing to file");
                            donkyException.initCause(exception);
                            handler.notifyError(donkyResultListener, donkyException, null);
                        } finally {
                            if (outStream != null) {
                                outStream.close();
                            }
                            stream.close();
                            Asset assetDetails = new Asset(file.getName(), mimeType, file.length());
                            assetDetails.setAssetId(assetId);
                            assetDetails.setFilePath(file.getAbsolutePath());
                            handler.notifySuccess(donkyResultListener, assetDetails);
                        }
                    } catch (Exception exception) {
                        log.error("Error closing file", exception);
                        DonkyException donkyException = new DonkyException("Error closing file");
                        donkyException.initCause(exception);
                        handler.notifyError(donkyResultListener, donkyException, null);
                    }
                }
            });
        }
    }

    /**
     * Create download folder if needed
     */
    private boolean initDownloadFolder() {

        File folder = new File(applicationDownloadsFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }

        return folder.exists();
    }

    /**
     * returns application name
     *
     * @param context Application context to check the app name for constructing the temp folder name.
     * @return
     */
    private static String getFolderName(Context context) {
        String name = null;
        ApplicationInfo info = context.getApplicationInfo();
        if (info != null) {
            int stringId = info.labelRes;
            try {
                name = context.getString(stringId);
            } catch (Resources.NotFoundException e) {
                //Application name not found. Use package name instead.
            }
        }
        if (TextUtils.isEmpty(name)) {
            name = context.getPackageName();
        }
        if (TextUtils.isEmpty(name)) {
            name = "Donky";
        }
        name = "temp_" + name;
        return name;
    }

    /**
     * Download Asset synchronously. Cannot be performed in the main thread.
     *
     * @param assetId Asset id.
     * @return Image asset.
     */
    public Bitmap downloadImageAsset(String assetId) {

        if (!TextUtils.isEmpty(assetId) && isInitialised()) {

            try {

                String path = getAssetUrl(assetId);

                Request request = new Request.Builder().url(path).build();

                Response response = okHttpClient.newCall(request).execute();

                if (response != null) {

                    ResponseBody responseBody = response.body();

                    InputStream inputStream = responseBody.byteStream();

                    Resources resources = context.getResources();

                    int size = ImageUtils.getPixelsFromDP(resources, 128);

                    return ImageUtils.resizeBitmap(BitmapFactory.decodeStream(inputStream), size, size, true);

                }

            } catch (Exception exception) {
                log.error("Error downloading avatar", exception);
            }
        }

        return null;

    }

    /**
     * Construct full asset network url for given assetId.
     *
     * @param assetId The assetId to get the URL for.
     * @return The full URL on Donky Network to the requested asset.
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
