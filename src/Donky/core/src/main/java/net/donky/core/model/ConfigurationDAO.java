package net.donky.core.model;

import android.content.Context;

import net.donky.core.ModuleDefinition;

import java.util.Map;

/**
 * Database Access Object for configuration settings.
 *
 * Created by Marcin Swierczek
 * 22/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ConfigurationDAO extends SharedPreferencesBaseDAO {

    /**
     * File name for device details storage.
     */
    private static final String SHARED_PREFERENCES_FILENAME_INTERNAL = "PreferencesInternal";

    /**
     * Key names for Shared Preferences storage.
     */
    private static final String KEY_GCM_REGISTRATION_ID = "registrationId";
    private static final String KEY_GCM_SENDER_ID = "senderId";
    private static final String KEY_DONKY_API_KEY = "apiKey";
    private static final String KEY_APP_VERSION = "appVersion";
    private static final String KEY_GCM_REGISTRATION_APP_VERSION_CODE = "appVersionCode";
    private static final String KEY_GCM_ALLOW_PUSH_NOTIFICATIONS = "allowPush";
    private static final String KEY_USER_SUSPENDED = "userSuspended";
    private static final String KEY_SECURE_SERVICE_DOMAIN = "secureServiceDomain";
    private static final String KEY_AUTHORISATION_TOKEN = "authorisationToken";
    private static final String KEY_SIGNAL_R_URL = "signalRUrl";
    private static final String KEY_TOKEN_EXPIRY = "tokenExpiry";
    private static final String KEY_TOKEN_TYPE = "tokenType";
    private static final String KEY_TOKEN_EXPIRES_IN_SECONDS = "tokenExpiresInSeconds";
    private static final String KEY_MODULES_KEY_SET = "extVerKeySet";
    private static final String KEY_CONFIGURATION_ITEMS = "configurationItemsKeySet";

    public static final String KEY_CONFIGURATION_DefaultGCMSenderId = "DefaultGCMSenderId";
    public static final String KEY_CONFIGURATION_MinimumClientVersion = "MinimumClientVersion";
    public static final String KEY_CONFIGURATION_AssetDownloadNamedFileUrlFormat= "AssetDownloadNamedFileUrlFormat";
    public static final String KEY_CONFIGURATION_AssetDownloadUrlFormat = "AssetDownloadUrlFormat";
    public static final String KEY_CONFIGURATION_AlwaysSubmitErrors = "AlwaysSubmitErrors";
    public static final String KEY_CONFIGURATION_ContactDiscoveryMaxItems = "ContactDiscoveryMaxItems";
    public static final String KEY_CONFIGURATION_DeviceCommsConnectionRetrySchedule = "DeviceCommsConnectionRetrySchedule";
    public static final String KEY_CONFIGURATION_DeviceCommsFeatureOn = "DeviceCommsFeatureOn";
    public static final String KEY_CONFIGURATION_DeviceCommsPingInterval = "DeviceCommsPingInterval";
    public static final String KEY_CONFIGURATION_ForwardingOverlayMessageMaxLength = "ForwardingOverlayMessageMaxLength";
    public static final String KEY_CONFIGURATION_MaxAttachments = "MaxAttachments";
    public static final String KEY_CONFIGURATION_MaximumGroupParticipants = "MaximumGroupParticipants";
    public static final String KEY_CONFIGURATION_MaxMinutesWithoutNotificationExchange = "MaxMinutesWithoutNotificationExchange";
    public static final String KEY_CONFIGURATION_RichMessageAvailabilityDays = "RichMessageAvailabilityDays";
    public static final String KEY_CONFIGURATION_ServerUnavailablePollPeriod = "ServerUnavailablePollPeriod";
    public static final String KEY_CONFIGURATION_ServerUnavailableWarningPeriodSeconds = "ServerUnavailableWarningPeriodSeconds";
    public static final String KEY_CONFIGURATION_UploadImageJpegQuality = "UploadImageJpegQuality";
    public static final String KEY_CONFIGURATION_UploadImageMaxResX = "UploadImageMaxResX";
    public static final String KEY_CONFIGURATION_UploadImageMaxResY = "UploadImageMaxResY";
    public static final String KEY_CONFIGURATION_CustomContentMaxSizeBytes = "CustomContentMaxSizeBytes";
    public static final String KEY_CONFIGURATION_LocationUpdateIntervalSeconds = "LocationUpdateIntervalSeconds";

    public static int DEFAULT_RICH_MESSAGE_AVAILABILITY_DAYS = 30;

    public ConfigurationDAO(Context context) {
        super(context, SHARED_PREFERENCES_FILENAME_INTERNAL);
    }

    /**
     * @return Donky Network Api Key used to identify Donky App Space
     */
    public String getDonkyNetworkApiKey() {
        return getString(KEY_DONKY_API_KEY, null);
    }

    /**
     * @param apiKey Donky Network Api Key used to identify Donky App Space
     */
    public void setDonkyNetworkApiKey(String apiKey) {
        setString(KEY_DONKY_API_KEY, apiKey);
    }

    /**
     * @return RegistrationId used to identify device in GCM network.
     */
    public String getGcmRegistrationId() {
        return getString(KEY_GCM_REGISTRATION_ID, null);
    }

    /**
     * @param registrationId RegistrationId used to identify device in GCM network.
     */
    public void setGcmRegistrationId(String registrationId) {
        setString(KEY_GCM_REGISTRATION_ID, registrationId);
    }

    /**
     * @return Sender id used to register to GCM network.
     */
    public String getGcmSenderId() {
        return getString(KEY_GCM_SENDER_ID, null);
    }

    /**
     * @param appVersion Application version provided during initialisation.
     */
    public void setAppVersion(String appVersion) {
        setString(KEY_APP_VERSION, appVersion);
    }

    /**
     * @return Application version provided during initialisation.
     */
    public String getAppVersion() {
        return getString(KEY_APP_VERSION, null);
    }

    /**
     * @param senderId Sender id used to register to GCM network.
     */
    public void setGcmSenderId(String senderId) {
        setString(KEY_GCM_SENDER_ID, senderId);
    }

    /**
     * @return True if SDK should register to GCM to receive Push Messages.
     */
    public boolean isAllowPushNotifications() {
        return getBoolean(KEY_GCM_ALLOW_PUSH_NOTIFICATIONS, true);
    }

    /**
     * @param allowPushNotifications True if SDK should register to GCM to receive Push Messages.
     */
    public void setAllowPushNotifications(boolean allowPushNotifications) {
        setBoolean(KEY_GCM_ALLOW_PUSH_NOTIFICATIONS, allowPushNotifications);
    }

    /**
     * @return Application version obtain at a time of last GCM registration. If App was updated GCM should re-register.
     */
    public String getGcmRegistrationAppVersion() {
        return getString(KEY_GCM_REGISTRATION_APP_VERSION_CODE, null);
    }

    /**
     * @param appVersion Application version obtain at a time of last GCM registration. If App was updated GCM should re-register.
     */
    public void setGcmRegistrationAppVersion(String appVersion) {
        setString(KEY_GCM_REGISTRATION_APP_VERSION_CODE, appVersion);
    }

    /**
     * @return True if user is not allowed to interact with Donky Network.
     */
    public Boolean isUserSuspended() {
        return getBoolean(KEY_USER_SUSPENDED, false);
    }

    /**
     * @param isUserSuspended True if user is not allowed to interact with Donky Network.
     */
    public void setUserSuspended(boolean isUserSuspended) {
        setBoolean(KEY_USER_SUSPENDED, isUserSuspended);
    }

    /**
     * @return Return root url for Donky Network.
     */
    public String getSecureServiceDomain() {
        return getString(KEY_SECURE_SERVICE_DOMAIN, null);
    }

    /**
     * @param secureServiceDomain Root url for Donky Network.
     */
    public void setSecureServiceDomain(String secureServiceDomain) {
        setString(KEY_SECURE_SERVICE_DOMAIN, secureServiceDomain);
    }

    /**
     * @return Return root url for Donky Network.
     */
    public String getAuthorisationToken() {
        return getString(KEY_AUTHORISATION_TOKEN, null);
    }

    public void setAuthorisationToken(String authorisationToken) {
        setString(KEY_AUTHORISATION_TOKEN, authorisationToken);
    }

    public String getSignalRUrl() {
        return getString(KEY_SIGNAL_R_URL, null);
    }

    public void setSignalRUrl(String signalRUrl) {
        setString(KEY_SIGNAL_R_URL, signalRUrl);
    }

    public String getTokenExpiry() {
        return getString(KEY_TOKEN_EXPIRY, null);
    }

    public void setTokenExpiry(String tokenExpiry) {
        setString(KEY_TOKEN_EXPIRY, tokenExpiry);
    }

    public String getTokenType() {
        return getString(KEY_TOKEN_TYPE, null);
    }

    public void setTokenType(String tokenType) {
        setString(KEY_TOKEN_TYPE, tokenType);
    }

    public int getTokenExpiresInSeconds() {
        return getInteger(KEY_TOKEN_EXPIRES_IN_SECONDS, -1);
    }

    public void setTokenExpiresInSeconds(int tokenExpirySeconds) {
        setInteger(KEY_TOKEN_EXPIRES_IN_SECONDS, tokenExpirySeconds);
    }

    /**
     * Save Module definition
     * @param moduleDefinition Module definition
     */
    public void addModule(ModuleDefinition moduleDefinition) {
        if (moduleDefinition != null) {
            addToStringMap(KEY_MODULES_KEY_SET, moduleDefinition.getName(), moduleDefinition.getVersion());
        }
    }

    /**
     * Save Module definitions
     *
     * @param moduleDetails Module definitions
     */
    public void addModuleDetails(Map<String, String> moduleDetails) {
        addToStringMap(KEY_MODULES_KEY_SET, moduleDetails);
    }

    /**
     * @return Module definition
     */
    public Map<String, String> getModules() {
        return getStringMap(KEY_MODULES_KEY_SET);
    }

    public void updateConfiguration(Map<String, String> configurationItems) {
        setStringMap(KEY_CONFIGURATION_ITEMS, configurationItems);
    }

    public java.util.TreeMap<String, String> getConfigurationItems() {
        return getStringMap(KEY_CONFIGURATION_ITEMS);
    }

    /**
     * Gets max availability days for network content.
     *
     * @return Max availability days for network content.
     */
    public Integer getMaxAvailabilityDays() {

        Integer availabilityDays;

        try {
            availabilityDays = Integer.parseInt(DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().get(ConfigurationDAO.KEY_CONFIGURATION_RichMessageAvailabilityDays));
        } catch (NumberFormatException exception) {
            availabilityDays = ConfigurationDAO.DEFAULT_RICH_MESSAGE_AVAILABILITY_DAYS;
        }

        return availabilityDays;
    }
}
