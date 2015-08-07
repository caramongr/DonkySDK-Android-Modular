package net.donky.core.network.restapi.authentication;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.logging.DLog;
import net.donky.core.network.NetworkResultListener;
import net.donky.core.network.RetryPolicy;
import net.donky.core.network.UserSuspendedException;
import net.donky.core.network.restapi.GenericServiceRequest;

import java.util.Map;

import retrofit.RetrofitError;
import retrofit.mime.TypedInput;

/**
 * Implements basic responsibilities of every authentication network request.
 *
 * Created by Marcin Swierczek
 * 09/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class GenericAuthenticationServiceRequest<T> extends GenericServiceRequest {

    private final RetryPolicy retryPolicy;

    protected GenericAuthenticationServiceRequest() {
        this.retryPolicy = new RetryPolicy();
    }

    /**
     * @return Retry policy used for this network request.
     */
    RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Register request to listen for connection restored system events.
     */
    protected abstract void doStartListenForConnectionRestored();

    /**
     * Synchronous implementation of particular REST call.
     *
     * @param apiKey Application space unique identifier on Donky Network.
     * @return Result of the network call.
     */
    protected abstract T doSynchronousCall(String apiKey) throws RetrofitError;

    /**
     * Asynchronous implementation of particular REST call.
     *
     * @param apiKey   Application space unique identifier on Donky Network.
     * @param listener The callback to invoke when the command has executed.
     */
    protected abstract void doAsynchronousCall(String apiKey, NetworkResultListener<T> listener);

    /**
     * Perform synchronous network call.
     * This will handle automatically:
     * - Internet connection changes.
     * - Network errors and retries.
     *
     * @return Generic result of network call.
     * @throws DonkyException
     */
    public T performSynchronous(final String apiKey) throws DonkyException {

        if (isConnectionAvailable()) {

            try {

                return doSynchronousCall(apiKey);

            } catch (RetrofitError error) {

                retrofit.client.Response r = error.getResponse();

                if (r != null) {

                    int statusCode = r.getStatus();

                    if (getRetryPolicy().shouldRetryForStatusCode(statusCode) && getRetryPolicy().retry()) {

                        try {
                            Thread.sleep(getRetryPolicy().getDelayBeforeNextRetry());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return performSynchronous(apiKey);

                    } else if (statusCode == 401) {

                        if (this instanceof Login) {

                            try {

                                DonkyAccountController.getInstance().reRegisterWithSameUserDetailsSynchronously();

                                return performSynchronous(apiKey);

                            } catch (DonkyException exception) {

                                DonkyException donkyException = new DonkyException("Error performing network call. User don't exist. Re-registering failed.");
                                donkyException.initCause(error);
                                throw donkyException;

                            }

                        } else {

                            DonkyException donkyException = new DonkyException("Error performing network call. Probably wrong API or AppSpace deleted.");
                            donkyException.initCause(error);
                            throw donkyException;

                        }

                    } else if (statusCode == 403) {

                        DonkyAccountController.getInstance().setSuspended(true);

                        UserSuspendedException userSuspendedException = new UserSuspendedException();
                        userSuspendedException.initCause(error);
                        throw userSuspendedException;

                    } else if (statusCode == 400) {

                        TypedInput body = r.getBody();

                        String failureJson = readInputStream(body);

                        new DLog("GenericAuthenticationServiceRequest").error("Client Bad Request " + failureJson, error);

                        parseFailureDetails(failureJson);

                        DonkyException donkyException = new DonkyException("Validation failures.", getValidationFailures());
                        donkyException.initCause(error);
                        throw donkyException;

                    } else {

                        DonkyException donkyException = new DonkyException("Error performing network call.");
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
            throw new DonkyException("Internet connection not available.");
        }
    }

    /**
     * Perform asynchronous network call.
     * This will handle automatically:
     * - Internet connection changes.
     * - Network errors and retries.
     *
     * @param apiKey   Application space unique identifier on Donky Network.
     * @param listener The callback to invoke when the command has executed.
     */
    public void performAsynchronous(final String apiKey, final DonkyResultListener<T> listener) {

        if (isConnectionAvailable()) {

            doAsynchronousCall(apiKey, new NetworkResultListener<T>() {

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

                            try {
                                Thread.sleep(getRetryPolicy().getDelayBeforeNextRetry());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            performAsynchronous(apiKey, listener);

                        } else if (statusCode == 401) {

                            if (GenericAuthenticationServiceRequest.this instanceof Login) {

                                DonkyAccountController.getInstance().reRegisterWithSameUserDetails(new DonkyListener() {

                                    @Override
                                    public void success() {

                                        performAsynchronous(apiKey, listener);

                                    }

                                    @Override
                                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                                        new DLog("GenericAuthenticationServiceRequest").warning("Error performing network call. User don't exist. Re-registering failed.");

                                        if (listener != null) {
                                            listener.error(donkyException, validationErrors);
                                        }
                                    }
                                });

                            } else {

                                new DLog("GenericAuthenticationServiceRequest").warning("Error performing register network call. Probably wrong API or AppSpace deleted.");

                            }

                        } else if (statusCode == 403) {

                            DonkyAccountController.getInstance().setSuspended(true);

                            listener.userSuspended();

                        } else if (statusCode == 400) {

                            TypedInput body = r.getBody();

                            String failureJson = readInputStream(body);

                            new DLog("GenericAuthenticationServiceRequest").error("Client Bad Request " + failureJson, error);

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

        } else {

            doStartListenForConnectionRestored();
            DonkyException donkyException = new DonkyException("Internet connection not available.");

            if (listener != null) {
                listener.error(donkyException, null);
            }

        }
    }
}