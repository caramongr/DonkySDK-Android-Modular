package net.donky.core.signalr.internal;

import android.text.TextUtils;

import net.donky.core.DonkyException;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;

import donky.microsoft.aspnet.signalr.client.Action;
import donky.microsoft.aspnet.signalr.client.ConnectionState;
import donky.microsoft.aspnet.signalr.client.ErrorCallback;
import donky.microsoft.aspnet.signalr.client.LogLevel;
import donky.microsoft.aspnet.signalr.client.Logger;
import donky.microsoft.aspnet.signalr.client.Platform;
import donky.microsoft.aspnet.signalr.client.SignalRFuture;
import donky.microsoft.aspnet.signalr.client.StateChangedCallback;
import donky.microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import donky.microsoft.aspnet.signalr.client.hubs.HubConnection;
import donky.microsoft.aspnet.signalr.client.hubs.HubProxy;
import donky.microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

/**
 * Class to manage signalR connections. SignalR channel will be used for synchronisation with network when application is operating in the foreground.
 *
 * Created by Marcin Swierczek
 * 18/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class HubConnectionFactory {

    private static final String hubName = "NetworkHub";

    private String signalRURLInUse;

    private String authTokenInUse;

    private HubConnection connection;

    private HubProxy hubProxy;

    private DLog log;

    private PushHandler pushHandler;

    private ConnectionListener connectionListener;

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    public HubConnectionFactory(PushHandler pushHandler, ConnectionListener connectionListener) {
        this.pushHandler = pushHandler;
        this.connectionListener = connectionListener;
        this.log = new DLog("HubConnectionFactory");
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
    }

    /**
     * Setup signalR connection and connect. SignalR channel will be used for synchronisation with network when application is operating in the foreground.
     * @param shouldConnect
     */
    public void startSignalR(boolean shouldConnect) {
        String signalRURL = DonkyDataController.getInstance().getConfigurationDAO().getSignalRUrl();
        String authToken = DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken();
        setupSignalRConnection(authToken, signalRURL, shouldConnect);
    }

    /**
     * Stops signalR connection. Standard REST call for synchronisation will be use instead.
     */
    public void stopSignalR() {
        if (connection != null) {
            try {
                connection.stop();
            } catch (Exception exception) {
                log.warning("SignalR error when stopping. E.g. mTransport.abort can return null.");
            }
        }
    }

    /**
     * Returns Hub Proxy to be used for sending notifications through signalR.
     *
     * @return Hub Proxy to be used for sending notifications through signalR.
     */
    public HubProxy getHubProxy() {
        return hubProxy;
    }

    /**
     * Creates signalR connection if there is none currently used or the authorisation token or singalR URL was updated.
     * @param authToken Authorisation token assigned by the network.
     * @param signalRURL URL to be used for signalR calls
     * @param shouldConnect
     */
    public boolean setupSignalRConnection(String authToken, String signalRURL, boolean shouldConnect) {

        boolean newConnectionSetup = false;

        if (TextUtils.isEmpty(authToken) || TextUtils.isEmpty(signalRURL)) {
            log.warning("Connection require auth token and signalR url");
            return false;
        }

        if (!authToken.equals(authTokenInUse)) {
            authTokenInUse = authToken;
            newConnectionSetup = true;
        }

        if (!signalRURL.equals(signalRURLInUse)) {
            signalRURLInUse = signalRURL;
            newConnectionSetup = true;
        }

        if (newConnectionSetup && connection != null) {
            if (isConnected()) {
                stopSignalR();
            }
            createHubConnection(authToken, signalRURL, shouldConnect);
            return false;
        } else if (connection == null) {
            createHubConnection(authToken, signalRURL, shouldConnect);
            return false;
        } else if (isDisconnected() && shouldConnect) {
            connectSignalR();
            return false;
        } else {
            return true;
        }

    }

    /**
     * Creates signalR connection.
     * @param authToken Authorisation token assigned by the network.
     * @param signalRURL URL to be used for signalR calls
     * @param shouldConnect
     */
    private void createHubConnection(String authToken, String signalRURL, boolean shouldConnect) {

        connection = new HubConnection(signalRURL, getQueryString(authToken), false, new Logger() {

            @Override
            public void log(String s, LogLevel logLevel) {
                if (logLevel == LogLevel.Critical) {
                    log.error("SignalR: " + s);
                    // Microsoft SignalR library catches some exceptions silently, logging them as critical. E.g. doesn't handle long messages and fail silently. Need to fall back to REST in that case.
                    DonkyException donkyException = new DonkyException(s);
                    connectionListener.notifyConnectionError(donkyException, null);
                } else if (logLevel == LogLevel.Information) {
                    log.debug("SignalR: " + s);
                } else if (logLevel == LogLevel.Verbose) {
                    log.debug("SignalR: " + s);
                }
            }

        });

        hubProxy = connection.createHubProxy(hubName);

        hubProxy.on("push", new SubscriptionHandler1<Object>() {
            @Override
            public void run(Object p1) {
                log.info("SignalR: push received!");
                pushHandler.handlePush(p1);
            }
        }, Object.class);

        connection.connected(new Runnable() {

            @Override
            public void run() {
                log.info("SignalR: SignalR connected successfully. Connection id = " + connection.getConnectionId());
            }

        });

        connection.error(new ErrorCallback() {

            @Override
            public void onError(Throwable error) {
                log.warning("SignalR: Hub connection error: " + error.getLocalizedMessage());
                DonkyException donkyException = new DonkyException(error.getLocalizedMessage());
                donkyException.initCause(error);
                connectionListener.notifyConnectionError(donkyException, null);
            }

        });

        connection.connectionSlow(new Runnable() {
            @Override
            public void run() {
                log.warning("SignalR: Hub connection slow");
            }
        });

        connection.stateChanged(new StateChangedCallback() {
            @Override
            public void stateChanged(ConnectionState oldState, ConnectionState newState) {
                log.info("SignalR: State changed from " + oldState + " to " + newState);
                if (newState == ConnectionState.Disconnected) {
                    log.info("SignalR: Disconnected connection with id = " + connection.getConnectionId());
                }

            }
        });

        if (shouldConnect) {
            connectSignalR();
        }
    }

    /**
     * Check if the signalR has been disconnected.
     *
     * @return True if the signalR has been disconnected.
     */
    public boolean isDisconnected() {
        return connection.getState() == ConnectionState.Disconnected;
    }

    /**
     * Check if the signalR has been disconnected.
     *
     * @return True if the signalR has been disconnected.
     */
    public boolean isConnected() {
        if (connection == null) {
            return false;
        } else {
            return connection.getState() == ConnectionState.Connected;
        }
    }

    /**
     * Connect to signalR on the Donky Network.
     */
    public void connectSignalR() {

        if (connection != null && isDisconnected()) {

            SignalRFuture<Void> awaitConnection = connection.start();

            awaitConnection.done(new Action<Void>() {

                @Override
                public void run(Void obj) throws Exception {
                    log.info("SignalR: Await connection SignalRFuture done");
                }

            });

            awaitConnection.onCancelled(new Runnable() {
                @Override
                public void run() {
                    log.warning("SignalR: Await connection SignalRFuture cancelled");
                }
            });

            awaitConnection.onError(new ErrorCallback() {

                @Override
                public void onError(Throwable error) {
                    log.warning("SignalR: Await connection SignalRFuture error");
                }

            });
        }
    }

    /**
     * Query string for signalR connection.
     *
     * @param authToken Authorisation token assigned by the network.
     * @return Query string for signalR connection.
     */
    private String getQueryString(String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            return "access_token=" + authToken;
        } else {
            return "access_token=" +  DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken();
        }
    }

}
