package net.donky.core.network;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.events.NetworkStateChangedEvent;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.content.ContentNotification;
import net.donky.core.network.restapi.authentication.Login;
import net.donky.core.network.restapi.authentication.LoginResponse;
import net.donky.core.network.restapi.authentication.Register;
import net.donky.core.network.restapi.authentication.RegisterResponse;
import net.donky.core.network.restapi.secured.DeletePushConfigurationRequest;
import net.donky.core.network.restapi.secured.GetServerNotificationRequest;
import net.donky.core.network.restapi.secured.GetTags;
import net.donky.core.network.restapi.secured.Synchronise;
import net.donky.core.network.restapi.secured.SynchroniseResponse;
import net.donky.core.network.restapi.secured.UpdateClient;
import net.donky.core.network.restapi.secured.UpdateDevice;
import net.donky.core.network.restapi.secured.UpdatePushConfiguration;
import net.donky.core.network.restapi.secured.UpdateRegistration;
import net.donky.core.network.restapi.secured.UpdateTags;
import net.donky.core.network.restapi.secured.UpdateUser;
import net.donky.core.network.restapi.secured.UploadLog;
import net.donky.core.network.restapi.secured.UploadLogResponse;
import net.donky.core.observables.SubscriptionController;
import net.donky.core.settings.AppSettings;

import org.json.JSONException;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controller for all Core library network activities.
 * <p/>
 * Created by Marcin Swierczek
 * 16/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyNetworkController {

    /**
     * Indicates whether the device has network connectivity
     */
    public enum ConnectionType {
        NOT_CONNECTED,
        CELLULAR,
        WIFI
    }

    private ConnectionType connectionType;

    private final DLog log;

    private Context context;

    private String apiKey;

    private BroadcastReceiver connectivityChangesBroadcastReceiver;

    private final AtomicBoolean isNotificationExchangeInProgress;

    private final AtomicBoolean reRunNotificationExchange;

    private final AtomicLong lastNotificationExchangeTimestamp;

    private static final Object sharedLock = new Object();

    // Private constructor. Prevents instantiation from other classes.
    private DonkyNetworkController() {
        isNotificationExchangeInProgress = new AtomicBoolean(false);
        reRunNotificationExchange = new AtomicBoolean(false);
        lastNotificationExchangeTimestamp = new AtomicLong(System.currentTimeMillis());
        connectionType = ConnectionType.NOT_CONNECTED;
        log = new DLog("NetworkController");
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyNetworkController INSTANCE = new DonkyNetworkController();
    }

    /**
     * @return Instance of Network Controller singleton.
     */
    public static DonkyNetworkController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application, String apiKey) {
        this.context = application.getApplicationContext();
        this.apiKey = apiKey;
        registerForConnectivityChanges();
    }

    public long getLastSynchronisationTimestamp() {
        return lastNotificationExchangeTimestamp.get();
    }

    /**
     * Performs a notification synchronisation. This method is non-blocking.
     */
    public void synchronise() {

        if (DonkyAccountController.getInstance().isRegistered() && !DonkyAccountController.getInstance().isUserSuspended()) {

            synchronise(new DonkyListener() {

                @Override
                public void success() {
                    log.info("Synchronise finished successfully.");
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    log.error("Synchronise finished with error.", donkyException);
                }
            });

        } else if (DonkyAccountController.getInstance().isUserSuspended()) {
            log.warning("Cancel synchronisation. User suspended.");
        } else {
            log.warning("Cancel synchronisation. User not registered.");
        }
    }

    /**
     * Performs a notification synchronisation. This method is blocking and cannot be called from the main thread.
     * Mainly to be used by GCM Intent Service.
     */
    public void synchroniseSynchronously() {

        if (DonkyAccountController.getInstance().isRegistered() && !DonkyAccountController.getInstance().isUserSuspended()) {

            if (!isNotificationExchangeInProgress.get()) {

                synchronized (sharedLock) {
                    isNotificationExchangeInProgress.set(true);
                    reRunNotificationExchange.set(false);
                    sharedLock.notifyAll();
                }

                try {

                    final Synchronise synchroniseRequest = new Synchronise();

                    log.sensitive(synchroniseRequest.toString());

                    SynchroniseResponse result = null;

                    try {

                        result = synchroniseRequest.performSynchronous();

                        DonkyDataController.getInstance().getNotificationDAO().removeNotifications(synchroniseRequest.getClientNotifications());

                    } catch (DonkyException donkyException) {

                        log.error("Error performing synchronisation.", donkyException);

                        if (!(donkyException instanceof ConnectionException)) {

                            DonkyDataController.getInstance().getNotificationDAO().removeNotifications(synchroniseRequest.getClientNotifications());

                        }

                    }

                    if (result != null) {

                        log.sensitive(result.toString());

                        List<ClientNotification> clientNotifications = synchroniseRequest.getClientNotifications();

                        try {

                            processSynchronisationResponse(result);

                        } catch (Exception e) {
                            log.error("Error processing notification sync response.", e);
                        }

                        // Notify listeners for outbound/client notifications
                        for (ClientNotification notification : clientNotifications) {
                            SubscriptionController.getInstance().notifyAboutOutboundClientNotification(notification);
                        }

                        final Handler handler = new Handler();

                        final SynchroniseResponse finalResult = result;

                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                if (DonkyDataController.getInstance().getNotificationDAO().isNotificationPending() || finalResult.isMoreNotificationsAvailable() || reRunNotificationExchange.get()) {

                                    synchroniseSynchronously();

                                }
                            }
                        }, 3000);

                        synchronized (sharedLock) {
                            isNotificationExchangeInProgress.set(false);
                            sharedLock.notifyAll();
                        }

                    } else {

                        synchronized (sharedLock) {
                            isNotificationExchangeInProgress.set(false);
                            sharedLock.notifyAll();
                        }

                        log.error("Null synchronise response.");
                    }

                } catch (Exception e) {

                    synchronized (sharedLock) {
                        isNotificationExchangeInProgress.set(false);
                        sharedLock.notifyAll();
                    }

                    log.error("Error performing synchronisation synchronously", e);
                }
            }

        } else if (DonkyAccountController.getInstance().isUserSuspended()) {

            log.warning("Cancel synchronisation. User suspended. Trying to authenticate.");

            try {

                DonkyAccountController.getInstance().authenticate();

                if (!DonkyAccountController.getInstance().isUserSuspended()) {

                    log.info("User un-suspended. Authentication successful.");

                    synchroniseSynchronously();

                }

            } catch (DonkyException e) {

                log.warning("User suspended. Re-authentication failed.");

            }

        } else {
            log.warning("Cancel synchronisation. User not registered.");
        }
    }

    /**
     * Performs a notification synchronisation. This method is non-blocking.
     *
     * @param listener Callbacks are made during the Synchronise flow.
     */
    public void synchronise(final DonkyListener listener) {

        if (DonkyAccountController.getInstance().isRegistered() && !DonkyAccountController.getInstance().isUserSuspended()) {

            if (!isNotificationExchangeInProgress.get()) {

                synchronized (sharedLock) {
                    isNotificationExchangeInProgress.set(true);
                    reRunNotificationExchange.set(false);
                    sharedLock.notifyAll();
                }

                try {

                    final Synchronise synchroniseRequest = new Synchronise();

                    synchroniseRequest.performAsynchronous(new DonkyResultListener<SynchroniseResponse>() {

                        @Override
                        public void success(final SynchroniseResponse result) {

                            DonkyDataController.getInstance().getNotificationDAO().removeNotifications(synchroniseRequest.getClientNotifications());

                            if (result != null) {

                                log.sensitive(result.toString());

                                List<ClientNotification> clientNotifications = synchroniseRequest.getClientNotifications();

                                //Remove sent client notifications from the database.
                                DonkyDataController.getInstance().getNotificationDAO().removeNotifications(clientNotifications);

                                try {

                                    processSynchronisationResponse(result);

                                } catch (JSONException e) {

                                    DonkyException donkyException = new DonkyException("Error processing notification sync response.");
                                    donkyException.initCause(e);
                                    if (listener != null) {
                                        listener.error(donkyException, null);
                                    }
                                }

                                for (ClientNotification notification : clientNotifications) {
                                    SubscriptionController.getInstance().notifyAboutOutboundClientNotification(notification);
                                }

                                final Handler handler = new Handler();

                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {

                                        if (DonkyDataController.getInstance().getNotificationDAO().isNotificationPending() || result.isMoreNotificationsAvailable() || reRunNotificationExchange.get()) {

                                            synchronise(listener);

                                        }
                                    }
                                }, 3000);

                                synchronized (sharedLock) {
                                    isNotificationExchangeInProgress.set(false);
                                    sharedLock.notifyAll();
                                }

                            } else {

                                synchronized (sharedLock) {
                                    isNotificationExchangeInProgress.set(false);
                                    sharedLock.notifyAll();
                                }

                                log.error("Invalid synchronise response.");
                                DonkyException donkyException = new DonkyException("Invalid synchronise response.");
                                if (listener != null) {
                                    listener.error(donkyException, null);
                                }
                            }
                        }

                        @Override
                        public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                            if (!(donkyException instanceof ConnectionException)) {
                                DonkyDataController.getInstance().getNotificationDAO().removeNotifications(synchroniseRequest.getClientNotifications());
                            }

                            synchronized (sharedLock) {
                                isNotificationExchangeInProgress.set(false);
                                sharedLock.notifyAll();
                            }

                            if (listener != null) {
                                listener.error(donkyException, validationErrors);
                            }
                        }
                    });

                } catch (Exception e) {

                    synchronized (sharedLock) {
                        isNotificationExchangeInProgress.set(false);
                        sharedLock.notifyAll();
                    }

                    log.error("Error performing sync", e);
                }
            }

        } else if (DonkyAccountController.getInstance().isUserSuspended()) {

            log.warning("Cancel synchronisation. User suspended. Trying to authenticate.");

            DonkyAccountController.getInstance().authenticate(new DonkyListener() {

                @Override
                public void success() {

                    if (!DonkyAccountController.getInstance().isUserSuspended()) {

                        log.warning("User un-suspended. Authentication successful.");

                        synchronise(listener);

                    }
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    log.warning("User suspended. Re-authentication failed.");
                }
            });

        } else {
            log.warning("Cancel synchronisation. User not registered.");
        }
    }

    /**
     * Gets a specific server notification from the network
     *
     * @param notificationId The server notification id
     * @param listener       The downloaded notification (or null if not found)
     */
    public void getServerNotification(final String notificationId, final DonkyResultListener<ServerNotification> listener) {
        GetServerNotificationRequest request = new GetServerNotificationRequest(notificationId);
        request.performAsynchronous(listener);
    }

    /**
     * Gets a specific server notification from the network. Blocking version.
     *
     * @param notificationId The server notification id
     * @throws DonkyException
     */
    public ServerNotification getServerNotification(final String notificationId) throws DonkyException {
        GetServerNotificationRequest request = new GetServerNotificationRequest(notificationId);
        return request.performSynchronous();
    }

    /**
     * Dispatch received server notifications.
     *
     * @param response Network response from sync notifications call.
     * @throws JSONException
     */
    private void processSynchronisationResponse(SynchroniseResponse response) throws JSONException {

        if (response != null) {

            List<ServerNotification> serverNotifications = response.getServerNotifications();

            if (serverNotifications != null && !serverNotifications.isEmpty()) {
                SynchronisationHandler synchronisationHandler = new SynchronisationHandler(serverNotifications);
                synchronisationHandler.processServerNotifications();
            }

            List<SynchroniseResponse.FailedClientNotification> failedClientNotifications = response.getFailedClientNotifications();

            if (failedClientNotifications != null && !failedClientNotifications.isEmpty()) {

                StringBuilder sb = new StringBuilder();

                for (SynchroniseResponse.FailedClientNotification failedClientNotification : failedClientNotifications) {
                    sb.append('\n');
                    sb.append(failedClientNotification.toString());
                }

                log.error("Client notifications FAILED: " + sb.toString());
            }

        }
    }

    /**
     * Adds content notifications to the queue for submission to the network.
     *
     * @param contentNotifications Content notifications to send when next notification sync.
     */
    public void queueContentNotifications(List<ContentNotification> contentNotifications) {
        DonkyDataController.getInstance().getNotificationDAO().addContentNotifications(contentNotifications);
    }

    /**
     * Send content notifications via the Donky network immediately if possible.
     *
     * @param contentNotifications The notifications to send.
     * @param listener             The callback to invoke when the notifications has been sent.
     */
    public void sendContentNotifications(List<ContentNotification> contentNotifications, DonkyListener listener) {
        queueContentNotifications(contentNotifications);
        synchronise(listener);
    }

    /**
     * Send content notification via the Donky network immediately if possible.
     *
     * @param contentNotification The notification to send.
     * @param listener            The callback to invoke when the notifications has been sent.
     */
    public void sendContentNotification(ContentNotification contentNotification, DonkyListener listener) {
        List<ContentNotification> list = new LinkedList<>();
        list.add(contentNotification);
        queueContentNotifications(list);
        synchronise(listener);
    }

    /**
     * Adds client notifications to the queue for submission to the network.
     *
     * @param clientNotifications Client notifications to send when next notification sync.
     */
    public void queueClientNotifications(List<ClientNotification> clientNotifications) {
        DonkyDataController.getInstance().getNotificationDAO().addNotifications(clientNotifications);
    }

    /**
     * Adds client notification to the queue for submission to the network.
     *
     * @param clientNotification Client notification to send when next notification sync.
     */
    public void queueClientNotification(ClientNotification clientNotification) {
        DonkyDataController.getInstance().getNotificationDAO().addNotification(clientNotification);
    }

    /**
     * Send notifications via the Donky network immediately if possible.
     *
     * @param clientNotifications The notification to send.
     * @param listener            The callback to invoke when the notifications has been sent.
     */
    public void sendClientNotifications(List<ClientNotification> clientNotifications, DonkyListener listener) {
        queueClientNotifications(clientNotifications);
        synchronise(listener);
    }

    /**
     * Send notification via the Donky network immediately if possible.
     *
     * @param clientNotification The notification to send.
     * @param listener           The callback to invoke when the notifications has been sent.
     */
    public void sendClientNotification(ClientNotification clientNotification, DonkyListener listener) {
        queueClientNotification(clientNotification);
        synchronise(listener);
    }

    /**
     * Login to Donky Network. This is blocking method.
     *
     * @param loginRequest Login request to be send to Donky Network.
     * @return Login response from Donky Network.
     * @throws DonkyException
     */
    public LoginResponse loginToNetwork(Login loginRequest) throws DonkyException {
        log.sensitive(loginRequest.toString());
        return loginRequest.performSynchronous(apiKey);
    }

    /**
     * Login to Donky Network. This is non-blocking method.
     *
     * @param loginRequest Login request to be send to Donky Network.
     * @param listener     Callback to be invoked when authenticate finish.
     */
    public void loginToNetwork(final Login loginRequest, final DonkyResultListener<LoginResponse> listener) {
        log.sensitive(loginRequest.toString());
        loginRequest.performAsynchronous(apiKey, listener);
    }

    /**
     * Register to Donky Network. This is blocking method.
     *
     * @param registerRequest Register request to be send to Donky Network.
     */
    public RegisterResponse registerToNetwork(final Register registerRequest) throws DonkyException {
        return registerRequest.performSynchronous(apiKey);
    }

    /**
     * Register to Donky Network. This is non-blocking method.
     *
     * @param registerRequest Register request to be send to Donky Network.
     * @param listener        Callback to be invoked when register finish.
     */
    public void registerToNetwork(final Register registerRequest, final DonkyResultListener<RegisterResponse> listener) {
        registerRequest.performAsynchronous(apiKey, listener);
    }

    /**
     * Update user details on the Network.
     *
     * @param updateUserRequest Network request to be executed.
     * @param listener          The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void updateUserOnNetwork(final UpdateUser updateUserRequest, final DonkyListener listener) {

        updateUserRequest.performAsynchronous(new DonkyResultListener<Void>() {

            @Override
            public void success(Void result) {
                if (listener != null) {
                    listener.success();
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }

    /**
     * Update device details on the Network.
     *
     * @param updateDeviceRequest Network request to be executed.
     * @param listener            The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void updateDeviceOnNetwork(final UpdateDevice updateDeviceRequest, final DonkyListener listener) {

        updateDeviceRequest.performAsynchronous(new DonkyResultListener<Void>() {

            @Override
            public void success(Void result) {
                if (listener != null) {
                    listener.success();
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }

    /**
     * Update client details on the Network.
     *
     * @param updateClientRequest Network request to be executed.
     * @param listener            The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void updateClientOnNetwork(final UpdateClient updateClientRequest, final DonkyListener listener) {

        updateClientRequest.performAsynchronous(new DonkyResultListener<Void>() {

            @Override
            public void success(Void result) {
                if (listener != null) {
                    listener.success();
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }

    public void updateRegistrationOnNetwork(final UpdateRegistration request, final DonkyListener listener) {

        request.performAsynchronous(new DonkyResultListener<Void>() {
            @Override
            public void success(Void result) {
                if (listener != null) {
                    listener.success();
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }


    /**
     * Update push chanel (GCM) details on the Network.
     *
     * @param updatePushConfiguration Network request to be executed.
     * @param listener                The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void updatePushConfigurationOnNetwork(final UpdatePushConfiguration updatePushConfiguration, final DonkyListener listener) {

        updatePushConfiguration.performAsynchronous(new DonkyResultListener<Void>() {

            @Override
            public void success(Void result) {
                if (listener != null) {
                    listener.success();
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }

    /**
     * Delete the push chanel (GCM) details on the network.
     *
     * @param listener The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void deletePushConfigurationOnNetwork(final DonkyListener listener) {

        DeletePushConfigurationRequest request = new DeletePushConfigurationRequest();
        request.performAsynchronous(new DonkyResultListener<Void>() {

            @Override
            public void success(Void result) {
                if (listener != null) {
                    listener.success();
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }

    /**
     * Get the list of tags from Donky Network and information if they are selected by the user.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void getTags(DonkyResultListener<List<TagDescription>> listener) {

        GetTags request = new GetTags();

        request.performAsynchronous(listener);

    }

    /**
     * Updates the list of tags selected by the user on Donky Network.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void updateTags(List<TagDescription> tags, final DonkyListener listener) {

        UpdateTags request = new UpdateTags(tags);

        request.performAsynchronous(new DonkyResultListener<Void>() {

            @Override
            public void success(Void result) {

                if (listener != null) {
                    listener.success();
                }

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }

            }

        });

    }

    /**
     * Uploads the current debug log to the Donky Network.
     *
     * @param log      Log to be uploaded.
     * @param reason   Reason for the upload.
     * @param listener The callback to invoke when the notification has been sent.
     */
    public void submitLog(String log, UploadLog.SubmissionReason reason, final DonkyResultListener listener) {

        UploadLog request = new UploadLog(log, reason);

        request.performAsynchronous(new DonkyResultListener<UploadLogResponse>() {

            @Override
            public void success(UploadLogResponse result) {
                if (listener != null) {
                    listener.success(result);
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }

    /**
     * @return True if internet connection is available.
     */
    public synchronized boolean isInternetConnectionAvailable() {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {

                connectionType = ConnectionType.WIFI;
                return true;

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                connectionType = ConnectionType.CELLULAR;
                return true;

            } else {

                connectionType = ConnectionType.NOT_CONNECTED;
                return false;

            }
        }
        return false;
    }

    /**
     * Register receiver for internet connectivity changes.
     * All {@link OnConnectionListener#onConnected()} callbacks will be invoked when internet connection was lost and restored.
     */
    private void registerForConnectivityChanges() {

        if (connectivityChangesBroadcastReceiver == null) {

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            if (connectivityChangesBroadcastReceiver != null) {

                context.unregisterReceiver(connectivityChangesBroadcastReceiver);
            }
            connectivityChangesBroadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                        boolean isNetworkConnected = isInternetConnectionAvailable();

                        if (isNetworkConnected) {

                            OnConnectionListener.notifyAllConnectionListeners();

                            DonkyCore.publishLocalEvent(new NetworkStateChangedEvent(isNetworkConnected, connectionType));

                            if (lastNotificationExchangeTimestamp.get() > AppSettings.getInstance().getSyncDelaySeconds() &&
                                    DonkyDataController.getInstance().getNotificationDAO().isNotificationPending()) {
                                synchronise();
                            }
                        }
                    }
                }
            };
            context.registerReceiver(connectivityChangesBroadcastReceiver, filter);
        }
    }

    /**
     * Register receiver for internet connectivity changes.
     * None of the {@link OnConnectionListener#onConnected()} callbacks will be invoked any more.
     */
    private void unRegisterForConnectivityChanges() {

        if (connectivityChangesBroadcastReceiver != null && context != null) {

            context.unregisterReceiver(connectivityChangesBroadcastReceiver);
            connectivityChangesBroadcastReceiver = null;
        }
    }

    /**
     * @return True if authentication token is still valid.
     */
    public boolean isAuthenticationTokenValid() {

        String tokenExpiry = DonkyDataController.getInstance().getConfigurationDAO().getTokenExpiry();

        if (tokenExpiry != null) {

            Date date = DateAndTimeHelper.parseUtcDate(tokenExpiry);

            if (date != null) {

                if (date.getTime() > System.currentTimeMillis()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {

        super.finalize();
        unRegisterForConnectivityChanges();
    }

    /**
     * Crate authorization header.
     *
     * @return Authorization header.
     */
    public String getAuthorization() {
        return DonkyDataController.getInstance().getConfigurationDAO().getTokenType() + " " + DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken();
    }

    public boolean isNotificationsSyncInProgress() {
        return isNotificationExchangeInProgress.get();
    }

    public void setReRunNotificationExchange(boolean reRun) {
        reRunNotificationExchange.set(reRun);
    }
}
