package net.donky.core.settings;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Access point to all static app settings defined in xml resource file.
 *
 * Created by Marcin Swierczek
 * 19/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AppSettings {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private static final String CLIENT_VERSION = "2.0.0.0";

    private static final String DEFAULT_SERVICE_URL = "https://client-api.mobiledonky.com";

    private static final String DEFAULT_GCM_SENDER_ID =  null;

    private static final String KEY_LOGGING_ENABLED = "LoggingEnabled";

    private static final String KEY_ERROR_LOGS_ENABLED = "ErrorLogsEnabled";

    private static final String KEY_WARNING_LOGS_ENABLED = "WarningLogsEnabled";

    private static final String KEY_INFO_LOGS_ENABLED = "InfoLogsEnabled";

    private static final String KEY_DEBUG_LOGS_ENABLED = "DebugLogsEnabled";

    private static final String KEY_SENSITIVE_LOGS_ENABLED = "SensitiveLogsEnabled";

    private static final String KEY_AUTH_ROOT_URL = "ServiceURL";

    private static final String KEY_NEXT_SYNCHRONISE_DELAY_SECONDS = "syncDelaySeconds";

    private static final String KEY_GCM_SENDER_ID = "gcmSenderId";

    private static final String KEY_MINIMAL_TIME_BETWEEN_SUBMITING_LOGS = "minimalTimeBetweenSubmittingLogsSeconds";

    private boolean loggingEnabled;
    private boolean errorLogsEnabled;
    private boolean warningLogsEnabled;
    private boolean infoLogsEnabled;
    private boolean debugLogsEnabled;
    private boolean sensitiveLogsEnabled;

    private String authRootUrl;

    private int syncDelaySeconds;

    private String gcmSenderId;

    private int minimalTimeBetweenSubmittingLogsSeconds;

    // Private constructor. Prevents instantiation from other classes.
    private AppSettings() {
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final AppSettings INSTANCE = new AppSettings();
    }

    public static AppSettings getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise all settings. This should be called only by Donky Core module.
     * @param application Instance of Application class.
     */
    public void init(Application application) {
        loggingEnabled = getBoolean(application.getApplicationContext(), KEY_LOGGING_ENABLED, true);
        errorLogsEnabled = getBoolean(application.getApplicationContext(), KEY_ERROR_LOGS_ENABLED, true);
        warningLogsEnabled = getBoolean(application.getApplicationContext(), KEY_WARNING_LOGS_ENABLED, true);
        infoLogsEnabled = getBoolean(application.getApplicationContext(), KEY_INFO_LOGS_ENABLED, true);
        debugLogsEnabled = getBoolean(application.getApplicationContext(), KEY_DEBUG_LOGS_ENABLED, true);
        sensitiveLogsEnabled = getBoolean(application.getApplicationContext(), KEY_SENSITIVE_LOGS_ENABLED, false);
        authRootUrl = getString(application.getApplicationContext(), KEY_AUTH_ROOT_URL, DEFAULT_SERVICE_URL);
        syncDelaySeconds = getInt(application.getApplicationContext(), KEY_NEXT_SYNCHRONISE_DELAY_SECONDS, 60);
        gcmSenderId = getString(application.getApplicationContext(), KEY_GCM_SENDER_ID, DEFAULT_GCM_SENDER_ID);
        minimalTimeBetweenSubmittingLogsSeconds= getInt(application.getApplicationContext(), KEY_MINIMAL_TIME_BETWEEN_SUBMITING_LOGS, 30);
    }

    /**
     * Translate String representation of a boolean to a boolean value.
     *
     * @param context      Application Context.
     * @param key          Key for Donky client setting.
     * @param defaultValue Returned when no string with given key was found in application resources.
     * @return Value of the String application resource.
     */
    private boolean getBoolean(Context context, String key, boolean defaultValue) {
        String resValueAsString = getString(context, key, null);
        if (resValueAsString != null) {
            if ("true".equalsIgnoreCase(resValueAsString) || "yes".equalsIgnoreCase(resValueAsString) || "1".equalsIgnoreCase(resValueAsString)) {
                return true;
            } else if ("false".equalsIgnoreCase(resValueAsString) || "no".equalsIgnoreCase(resValueAsString) || "0".equalsIgnoreCase(resValueAsString)) {
                return false;
            }
        }
        return defaultValue;
    }

    /**
     * Method to get String from application resources.
     *
     * @param appContext Application Context.
     * @param key        Key for String application resource.
     * @return Value of the String application resource.
     */
    private String getString(Context appContext, String key, String defaultValue) {
        Resources resources = appContext.getResources();
        int id = resources.getIdentifier(key, "string", appContext.getPackageName());
        if (id != 0) {
            return resources.getString(id);
        }
        return defaultValue;
    }

    /**
     * Method to get integer from application resources.
     *
     * @param appContext Application Context.
     * @param key        Key for String application resource.
     * @return Value of the String application resource.
     */
    private int getInt(Context appContext, String key, int defaultValue) {
        Resources resources = appContext.getResources();
        int id = resources.getIdentifier(key, "string", appContext.getPackageName());
        if (id != 0) {
            return resources.getInteger(id);
        }
        return defaultValue;
    }

    /**
     * @return True if SDK will log any messages.
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * @return True if SDK will log messages marked as error.
     */
    public boolean isErrorLogsEnabled() {
        return errorLogsEnabled;
    }

    /**
     * @return True if SDK will log messages marked as warning.
     */
    public boolean isWarningLogsEnabled() {
        return warningLogsEnabled;
    }

    /**
     * @return True if SDK will log messages marked as information.
     */
    public boolean isInfoLogsEnabled() {
        return infoLogsEnabled;
    }

    /**
     * @return True if SDK will log messages marked as debug message.
     */
    public boolean isDebugLogsEnabled() {
        return debugLogsEnabled;
    }

    /**
     * @return True if SDK will log messages marked as sensitive.
     */
    public boolean isSensitiveLogsEnabled() {
        return sensitiveLogsEnabled;
    }

    /**
     * @return Url to perform Donky Authentication.
     */
    public String getAuthRootUrl() {
        return authRootUrl;
    }

    /**
     * @return Delay after which SDK can perform notification sync when internet connection was restored.
     */
    public int getSyncDelaySeconds() {
        return syncDelaySeconds;
    }

    /**
     * @return GCM sender id used to identify which server can send GCM messages. Needed to obtain registrationId used to identify device.
     */
    public String getGcmSenderId() {
        return gcmSenderId;
    }

    /**
     * @return Version code of Donky Core Module.
     */
    public static String getVersion() {
        return CLIENT_VERSION;
    }

    /**
     * @return Minimum time between subsequent submission of logs to the Donky Network.
     */
    public int getMinTimeSubmittingLogs() {
        return minimalTimeBetweenSubmittingLogsSeconds;
    }
}
