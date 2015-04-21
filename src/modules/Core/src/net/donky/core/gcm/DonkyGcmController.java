package net.donky.core.gcm;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.RetryPolicy;
import net.donky.core.network.restapi.secured.UpdatePushConfiguration;
import net.donky.core.settings.AppSettings;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for all Google Cloud Messaging related functionality.
 * <p/>
 * Created by Marcin Swierczek
 * 16/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGcmController {

    private final DLog log;

    private Context context;

    // Private constructor. Prevents instantiation from other classes.
    private DonkyGcmController() {
        log = new DLog("GcmController");
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyGcmController INSTANCE = new DonkyGcmController();
    }

    /**
     * Get instance of GCM Controller singleton.
     *
     * @return Instance of GCM Controller singleton.
     */
    public static DonkyGcmController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application) {
        this.context = application.getApplicationContext();
    }

    /**
     * Register to Google Cloud Services. This will allow app to receive push messages. Registration process will be moved to background if this method will be called from main thread.
     *
     * @param listener The callback to invoke when the command has executed.
     */
    public void registerPush(final DonkyListener listener) {

        if (DonkyCore.isInitialised() && !isRegisteredToGCM()) {

            String senderId = AppSettings.getInstance().getGcmSenderId();

            if (TextUtils.isEmpty(senderId)) {

                senderId = DonkyDataController.getInstance().getConfigurationDAO().getGcmSenderId();

                if (TextUtils.isEmpty(senderId)) {

                    if (listener != null) {
                        listener.error(new DonkyException("GCM sender id not found."), null);
                    }

                    return;
                }
            }

            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

            if (resultCode == ConnectionResult.SUCCESS) {

                log.info("Registering to GCM");

                registerToGcmInBackground(senderId, new GcmRegistrationListener() {

                    @Override
                    public void success(String gcmRegistrationId) {

                        DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(gcmRegistrationId);

                        DonkyNetworkController.getInstance().updatePushConfigurationOnNetwork(new UpdatePushConfiguration(gcmRegistrationId), listener);

                    }

                    @Override
                    public void failed(DonkyException exception) {

                        if (listener != null) {
                            listener.error(exception, null);
                        }

                    }

                    @Override
                    public void googlePlayServicesNotAvailable(int googlePlayServicesUtilResultCode) {

                        DonkyException donkyException = new DonkyException("Google Play Services not available, code " + googlePlayServicesUtilResultCode);

                        if (listener != null) {
                            listener.error(donkyException, null);
                        }

                    }
                });

            } else {

                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    log.error("Google Play Services is probably not up to date. User recoverable Error Code is " + resultCode);
                } else {
                    log.error("This device is not supported by Google Play Services.");
                }

                DonkyException donkyException = new DonkyException("Google Play Services not available. Connection Status Code " + resultCode);

                if (listener != null) {
                    listener.error(donkyException, null);
                }

            }

        } else {

            if (listener != null) {
                listener.success();
            }

        }

    }

    /**
     * Unregister from Google Cloud Services. Unregister process will be moved to background if this method will be called from main thread.
     *
     * @param listener The callback to invoke when the command has executed.
     */
    public void unregisterPush(final DonkyListener listener) {

        if (DonkyCore.isInitialised()) {

            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

            if (resultCode == ConnectionResult.SUCCESS) {

                unregisterFromGcmInBackground(new DonkyListener() {

                    @Override
                    public void success() {

                        DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);

                        DonkyNetworkController.getInstance().deletePushConfigurationOnNetwork(listener);

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        if (listener != null) {
                            listener.error(donkyException, null);
                        }

                    }
                });

            } else {

                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    log.error("Google Play Services is probably not up to date. User recoverable Error Code is " + resultCode);
                } else {
                    log.error("This device is not supported by Google Play Services.");
                }

                DonkyException donkyException = new DonkyException("Google Play Services not available. Connection Status Code " + resultCode);

                if (listener != null) {
                    listener.error(donkyException, null);
                }

            }

        }
    }

    /**
     * Registers the application with GCM servers asynchronously. Performs retries if unsuccessful.
     * Stores the registration ID and app versionCode in the application's shared preferences.
     *
     * @param senderId Sender id identifier in Google Cloud Messaging
     * @param listener Callback to invoke when method finishes.
     */
    private void registerToGcmInBackground(final String senderId, final GcmRegistrationListener listener) {

        new AsyncTask<Void, Void, String>() {

            IOException exception;

            @Override
            protected String doInBackground(Void... params) {

                RetryPolicy retryPolicy = new RetryPolicy();

                String registrationId = null;

                try {

                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                    if (gcm != null) {

                        while (registrationId == null && retryPolicy.retry()) {

                            registrationId = gcm.register(senderId);

                            if (TextUtils.isEmpty(registrationId)) {

                                try {
                                    Thread.sleep(retryPolicy.getDelayBeforeNextRetry());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            } else {

                                DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(registrationId);
                            }
                        }
                    }

                } catch (IOException e) {

                    exception = e;
                    log.error("Error registering to GCM", e);

                }

                return registrationId;
            }

            @Override
            protected void onPostExecute(String registrationId) {

                if (!TextUtils.isEmpty(registrationId) && listener != null) {

                    listener.success(registrationId);

                } else if (listener != null) {

                    DonkyException donkyException = new DonkyException("Error registering for GCM.");
                    donkyException.initCause(exception);

                    listener.failed(donkyException);

                }
            }
        }.execute(null, null, null);
    }

    /**
     * Unregisters the application from GCM servers asynchronously.
     *
     * @param listener The callback to invoke when the command has executed.
     */
    private void unregisterFromGcmInBackground(final DonkyListener listener) {

        new AsyncTask<Void, Void, Boolean>() {

            IOException exception;

            @Override
            protected Boolean doInBackground(Void... params) {

                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                if (gcm != null) {

                    try {

                        gcm.unregister();

                    } catch (IOException e) {

                        exception = e;

                        return false;

                    }

                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                if (result && listener != null) {

                    listener.success();

                } else if (listener != null) {

                    DonkyException donkyException = new DonkyException("Error registering for GCM.");
                    donkyException.initCause(exception);

                    listener.error(donkyException, null);

                }
            }
        }.execute(null, null, null);

    }

    /**
     * Check if SDK has already registered to Google Cloud Messaging.
     *
     * @return True if already registered to Google Cloud Messaging.
     */
    public boolean isRegisteredToGCM() {

        boolean isRegistered = !TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationId());

        String appVersion = DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationAppVersion();

        Integer registeredVersion = Integer.getInteger(appVersion);

        int currentVersion = getAppVersion();

        if (registeredVersion != null && registeredVersion != currentVersion) {

            DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);

            DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationAppVersion(String.valueOf(currentVersion));

            return false;

        }

        return isRegistered;
    }

    /**
     * Get Application version registered by the OS.
     *
     * @return Application version registered by the OS.
     */
    private int getAppVersion() {
        try {

            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {

            DonkyException donkyException = new DonkyException("Could not get package name");
            donkyException.initCause(e);
            log.error("Could not get package name", donkyException);

            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}