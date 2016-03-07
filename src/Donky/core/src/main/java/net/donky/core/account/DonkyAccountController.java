package net.donky.core.account;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.donky.core.ChallengeOptions;
import net.donky.core.DonkyAuthenticator;
import net.donky.core.DonkyCore;
import net.donky.core.DonkyCountDownLatch;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.events.RegistrationChangedEvent;
import net.donky.core.events.StandardContactsUpdateEvent;
import net.donky.core.gcm.DonkyGcmController;
import net.donky.core.helpers.IdHelper;
import net.donky.core.lifecycle.LifeCycleObserver;
import net.donky.core.logging.DLog;
import net.donky.core.logging.DonkyLoggingController;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.RetryPolicy;
import net.donky.core.network.restapi.FailureDetails;
import net.donky.core.network.restapi.RestClient;
import net.donky.core.network.restapi.authentication.Login;
import net.donky.core.network.restapi.authentication.LoginAuth;
import net.donky.core.network.restapi.authentication.LoginResponse;
import net.donky.core.network.restapi.authentication.Register;
import net.donky.core.network.restapi.authentication.RegisterAuth;
import net.donky.core.network.restapi.authentication.RegisterResponse;
import net.donky.core.network.restapi.authentication.StartAuth;
import net.donky.core.network.restapi.authentication.StartAuthResponse;
import net.donky.core.network.restapi.secured.UpdateClient;
import net.donky.core.network.restapi.secured.UpdateDevice;
import net.donky.core.network.restapi.secured.UpdateRegistration;
import net.donky.core.network.restapi.secured.UpdateUser;
import net.donky.core.settings.AppSettings;

import java.util.HashMap;
import java.util.List;
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
     * True if user registration is in progress.
     */
    private final AtomicBoolean isAuthenticatedRegistrationInProgress;

    /**
     * True if user has been suspended on the Network.
     */
    private final AtomicBoolean isSuspended;

    /**
     * True if login call is in progress.
     */
    private final AtomicBoolean isLoginInProgress;

    /**
     * True if sdk requires user authentication.
     */
    private final AtomicBoolean isAuthenticationRequired;

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

    /**
     * Callback to get the authentication token from integrator.
     */
    private DonkyAuthenticator donkyAuthenticator;

    // Private constructor. Prevents instantiation from other classes.
    private DonkyAccountController() {
        log = new DLog("AccountController");
        isRegistered = new AtomicBoolean(false);
        isSuspended = new AtomicBoolean(false);
        isLoginInProgress = new AtomicBoolean(false);
        isAuthenticationRequired = new AtomicBoolean(DonkyDataController.getInstance().getConfigurationDAO().getIsAuthenticatingUser());
        isAuthenticatedRegistrationInProgress = new AtomicBoolean(false);
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
        isRegistered.set(DonkyDataController.getInstance().getUserDAO().getUserDetails().getUserId() != null);
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

    public boolean isAuthenticationRequired() {
        return isAuthenticationRequired.get();
    }

    /**
     * This method saves init state for authenticated user SDK mode. Once called the SDK will expect {@link DonkyAuthenticator} callback to return Auth token on every authentication challenge. To be used internally.
     *
     * @param apiKey             Donky API key.
     * @param donkyAuthenticator Callback to obtain auth token.
     * @param appVersion         Application version to save.
     */
    public void startAuthenticationMode(@NonNull String apiKey, @NonNull DonkyAuthenticator donkyAuthenticator, String appVersion) {
        DonkyDataController.getInstance().getConfigurationDAO().setAppVersion(appVersion);
        DonkyDataController.getInstance().getConfigurationDAO().setDonkyNetworkApiKey(apiKey);
        DonkyDataController.getInstance().getConfigurationDAO().setIsAuthenticatingUser(true);
        isAuthenticationRequired.set(true);
        this.donkyAuthenticator = donkyAuthenticator;
    }

    /**
     * This method saves init state for non-authenticated user SDK mode. Once called the SDK will ignore {@link DonkyAuthenticator} callback. To be used internally.
     * @param apiKey
     * @param appVersion
     */
    public void startNonAuthenticationMode(String apiKey, String appVersion) {
        DonkyDataController.getInstance().getConfigurationDAO().setAppVersion(appVersion);
        DonkyDataController.getInstance().getConfigurationDAO().setDonkyNetworkApiKey(apiKey);
        DonkyDataController.getInstance().getConfigurationDAO().setIsAuthenticatingUser(false);
        isAuthenticationRequired.set(false);
        this.donkyAuthenticator = null;
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

            String oldUserId = DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId();

            if (isAuthenticationRequired.get() && !oldUserId.equals(userDetails.getUserId())) {

                log.warning("In authenticated mode the change of user id not allowed");
                if (listener != null) {
                    listener.error(new DonkyException("In authenticated mode the change of user id not allowed"), null);
                }

            } else {

                DonkyNetworkController.getInstance().updateRegistrationOnNetwork(new UpdateRegistration(userDetails, deviceDetails), new DonkyListener() {
                    @Override
                    public void success() {

                        userDetails.setLastUpdated(System.currentTimeMillis());
                        DonkyDataController.getInstance().getUserDAO().setUserDetails(userDetails);

                        DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(deviceDetails);

                        if (listener != null) {
                            listener.success();
                        }

                        DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));
                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                        log.warning("Failed to update registration details, replacing instead.");
                        if (FailureDetails.isValidationErrorMapContainingUserIdAlreadyTaken(validationErrors)) {
                            replaceRegistration(userDetails, deviceDetails, listener);
                        } else if (listener != null) {
                            listener.error(donkyException, validationErrors);
                        }

                    }
                });
            }

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
    public void updateUserDetails(@NonNull final UserDetails user, final DonkyListener listener) {

        if (isAccountReady()) {

            String oldUserId = DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId();

            if (isAuthenticationRequired.get() && !oldUserId.equals(user.getUserId())) {

                log.warning("In authenticated mode the change of user id not allowed");
                if (listener != null) {
                    listener.error(new DonkyException("In authenticated mode the change of user id not allowed"), null);
                }

            } else {

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

                        log.warning("Failed to update user details, replacing instead.");
                        if (FailureDetails.isValidationErrorMapContainingUserIdAlreadyTaken(validationErrors)) {
                            replaceRegistration(user, getDeviceDetails(), listener);
                        } else if (listener != null) {
                            listener.error(donkyException, validationErrors);
                        }

                    }
                });
            }
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

                String newOSVersion = DeviceDetails.getOSVersion();
                String oldOSVersion = DeviceDetails.getOSVersion();

                boolean areRegistrationDetailsDifferent = false;
                boolean areRegistrationDetailsReplaced = false;
                boolean osVersionChanged = (newOSVersion != null && !newOSVersion.equals(oldOSVersion));
                boolean deviceDetailsChanged = deviceDetails != null && !deviceDetails.equals(getDeviceDetails());
                boolean userDetailsChanged = userDetails != null && !userDetails.equals(getCurrentDeviceUser());

                if (userDetailsChanged && (deviceDetailsChanged || osVersionChanged)) {

                    try {
                        (new UpdateRegistration(userDetails, deviceDetails)).performSynchronous();
                    } catch (DonkyException donkyException) {
                        if (FailureDetails.isValidationErrorMapContainingUserIdAlreadyTaken(donkyException.getValidationErrors())) {
                            replaceRegistrationSynchronously(userDetails, deviceDetails);
                            areRegistrationDetailsReplaced = true;
                        }
                    }
                    userDetails.setLastUpdated(System.currentTimeMillis());
                    DonkyDataController.getInstance().getUserDAO().setUserDetails(userDetails);
                    DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(deviceDetails);
                    areRegistrationDetailsDifferent = true;
                    log.info("Registration details changed: user+device");

                } else if (userDetailsChanged) {

                    try {
                        (new UpdateUser(userDetails)).performSynchronous();
                    } catch (DonkyException donkyException) {
                        if (FailureDetails.isValidationErrorMapContainingUserIdAlreadyTaken(donkyException.getValidationErrors())) {
                            replaceRegistrationSynchronously(userDetails, deviceDetails);
                            areRegistrationDetailsReplaced = true;
                        }
                    }
                    userDetails.setLastUpdated(System.currentTimeMillis());
                    DonkyDataController.getInstance().getUserDAO().setUserDetails(userDetails);
                    areRegistrationDetailsDifferent = true;
                    log.info("Registration details changed: user");

                } else if (deviceDetailsChanged || osVersionChanged) {

                    (new UpdateDevice(deviceDetails)).performSynchronous();

                    DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(deviceDetails);
                    if (deviceDetailsChanged) {
                        areRegistrationDetailsDifferent = true;
                    }
                    log.info("Registration details changed: device");

                }

                String newSDKVersion = AppSettings.getVersion();
                String oldSDKVersion = DonkyDataController.getInstance().getSoftwareVersionsDAO().getSavedDonkySDKVersion();

                List<ModuleDefinition> newModules = DonkyCore.getInstance().getRegisteredModules();
                Map<String, String> newModulesVersions = new HashMap<>();
                for (ModuleDefinition moduleDefinition : newModules) {
                    newModulesVersions.put(moduleDefinition.getName(), moduleDefinition.getVersion());
                }
                Map<String, String> oldModulesVersions = DonkyDataController.getInstance().getSoftwareVersionsDAO().getSavedDonkySDKModulesVersions();

                boolean isAppVersionChanged = !TextUtils.isEmpty(appVersion) && !appVersion.equals(DonkyDataController.getInstance().getConfigurationDAO().getAppVersion());
                boolean isSDKModulesChanged = !newModulesVersions.equals(oldModulesVersions);
                boolean isSDKVersionChanged = newSDKVersion != null && !newSDKVersion.equals(oldSDKVersion);

                if (isAppVersionChanged || isSDKModulesChanged || isSDKVersionChanged) {

                    (new UpdateClient(appVersion)).performSynchronous();
                    DonkyDataController.getInstance().getConfigurationDAO().setAppVersion(appVersion);
                    log.warning("Registration details changed: client");

                }

                if (isSDKVersionChanged || isSDKModulesChanged || osVersionChanged) {
                    DonkyDataController.getInstance().getSoftwareVersionsDAO().setSoftwareVersions(newOSVersion, newSDKVersion, newModulesVersions);
                }

                if (areRegistrationDetailsDifferent) {
                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails, areRegistrationDetailsReplaced));
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
     * Authenticate on the Donky Network. This method is blocking and cannot be performed from main thread. This version is for not authenticated user SDK mode.
     *
     * @throws DonkyException
     */
    private void authenticateInNonAuthMode() throws DonkyException {

        if (DonkyCore.isInitialised() && isRegistered()) {

            if (!isLoginInProgress.get()) {

                isLoginInProgress.set(true);

                try {

                    Login loginRequest = new Login();

                    log.sensitive(loginRequest.toString());

                    LoginResponse response = DonkyNetworkController.getInstance().loginToNetwork(loginRequest);

                    if (processLoginResponse(response)) {

                        setSuspended(false);

                        log.sensitive(response.toString());

                    } else {

                        log.error("Invalid authenticate response.");
                        throw new DonkyException("Invalid authenticate response.");

                    }

                    log.info("Successfully logged into Donky Network.");

                } catch (DonkyException donkyException) {
                    handleUserNotFound(donkyException);
                } finally {
                    isLoginInProgress.set(false);
                }
            }

        } else {
            throw new DonkyException("Account is not ready. Please register first.");
        }
    }

    /**
     * Authenticate on the Donky Network. This version is for not authenticated user SDK mode.
     *
     * @param listener Callback to invoke when task is completed.
     */
    private void authenticateInNonAuthMode(final DonkyListener listener) {

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

                                handleUserNotFound(donkyException, validationErrors, listener);

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
     * Authenticate on the Donky Network. This method is blocking and cannot be performed from main thread. This version is for authenticated user SDK mode.
     *
     * @throws DonkyException Thrown when login failed.
     */
    private void authenticateInAuthMode() throws DonkyException {

        if (DonkyCore.isInitialised() && isRegistered()) {

            if (!isLoginInProgress.get()) {

                isLoginInProgress.set(true);

                try {

                    AuthenticationChallengeDetails details = challengeAuthentication(DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId());

                    LoginAuth loginRequest = new LoginAuth(details);

                    log.sensitive(loginRequest.toString());

                    LoginResponse response = DonkyNetworkController.getInstance().loginToNetwork(loginRequest);

                    if (processLoginResponse(response)) {

                        setSuspended(false);

                        log.sensitive(response.toString());

                    } else {

                        log.error("Invalid authenticate response.");
                        throw new DonkyException("Invalid authenticate response.");

                    }

                    log.info("Successfully logged into Donky Network.");

                } catch (DonkyException donkyException) {
                    handleUserNotFound(donkyException);
                } finally {
                    isLoginInProgress.set(false);
                }

            }

        } else {
            throw new DonkyException("Account is not ready. Please register first.");
        }
    }

    /**
     * Authenticate on the Donky Network. This method is blocking and cannot be performed from main thread. This version is for authenticated user SDK mode.
     *
     * @param listener Callback to invoke when task is completed.
     */
    private void authenticateInAuthMode(final DonkyListener listener) {

        Map<String, String> validationIssues = checkIfCanChallengeAuthentication();

        if (validationIssues.isEmpty()) {

            final String forUserID = DonkyAccountController.getInstance().getCurrentDeviceUser().getUserId();

            challengeAuthentication(forUserID, new DonkyResultListener<AuthenticationChallengeDetails>() {

                @Override
                public void success(AuthenticationChallengeDetails result) {

                    if (result.getToken() != null) {

                        LoginAuth loginRequest = new LoginAuth(result);

                        log.sensitive(loginRequest.toString());

                        DonkyNetworkController.getInstance().loginToNetwork(loginRequest, new DonkyResultListener<LoginResponse>() {

                            @Override
                            public void success(LoginResponse response) {

                                isLoginInProgress.set(false);

                                log.sensitive(response.toString());

                                if (processLoginResponse(response)) {

                                    setSuspended(false);

                                    if (listener != null) {
                                        listener.success();
                                    }

                                } else {

                                    if (listener != null) {
                                        listener.error(new DonkyException("Invalid authenticate response."), null);
                                    }

                                }
                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

                                isLoginInProgress.set(false);

                                handleUserNotFound(donkyException, validationErrors, listener);

                            }
                        });
                    } else {

                        isLoginInProgress.set(false);

                        log.warning("Auth failed. Null token provided by integrators authenticator callback.");
                        if (listener != null) {
                            listener.error(new DonkyException("Auth failed. Null token provided by integrators authenticator callback."), null);
                        }

                    }
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
                listener.error(new DonkyException("Validation errors when trying to authenticate user."), validationIssues);
            }
        }
    }

    /**
     * Authenticate on the Donky Network. This method is blocking and cannot be performed from main thread.
     *
     * @throws DonkyException
     */
    public void authenticate() throws DonkyException {
        if (isAuthenticationRequired.get()) {
            authenticateInAuthMode();
        } else {
            authenticateInNonAuthMode();
        }
    }

    /**
     * Authenticate on the Donky Network.
     *
     * @param listener Callback to invoke when task is completed.
     */
    public void authenticate(final DonkyListener listener) {
        if (isAuthenticationRequired.get()) {
            authenticateInAuthMode(listener);
        } else {
            authenticateInNonAuthMode(listener);
        }
    }

    /**
     * Handle internally a scenario in which user has been deleted on the Network. Re-registering with local data.
     */
    private void handleUserNotFound(DonkyException donkyException, Map<String, String> validationErrors, DonkyListener donkyListener) {
        if (validationErrors.containsKey("AuthenticationDetail") && validationErrors.get("AuthenticationDetail").equals("UserNotFound")) {
            reRegisterWithSameUserDetails(donkyListener);
        } else if (donkyListener != null) {
            donkyListener.error(donkyException, validationErrors);
        }
    }

    /**
     * Handle internally a scenario in which user has been deleted on the Network. Re-registering with local data.
     */
    private void handleUserNotFound(DonkyException donkyException) throws DonkyException {
        if (donkyException != null) {
            Map<String, String> validationErrors = donkyException.getValidationErrors();
            if (validationErrors != null && validationErrors.containsKey("AuthenticationDetail") && validationErrors.get("AuthenticationDetail").equals("UserNotFound")) {
                reRegisterWithSameUserDetailsSynchronously();
            } else {
                throw donkyException;
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
        user.setLastUpdated(System.currentTimeMillis());
        DonkyDataController.getInstance().getUserDAO().setUserDetails(user);

        if (deviceDetails != null) {

            DonkyDataController.getInstance().getDeviceDAO().setDeviceDetails(new DeviceDetails(
                    deviceDetails.getDeviceName(),
                    deviceDetails.getDeviceType(),
                    deviceDetails.getAdditionalProperties()));
        }

        DonkyDataController.getInstance().getConfigurationDAO().setAppVersion(appVersion);

        DonkyDataController.getInstance().getSoftwareVersionsDAO().setSoftwareVersions(DeviceDetails.getOSVersion(), AppSettings.getVersion(), DonkyCore.getInstance().getRegisteredModules());
    }

    /**
     * Register device on the donky Network and save all registration details on the device. If user is already registered the old registration details will be kept.
     *
     * @param apiKey        Donky Network identifier for application space
     * @param userDetails   User details to use for the registration.
     * @param deviceDetails Device details to use for the registration.
     * @param appVersion    Application version.
     * @throws DonkyException
     */
    public void register(final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, String appVersion) throws DonkyException {
        register(apiKey, userDetails, deviceDetails, appVersion, false);
    }

    /**
     * Register device on the donky Network and save all registration details on the device. The created user authentication on Donky Network will require Open Auth token to be supplied by {@link DonkyAuthenticator} object.
     * If user is already registered the old registration details will be kept.
     *
     * @param userDetails   User details to use for the registration.
     * @param deviceDetails Device details to use for the registration.
     * @param appVersion    Application version.
     * @param listener      donkyAuthenticator
     * @throws DonkyException
     */
    public void registerAuthenticated(final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, final DonkyListener listener) {
        registerAuthenticated(userDetails, deviceDetails, appVersion, !TextUtils.isEmpty(DonkyDataController.getInstance().getUserDAO().getUserNetworkId()), listener);
    }

    /**
     * Register device on the donky Network and save all registration details on the device. The created user authentication on Donky Network will require Open Auth token to be supplied by {@link DonkyAuthenticator} object.
     * If user is already registered the old registration details will be kept.
     *
     * @param userDetails   User details to use for the registration.
     * @param deviceDetails Device details to use for the registration.
     * @param appVersion    Application version.
     * @param listener      donkyAuthenticator
     * @throws DonkyException
     */
    private void registerAuthenticated(final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, final boolean isReplacement, final DonkyListener listener) {

        final String apiKey = DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey();
        final Map<String, String> validationIssues = checkIfCanRegisterAuthenticated(apiKey);

        if (validationIssues.isEmpty()) {

            isAuthenticatedRegistrationInProgress.set(true);

            //clear the GCM registration
            DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);
            DonkyDataController.getInstance().getUserDAO().setUserNetworkId(null);
            DonkyDataController.getInstance().getDeviceDAO().setDeviceSecret(null);
            isRegistered.set(false);

            challengeAuthentication(null, new DonkyResultListener<AuthenticationChallengeDetails>() {

                @Override
                public void success(final AuthenticationChallengeDetails authDetails) {

                    if (!TextUtils.isEmpty(authDetails.getToken())) {

                        DonkyCore.getInstance().processInBackground(new Runnable() {

                            @Override
                            public void run() {

                                DonkyException exception = null;

                                try {
                                    register(apiKey, userDetails, deviceDetails, appVersion, false, authDetails, isReplacement);
                                } catch (DonkyException e) {
                                    exception = e;
                                    if (listener != null) {
                                        listener.error(e, e.getValidationErrors());
                                    }
                                } finally {
                                    isAuthenticatedRegistrationInProgress.set(false);
                                }

                                if (exception == null) {

                                    DonkyGcmController.getInstance().registerPush(new DonkyListener() {

                                        @Override
                                        public void success() {
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
                            }
                        });
                    } else {
                        isAuthenticatedRegistrationInProgress.set(false);
                        log.warning("Auth failed. Null token provided by integrators authenticator callback.");
                        if (listener != null) {
                            listener.error(new DonkyException("Null token provided by integrators authenticator callback."), null);
                        }
                    }
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    isAuthenticatedRegistrationInProgress.set(false);
                    if (listener != null) {
                        listener.error(donkyException, validationErrors);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.error(new DonkyException("Validation errors when trying to register user."), validationIssues);
            }
        }
    }

    /**
     * Register device on the donky Network and save all registration details on the device. The created user authentication on Donky Network will require Open Auth token to be supplied by {@link DonkyAuthenticator} object.
     * If user is already registered the old registration details will be kept.
     *
     * @param userDetails   User details to use for the registration.
     * @param deviceDetails Device details to use for the registration.
     * @param appVersion    Application version.
     * @throws DonkyException
     */
    public void registerAuthenticated(final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion) throws DonkyException {
        registerAuthenticated(userDetails, deviceDetails, appVersion, !TextUtils.isEmpty(DonkyDataController.getInstance().getUserDAO().getUserNetworkId()));
    }

    /**
     * Register device on the donky Network and save all registration details on the device. The created user authentication on Donky Network will require Open Auth token to be supplied by {@link DonkyAuthenticator} object.
     * If user is already registered the old registration details will be kept.
     *
     * @param userDetails   User details to use for the registration.
     * @param deviceDetails Device details to use for the registration.
     * @param appVersion    Application version.
     * @throws DonkyException
     */
    private void registerAuthenticated(final UserDetails userDetails, final DeviceDetails deviceDetails, final String appVersion, final boolean isReplacement) throws DonkyException {

        final String apiKey = DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey();
        final Map<String, String> validationIssues = checkIfCanRegisterAuthenticated(apiKey);

        if (validationIssues.isEmpty()) {

            isAuthenticatedRegistrationInProgress.set(true);

            DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);
            DonkyDataController.getInstance().getUserDAO().setUserNetworkId(null);
            DonkyDataController.getInstance().getDeviceDAO().setDeviceSecret(null);
            isRegistered.set(false);

            AuthenticationChallengeDetails authDetails = challengeAuthentication(null);

            if (!TextUtils.isEmpty(authDetails.getToken())) {

                try {
                    register(apiKey, userDetails, deviceDetails, appVersion, false, authDetails, isReplacement);
                } finally {
                    isAuthenticatedRegistrationInProgress.set(false);
                }

            } else {
                isAuthenticatedRegistrationInProgress.set(false);
                throw new DonkyException("Null token provided by integrators authenticator callback.");
            }

        } else {
            throw new DonkyException("Validation errors when trying to authenticate user.");
        }
    }

    /**
     * Register device on the donky Network and save all registration details on the device. The created user authentication on Donky Network will require Open Auth token to be supplied by {@link DonkyAuthenticator} object.
     * If user is already registered the old registration details will be kept.
     *
     * @param listener donkyAuthenticator
     * @throws DonkyException
     */
    public void registerAuthenticated(final DonkyListener listener) {
        registerAuthenticated(null, null, null, listener);
    }

    /**
     * Validate if SDK state allows calling registration method.
     *
     * @param apiKey Donky API key.
     * @return Map of validation issues
     */
    private Map<String, String> checkIfCanRegisterAuthenticated(final String apiKey) {

        Map<String, String> validationErrors = new HashMap<>();

        synchronized (sharedLock) {
            if (context == null) {
                validationErrors.put("Context", "Null context. Cannot register authenticated");
            } else if (!DonkyCore.isInitialised()) {
                validationErrors.put("NotInitialised", "SDK not initialised. Cannot register authenticated");
            } else if (!isAuthenticationRequired.get()) {
                log.warning("SDK not initialised in authenticated user mode. Cannot register authenticated");
                validationErrors.put("NotAuth", "SDK not initialised in authenticated user mode. Cannot register authenticated");
            } else if (donkyAuthenticator == null) {
                log.error("Authenticator not provided. Cannot authenticate.");
                validationErrors.put("Authenticator", "Null authenticator. Cannot register in authenticated mode.");
            } else if (TextUtils.isEmpty(apiKey)) {
                log.error("Null Api Key. Cannot register authenticated");
                validationErrors.put("ApiKey", "Null Api Key. Cannot register authenticated");
            } else if (isAuthenticatedRegistrationInProgress.get()) {
                validationErrors.put("InProgress", "User registration in progress. Cannot register authenticated");
            }
            sharedLock.notifyAll();
        }
        return validationErrors;
    }

    /**
     * Validate if SDK state allows calling authentication in auth SDK mode method.
     *
     * @return Map of validation issues
     */
    private Map<String, String> checkIfCanChallengeAuthentication() {

        Map<String, String> validationErrors = new HashMap<>();

        synchronized (sharedLock) {
            if (context == null) {
                log.warning("Null context. Cannot authenticate");
                validationErrors.put("Context", "Null context. Cannot authenticate");
            } else if (!DonkyCore.isInitialised()) {
                log.warning("Not initialised. Cannot authenticate.");
                validationErrors.put("NotInitialised", "Not initialised. Cannot authenticate.");
            } else if (!isAuthenticationRequired.get()) {
                log.warning("SDK not initialised in authenticated user mode. Cannot authenticate.");
                validationErrors.put("NotAuth", "SDK not initialised in authenticated user mode. Cannot authenticate.");
            } else if (donkyAuthenticator == null) {
                log.error("Authenticator not provided. Cannot authenticate.");
                validationErrors.put("Authenticator", "Authenticator not provided. Cannot authenticate");
            } else if (isAuthenticatedRegistrationInProgress.get()) {
                validationErrors.put("RegistrationInProgress", "User registration in progress. Cannot authenticate.");
            } else if (isLoginInProgress.get()) {
                validationErrors.put("InProgress", "User authentication in progress. Cannot register authenticated");
            }
            sharedLock.notifyAll();
        }
        return validationErrors;
    }

    /**
     * Obtain nonce, correlation id and obtain the auth token from Authenticator provided in initialisation method.
     */
    private AuthenticationChallengeDetails challengeAuthentication(final String forUserID) throws DonkyException {

        final DonkyCountDownLatch<AuthenticationChallengeDetails> doneSignal = new DonkyCountDownLatch<>(1);

        challengeAuthentication(forUserID, new DonkyResultListener<AuthenticationChallengeDetails>() {
            @Override
            public void success(AuthenticationChallengeDetails result) {
                doneSignal.setResult(result);
                doneSignal.countDown();
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                doneSignal.setError(donkyException, validationErrors);
                doneSignal.countDown();
            }
        });

        DonkyException donkyException;

        AuthenticationChallengeDetails result = null;
        try {
            doneSignal.await();
            donkyException = doneSignal.getDonkyException();
            result = doneSignal.getResult();
        } catch (InterruptedException e) {
            donkyException = new DonkyException("InterruptedException when waiting for authentication");
            donkyException.initCause(e);
        }

        if (donkyException != null) {
            throw donkyException;
        }

        if (result == null) {
            throw new DonkyException("Null authorisation details while login.");
        }

        return result;
    }

    /**
     * Obtain nonce, correlation id and obtain the auth token from Authenticator provided in initialisation method.
     *
     * @param listener Callback with authentication challenge details.
     */
    private void challengeAuthentication(final String forUserID, final DonkyResultListener<AuthenticationChallengeDetails> listener) {

        final StartAuth startAuth = new StartAuth();

        DonkyNetworkController.getInstance().startAuthentication(startAuth, new DonkyResultListener<StartAuthResponse>() {

            @Override
            public void success(final StartAuthResponse authDetails) {

                if (donkyAuthenticator != null && authDetails != null) {

                    try {

                        donkyAuthenticator.onAuthenticationChallenge(new DonkyAuthClient() {
                            @Override
                            public void authenticateWithToken(String token) {
                                if (TextUtils.isEmpty(token)) {
                                    log.warning("Null token provided by integrator");
                                }
                                if (listener != null) {
                                    listener.success(new AuthenticationChallengeDetails(authDetails.getAuthenticationId(), authDetails.getNonce(), token));
                                }
                            }
                        }, new ChallengeOptions(forUserID, authDetails.getNonce()));

                    } catch (Exception e) {
                        if (listener != null) {
                            DonkyException donkyException = new DonkyException("Donky authenticator failed to obtain token from integrator app");
                            donkyException.initCause(e);
                            listener.error(donkyException, null);
                        }
                    }

                } else {
                    if (listener != null) {
                        listener.error(new DonkyException("Auth. start failed. Check if authenticator was set."), null);
                    }
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
     * If no user has been registered yet and SDK has been initialised in non authenticated user mode this method will create a new user/device on the network and save data locally. If successful the SDK will be ready use.
     *
     * @param userDetails   User details to use for the registration or update.
     * @param deviceDetails Device details to use for the registration or update.
     * @param listener Callback with The callback to invoke when the command has executed. Registration errors will be fed back through this.

     */
    public void register(final UserDetails userDetails, final DeviceDetails deviceDetails, final DonkyListener listener) {

        if (isRegistered()) {
            log.warning("User already registered. Registration cancelled.");
            DonkyCore.getInstance().postError(new Handler(Looper.getMainLooper()), listener, new DonkyException("User already registered. Non-auth registration cancelled."));
        } else if (isAuthenticationRequired()) {
            log.warning("SDK initialised in authenticated mode. Non-auth registration cancelled.");
            DonkyCore.getInstance().postError(new Handler(Looper.getMainLooper()), listener, new DonkyException("SDK initialised in authenticated mode. Non-auth registration cancelled."));
        } else {
            DonkyCore.getInstance().processInBackground(new Runnable() {
                @Override
                public void run() {

                    DonkyException donkyException = null;

                    try {
                        register(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, DonkyDataController.getInstance().getConfigurationDAO().getAppVersion(), false, null, true);
                    } catch (DonkyException e) {
                        donkyException = e;
                        log.error(e.getLocalizedMessage(), donkyException);
                        DonkyCore.getInstance().postError(new Handler(Looper.getMainLooper()), listener, donkyException);
                    }

                    if (donkyException == null) {
                        DonkyGcmController.getInstance().registerPush(listener);
                    }
                }
            });
        }
    }

    /**
     * Method for internal usage only. Register device on the donky Network and save all registration details on the device. If user is already registered update registration data.
     * This method will also authenticate user and perform initial synchronisation.
     *
     * @param apiKey        Donky Network identifier for application space
     * @param userDetails   User details to use for the registration or update.
     * @param deviceDetails Device details to use for the registration or update.
     * @param appVersion    Application version.
     * @param overrideUserDetails Should update user details if different.
     * @throws DonkyException
     */
    public void register(final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, String appVersion, boolean overrideUserDetails) throws DonkyException {
        register(apiKey, userDetails, deviceDetails, appVersion, overrideUserDetails, null, true);
    }

    /**
     * Register device on the donky Network and save all registration details on the device. If user is already registered update registration data.
     * This method will also authenticate user and perform initial synchronisation.
     *
     * @param apiKey              Donky Network identifier for application space
     * @param userDetails         User details to use for the registration or update.
     * @param deviceDetails       Device details to use for the registration or update.
     * @param appVersion          Application version.
     * @param overrideCurrentUser Should update user details if different.
     * @param authDetails         Details for authentication with external system.
     * @param isReplacement       Is this registration replacing another user.
     * @throws DonkyException
     */
    private void register(final String apiKey, final UserDetails userDetails, final DeviceDetails deviceDetails, String appVersion, boolean overrideCurrentUser, AuthenticationChallengeDetails authDetails, boolean isReplacement) throws DonkyException {

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

                Register registerRequest;
                RegisterResponse response;

                if (authDetails != null) {
                    RegisterAuth registerAuthRequest = new RegisterAuth(apiKey, userDetails, deviceDetails, appVersion, overrideCurrentUser, authDetails);
                    log.sensitive(registerAuthRequest.toString());
                    response = DonkyNetworkController.getInstance().registerToNetwork(registerAuthRequest);
                    registerRequest = registerAuthRequest;
                } else {
                    registerRequest = new Register(apiKey, userDetails, deviceDetails, appVersion, overrideCurrentUser);
                    log.sensitive(registerRequest.toString());
                    response = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);
                }

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

                    if (isAnonymousRegistration) {
                        UserDetails newUser = DonkyAccountController.getInstance().getCurrentDeviceUser();
                        DeviceDetails newDevice = DonkyAccountController.getInstance().getDeviceDetails();
                        DonkyCore.publishLocalEvent(new RegistrationChangedEvent(newUser, newDevice, isReplacement));
                    } else {
                        DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails, isReplacement));
                    }

                    DonkyNetworkController.getInstance().synchronise();

                    DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);

                } else {

                    log.warning("Error registering. Registration process not completed. " + response.toString());

                    throw new DonkyException("Invalid registration response.", registerRequest.getValidationFailures());
                }

                Map<String, String> failures = registerRequest.getValidationFailures();

                if (failures != null && !failures.isEmpty()) {

                    throw new DonkyException("Validation Failures for registration.", failures);

                }

            } else if (overrideCurrentUser) {

                synchronized (sharedLock) {
                    isRegistered.set(true);
                    sharedLock.notifyAll();
                }

                updateRegistrationDetailsIfChanged(userDetails, deviceDetails, appVersion);

                DonkyNetworkController.getInstance().synchronise();

            } else {

                synchronized (sharedLock) {
                    isRegistered.set(true);
                    sharedLock.notifyAll();
                }

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
        if ((!TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken()) || isAuthenticationRequired()) && isRegistered.get()) {

            synchronized (sharedLock) {
                isRegistered.set(false);
                sharedLock.notifyAll();
            }

            if (!isAuthenticationRequired.get()) {

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
                                    DonkyDataController.getInstance().getConfigurationDAO().getAppVersion(), true);

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

            } else {

                final UserDetails userDetails = DonkyAccountController.getInstance().getCurrentDeviceUser();

                // Necessary when upgrading from anonymous user.
                if (userDetails != null && userDetails.getUserDisplayName() == null) {
                    userDetails.setUserDisplayName(userDetails.getUserId());
                }

                final DeviceDetails deviceDetails = DonkyAccountController.getInstance().getDeviceDetails();

                registerAuthenticated(userDetails, deviceDetails, DonkyDataController.getInstance().getConfigurationDAO().getAppVersion(), false, new DonkyListener() {

                    @Override
                    public void success() {
                        setSuspended(false);
                        log.info("Successfully re-registered into Donky Network.");
                        registerPushIfEmpty();
                        if (listener != null) {
                            listener.success();
                        }
                    }

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                        log.error("Error re-registering with the same data.");
                        if (listener != null) {
                            listener.error(donkyException, validationErrors);
                        }
                    }
                });
            }
        } else if (listener != null) {
            listener.error(new DonkyException("Wrong SDK state when attempting to re-register"), null);
        }
    }

    /**
     * Recover in case the registration was deleted on the server. This method is for Donky Core internal use only.
     */
    public void reRegisterWithSameUserDetailsSynchronously() throws DonkyException {

        // Do not re-register if this is called in response to initial registration failure.
        if ((!TextUtils.isEmpty(DonkyDataController.getInstance().getConfigurationDAO().getAuthorisationToken()) || isAuthenticationRequired()) && isRegistered.get()) {

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

            if (!isAuthenticationRequired.get()) {

                Register registerRequest = new Register(
                        DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(),
                        userDetails,
                        deviceDetails,
                        DonkyDataController.getInstance().getConfigurationDAO().getAppVersion(),
                        true);

                log.sensitive(registerRequest.toString());

                RegisterResponse response = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);

                if (processRegistrationResponse(response)) {

                    log.sensitive(response.toString());

                    synchronized (sharedLock) {
                        isRegistered.set(true);
                        setSuspended(false);
                        sharedLock.notifyAll();
                    }

                    registerPushIfEmpty();

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

                } else {
                    throw new DonkyException("Error re-registering with the same data. Invalid registration response.");
                }

            } else {

                registerAuthenticated(userDetails, deviceDetails, DonkyDataController.getInstance().getConfigurationDAO().getAppVersion(), false);

                setSuspended(false);
                registerPushIfEmpty();
                DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails));

            }
        } else {
            throw new DonkyException("Wrong SDK state when attempting to re-register");
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

        if ((isAuthenticationRequired.get() && !isRegistered()) || isUserSuspended()) {
            doReplaceUser(userDetails, deviceDetails, listener);
        } else {
            DonkyNetworkController.getInstance().synchronise(new DonkyListener() {

                @Override
                public void success() {
                    doReplaceUser(userDetails, deviceDetails, listener);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    log.error("Error performing synchronisation.", donkyException);
                    doReplaceUser(userDetails, deviceDetails, listener);
                }
            });
        }
    }

    private void doReplaceUser(final UserDetails userDetails, final DeviceDetails deviceDetails, final DonkyListener listener) {

        if (!isAuthenticationRequired()) {
            synchronized (sharedLock) {
                isRegistered.set(false);
                sharedLock.notifyAll();
            }

            //clear the GCM registration
            DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);

            final String appVersion = DonkyDataController.getInstance().getConfigurationDAO().getAppVersion();

            final String newDeviceSecret = IdHelper.generateId();

            Register registerRequest = new Register(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion, true);
            registerRequest.replaceDeviceSecret(newDeviceSecret);

            DonkyNetworkController.getInstance().registerToNetwork(registerRequest, new DonkyResultListener<RegisterResponse>() {

                @Override
                public void success(RegisterResponse response) {
                    handleReplaceRegistrationSuccess(response, userDetails, deviceDetails, newDeviceSecret, appVersion, listener);
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
        } else {
            registerAuthenticated(userDetails, deviceDetails, DonkyDataController.getInstance().getConfigurationDAO().getAppVersion(), new DonkyListener() {

                @Override
                public void success() {

                    log.info("Successfully replaced registration.");

                    registerPushIfEmpty();

                    if (listener != null) {
                        listener.success();
                    }
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    log.warning("Error replacing registration.");
                    if (listener != null) {
                        listener.error(donkyException, validationErrors);
                    }
                }
            });
        }
    }

    private void handleReplaceRegistrationSuccess(final RegisterResponse response, final UserDetails userDetails, final DeviceDetails deviceDetails, String deviceSecret, String appVersion, final DonkyListener listener) {

        log.sensitive(response.toString());

        DonkyDataController.getInstance().getConfigurationDAO().setAuthorisationToken(null);

        if (processRegistrationResponse(response)) {

            DonkyDataController.getInstance().getDeviceDAO().setDeviceSecret(deviceSecret);

            final boolean isAnonymousRegistration = ((userDetails == null || TextUtils.isEmpty(userDetails.getUserId())));

            saveRegistrationData(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion, isAnonymousRegistration);

            synchronized (sharedLock) {
                isRegistered.set(true);
                sharedLock.notifyAll();
            }

            log.info("Successfully replaced registration.");

            registerPushIfEmpty();

            DonkyNetworkController.getInstance().synchronise(new DonkyListener() {

                @Override
                public void success() {

                    DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails, true));

                    if (listener != null) {
                        listener.success();
                    }
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    if (listener != null) {
                        listener.error(new DonkyException("Error synchronising after replacing registration."), null);
                    }
                }
            });

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

    /**
     * Replaces the current registration with new details.  This will remove the existing registration details and create a new registration (not update the existing one).
     * This is a blocking method.
     *
     * @param userDetails   User details to use for the registration
     * @param deviceDetails Device details to use for the registration
     */
    private void replaceRegistrationSynchronously(final UserDetails userDetails, final DeviceDetails deviceDetails) throws DonkyException {

        DonkyNetworkController.getInstance().synchroniseSynchronously();

        //clear the GCM registration
        DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);

        final String newDeviceSecret = IdHelper.generateId();

        final String appVersion = DonkyDataController.getInstance().getConfigurationDAO().getAppVersion();

        if (!isAuthenticationRequired()) {

            Register registerRequest = new Register(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion, true);
            registerRequest.replaceDeviceSecret(newDeviceSecret);

            RegisterResponse registerResponse = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);

            processReplaceRegistrationSuccess(registerResponse, userDetails, deviceDetails, newDeviceSecret, appVersion);

        } else {

            isAuthenticatedRegistrationInProgress.set(true);

            try {

                AuthenticationChallengeDetails details = challengeAuthentication(null);

                //clear the GCM registration
                if (isRegistered()) {
                    DonkyDataController.getInstance().getConfigurationDAO().setGcmRegistrationId(null);
                    DonkyDataController.getInstance().getUserDAO().setUserNetworkId(null);
                    DonkyDataController.getInstance().getUserDAO().setUserDetails(new UserDetails());
                    DonkyDataController.getInstance().getDeviceDAO().setDeviceSecret(null);
                    isRegistered.set(false);
                }

                if (details != null && details.getToken() != null) {

                    RegisterAuth registerRequest = new RegisterAuth(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion, true, details);
                    registerRequest.replaceDeviceSecret(newDeviceSecret);

                    RegisterResponse registerResponse = DonkyNetworkController.getInstance().registerToNetwork(registerRequest);

                    processReplaceRegistrationSuccess(registerResponse, userDetails, deviceDetails, newDeviceSecret, appVersion);

                } else {
                    throw new DonkyException("Authentication details missing.");
                }

            } finally {
                isAuthenticatedRegistrationInProgress.set(false);
            }
        }
    }

    private void processReplaceRegistrationSuccess(RegisterResponse registerResponse, UserDetails userDetails, DeviceDetails deviceDetails, String newDeviceSecret, String appVersion) {

        log.sensitive(registerResponse.toString());

        DonkyDataController.getInstance().getConfigurationDAO().setAuthorisationToken(null);

        if (processRegistrationResponse(registerResponse)) {

            DonkyDataController.getInstance().getDeviceDAO().setDeviceSecret(newDeviceSecret);

            final boolean isAnonymousRegistration = ((userDetails == null || TextUtils.isEmpty(userDetails.getUserId())));

            saveRegistrationData(DonkyDataController.getInstance().getConfigurationDAO().getDonkyNetworkApiKey(), userDetails, deviceDetails, appVersion, isAnonymousRegistration);

            synchronized (sharedLock) {
                isRegistered.set(true);
                sharedLock.notifyAll();
            }

            log.info("Successfully replaced registration.");

            DonkyCore.publishLocalEvent(new RegistrationChangedEvent(userDetails, deviceDetails, true));

            registerPushIfEmpty();

        } else {

            synchronized (sharedLock) {
                isRegistered.set(true);
                sharedLock.notifyAll();
            }

            log.error("Reregistration process failed. processRegistrationResponse returned false.");

        }

    }

    private void registerPushIfEmpty() {

        String gcmRegistrationId = DonkyDataController.getInstance().getConfigurationDAO().getGcmRegistrationId();

        if (TextUtils.isEmpty(gcmRegistrationId)) {
            DonkyGcmController.getInstance().registerPush(null);
        }
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
                newUserDetails.setLastUpdated(System.currentTimeMillis());
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

                    if (response.getAccessDetails().getStandardContacts() != null && response.getAccessDetails().getStandardContacts().getStandardContactsList() != null && !response.getAccessDetails().getStandardContacts().getStandardContactsList().isEmpty()) {
                        DonkyCore.publishLocalEvent(new StandardContactsUpdateEvent(response.getAccessDetails().getStandardContacts()));
                    }

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

            DonkyNetworkController.getInstance().startSignalR();

            if (response.getStandardContacts() != null && response.getStandardContacts().getStandardContactsList() != null && !response.getStandardContacts().getStandardContactsList().isEmpty()) {
                DonkyCore.publishLocalEvent(new StandardContactsUpdateEvent(response.getStandardContacts()));
            }

            return true;

        } else {

            log.warning("No access toke from authenticate response.");

        }

        return false;
    }
}