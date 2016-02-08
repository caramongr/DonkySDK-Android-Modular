package net.donky.core.network.restapi.secured;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.ConnectionException;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.RetryPolicy;
import net.donky.core.network.UserSuspendedException;
import net.donky.core.network.restapi.GenericServiceRequest;

import java.util.Map;

import retrofit.RetrofitError;
import retrofit.mime.TypedInput;

/**
 * Generic request for secured network API. Responsible for handling network error codes. Extends Network connection listener fictionalises.
 *
 * Created by Marcin Swierczek
 * 23/03/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class GenericSecuredServiceRequest<T> extends GenericServiceRequest {

    private final RetryPolicy retryPolicy;

    GenericSecuredServiceRequest() {
        this.retryPolicy = new RetryPolicy();
    }

    /**
     * Retry policy used for this network request.
     *
     * @return Retry policy used for this network request.
     */
    RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Synchronous implementation of particular REST call.
     *
     * @param authorization Authorisation header.
     * @return Result of the network call.
     */
    protected abstract T doSynchronousCall(String authorization);

    /**
     * Asynchronous implementation of particular REST call.
     *
     * @param authorization Authorisation header.
     * @param listener The callback to invoke when the command has executed.
     */
    protected abstract void doAsynchronousCall(String authorization, NetworkResultListener<T> listener);

    /**
     * Register request to listen for connection restored system events.
     */
    protected abstract void doStartListenForConnectionRestored();

    /**
     * Perform synchronous network call.
     * This will handle automatically:
     * - Internet connection changes.
     * - Network errors and retries.
     *
     * @return Generic result of network call.
     * @throws net.donky.core.DonkyException
     */
    public T performSynchronous() throws DonkyException {

        if (isConnectionAvailable()) {

            if (!DonkyNetworkController.getInstance().isAuthenticationTokenValid() || DonkyAccountController.getInstance().isUserSuspended()) {
                DonkyAccountController.getInstance().authenticate();
            }

            try {

                return doSynchronousCall(DonkyNetworkController.getInstance().getAuthorization());

            } catch (RetrofitError error) {

                retrofit.client.Response r = error.getResponse();

                if (r != null) {

                    int statusCode = r.getStatus();

                    if (statusCode == 400) {

                        TypedInput body = r.getBody();

                        String failureJson = readInputStream(body);

                        new DLog("GenericSecuredServiceRequest").error("Client Bad Request " + failureJson, error);

                        parseFailureDetails(failureJson);

                        DonkyException donkyException = new DonkyException("Validation failures.", getValidationFailures());
                        donkyException.initCause(error);
                        throw donkyException;

                    }

                    if (getRetryPolicy().shouldRetryForStatusCode(statusCode) && getRetryPolicy().retry()) {

                        try {
                            Thread.sleep(getRetryPolicy().getDelayBeforeNextRetry());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return performSynchronous();

                    } else if (statusCode == 401) {

                        if (retryPolicy.isWasRetriedAlready() && getRetryPolicy().retry()) {
                            try {
                                Thread.sleep(getRetryPolicy().getDelayBeforeNextRetry());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        DonkyAccountController.getInstance().authenticate();

                        return performSynchronous();

                    } else if (statusCode == 403) {

                        DonkyDataController.getInstance().getConfigurationDAO().setUserSuspended(true);
                        DonkyAccountController.getInstance().setSuspended(true);

                        UserSuspendedException userSuspendedException = new UserSuspendedException();
                        userSuspendedException.initCause(error);
                        throw userSuspendedException;

                    } else {

                        DonkyException donkyException = new DonkyException("Error performing network call,");
                        donkyException.initCause(error);
                        throw donkyException;

                    }

                } else {

                    DonkyException donkyException = new DonkyException("Error performing network call. Null response.");
                    donkyException.initCause(error);
                    throw donkyException;

                }
            }
        } else {

            doStartListenForConnectionRestored();
            throw new ConnectionException("Internet connection not available.");
        }
    }

    /**
     * Perform asynchronous network call.
     * This will handle automatically:
     * - Internet connection changes.
     * - Network errors and retries.
     *
     * @param listener Callback to be invoked when the operation finishes.
     */
    public void performAsynchronous(final DonkyResultListener<T> listener) {

        if (isConnectionAvailable()) {

            if (!DonkyNetworkController.getInstance().isAuthenticationTokenValid() || DonkyAccountController.getInstance().isUserSuspended()) {

                DonkyAccountController.getInstance().authenticate(new DonkyListener() {

                    @Override
                    public void success() {

                        doAsynchronousCall(DonkyNetworkController.getInstance().getAuthorization(), new NetworkResultListener<T>() {

                            @Override
                            public void success(T result) {

                                if (listener != null) {
                                    listener.success(result);
                                }

                            }

                            @Override
                            public void onFailure(RetrofitError error) {

                                retrofit.client.Response r = error.getResponse();

                                if (r != null) {

                                    int statusCode = r.getStatus();

                                    if (getRetryPolicy().shouldRetryForStatusCode(statusCode) && getRetryPolicy().retry()) {

                                        Handler handler = new Handler(Looper.getMainLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                performAsynchronous(listener);
                                            }
                                        }, getRetryPolicy().getDelayBeforeNextRetry());


                                    } else if (statusCode == 401) {

                                        if (retryPolicy.isWasRetriedAlready() && getRetryPolicy().retry()) {
                                            Handler handler = new Handler(Looper.getMainLooper());
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    performAsyncCallAgainAfterAuthentication(listener);
                                                }
                                            }, getRetryPolicy().getDelayBeforeNextRetry());
                                        } else {
                                            performAsyncCallAgainAfterAuthentication(listener);
                                        }

                                    } else if (statusCode == 403) {

                                        DonkyDataController.getInstance().getConfigurationDAO().setUserSuspended(true);
                                        DonkyAccountController.getInstance().setSuspended(true);

                                        if (listener != null) {
                                            listener.userSuspended();
                                        }

                                    } else if (statusCode == 400) {

                                        TypedInput body = r.getBody();

                                        String failureJson = readInputStream(body);

                                        new DLog("GenericSecuredServiceRequest").error("Client Bad Request " + failureJson, error);

                                        parseFailureDetails(failureJson);

                                        if (listener != null) {
                                            listener.error(null, getValidationFailures());
                                        }

                                    } else {

                                        DonkyException donkyException = new DonkyException("Error performing network call. " + error.getResponse().getReason());
                                        donkyException.initCause(error);
                                        listener.error(donkyException, null);
                                    }

                                } else {

                                    DonkyException donkyException = new DonkyException("Error performing network call. Null response.");
                                    donkyException.initCause(error);

                                    if (listener != null) {
                                        listener.error(donkyException, null);
                                    }

                                }
                            }
                        });
                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        if (listener != null) {
                            listener.error(donkyException, validationErrors);
                        }

                    }
                });

            } else {

                doAsynchronousCall(DonkyNetworkController.getInstance().getAuthorization(), new NetworkResultListener<T>() {

                    @Override
                    public void success(T result) {

                        if (listener != null) {
                            listener.success(result);
                        }

                    }

                    @Override
                    public void onFailure(final RetrofitError error) {

                        final retrofit.client.Response r = error.getResponse();

                        if (r != null) {

                            int statusCode = r.getStatus();

                            if (getRetryPolicy().shouldRetryForStatusCode(statusCode) && getRetryPolicy().retry()) {

                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        performAsynchronous(listener);
                                    }
                                }, getRetryPolicy().getDelayBeforeNextRetry());

                            } else if (statusCode == 401) {

                                if (retryPolicy.isWasRetriedAlready() && getRetryPolicy().retry()) {

                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            performAsyncCallAgainAfterAuthentication(listener);
                                        }
                                    }, getRetryPolicy().getDelayBeforeNextRetry());
                                } else {
                                    performAsyncCallAgainAfterAuthentication(listener);
                                }

                            } else if (statusCode == 403) {

                                DonkyDataController.getInstance().getConfigurationDAO().setUserSuspended(true);
                                DonkyAccountController.getInstance().setSuspended(true);

                                if (listener != null) {
                                    listener.userSuspended();
                                }

                            } else if (statusCode == 400) {

                                TypedInput body = r.getBody();

                                String failureJson = readInputStream(body);

                                new DLog("GenericSecuredServiceRequest").error("Client Bad Request " + failureJson, error);

                                parseFailureDetails(failureJson);

                                if (listener != null) {
                                    listener.error(null, getValidationFailures());
                                }

                            } else {

                                DonkyException donkyException = new DonkyException("Error performing network call. " + error.getResponse().getReason());
                                donkyException.initCause(error);

                                if (listener != null) {
                                    listener.error(donkyException, null);
                                }

                            }

                        } else {

                            DonkyException donkyException = new DonkyException("Error performing network call. Null response.");
                            donkyException.initCause(error);

                            if (listener != null) {
                                listener.error(donkyException, null);
                            }

                        }
                    }
                });
            }

        } else {

            doStartListenForConnectionRestored();

            ConnectionException donkyException = new ConnectionException("Internet connection not available.");

            if (listener != null) {
                listener.error(donkyException, null);
            }

        }
    }

    private void performAsyncCallAgainAfterAuthentication(final DonkyResultListener<T> listener) {
        DonkyAccountController.getInstance().authenticate(new DonkyListener() {

            @Override
            public void success() {
                performAsynchronous(listener);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (listener != null) {
                    listener.error(donkyException, validationErrors);
                }
            }
        });
    }
}
