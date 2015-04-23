package net.donky.core.account;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.events.RegistrationChangedEvent;
import net.donky.core.helpers.IdHelper;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.logging.DLog;
import net.donky.core.logging.DonkyLoggingController;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.RetryPolicy;
import net.donky.core.network.restapi.RestClient;
import net.donky.core.network.restapi.authentication.Login;
import net.donky.core.network.restapi.authentication.LoginResponse;
import net.donky.core.network.restapi.authentication.Register;
import net.donky.core.network.restapi.authentication.RegisterResponse;
import net.donky.core.network.restapi.secured.UpdateClient;
import net.donky.core.network.restapi.secured.UpdateDevice;
import net.donky.core.network.restapi.secured.UpdateRegistration;
import net.donky.core.network.restapi.secured.UpdateUser;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for all account registration related functionality.
 * <p/>
 * Created by Marcin Swierczek
 * 16/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyAccountController {

    /**
     * Time period before token expire to re-authenticate.
     */
    private static final long RE_LOGIN_TIME_PERIOD_BEFORE_TOKEN_EXPIRES = 1000 * 60 * 3; //Login user 3 min before token expires

    /**
     * Alarm identifier for re-authenticate schedule.
     */
    private static final int LOGIN_ALARM_ID = 3429;

    /**
     * True if user is registered successfully.
     */
    private final AtomicBoolean isRegistered;

    /**
     * True if user has been suspended on the Network.
     */
    private final AtomicBoolean isSuspended;

    /**
     * True if user has been suspended on the Network.
     */
    private final AtomicBoolean isLoginInProgress;

    /**
     * Logging helper.
     */
    private final DLog log;

    /**
     * Application context.
     */
    private Context context;

    /**
     * Object to lock on.
     */
    private static final Object sharedLock = new Object();

    // Private constructor. Prevents instantiation from other classes.
    private DonkyAccountController() {
        log = new DLog("AccountController");
        isRegistered = new AtomicBoolean(false);
        isSuspended = new AtomicBoolean(false);
        isLoginInProgress = new AtomicBoolean(false);
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyAccountController INSTANCE = new DonkyAccountController();
    }

    /**
     * Get instance of Account Controller singleton.
     *
     * @return Instance of Account Controller singleton.
     */
    public static DonkyAccountController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application) {
        this.context = application.getApplicationContext();
        isSuspended.set(DonkyDataController.getInstance().getConfigurationDAO().isUserSuspended());
        RestClient.getInstance().init();
    }

    /**
     * Did the registration process finish successfully.
     *
     * @return True if registration process was completed successfully.
     */
    public boolean isRegistered() {
        return isRegistered.get();
    }

    private boolean isLoginInProgress() {
        return isLoginInProgress.get();
    }

    /**
     * Sets if user activity should be suspended. This should be used by Core Module only and controlled by the Donky network.
     *
     * @param isSuspended True if user activity should be suspended.
     */
    public void setSuspended(boolean isSuspended) {

        this.isSuspended.set(isSuspended);

        DonkyDataController.getInstance().getConfigurationDAO().setUserSuspended(isSuspended);

    }

    /**
     * Is Account ready for all secured operations on the network.
     *
     * @return True if Account ready for all secured operations on the network.
     */
    boolean isAccountReady() {
        return DonkyCore.isInitialised() && isRegistered() && !isUserSuspended();
    }

    /**
     * Update any custom data related to the registration.
     *
     * @param userDetails   User details to be updated.
     * @param deviceDetails Device details to be updated.
     * @param listener      The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void updateRegistrationDetails(final UserDetails userDetails, final DeviceDetails deviceDetails, final DonkyListener listener) {

        if (isAccountReady()) {

            DonkyNetworkController.getInstance().updateRegistrationOnNetwork(new UpdateRegistration(userDetails, deviceDetails), new DonkyListener() {
                @Override
                public void success() {

                    DonkyDataController.getInstance().getUserDAO().setUserDetails(userDetails);

                    DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(deviceDetails);

                    if (listener != null) {
                        listener.success();
                    }

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    if (listener != null) {
                        listener.error(donkyException, validationErrors);
                    }

                }
            });

        } else {

            if (listener != null) {
                listener.error(new DonkyException("Account is not ready. Please review registration process and if account has been suspended."), null);
            }

        }
    }

    /**
     * Update user registration details.
     *
     * @param user     New user details.
     * @param listener Callback to invoke when task is completed.
     */
    public void updateUserDetails(final UserDetails user, final DonkyListener listener) {

        if (isAccountReady()) {

            UpdateUser request = new UpdateUser(user);

            DonkyNetworkController.getInstance().updateUserOnNetwork(request, new DonkyListener() {

                @Override
                public void success() {

                    DonkyDataController.getInstance().getUserDAO().setUserDetails(user);

                    if (listener != null) {
                        listener.success();
                    }

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(user, getDeviceDetails()));
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    if (listener != null) {
                        listener.error(donkyException, validationErrors);
                    }

                }
            });

        } else {

            if (listener != null) {
                listener.error(new DonkyException("Account is not ready. Please review registration process and if account has been suspended."), null);
            }

        }
    }

    /**
     * Update device registration details.
     *
     * @param deviceDetails New device details.
     * @param listener      Callback to invoke when task is completed.
     */
    public void updateDeviceDetails(final DeviceDetails deviceDetails, final DonkyListener listener) {

        if (isAccountReady()) {

            UpdateDevice request = new UpdateDevice(deviceDetails);

            DonkyNetworkController.getInstance().updateDeviceOnNetwork(request, new DonkyListener() {

                @Override
                public void success() {

                    DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(deviceDetails);

                    if (listener != null) {
                        listener.success();
                    }

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(getCurrentDeviceUser(), deviceDetails));
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                    if (listener != null) {
                        listener.error(donkyException, validationErrors);
                    }

                }
            });

        } else {

            if (listener != null) {
                listener.error(new DonkyException("Account is not ready. Please review registration process and if account has been suspended."), null);
            }

        }
    }

    /**
     * Update client details on the network.
     *
     * @param listener Callback to invoke when task is completed.
     */
    public void updateClient(final DonkyListener listener) {

        if (isAccountReady()) {

            UpdateClient request = new UpdateClient();

            DonkyNetworkController.getInstance().updateClientOnNetwork(request, listener);

        } else {

            if (listener != null) {
                listener.error(new DonkyException("Account is not ready. Please review registration process and if account has been suspended."), null);
            }

        }
    }

    /**
     * Update registration details on the Donky Network if different from the one saved on the device. if different from the one saved on the device. This method is blocking and cannot be called from main thread.
     * This method is blocking and cannot be called from main thread.
     *
     * @param userDetails   User details to be updated.
     * @param deviceDetails Device details to be updated.
     * @param appVersion    Application version.
     */
    private void updateRegistrationDetailsIfChanged(final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion) {

        if (isAccountReady()) {

            try {

                boolean areRegistrationDetailsDifferent = false;

                if (userDetails != null && !userDetails.equals(getCurrentDeviceUser())) {

                    (new UpdateUser(userDetails)).performSynchronous();

                    DonkyDataController.getInstance().getUserDAO().setUserDetails(userDetails);

                    areRegistrationDetailsDifferent = true;

                    log.warning("Registration details changed: user");
                }

                if (deviceDetails != null && !deviceDetails.equals(getDeviceDetails())) {

                    (new UpdateDevice(deviceDetails)).performSynchronous();

                    DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(deviceDetails);

                    areRegistrationDetailsDifferent = true;

                    log.warning("Registration details changed: device");
                }

                if (!TextUtils.isEmpty(appVersion) && !appVersion.equals(DonkyDataController.getInstance().getConfigurationDAO().getAppVersion())) {

                    (new UpdateClient(appVersion)).performSynchronous();

                    DonkyDataController.getInstance().getConfigurationDAO().setAppVersion(appVersion);

                    log.warning("Registration details changed: client");

                }

                if (areRegistrationDetailsDifferent) {

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

                }

            } catch (Exception e) {

                log.error("Error checking registration differences.", e);
            }

        } else {

            log.warning("User details couldn't be updated on initialisation. Check if account was suspended.");
        }
    }

    /**
     * Gets the current user and device registration details.
     *
     * @return UserDetails and DeviceDetails
     */
    public RegistrationDetails getRegistrationDetails() {

        if (DonkyCore.isInitialised()) {

            return new RegistrationDetails(getCurrentDeviceUser(), getDeviceDetails());

        } else {
            return null;
        }

    }

    /**
     * Get information about current user registration.
     *
     * @return Information about current user.
     */
    public UserDetails getCurrentDeviceUser() {

        if (DonkyCore.isInitialised()) {

            return DonkyDataController.getInstance().getUserDAO().getUserDetails();

        } else {
            return null;
        }
    }

    /**
     * Get current device registration details.
     *
     * @return Current device registration details.
     */
    public DeviceDetails getDeviceDetails() {

        if (DonkyCore.isInitialised()) {

            return DonkyDataController.getInstance().getDeviceDAO().getDeviceDetails();

        } else {
            return null;
        }

    }

    /**
     * Is user allowed to interact with secured services on the Network.
     *
     * @return True if user is not allowed to interact with secured services on the Network.
     */
    public boolean isUserSuspended() {

        return DonkyDataController.getInstance().getConfigurationDAO().isUserSuspended();

    }

    /**
     * Authenticate on the Donky Network. This method is blocking and cannot be performed from main thread.
     *
     * @throws DonkyException
     */
    public void authenticate() throws DonkyException {

        if (DonkyCore.isInitialised() && isRegistered()) {

            if (!isLoginInProgress.get()) {

                isLoginInProgress.set(true);

                Login loginRequest = new Login();

                log.sensitive(loginRequest.toString());

                LoginResponse response = DonkyNetworkController.getInstance().loginToNetwork(loginRequest);

                if (processLoginResponse(response)) {

                    setSuspended(false);

                    log.sensitive(response.toString());

                } else {

                    isLoginInProgress.set(false);

                    log.error("Invalid authenticate response.");
                    throw new DonkyException("Invalid authenticate response.");

                }

                isLoginInProgress.set(false);

                log.info("Successfully logged into Donky Network.");

            }

        } else {

            throw new DonkyException("Account is not ready. Please register first.");
        }
    }

    /**
     * Authenticate on the Donky Network.
     *
     * @param listener Callback to invoke when task is completed.
     */
    public void authenticate(final DonkyListener listener) {

        if (DonkyCore.isInitialised() && isRegistered()) {

            if (!isLoginInProgress.get()) {

                isLoginInProgress.set(true);

                Login loginRequest = new Login();

                log.sensitive(loginRequest.toString());

                DonkyNetworkController.getInstance().loginToNetwork(loginRequest, new DonkyResultListener<LoginResponse>() {

                            @Override
                            public void success(LoginResponse result) {

                                log.sensitive(result.toString());

                                if (processLoginResponse(result)) {

                                    setSuspended(false);

                                    isLoginInProgress.set(false);

                                    if (listener != null) {
                                        listener.success();
                                    }

                                } else {

                                    isLoginInProgress.set(false);

                                    if (listener != null) {
                                        listener.error(new DonkyException("Invalid authenticate response."), null);
                                    }

                                }

                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                                isLoginInProgress.set(false);

                                if (listener != null) {
                                    listener.error(donkyException, validationErrors);
                                }

                            }
                        }
                );

            }

        } else {

            if (listener != null) {
                listener.error(new DonkyException("Account is not ready. Please register first."), null);
            }

        }
    }

    /**
     * Save registration data on device.
     *
     * @param apiKey          Identifier for Donky Network Application Space.
     * @param userDetails     User details to save.
     * @param deviceDetails   Device details to save.
     * @param appVersion      Application version to save.
     * @param isUserAnonymous Is the user registration anonymous one.
     */
    private void saveRegistrationData(final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, String appVersion, boolean isUserAnonymous) {

        DonkyDataController.getInstance().getConfigurationDAO().setDonkyNetworkApiKey(apiKey);

        UserDetails user = DonkyDataController.getInstance().getUserDAO().getUserDetails();

        if (userDetails != null) {

            user.setUserId(userDetails.getUserId());
            user.setUserDisplayName(userDetails.getUserDisplayName());
            user.setUserFirstName(userDetails.getUserFirstName());
            user.setUserLastName(userDetails.getUserLastName());
            user.setUserMobileNumber(userDetails.getUserMobileNumber());
            user.setUserCountryCode(userDetails.getCountryCode());
            user.setUserEmailAddress(userDetails.getUserEmailAddress());
            user.setUserAdditionalProperties(userDetails.getUserAdditionalProperties());
            user.setSelectedTags(userDetails.getSelectedTags());

        }

        user.setAnonymous(isUserAnonymous);

        DonkyDataController.getInstance().getUserDAO().setUserDetails(user);

        if (deviceDetails != null) {

            DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(new DeviceDetails(
                    deviceDetails.getDeviceName(),
                    deviceDetails.getDeviceType(),
                    deviceDetails.getAdditionalProperties()));
        }

        DonkyDataController.getInstance().getConfigurationDAO().setAppVersion(appVersion);
    }

    /**
     * Register device on the donky Network and save all registration details on the device. If user is already registered update registration data.
     * This method will also authenticate user and perform initial synchronisation.
     *
     * @param apiKey        Donky Network identifier for application space
     * @param userDetails   User details to use for the registration or update.
     * @param deviceDetails Device details to use for the registration or update.
     * @param appVersion    Application version.
     * @throws DonkyException
     */
    public void register(final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, String appVersion) throws DonkyException {

        if (DonkyCore.isInitialised() && !TextUtils.isEmpty(apiKey) && context != null) {

            boolean isAllowedToRegister = TextUtils.isEmpty(DonkyDataController.getInstance().getUserDAO().getUserNetworkId()) || !apiKey.equals(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey());

            if (isAllowedToRegister) {

                String deviceId = DonkyDataController.getInstance().getDeviceDAO().getDeviceId();
                if (deviceId == null) {
                    deviceId = IdHelper.generateDeviceId(context);
                    DonkyDataController.getInstance().getDeviceDAO().setDeviceId(deviceId);
                }

                String deviceSecret = DonkyDataController.getInstance().getDeviceDAO().getDeviceSecret();
                if (deviceSecret == null) {
                    deviceSecret = IdHelper.generateId();
                    DonkyDataController.getInstance().getDeviceDAO().setDeviceSecret(deviceSecret);
                }

                Register registerRequest = new Register(apiKey, userDetails, deviceDetails, appVersion);

                log.sensitive(registerRequest.toString());

                RegisterResponse response = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);

                if (processRegistrationResponse(response)) {

                    log.sensitive(response.toString());

                    boolean isAnonymousRegistration = (userDetails == null || TextUtils.isEmpty(userDetails.getUserId()));

                    saveRegistrationData(apiKey, userDetails, deviceDetails, appVersion, isAnonymousRegistration);

                    synchronized (sharedLock) {
                        isRegistered.set(true);
                        setSuspended(false);
                        sharedLock.notifyAll();
                    }

                    log.info("Successfully registered into Donky Network.");

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

                    DonkyNetworkController.getInstance().synchronise();

                    DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);

                } else {

                    log.warning("Error registering. Registration process not completed. " + response.toString());

                    throw new DonkyException("Invalid registration response.");
                }

            } else {

                synchronized (sharedLock) {
                    isRegistered.set(true);
                    sharedLock.notifyAll();
                }

                updateRegistrationDetailsIfChanged(userDetails, deviceDetails, appVersion);

                DonkyNetworkController.getInstance().synchronise();
            }
        } else {

            throw new DonkyException("Cannot register - check provided API key.");
        }
    }

    /**
     * Recover in case the registration was deleted on the server. This method is for Donky Core internal use only.
     */
    public void reRegisterWithSameUserDetails(final DonkyListener listener) {

        // Do not re-register if this is called in response to initial registration failure.
        if (!TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken()) && isRegistered.get()) {

            synchronized (sharedLock) {
                isRegistered.set(false);
                sharedLock.notifyAll();
            }

            new AsyncTask<Void, Void, Exception>() {

                @Override
                protected Exception doInBackground(Void... params) {

                    Exception exception = null;

                    try {

                        UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

                        // Necessary when upgrading from anonymous user.
                        if (userDetails != null && userDetails.getUserDisplayName() == null) {
                            userDetails.setUserDisplayName(userDetails.getUserId());
                        }

                        DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();

                        Register registerRequest = new Register(
                                DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(),
                                userDetails,
                                deviceDetails,
                                DonkyDataController.getInstance().getConfigurationDAO().getAppVersion());

                        log.sensitive(registerRequest.toString());

                        RegisterResponse response = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);

                        if (processRegistrationResponse(response)) {

                            log.sensitive(response.toString());

                            synchronized (sharedLock) {
                                isRegistered.set(true);
                                setSuspended(false);
                                sharedLock.notifyAll();
                            }

                            DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

                        } else {

                            exception = new DonkyException("Error re-registering with the same data. Invalid registration response.");

                        }

                    } catch (Exception e) {

                        exception = e;

                    }

                    return exception;
                }

                @Override
                protected void onPostExecute(Exception exception) {

                    if (exception == null) {

                        log.info("Successfully re-registered into Donky Network.");

                        if (listener != null) {
                            listener.success();
                        }

                    } else {

                        log.error("Error re-registering with the same data.");

                        DonkyException donkyException = new DonkyException("Error re-registering with the same data.");
                        donkyException.initCause(exception);

                        if (listener != null) {
                            listener.error(donkyException, null);
                        }

                    }
                }
            }.execute(null, null, null);
        }
    }

    /**
     * Recover in case the registration was deleted on the server. This method is for Donky Core internal use only.
     */
    public void reRegisterWithSameUserDetailsSynchronously() throws DonkyException {

        // Do not re-register if this is called in response to initial registration failure.
        if (!TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken()) && isRegistered.get()) {

            synchronized (sharedLock) {
                isRegistered.set(false);
                sharedLock.notifyAll();
            }

            UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

            // Necessary when upgrading from anonymous user.
            if (userDetails != null && userDetails.getUserDisplayName() == null) {
                userDetails.setUserDisplayName(userDetails.getUserId());
            }

            DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();

            Register registerRequest = new Register(
                    DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(),
                    userDetails,
                    deviceDetails,
                    DonkyDataController.getInstance().getConfigurationDAO().getAppVersion());

            log.sensitive(registerRequest.toString());

            RegisterResponse response = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);

            if (processRegistrationResponse(response)) {

                log.sensitive(response.toString());

                synchronized (sharedLock) {
                    isRegistered.set(true);
                    setSuspended(false);
                    sharedLock.notifyAll();
                }

                DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

            } else {

                throw new DonkyException("Error re-registering with the same data. Invalid registration response.");

            }
        }
    }

    /**
     * Replaces the current registration with new details.  This will remove the existing registration details and create a new registration (not update the existing one).
     *
     * @param userDetails   User details to use for the registration
     * @param deviceDetails Device details to use for the registration
     * @param listener      The callback to invoke when the process completes. Registration errors will be fed back through this.
     */
    public void replaceRegistration(final UserDetails userDetails, final DeviceDetails deviceDetails, final DonkyListener listener) {


        DonkyNetworkController.getInstance().synchronise(new DonkyListener() {

            @Override
            public void success() {

                final String appVersion = DonkyDataController.getInstance().getConfigurationDAO().getAppVersion();

                Register registerRequest = new Register(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion);

                DonkyNetworkController.getInstance().registerToNetwork(registerRequest, new DonkyResultListener<RegisterResponse>() {

                    @Override
                    public void success(RegisterResponse response) {

                        log.sensitive(response.toString());

                        if (processRegistrationResponse(response)) {

                            final boolean isAnonymousRegistration = ((userDetails == null || TextUtils.isEmpty(userDetails.getUserId())));

                            saveRegistrationData(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion, isAnonymousRegistration);

                            synchronized (sharedLock) {
                                isRegistered.set(true);
                                sharedLock.notifyAll();
                            }

                            log.info("Successfully replaced registration.");

                            DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

                            if (listener != null) {
                                listener.success();
                            }

                        } else {

                            synchronized (sharedLock) {
                                isRegistered.set(true);
                                sharedLock.notifyAll();
                            }

                            log.warning("Reregistration process failed.");

                            if (listener != null) {
                                listener.error(new DonkyException("Error processing registration response"), null);
                            }
                        }

                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        synchronized (sharedLock) {
                            isRegistered.set(true);
                            sharedLock.notifyAll();
                        }

                        log.error("Error replacing registration.", donkyException);

                        if (listener != null) {
                            listener.error(new DonkyException("Error replacing registration."), validationErrors);
                        }
                    }
                });
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                synchronized (sharedLock) {
                    isRegistered.set(true);
                    sharedLock.notifyAll();
                }

                log.error("Error performing synchronisation.", donkyException);

                if (listener != null) {
                    listener.error(new DonkyException("Synchronise before replacing registration failed. Registration change canceled."), validationErrors);
                }
            }
        });
    }

    /**
     * Save Configuration items delivered in registration/authenticate response.
     *
     * @param getConfigurationItems Dictionary of configuration items.
     */
    private void updateConfigurationItems(Map<String, String> getConfigurationItems) {

        if (getConfigurationItems != null) {

            DonkyDataController.getInstance().getConfigurationDAO().updateConfiguration(getConfigurationItems);

            RetryPolicy.setConnectionRetrySchedule(getConfigurationItems.get(ConfigurationDAO.KEY_CONFIGURATION_DeviceCommsConnectionRetrySchedule));

            String senderId = getConfigurationItems.get(ConfigurationDAO.KEY_CONFIGURATION_DefaultGCMSenderId);

            DonkyDataController.getInstance().getConfigurationDAO().setGcmSenderId(senderId);

            String autoSubmitStringValue = getConfigurationItems.get(ConfigurationDAO.KEY_CONFIGURATION_AlwaysSubmitErrors);

            DonkyLoggingController.getInstance().setAutoSubmit(!TextUtils.isEmpty(autoSubmitStringValue) && autoSubmitStringValue.equals("true"));

            String maxMinutesWithoutNotificationExchange = getConfigurationItems.get(ConfigurationDAO.KEY_CONFIGURATION_MaxMinutesWithoutNotificationExchange);

            LifeCycleObserver.getInstance().setMaxMinutesWithoutNotificationExchange(maxMinutesWithoutNotificationExchange);

        }
    }

    /**
     * Save data from registration response.
     *
     * @param response Network registration response.
     * @return True if essential data found.
     */
    private boolean processRegistrationResponse(RegisterResponse response) {

        if (response != null) {

            String networkId = response.getNetworkId();
            String userId = response.getUserId();

            if (!TextUtils.isEmpty(networkId)) {

                UserDetails newUserDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();
                newUserDetails.setUserId(userId);
                DonkyDataController.getInstance().getUserDAO().setUserDetails(newUserDetails);
                DonkyDataController.getInstance().getUserDAO().setUserNetworkId(networkId);

                if (response.getAccessDetails() != null) {

                    String authToken = response.getAccessDetails().getAccessToken();
                    String secureServiceDomain = response.getAccessDetails().getSecureServiceRootUrl();
                    String signalRUrl = response.getAccessDetails().getSignalRUrl();
                    String tokenExpiry = response.getAccessDetails().getExpiresOn();
                    String tokenType = response.getAccessDetails().getTokenType();

                    DonkyDataController.getInstance().getConfigurationDAO().setAuthorisationToken(authToken);
                    DonkyDataController.getInstance().getConfigurationDAO().setTokenType(tokenType);
                    DonkyDataController.getInstance().getConfigurationDAO().setSecureServiceDomain(secureServiceDomain);
                    DonkyDataController.getInstance().getConfigurationDAO().setSignalRUrl(signalRUrl);
                    DonkyDataController.getInstance().getConfigurationDAO().setTokenExpiry(tokenExpiry);

                    if (!TextUtils.isEmpty(secureServiceDomain)) {

                        RestClient.getInstance().setupSecuredRestAdapter(secureServiceDomain);

                    }

                    updateConfigurationItems(response.getAccessDetails().getConfigurationItems());
                }

                return true;

            } else {

                log.warning("Empty network id from the network.");

            }
        }

        return false;
    }

    /**
     * Save data from registration response.
     *
     * @param response Network authenticate response.
     * @return True if essential data found.
     */
    private boolean processLoginResponse(LoginResponse response) {

        if (response != null && response.getAccessToken() != null) {

            DonkyDataController.getInstance().getConfigurationDAO().setAuthorisationToken(response.getAccessToken());
            DonkyDataController.getInstance().getConfigurationDAO().setTokenExpiresInSeconds(response.getExpiresInSeconds());
            DonkyDataController.getInstance().getConfigurationDAO().setTokenType(response.getTokenType());
            DonkyDataController.getInstance().getConfigurationDAO().setTokenExpiry(response.getExpiresOn());
            DonkyDataController.getInstance().getConfigurationDAO().setSecureServiceDomain(response.getSecureServiceRootUrl());
            DonkyDataController.getInstance().getConfigurationDAO().setSignalRUrl(response.getSignalRUrl());

            if (!TextUtils.isEmpty(response.getSecureServiceRootUrl())) {

                RestClient.getInstance().setupSecuredRestAdapter(response.getSecureServiceRootUrl());
            }

            updateConfigurationItems(response.getConfigurationItems());

            return true;
        } else {

            log.warning("No access toke from authenticate response.");

        }

        return false;
    }
}