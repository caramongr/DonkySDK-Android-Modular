package net.donky.core.signalr;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.internal.LinkedTreeMap;

import net.donky.core.DonkyException;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.SynchronisationHandler;
import net.donky.core.network.restapi.secured.SynchroniseResponse;
import net.donky.core.network.signalr.SignalRController;
import net.donky.core.signalr.internal.ConnectionListener;
import net.donky.core.signalr.internal.HubConnectionFactory;
import net.donky.core.signalr.internal.PushHandler;
import net.donky.core.signalr.internal.tasks.SynchronisationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Controller for SignalR communication channel. The Core Module is responsible for controlling this functionality so there is no need to call any mathod in this class by the integrator.
 *
 * Created by Marcin Swierczek
 * 11/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkySignalRController extends SignalRController implements PushHandler {

    private static final String NOTIFICATION_TYPE_PENDING = "NotificationPending";

    private final String KEY_TYPE = "type";
    private final String KEY_DATA = "data";
    private final String TYPE_USER_TYPING = "UserIsTyping";

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final int KEEP_ALIVE_TIME_SEC = 10;

    private DLog log;

    private final HubConnectionFactory hubConnectionFactory;

    ThreadPoolExecutor threadPoolExecutor;

    BlockingDeque<Runnable> workQueue;

    Handler mainThreadHandler;

    SynchronisationTask synchronisationTask;

    private static final Object sharedLock = new Object();

    volatile boolean isTaskInProgress;

    private static final long TIMEOUT = 1000;

    // Private constructor. Prevents instantiation from other classes.
    private DonkySignalRController() {
        this.log = new DLog("DonkySignalRController");
        hubConnectionFactory = new HubConnectionFactory(this, new ConnectionListener() {

            @Override
            public void notifyConnectionError(DonkyException donkyException, Map<String, String> validationErrors) {
                if (isTaskInProgress && synchronisationTask != null) {
                    synchronisationTask.notifyConnectionError(donkyException, validationErrors);
                }
            }

        });
        workQueue = new LinkedBlockingDeque<>();
        mainThreadHandler = new Handler(Looper.getMainLooper());
        threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME_SEC, TimeUnit.SECONDS, workQueue);
    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkySignalRController INSTANCE = new DonkySignalRController();
    }

    /**
     * Get instance of DonkySignalRController singleton.
     *
     * @return Instance of Account Controller singleton.
     */
    public static DonkySignalRController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky.
     *
     * @param application Application instance.
     */
    void init(Application application) {
    }

    /**
     * Checks if signalR is connected.
     *
     * @return True if signalR is connected.
     */
    public boolean isConnected() {
        return hubConnectionFactory.isConnected();
    }

    @Override
    public synchronized void startSignalR() {
        if (!hubConnectionFactory.isConnected()) {
            hubConnectionFactory.startSignalR(LifeCycleObserver.getInstance().isApplicationForegrounded());
        }
    }

    @Override
    public void stopSignalR() {
        hubConnectionFactory.stopSignalR();
    }

    @Override
    public synchronized void synchronise(final List<ClientNotification> clientNotificationsToSend, final DonkyResultListener<SynchroniseResponse> resultListener) {

        log.debug("SignalR: synchronise called");

        if (!DonkyAccountController.getInstance().isRegistered()) {
            log.warning("SignalR: Cancel synchronisation. User not registered.");
            postError(new DonkyException("Cancel synchronisation. User not registered."), null, resultListener);
        } else if (DonkyAccountController.getInstance().isUserSuspended()) {
            log.warning("SignalR: Cancel synchronisation. User suspended.");
            postError(new DonkyException("Cancel synchronisation. User suspended."), null, resultListener);
        } else if (isTaskInProgress) {
            log.debug("SignalR: Synchronisation signalR task in progress.");
            postError(new DonkyException("Cancel synchronisation. Synchronisation signalR task in progress. Falling back to REST API."), null, resultListener);
        } else if (!hubConnectionFactory.isConnected()) {
            log.debug("SignalR: SignalR not connected. Falling back to REST API.");
            postError(new DonkyException("SignalR: SignalR not connected. Falling back to REST API."), null, resultListener);
        } else {

            String signalRURL = DonkyDataController.getInstance().getConfigurationDAO().getSignalRUrl();
            String authToken = DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken();

            if (hubConnectionFactory.setupSignalRConnection(authToken, signalRURL, false)) {
                log.debug("SignalR: connection objects are ok. Invoking sync on network.");
                invokeSynchronise(clientNotificationsToSend, resultListener);
            } else {
                log.debug("SignalR: connection object was outdated. Falling back to REST API.");
                postError(new DonkyException("Recreating signalR connection. Falling back to REST API."), null, resultListener);
            }
        }
    }

    /**
     * Invoke synchronise on signalR network hub
     *
     * @param clientNotificationsToSend List of ClientNotifications to be sent.
     * @param resultListener            Result callback listener
     */
    private void invokeSynchronise(final List<ClientNotification> clientNotificationsToSend, final DonkyResultListener<SynchroniseResponse> resultListener) {

        isTaskInProgress = true;

        threadPoolExecutor.execute(new Runnable() {

            @Override
            public void run() {

                synchronized (sharedLock) {

                    synchronisationTask = new SynchronisationTask(hubConnectionFactory, clientNotificationsToSend, new DonkyResultListener<SynchroniseResponse>() {

                        @Override
                        public void success(final SynchroniseResponse result) {

                            log.debug("SignalR: synchronisation done " + result.toString());

                            if (isTaskInProgress) {
                                log.debug("SignalR: notifying core about successful synchronisation.");
                                isTaskInProgress = false;
                                mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (resultListener != null) {
                                            resultListener.success(result);
                                        }
                                    }
                                });
                            } else {
                                log.warning("SignalR: Not expected callback from sync.");
                            }
                        }

                        @Override
                        public void error(final DonkyException donkyException, final Map<String, String> validationErrors) {

                            log.debug("SignalR: synchronisation failure");

                            if (isTaskInProgress) {
                                log.debug("SignalR: notifying core about synchronisation failure.");
                                isTaskInProgress = false;
                                postError(donkyException, validationErrors, resultListener);
                            } else {
                                log.warning("SignalR: Not expected callback from sync.");
                            }
                        }

                    });

                    sharedLock.notifyAll();
                }

                synchronisationTask.performTask();

            }
        });

    }

    @Override
    public void handlePush(final Object obj) {

        threadPoolExecutor.execute(new Runnable() {

            @Override
            public void run() {

                synchronized (sharedLock) {

                    try {

                        if (obj instanceof ArrayList) {

                            ArrayList<?> combinedList = (ArrayList<?>) obj;

                            for (Object notification : combinedList) {

                                if (notification instanceof LinkedTreeMap) {

                                    LinkedTreeMap<?, ?> notificationTreeMap = (LinkedTreeMap<?, ?>) notification;

                                    ServerNotification serverNotification = new ServerNotification(notificationTreeMap);

                                    String type = (String) notificationTreeMap.get("type");

                                    if (NOTIFICATION_TYPE_PENDING.equals(type)) {

                                        String notificationId = (String) notificationTreeMap.get("id");

                                        if (!TextUtils.isEmpty(notificationId)) {

                                            try {

                                                serverNotification = DonkyNetworkController.getInstance().getServerNotification(notificationId);

                                                if (serverNotification != null) {
                                                    SynchronisationHandler synchronisationHandler = new SynchronisationHandler(serverNotification);
                                                    log.debug("SignalR: serverNotification downloaded " + serverNotification.getData());
                                                    synchronisationHandler.processServerNotifications(true);
                                                } else {
                                                    log.debug("SignalR: serverNotification missing when tried to download. Falling back to full sync.");
                                                    DonkyNetworkController.getInstance().synchroniseSynchronously();
                                                }

                                            } catch (Exception exception) {
                                                log.info("SignalR: error downloading notification");
                                            }

                                        }

                                    } else {

                                        try {
                                            SynchronisationHandler synchronisationHandler = new SynchronisationHandler(serverNotification);
                                            synchronisationHandler.processServerNotifications();
                                        } catch (Exception exception) {
                                            log.info("SignalR: error procising notification directly from signalR channel");
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception exception) {
                        log.info("SignalR: error processing push");
                    }

                    sharedLock.notifyAll();
                }
            }
        });
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
}
