package net.donky.core.network;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.JsonObject;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.events.NetworkStateChangedEvent;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.logging.DLog;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.assets.AssetType;
import net.donky.core.network.content.ContentNotification;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;
import net.donky.core.network.restapi.authentication.Login;
import net.donky.core.network.restapi.authentication.LoginAuth;
import net.donky.core.network.restapi.authentication.LoginResponse;
import net.donky.core.network.restapi.authentication.Register;
import net.donky.core.network.restapi.authentication.RegisterAuth;
import net.donky.core.network.restapi.authentication.RegisterResponse;
import net.donky.core.network.restapi.authentication.StartAuth;
import net.donky.core.network.restapi.authentication.StartAuthResponse;
import net.donky.core.network.restapi.secured.DeletePushConfigurationRequest;
import net.donky.core.network.restapi.secured.GetAllGeoFence;
import net.donky.core.network.restapi.secured.GetAllTriggers;
import net.donky.core.network.restapi.secured.GetContacts;
import net.donky.core.network.restapi.secured.GetConversationsHistory;
import net.donky.core.network.restapi.secured.GetMessagesHistory;
import net.donky.core.network.restapi.secured.GetPlatformUsersRequest;
import net.donky.core.network.restapi.secured.GetServerNotificationRequest;
import net.donky.core.network.restapi.secured.GetTags;
import net.donky.core.network.restapi.secured.IsValidPlatformUserRequest;
import net.donky.core.network.restapi.secured.IsValidPlatformUserResponse;
import net.donky.core.network.restapi.secured.Synchronise;
import net.donky.core.network.restapi.secured.SynchroniseResponse;
import net.donky.core.network.restapi.secured.UpdateClient;
import net.donky.core.network.restapi.secured.UpdateDevice;
import net.donky.core.network.restapi.secured.UpdatePushConfiguration;
import net.donky.core.network.restapi.secured.UpdateRegistration;
import net.donky.core.network.restapi.secured.UpdateTags;
import net.donky.core.network.restapi.secured.UpdateUser;
import net.donky.core.network.restapi.secured.UploadAsset;
import net.donky.core.network.restapi.secured.UploadAssetResponse;
import net.donky.core.network.restapi.secured.UploadLog;
import net.donky.core.network.restapi.secured.UploadLogResponse;
import net.donky.core.network.signalr.SignalRController;
import net.donky.core.observables.SubscriptionController;
import net.donky.core.settings.AppSettings;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controller for all Core library network activities.
 *
 * Created by Marcin Swierczek
 * 16/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyNetworkController {

    private final SynchronisationManager synchronisationManager;

    private SignalRController signalRController;

    private Handler mainThreadHandler;

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

    private final ArrayDeque<String> lastNotificationsBuffer;

    // Private constructor. Prevents instantiation from other classes.
    private DonkyNetworkController() {
        log = new DLog("NetworkController");
        synchronisationManager = new SynchronisationManager();
        connectionType = ConnectionType.NOT_CONNECTED;
        mainThreadHandler = new Handler(Looper.getMainLooper());
        lastNotificationsBuffer = new ArrayDeque<>();
    }

    /**
     * Initializes singleton.
     *
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
        signalRController = (SignalRController) DonkyCore.getInstance().getService(SignalRController.SERVICE_NAME);
        registerForConnectivityChanges();
    }

    /**
     * Should the signalR channel be used instead of REST
     *
     * @return True if signalR channel should be used.
     */
    public synchronized boolean shouldUseSignalRChannel() {
        return signalRController != null && LifeCycleObserver.getInstance().isApplicationForegrounded();
    }

    public void startSignalR() {
        if (signalRController != null) {
            signalRController.startSignalR();
        }
    }

    public long getLastSynchronisationTimestamp() {
        return synchronisationManager.lastNotificationExchangeTimestamp.get();
    }

    /**
     * Performs a notification synchronisation. This method is non-blocking.
     */
    public void synchronise() {
        synchronisationManager.synchronise(null);
    }

    public void synchronise(DonkyListener donkyListener) {
        synchronisationManager.synchronise(donkyListener);
    }

    public void synchroniseSynchronously() {
        synchronisationManager.synchroniseSynchronously();
    }

    /**
     * Gets a specific server notification from the network
     *
     * @param notificationId The server notification id
     * @param listener       The downloaded notification (or null if not found)
     */
    public void getServerNotification(final String notificationId, final DonkyResultListener<ServerNotification> listener) {
        GetServerNotificationRequest request = new GetServerNotificationRequest(notificationId);
        request.performAsynchronous(new DonkyResultListener<ServerNotification>() {
            @Override
            public void success(final ServerNotification result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });
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

    private Integer getCustomContentMaxSizeBytes() {

        String customContentMaxSizeBytes = DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().get(ConfigurationDAO.KEY_CONFIGURATION_CustomContentMaxSizeBytes);

        if (!TextUtils.isEmpty(customContentMaxSizeBytes)) {

            return Integer.parseInt(customContentMaxSizeBytes);

        }

        return null;
    }

    private boolean isContentNotificationRespectingSizeLimit(Integer customContentMaxSizeBytes, ContentNotification contentNotification) {

        final String json = contentNotification.getJsonString();

        if (!TextUtils.isEmpty(json)) {

            int size = json.getBytes().length;

            if (customContentMaxSizeBytes != null && customContentMaxSizeBytes < size) {

                return false;

            }
        }

        return true;
    }

    /**
     * Adds content notification to the queue for submission to the network.
     *
     * @param contentNotification Content notification to send when next notification sync.
     * @deprecated Please use {@link DonkyNetworkController#queueContentNotification(net.donky.core.network.content.ContentNotification)} or {@link DonkyNetworkController#queueContentNotifications(java.util.List)} instead.
     */
    @Deprecated
    public void queueContentNotifications(ContentNotification contentNotification) {
        List<ContentNotification> list = new LinkedList<>();
        list.add(contentNotification);
        queueContentNotifications(list);
    }

    /**
     * Adds content notifications to the queue for submission to the network. Notification should conform to size limit configured on the network [256K by default].
     *
     * @param contentNotifications Content notifications to send when next notification sync.
     * @return List of failed Content Notifications with a reason of failure. Null if none failed.
     */
    public ValidationResult<ContentNotification> queueContentNotifications(List<ContentNotification> contentNotifications) {

        ValidationResult validationResult = new ValidationResult<>();

        Integer sizeLimit = getCustomContentMaxSizeBytes();

        if (sizeLimit != null) {

            if (DonkyAccountController.getInstance().isRegistered()) {
                for (ContentNotification notification : contentNotifications) {

                    if (isContentNotificationRespectingSizeLimit(sizeLimit, notification)) {
                        DonkyDataController.getInstance().getNotificationDAO().addContentNotification(notification);
                    } else {
                        validationResult.addFailure(notification, ValidationResult.REASON_SIZE_LIMIT_EXCEEDED);
                    }
                }
            } else {
                log.warning("User not registered. Content notifications will not be queued.");
            }

        } else {

            DonkyDataController.getInstance().getNotificationDAO().addContentNotifications(contentNotifications);

        }

        return validationResult;
    }

    /**
     * Adds content notification to the queue for submission to the network.
     *
     * @param contentNotification Content notifications to send when next notification sync.
     */
    public ValidationResult<ContentNotification> queueContentNotification(ContentNotification contentNotification) {

        ValidationResult validationResult = new ValidationResult<>();

        if (contentNotification != null) {

            if (isContentNotificationRespectingSizeLimit(getCustomContentMaxSizeBytes(), contentNotification)) {

                if (DonkyAccountController.getInstance().isRegistered()) {
                    DonkyDataController.getInstance().getNotificationDAO().addContentNotification(contentNotification);
                } else {
                    log.warning("User not registered. Content notifications will not be queued.");
                }

            } else {

                validationResult.addFailure(contentNotification, ValidationResult.REASON_SIZE_LIMIT_EXCEEDED);

            }
        }

        return validationResult;
    }

    /**
     * Send content notifications via the Donky network immediately if possible.
     *
     * @param contentNotifications The notifications to send.
     * @param listener             The callback to invoke when the notifications has been sent.
     */
    public void sendContentNotifications(List<ContentNotification> contentNotifications, DonkyListener listener) {

        List<Pair<ContentNotification, String>> rejected = queueContentNotifications(contentNotifications).getFailures();

        if (!rejected.isEmpty()) {

            HashMap<String, String> validationFailures = new HashMap<>();

            for (Pair<ContentNotification, String> pair : rejected) {
                validationFailures.put(pair.first.getId(), pair.second);
            }

            postError(null, validationFailures, listener);
        }

        if (contentNotifications.size() - rejected.size() > 0) {
            synchronise(listener);
        }
    }

    /**
     * Send content notification via the Donky network immediately if possible.
     *
     * @param contentNotification The notification to send.
     * @param listener            The callback to invoke when the notifications has been sent.
     */
    public void sendContentNotification(ContentNotification contentNotification, DonkyListener listener) {

        List<Pair<ContentNotification, String>> rejected = queueContentNotification(contentNotification).getFailures();

        if (!rejected.isEmpty()) {

            Map validationFailures = new HashMap<String, String>();

            validationFailures.put(rejected.get(0).first.getId(), rejected.get(0).second);

            postError(null, validationFailures, listener);

        } else {
            synchronise(listener);
        }
    }

    /**
     * Adds client notifications to the queue for submission to the network.
     *
     * @param clientNotifications Client notifications to send when next notification sync.
     */
    public void queueClientNotifications(List<ClientNotification> clientNotifications) {
        if (DonkyAccountController.getInstance().isRegistered()) {
            DonkyDataController.getInstance().getNotificationDAO().addNotifications(clientNotifications);
        } else {
            log.warning("User not registered. Client notifications will not be queued.");
        }
    }

    /**
     * Adds client notification to the queue for submission to the network.
     *
     * @param clientNotification Client notification to send when next notification sync.
     */
    public void queueClientNotification(ClientNotification clientNotification) {
        if (DonkyAccountController.getInstance().isRegistered()) {
            DonkyDataController.getInstance().getNotificationDAO().addNotification(clientNotification);
        } else {
            log.warning("User not registered. Client notification will not be queued.");
        }

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
        try {
            return loginRequest.performSynchronous(apiKey);
        } catch (DonkyException donkyException) {
            if (signalRController != null) {
                signalRController.stopSignalR();
            }
            throw donkyException;
        }
    }

    /**
     * Login to Donky Network. This is non-blocking method.
     *
     * @param loginRequest Login request to be send to Donky Network.
     * @param listener     Callback to be invoked when authenticate finish.
     */
    public void loginToNetwork(final Login loginRequest, final DonkyResultListener<LoginResponse> listener) {
        log.sensitive(loginRequest.toString());
        loginRequest.performAsynchronous(apiKey, new DonkyResultListener<LoginResponse>() {
            @Override
            public void success(final LoginResponse result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (signalRController != null) {
                    signalRController.stopSignalR();
                }
                postError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Login to Donky Network. This is non-blocking method.
     *
     * @param loginRequest Login request to be send to Donky Network.
     * @param listener     Callback to be invoked when authenticate finish.
     */
    public void loginToNetwork(final LoginAuth loginRequest, final DonkyResultListener<LoginResponse> listener) {
        log.sensitive(loginRequest.toString());
        loginRequest.performAsynchronous(apiKey, new DonkyResultListener<LoginResponse>() {
            @Override
            public void success(final LoginResponse result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (signalRController != null) {
                    signalRController.stopSignalR();
                }
                postError(donkyException, validationErrors, listener);
            }
        });
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
     * Register to Donky Network. This is blocking method.
     *
     * @param registerRequest Register request to be send to Donky Network.
     */
    public RegisterResponse registerToNetwork(final RegisterAuth registerRequest) throws DonkyException {
        return registerRequest.performSynchronous(apiKey);
    }

    /**
     * Register to Donky Network. This is non-blocking method.
     *
     * @param registerRequest Register request to be send to Donky Network.
     * @param listener        Callback to be invoked when register finish.
     */
    public void registerToNetwork(final Register registerRequest, final DonkyResultListener<RegisterResponse> listener) {
        registerRequest.performAsynchronous(apiKey, new DonkyResultListener<RegisterResponse>() {
            @Override
            public void success(final RegisterResponse result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Register to Donky Network with authenticated user. This is non-blocking method.
     *
     * @param registerRequest Register request to be send to Donky Network.
     * @param listener        Callback to be invoked when register finish.
     */
    public void registerToNetwork(final RegisterAuth registerRequest, final DonkyResultListener<RegisterResponse> listener) {
        registerRequest.performAsynchronous(apiKey, new DonkyResultListener<RegisterResponse>() {
            @Override
            public void success(final RegisterResponse result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Get the nonce and correlationId from the network.
     *
     * @param donkyResultListener Callback with authentication challenge details.
     */
    public void startAuthentication(final StartAuth startAuth, final DonkyResultListener<StartAuthResponse> donkyResultListener) {
        startAuth.performAsynchronous(apiKey, donkyResultListener);
    }

    /**
     * Get the nonce and correlationId from the network.
     */
    public StartAuthResponse startAuthentication(final StartAuth startAuth) throws DonkyException {
        return startAuth.performSynchronous(apiKey);
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
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
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
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
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
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });
    }

    public void updateRegistrationOnNetwork(final UpdateRegistration request, final DonkyListener listener) {

        request.performAsynchronous(new DonkyResultListener<Void>() {
            @Override
            public void success(Void result) {
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
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
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Update push chanel (GCM) details on the Network. Blocking version.
     *
     * @param updatePushConfiguration Network request to be executed.
     */
    public void updatePushConfigurationOnNetwork(final UpdatePushConfiguration updatePushConfiguration) throws DonkyException {
        updatePushConfiguration.performSynchronous();
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
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Delete the push chanel (GCM) details on the network. Blocking version.
     */
    public void deletePushConfigurationOnNetwork() throws DonkyException {
        DeletePushConfigurationRequest request = new DeletePushConfigurationRequest();
        request.performSynchronous();
    }

    /**
     * Get the list of tags from Donky Network and information if they are selected by the user.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void getTags(final DonkyResultListener<List<TagDescription>> listener) {

        GetTags request = new GetTags();
        request.performAsynchronous(new DonkyResultListener<List<TagDescription>>() {
            @Override
            public void success(final List<TagDescription> result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
            }
        });

    }

    /**
     * Get the list of all geo fence trigger from Donky Network.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void getAllGeoFences(DonkyResultListener<List<GeoFence>> listener) {

        GetAllGeoFence request = new GetAllGeoFence();

        request.performAsynchronous(listener);

    }

    /**
     * Get the list of all active trigger from Donky Network.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void getAllTriggers(DonkyResultListener<List<Trigger>> listener) {

        GetAllTriggers request = new GetAllTriggers();

        request.performAsynchronous(listener);

    }

    /**
     * Checks on the network is platform user id is valid.
     *
     * @param externalUserId External user id to check if user with that id has been registered on the same App Space.
     */
    public void isPlatformUserIdValid(final String externalUserId, final DonkyResultListener<IsValidPlatformUserResponse> donkyResultListener) {
        IsValidPlatformUserRequest request = new IsValidPlatformUserRequest(externalUserId);
        request.performAsynchronous(new DonkyResultListener<IsValidPlatformUserResponse>() {
            @Override
            public void success(final IsValidPlatformUserResponse result) {
                if (donkyResultListener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            donkyResultListener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, donkyResultListener);
            }
        });
    }

    @Deprecated
    public void getMessagesHistory(final String conversationId, String direction, String dateTime, int count, final DonkyResultListener<List<JsonObject>> donkyResultListener) {

        if (TextUtils.isEmpty(conversationId) || TextUtils.isEmpty(direction) || TextUtils.isEmpty(dateTime) || count < 0) {
            postError(new DonkyException("Wrong arguments provided."), null, donkyResultListener);
            return;
        }

        String query = "message?direction=" + direction + "&time=" + dateTime + "&count=" + count;

        GetMessagesHistory request = new GetMessagesHistory(conversationId, query);

        request.performAsynchronous(new DonkyResultListener<List<JsonObject>>() {
            @Override
            public void success(final List<JsonObject> result) {
                if (donkyResultListener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            donkyResultListener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, donkyResultListener);
            }
        });
    }

    @Deprecated
    public void getConversationsHistory(final DonkyResultListener<List<JsonObject>> donkyResultListener) {
        getConversationsHistory(null, donkyResultListener);
    }

    @Deprecated
    public void getConversationsHistory(final List<String> conversationIds, final DonkyResultListener<List<JsonObject>> donkyResultListener) {

        String query = "conversation";

        if (conversationIds != null && !conversationIds.isEmpty()) {
            for (int i = 0; i < conversationIds.size(); i++) {
                query += (i == 0 ? "?" : "&") + "id=" + conversationIds.get(i);
            }
        }

        GetConversationsHistory request = new GetConversationsHistory(query);

        request.performAsynchronous(new DonkyResultListener<List<JsonObject>>() {
            @Override
            public void success(final List<JsonObject> result) {
                if (donkyResultListener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            donkyResultListener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, donkyResultListener);
            }
        });
    }

    @Deprecated
    public void getContacts(final List<String> profileIds, final DonkyResultListener<List<JsonObject>> donkyResultListener) {

        String query = "contact";

        if (profileIds != null && !profileIds.isEmpty()) {
            for (int i = 0; i < profileIds.size(); i++) {
                query += (i == 0 ? "?" : "&") + "profileId=" + profileIds.get(i);
            }
        }

        GetContacts request = new GetContacts(query);

        request.performAsynchronous(new DonkyResultListener<List<JsonObject>>() {
            @Override
            public void success(final List<JsonObject> result) {
                if (donkyResultListener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            donkyResultListener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, donkyResultListener);
            }
        });
    }

    @Deprecated
    public List<JsonObject> getContacts(final List<String> profileIds) {

        String query = "contact";

        if (profileIds != null && !profileIds.isEmpty()) {
            for (int i = 0; i < profileIds.size(); i++) {
                query += (i == 0 ? "?" : "&") + "profileId=" + profileIds.get(i);
            }
        }

        GetContacts request = new GetContacts(query);

        try {
            return request.performSynchronous();
        } catch (DonkyException exception) {
            DonkyException donkyException = new DonkyException(exception.getLocalizedMessage());
            donkyException.initCause(exception);
            log.error("Error querying contacts on the network");
            return null;
        }
    }

    @Deprecated
    public List<JsonObject> getContact(final String profileId) {

        String query;
        query = "contact?profileId=" + profileId;

        GetContacts request = new GetContacts(query);

        try {
            return request.performSynchronous();
        } catch (DonkyException exception) {
            DonkyException donkyException = new DonkyException(exception.getLocalizedMessage());
            donkyException.initCause(exception);
            log.error("Error querying contacts on the network");
            return null;
        }
    }

    /**
     * Checks on the network is platform user id is valid.
     *
     * @param phoneNumbers        Mobile phone numbers to check against users registered on the same App Space.
     * @param emailList           Emails to check against users registered on the same app space.
     * @param donkyResultListener Callback with the result containing discovered contacts on the network.
     */
    @Deprecated
    public void discoverUsersOnTheNetwork(final List<String> phoneNumbers, List<String> emailList, final DonkyResultListener<List<DiscoveredContact>> donkyResultListener) {
        GetPlatformUsersRequest request = new GetPlatformUsersRequest(phoneNumbers, emailList);
        request.performAsynchronous(new DonkyResultListener<List<DiscoveredContact>>() {
            @Override
            public void success(final List<DiscoveredContact> result) {
                if (donkyResultListener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            donkyResultListener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, donkyResultListener);
            }
        });
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
                postSuccess(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
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
    public void submitLog(String log, UploadLog.SubmissionReason reason, final DonkyResultListener<UploadLogResponse> listener) {

        UploadLog request = new UploadLog(log, reason);

        request.performAsynchronous(new DonkyResultListener<UploadLogResponse>() {

            @Override
            public void success(final UploadLogResponse result) {
                if (listener != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(result);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                postError(donkyException, validationErrors, listener);
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
     * Upload asset to Donky Network.
     *
     * @param listener Callback to be invoked when completed. Result contains id of uploaded asset.
     */
    public void uploadAsset(AssetType assetType, String mimeType, File asset, DonkyResultListener<UploadAssetResponse> listener) {
        UploadAsset request = new UploadAsset(assetType, mimeType, asset);
        request.performAsynchronous(listener);
    }

    /**
     * Upload asset to Donky Network.
     *
     * @return Asset ID
     */
    public UploadAssetResponse uploadAsset(AssetType assetType, String mimeType, File asset) throws DonkyException {
        UploadAsset request = new UploadAsset(assetType, mimeType, asset);
        return request.performSynchronous();
    }

    /**
     * Upload asset to Donky Network.
     *
     * @param listener Callback to be invoked when completed. Result contains id of uploaded asset.
     */
    public void uploadAsset(AssetType assetType, String mimeType, byte[] asset, DonkyResultListener<UploadAssetResponse> listener) {
        UploadAsset request = new UploadAsset(assetType, mimeType, asset);
        request.performAsynchronous(listener);
    }

    /**
     * Upload asset to Donky Network.
     *
     * @return Asset ID
     */
    public UploadAssetResponse uploadAsset(AssetType assetType, String mimeType, byte[] asset) throws DonkyException {
        UploadAsset request = new UploadAsset(assetType, mimeType, asset);
        return request.performSynchronous();
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

                            if (synchronisationManager.getLastNotificationExchangeTimestamp() > AppSettings.getInstance().getSyncDelaySeconds() &&
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
     * @return True if authentication token is still valid.
     */
    public boolean isAuthenticationTokenValid() {

        String tokenExpiry = DonkyDataController.getInstance().getConfigurationDAO().getTokenExpiry();

        if (tokenExpiry != null) {

            Date expireDate = DateAndTimeHelper.parseUtcDate(tokenExpiry);

            if (expireDate != null) {

                if (expireDate.after(new Date())) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean shouldIgnoreServerNotification(String id) {

        if (lastNotificationsBuffer.contains(id)) {
            log.debug("ID Buffer: already contains " + id);
            return true;
        } else {
            if (lastNotificationsBuffer.size() > 99) {
                lastNotificationsBuffer.remove();
                log.debug("ID Buffer: removed " + id);
            }
            lastNotificationsBuffer.add(id);
            log.debug("ID Buffer: added " + id);
            return false;
        }
    }

    /**
     * Crate authorization header.
     *
     * @return Authorization header.
     */
    public String getAuthorization() {
        return DonkyDataController.getInstance().getConfigurationDAO().getTokenType() + " " + DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken();
    }

    public synchronized boolean isNotificationsSyncInProgress() {
        return synchronisationManager.isNotificationsSyncInProgress();
    }

    public void setReRunNotificationExchange(boolean reRun) {
        synchronisationManager.setReRunNotificationExchange(reRun);
    }

    private void postSuccess(final DonkyListener donkyListener) {
        if (donkyListener != null) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.success();
                }
            });
        }
    }

    private void postError(final DonkyException donkyException, final Map<String, String> validationErrors, final DonkyListener donkyListener) {
        if (donkyListener != null) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.error(donkyException, validationErrors);
                }
            });
        }
    }

    private void postError(final DonkyException donkyException, final Map<String, String> validationErrors, final DonkyResultListener donkyListener) {
        if (donkyListener != null) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    donkyListener.error(donkyException, validationErrors);
                }
            });
        }
    }

    private void postError(final Exception exception, final DonkyListener donkyListener) {
        if (donkyListener != null) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    DonkyException donkyException = new DonkyException(exception.getLocalizedMessage());
                    donkyException.initCause(exception);
                    donkyListener.error(donkyException, null);
                }
            });
        }
    }

    /**
     * Class to encapsulate network/device synchronisation.
     */
    private class SynchronisationManager {

        private final Object sharedLock;

        private final Random randomGenerator;

        private final AtomicBoolean reRunNotificationExchange;

        private final AtomicLong lastNotificationExchangeTimestamp;

        private SyncTaskInfo latestSyncTaskInfo;

        private Set<Integer> startedSynchronisationIDs;

        SynchronisationManager() {
            sharedLock = new Object();
            randomGenerator = new Random();
            startedSynchronisationIDs = new HashSet<>();
            reRunNotificationExchange = new AtomicBoolean(false);
            lastNotificationExchangeTimestamp = new AtomicLong(System.currentTimeMillis());
        }

        /**
         * Perform the network/device synchronisation. Blocking method.
         */
        void synchroniseSynchronously() {
            if (startSync()) {
                try {
                    //since this method is mainly used by GCM service the signalR will be switched off so no need for a channel alternative to REST API at the moment.
                    doSynchroniseSynchronouslyUsingREST();
                } catch (Exception exception) {
                    DonkyException donkyException = new DonkyException(exception.getLocalizedMessage());
                    donkyException.initCause(exception);
                    log.error("Error when synchronising synchronously.", donkyException);
                } finally {
                    stopSync();
                }

                if (reRunNotificationExchange.get()) {
                    reRunNotificationExchange.set(false);
                    synchroniseSynchronously();
                }
            }
        }

        /**
         * Perform the network/device synchronisation. Non blocking method.
         *
         * @param donkyListener Callback to be invoked when completed.
         */
        void synchronise(final DonkyListener donkyListener) {

            try {

                if (startSync()) {

                    if (shouldUseSignalRChannel()) {

                        doSynchroniseUsingSignalR(new DonkyListener() {

                            @Override
                            public void success() {
                                stopSync();
                                if (reRunNotificationExchange.get()) {
                                    reRunNotificationExchange.set(false);
                                    synchronise(donkyListener);
                                } else {
                                    postSuccess(donkyListener);
                                }
                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                                doSynchroniseUsingREST(new DonkyListener() {

                                    @Override
                                    public void success() {
                                        stopSync();
                                        if (reRunNotificationExchange.get()) {
                                            reRunNotificationExchange.set(false);
                                            synchronise(donkyListener);
                                        } else {
                                            postSuccess(donkyListener);
                                        }
                                    }

                                    @Override
                                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                        stopSync();
                                        postError(donkyException, validationErrors, donkyListener);
                                    }
                                });
                            }
                        });

                    } else {

                        doSynchroniseUsingREST(new DonkyListener() {

                            @Override
                            public void success() {
                                stopSync();
                                if (reRunNotificationExchange.get()) {
                                    reRunNotificationExchange.set(false);
                                    synchronise(donkyListener);
                                } else {
                                    postSuccess(donkyListener);
                                }
                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                stopSync();
                                postError(donkyException, validationErrors, donkyListener);
                            }
                        });
                    }
                } else {
                    postError(new DonkyException("Synchronisation canceled"), null);
                }

            } catch (Exception exception) {
                stopSync();
                postError(exception, donkyListener);
            }
        }

        /**
         * Checks if SDK is ready to perform network/device synchronisation.
         * @return True if SDK is ready to perform network/device synchronisation.
         */
        boolean isSDKOperational() {

            if (!DonkyCore.isInitialised()) {
                log.warning("Cancel synchronisation. SDK not initialised.");
                return false;
            } else if (!DonkyAccountController.getInstance().isRegistered()) {
                log.warning("Cancel synchronisation. User not registered.");
                return false;
            } else if (DonkyAccountController.getInstance().isUserSuspended()) {
                log.warning("Cancel synchronisation. User suspended. Trying to authenticate again...");
                DonkyAccountController.getInstance().authenticate(new DonkyListener() {

                    @Override
                    public void success() {
                        if (!DonkyAccountController.getInstance().isUserSuspended()) {
                            log.info("User un-suspended. Authentication successful.");
                            synchronise(null);
                        }
                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                        log.info("User NOT un-suspended. Authentication failed.");
                    }
                });
                return false;
            }

            return true;
        }

        /**
         * Sets synchronisation state to 'in progress'
         *
         * @return True if synchronisation state was changed to 'in progress'
         */
        boolean startSync() {
            boolean isStarted = false;
            if (isSDKOperational()) {
                synchronized (sharedLock) {
                    if (!isNotificationsSyncInProgress()) {
                        latestSyncTaskInfo = new SyncTaskInfo(randomGenerator.nextInt(10000));
                        startedSynchronisationIDs.add(latestSyncTaskInfo.id);
                        lastNotificationExchangeTimestamp.set(System.currentTimeMillis());
                        log.info("Starting: " + latestSyncTaskInfo);
                        isStarted = true;
                    } else {
                        if (startedSynchronisationIDs.size() == 1) {
                            log.debug("Synchronisation in progress. Cancel new one. " + latestSyncTaskInfo.toString() + " Number of started synchronisations = " + startedSynchronisationIDs.size());
                        } else {
                            log.warning("Synchronisation in progress. Cancel new one. " + latestSyncTaskInfo.toString() + " Unexpected(!) number of started synchronisations = " + startedSynchronisationIDs.size());
                        }
                    }
                    sharedLock.notifyAll();
                }
            }
            return isStarted;
        }

        /**
         * Sets synchronisation state to 'finished'
         *
         * @return True if synchronisation state was changed to 'finished'
         */
        boolean stopSync() {
            boolean isStopped = false;
            synchronized (sharedLock) {
                if (isNotificationsSyncInProgress()) {
                    if (startedSynchronisationIDs.contains(latestSyncTaskInfo.id)) {
                        startedSynchronisationIDs.remove(latestSyncTaskInfo.id);
                    } else {
                        log.warning("Synchronisation ID " + latestSyncTaskInfo.id + " not found in history.");
                    }
                    log.info("Stopping: " + latestSyncTaskInfo);
                    latestSyncTaskInfo = null;
                    isStopped = true;
                } else {
                    log.debug("Synchronisation already stopped.");
                }
                sharedLock.notifyAll();
            }
            return isStopped;
        }

        /**
         * Performs a notification synchronisation. This method is blocking and cannot be called from the main thread.
         * Mainly to be used by GCM Intent Service. This method is using only REST synchronisation.
         */
        private void doSynchroniseSynchronouslyUsingREST() {

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

                if (DonkyDataController.getInstance().getNotificationDAO().isNotificationPending() || result.isMoreNotificationsAvailable()) {
                    reRunNotificationExchange.set(true);
                }

            } else {
                log.error("Null synchronise response.");
            }
        }

        /**
         * Perform SignalR synchronisation network call.
         *
         * @param listener Callbacks are made during the Synchronise flow.
         */
        private void doSynchroniseUsingSignalR(final DonkyListener listener) {

            final List<ClientNotification> clientNotificationsToSend = DonkyDataController.getInstance().getNotificationDAO().getNotifications();

            signalRController.synchronise(clientNotificationsToSend, new DonkyResultListener<SynchroniseResponse>() {

                @Override
                public void success(final SynchroniseResponse result) {
                    processResult(clientNotificationsToSend, result, listener);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    postError(donkyException, validationErrors, listener);
                }
            });

        }

        /**
         * Perform REST API synchronisation network call.
         *
         * @param listener Callback to be invoked when completed.
         */
        private void doSynchroniseUsingREST(final DonkyListener listener) {

            final Synchronise synchroniseRequest = new Synchronise();

            synchroniseRequest.performAsynchronous(new DonkyResultListener<SynchroniseResponse>() {

                @Override
                public void success(final SynchroniseResponse result) {
                    processResult(synchroniseRequest.getClientNotifications(), result, new DonkyListener() {
                        @Override
                        public void success() {
                            postSuccess(listener);
                        }

                        @Override
                        public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                            if (validationErrors == null) {
                                postError(donkyException, synchroniseRequest.getValidationFailures(), listener);
                            } else {
                                postError(donkyException, validationErrors, listener);
                            }
                        }
                    });
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    if (!(donkyException instanceof ConnectionException)) {
                        DonkyDataController.getInstance().getNotificationDAO().removeNotifications(synchroniseRequest.getClientNotifications());
                    }

                    if (validationErrors == null) {
                        postError(donkyException, synchroniseRequest.getValidationFailures(), listener);
                    } else {
                        postError(donkyException, validationErrors, listener);
                    }
                }
            });
        }

        /**
         * Process the response from synchronisation call and notify listeners.
         *
         * @param clientNotificationsToSend List of client notifications that have been sent in current notification exchange.
         * @param result                    The response from Donky Network
         * @param listener                  Callback to be invoked when completed.
         */
        private void processResult(final List<ClientNotification> clientNotificationsToSend, final SynchroniseResponse result, final DonkyListener listener) {

            DonkyCore.getInstance().processInBackground(new Runnable() {

                @Override
                public void run() {

                    if (result != null) {

                        DonkyException donkyException = null;

                        try {

                            log.sensitive(result.toString());

                            //Remove sent client notifications from the database.
                            DonkyDataController.getInstance().getNotificationDAO().removeNotifications(clientNotificationsToSend);

                            try {

                                processSynchronisationResponse(result);

                            } catch (JSONException exception) {
                                donkyException = new DonkyException("Error processing notification sync response. (SignalR channel)");
                                donkyException.initCause(exception);
                                postError(donkyException, null, listener);
                                return;
                            }

                            for (ClientNotification notification : clientNotificationsToSend) {
                                SubscriptionController.getInstance().notifyAboutOutboundClientNotification(notification);
                            }

                            if (DonkyDataController.getInstance().getNotificationDAO().isNotificationPending() || result.isMoreNotificationsAvailable()) {
                                reRunNotificationExchange.set(true);
                            }

                        } catch (Exception exception) {
                            donkyException = new DonkyException("Error processing notification sync response. (SignalR channel)");
                            donkyException.initCause(exception);
                            postError(donkyException, null, listener);
                        } finally {
                            if (donkyException == null) {
                                postSuccess(listener);
                            }
                        }

                    } else {
                        log.error("Invalid synchronise response.");
                        DonkyException donkyException = new DonkyException("Invalid synchronise response. (SignalR channel)");
                        postError(donkyException, null, listener);
                    }
                }
            });
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
         * Is notification exchange currently in progress. There should be only one at a time.
         *
         * @return True if notification exchange currently in progress.
         */
        public boolean isNotificationsSyncInProgress() {
            return latestSyncTaskInfo != null;
        }

        /**
         * Sets if the notification exchange should be repeated after the current one finishes.
         *
         * @param reRunNotificationExchange True if the notification exchange should be repeated after the current one finishes.
         */
        public void setReRunNotificationExchange(boolean reRunNotificationExchange) {
            this.reRunNotificationExchange.set(reRunNotificationExchange);
        }

        /**
         * Returns timestamp of the last notification exchange.
         *
         * @return Timestamp of the last notification exchange.
         */
        public long getLastNotificationExchangeTimestamp() {
            return lastNotificationExchangeTimestamp.get();
        }
    }

    /**
     * Class to wrap current synchronisation details for debugging purpose.
     */
    private class SyncTaskInfo {

        long timestampStarted;

        int id;

        SyncTaskInfo(int id) {
            this.timestampStarted = System.currentTimeMillis();
            this.id = id;
        }

        @Override
        public String toString() {
            Date startDate = new Date(timestampStarted);
            Date currentDate = new Date();
            return "Current synchronisation with id " + id + " and was started " + (currentDate.getTime() - startDate.getTime()) + " milliseconds ago.";
        }
    }
}
