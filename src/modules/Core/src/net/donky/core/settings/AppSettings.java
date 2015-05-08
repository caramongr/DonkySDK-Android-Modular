package net.donky.core.settings;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import net.donky.core.account.NewDeviceHandler;

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
    private static final String CLIENT_VERSION = "2.0.0.5";

    private static final String DEFAULT_SERVICE_URL = "https://client-api.mobiledonky.com";

    private static final String DEFAULT_GCM_SENDER_ID =  null;

    private static final String KEY_LOGGING_ENABLED = "LoggingEnabled";

    private static final String KEY_ERROR_LOGS_ENABLED = "ErrorLogsEnabled";

    private static final String KEY_WARNING_LOGS_ENABLED = "WarningLogsEnabled";

    private static final String KEY_INFO_LOGS_ENABLED = "InfoLogsEnabled";

    private static final String KEY_DEBUG_LOGS_ENABLED = "DebugLogsEnabled";

    private static final String KEY_SENSITIVE_LOGS_ENABLED = "SensitiveLogsEnabled";

    private static final String KEY_AUTH_ROOT_URL = "ServiceURL";

    private static final String KEY_NEXT_SYNCHRONISE_DELAY_SECONDS = "SyncDelaySeconds";

    private static final String KEY_GCM_SENDER_ID = "GcmSenderId";

    private static final String KEY_MINIMAL_TIME_BETWEEN_SUBMITTING_LOGS = "MinimalTimeBetweenSubmittingLogsSeconds";

    private static final String KEY_NEW_DEVICE_MESSAGE = "NewDeviceMessage";

    private static final String KEY_NEW_DEVICE_NOTIFICATION_ENABLED = "NewDeviceNotificationEnabled";

    private static final String KEY_NEW_DEVICE_TITLE = "NewDeviceTitle";

    private static final String DEFAULT_NEW_DEVICE_SMALL_ICON = "ic_donky_new_device_default";

    private static final String NEW_DEVICE_SMALL_ICON = "ic_donky_new_device";

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

    private String newDeviceMessage;
    private String newDeviceTitle;
    private int newDeviceSmallIcon;
    private boolean newDeviceNotificationEnabled;

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

        Context context = application.getApplicationContext();

        loggingEnabled = getBoolean(context, KEY_LOGGING_ENABLED, true);
        errorLogsEnabled = getBoolean(context, KEY_ERROR_LOGS_ENABLED, true);
        warningLogsEnabled = getBoolean(context, KEY_WARNING_LOGS_ENABLED, true);
        infoLogsEnabled = getBoolean(context, KEY_INFO_LOGS_ENABLED, true);
        debugLogsEnabled = getBoolean(context, KEY_DEBUG_LOGS_ENABLED, true);
        sensitiveLogsEnabled = getBoolean(context, KEY_SENSITIVE_LOGS_ENABLED, false);
        authRootUrl = getString(context, KEY_AUTH_ROOT_URL, DEFAULT_SERVICE_URL);
        syncDelaySeconds = getInt(context, KEY_NEXT_SYNCHRONISE_DELAY_SECONDS, 60);
        gcmSenderId = getString(context, KEY_GCM_SENDER_ID, DEFAULT_GCM_SENDER_ID);
        minimalTimeBetweenSubmittingLogsSeconds= getInt(context, KEY_MINIMAL_TIME_BETWEEN_SUBMITTING_LOGS, 30);

        newDeviceNotificationEnabled = getBoolean(context, KEY_NEW_DEVICE_NOTIFICATION_ENABLED, true);
        newDeviceMessage = getString(context, KEY_NEW_DEVICE_MESSAGE, "A new device {"+ NewDeviceHandler.newDeviceModelTag+"} ({"+ NewDeviceHandler.newDeviceOperatingSystemTag+"}) has been registered against your account; if you did not register this device please let us know immediately.");
        newDeviceTitle = getString(context, KEY_NEW_DEVICE_TITLE, getNewDeviceTitleDefaultValue(context));
        newDeviceSmallIcon = getResourceDrawableId(context, NEW_DEVICE_SMALL_ICON);
        if (newDeviceSmallIcon == 0) {
            newDeviceSmallIcon = getResourceDrawableId(context, DEFAULT_NEW_DEVICE_SMALL_ICON);
        }
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
     * Gets the resource id for given drawable file name.
     *
     * @param context Application Context.
     * @param resourceFileName Name of drawable file.
     * @return The resource id for given drawable file name. Returns 0 if not found.
     */
    private int getResourceDrawableId(Context context, String resourceFileName) {
        return context.getResources().getIdentifier(resourceFileName , "drawable", context.getPackageName());
    }

    private String getNewDeviceTitleDefaultValue(Context context) {

        final String defaultTitle = "New Device";

        try {

            int stringId = context.getApplicationInfo().labelRes;
            String name = context.getString(stringId);

            if (!TextUtils.isEmpty(name)) {

                return name;

            } else {

                return defaultTitle;
            }

        } catch (Exception exception) {

            return defaultTitle;
        }
    }

    /**
     * Should SDK log any message?
     *
     * @return True if SDK will log any messages.
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Should SDK log messages marked as error?
     *
     * @return True if SDK will log messages marked as error.
     */
    public boolean isErrorLogsEnabled() {
        return errorLogsEnabled;
    }

    /**
     * Should SDK log messages marked as warning?
     *
     * @return True if SDK will log messages marked as warning.
     */
    public boolean isWarningLogsEnabled() {
        return warningLogsEnabled;
    }

    /**
     * Should SDK log messages marked as information?
     *
     * @return True if SDK will log messages marked as information.
     */
    public boolean isInfoLogsEnabled() {
        return infoLogsEnabled;
    }

    /**
     * Should SDK log messages marked as debug message?
     *
     * @return True if SDK will log messages marked as debug message.
     */
    public boolean isDebugLogsEnabled() {
        return debugLogsEnabled;
    }

    /**
     * Should SDK log messages marked as sensitive?
     *
     * @return True if SDK will log messages marked as sensitive.
     */
    public boolean isSensitiveLogsEnabled() {
        return sensitiveLogsEnabled;
    }

    /**
     * URL to perform Donky Authentication.
     *
     * @return URL to perform Donky Authentication.
     */
    public String getAuthRootUrl() {
        return authRootUrl;
    }

    /**
     * Delay after which SDK can perform notification sync when internet connection was restored.
     *
     * @return Delay after which SDK can perform notification sync when internet connection was restored.
     */
    public int getSyncDelaySeconds() {
        return syncDelaySeconds;
    }

    /**
     * GCM sender id used to identify which server can send GCM messages. Needed to obtain registrationId used to identify device.
     *
     * @return GCM sender id used to identify which server can send GCM messages. Needed to obtain registrationId used to identify device.
     */
    public String getGcmSenderId() {
        return gcmSenderId;
    }

    /**
     *  Version code of Donky Core Module.
     *
     * @return Version code of Donky Core Module.
     */
    public static String getVersion() {
        return CLIENT_VERSION;
    }

    /**
     * Minimum time between subsequent submission of logs to the Donky Network.
     *
     * @return Minimum time between subsequent submission of logs to the Donky Network.
     */
    public int getMinTimeSubmittingLogsSeconds() {
        return minimalTimeBetweenSubmittingLogsSeconds;
    }

    /**
     * Message displayed in notification when new device registered against the user account.
     *
     * @return Message displayed in notification when new device registered against the user account.
     */
    public String getNewDeviceMessage() {
        return newDeviceMessage;
    }

    /**
     * Title displayed in notification when new device registered against the user account.
     *
     * @return Title displayed in notification when new device registered against the user account.
     */
    public String getNewDeviceTitle() {
        return newDeviceTitle;
    }

    /**
     * Small icon id for notification when new device registered against the user account.
     *
     * @return Small icon id for notification when new device registered against the user account.
     */
    public int getNewDeviceNotificationSmallIconID() {
        return newDeviceSmallIcon;
    }

    /**
     * Should the new device registration (against user account) Notification be processed by the Core SDK.
     *
     * @return True if new device registration notifications should be displayed in notification center.
     */
    public boolean isNewDeviceNotificationEnabled() {
        return newDeviceNotificationEnabled;
    }
}
