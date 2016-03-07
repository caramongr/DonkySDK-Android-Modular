package net.donky.core.gcm;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.restapi.secured.UpdatePushConfiguration;
import net.donky.core.settings.AppSettings;

import java.io.IOException;

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

            int connectionStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

            if (connectionStatus == ConnectionResult.SUCCESS) {

                log.info("Registering to GCM");

                doRegisterGCM(listener);

            } else {

                if (GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatus)) {
                    log.warning("Google Play Services is probably not up to date. User recoverable Error Code is " + connectionStatus);
                } else {
                    log.warning("This device is not supported by Google Play Services.");
                }

                if (listener != null) {
                    DonkyException donkyException = new DonkyException("Google Play Services not available. Connection Status Code " + connectionStatus);
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
     * Register to Google Cloud Services. This will allow app to receive push messages. Registration process will be moved to background if this method will be called from main thread.
     */
    public void registerPush() throws DonkyException {

        if (DonkyCore.isInitialised() && !isRegisteredToGCM()) {

            int connectionStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

            if (connectionStatus == ConnectionResult.SUCCESS) {

                log.info("Registering to GCM");

                doRegisterGCM();

            } else {

                if (GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatus)) {
                    log.warning("Google Play Services is probably not up to date. User recoverable Error Code is " + connectionStatus);
                } else {
                    log.warning("This device is not supported by Google Play Services.");
                }

                throw new DonkyException("Google Play Services not available. Connection Status Code " + connectionStatus);
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

            DonkyCore.getInstance().processInBackground(new Runnable() {

                @Override
                public void run() {

                    DonkyException donkyException = null;

                    boolean isRegistered = !TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationId());

                    String senderId = getSenderId();

                    if (isRegistered) {

                        if (!TextUtils.isEmpty(senderId)) {

                            try {
                                InstanceID.getInstance(context).deleteToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                            } catch (IOException e) {
                                donkyException = new DonkyException("Cannot delete GCM token from instanceID.");
                                donkyException.initCause(e);
                            }

                        } else {
                            donkyException = new DonkyException("GCM SenderId not found.");
                        }
                    }

                    if (DonkyAccountController.getInstance().isRegistered()) {
                        try {
                            DonkyNetworkController.getInstance().deletePushConfigurationOnNetwork();
                        } catch (DonkyException e) {
                            donkyException = new DonkyException("Error deleting GCM configuration on the network.");
                            donkyException.initCause(e);
                        }
                    }

                    if (listener != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        if (donkyException == null) {
                            DonkyCore.getInstance().postSuccess(handler, listener);
                        } else {
                            DonkyCore.getInstance().postError(handler, listener, donkyException);
                        }
                    }
                }
            });
        }
    }

    /**
     * Check if SDK has already registered to Google Cloud Messaging.
     *
     * @return True if already registered to Google Cloud Messaging.
     */
    public boolean isRegisteredToGCM() {

        /**
         * Check if instance id {@link InstanceID} was deleted so that tokens need to be recreated.
         */
        String currentInstanceId = InstanceID.getInstance(context).getId();
        String oldInstanceId = DonkyDataController.getInstance().getConfigurationDAO().getInstanceId();
        if (oldInstanceId == null || !oldInstanceId.equals(currentInstanceId)) {
            DonkyDataController.getInstance().getConfigurationDAO().setInstanceId(currentInstanceId);
            DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);
            return false;
        }

        /**
         * Check if GCM registration token is missing in DB
         */
        boolean isRegistered = !TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationId());

        /**
         * Check if App version changed
         */
        String appVersion = DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationAppVersion();

        int currentVersion = getAppVersion();

        Integer registeredVersion;

        try {
            registeredVersion = Integer.parseInt(appVersion);
        } catch (Exception exception) {
            registeredVersion = null;
        }

        if (registeredVersion == null || registeredVersion != currentVersion) {

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

            return -1;
        }
    }

    private String getSenderId() {

        String senderId = AppSettings.getInstance().getGcmSenderId();

        if (TextUtils.isEmpty(senderId)) {

            senderId = DonkyDataController.getInstance().getConfigurationDAO().getGcmSenderId();

            if (TextUtils.isEmpty(senderId)) {
                new DLog("DonkyGCMRegistrationService").warning("GCM sender id ");
                return null;
            }
        }

        return senderId;

    }

    private String getRegistrationToken() {

        String senderId = getSenderId();

        if (!TextUtils.isEmpty(senderId)) {
            InstanceID instanceID = InstanceID.getInstance(context);

            String token = null;

            try {
                token = instanceID.getToken(senderId,
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            } catch (IOException exception) {
                new DLog("DonkyGCMRegistrationService").error("Error obtaining push GCM registration token.", exception);
            }

            return token;
        }

        return null;
    }

    private void doRegisterGCM() {

        final String token = getRegistrationToken();

        DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(token);

        if (DonkyAccountController.getInstance().isRegistered()) {
            try {
                if (!TextUtils.isEmpty(token)) {
                    DonkyNetworkController.getInstance().updatePushConfigurationOnNetwork(new UpdatePushConfiguration(token));
                } else {
                    DonkyNetworkController.getInstance().deletePushConfigurationOnNetwork();
                }
            } catch (DonkyException exception) {
                new DLog("DonkyGCMRegistrationService").error("Error updating push configuration on the network.", exception);
            }
        }
    }

    private void doRegisterGCM(DonkyListener listener) {

        final String token = getRegistrationToken();

        DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(token);

        if (DonkyAccountController.getInstance().isRegistered()) {
            if (!TextUtils.isEmpty(token)) {
                DonkyNetworkController.getInstance().updatePushConfigurationOnNetwork(new UpdatePushConfiguration(token), listener);
            } else {
                DonkyNetworkController.getInstance().deletePushConfigurationOnNetwork(listener);
            }
        }
    }

}