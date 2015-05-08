package net.donky.core.logging;

import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.events.LogMessageEvent;
import net.donky.core.network.restapi.secured.UploadLog;
import net.donky.core.settings.AppSettings;

/**
 * Helper for log message commands.
 *
 * Created by Marcin Swierczek
 * 18/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DLog {

    /**
     * Tag for Donky SDK logs in LogCat
     */
    private final String tag;

    /**
     * Helper for log message commands.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     */
    public DLog(String tag) {
        this.tag = tag;
    }

    /**
     * Send an ERROR log message. Save that message to internal storage log file.
     *
     * @param msg The message you would like logged.
     */
    public void error(String msg) {

        try {

            if (isErrorLogsEnabled()) {
                Log.e(tag, msg);
            }

            DonkyLoggingController.getInstance().writeLog(msg, DonkyLoggingController.LogLevel.ERROR, null);

            if (DonkyLoggingController.getInstance().getAutoSubmit()) {
                DonkyLoggingController.getInstance().submitLog(UploadLog.SubmissionReason.AutomaticByDevice, null);
            }

            DonkyCore.publishLocalEvent(new LogMessageEvent(DonkyLoggingController.LogLevel.ERROR, msg, null));

        } catch (Exception e) {
            Log.e("Donky","Error logging");
        }
    }

    /**
     * Send an ERROR log message. Save that message to internal storage log file.
     *
     * @param msg The message you would like logged.
     * @param exception The Exception that caused the error.
     */
    public void error(String msg, Exception exception) {

        try {

            if (isErrorLogsEnabled()) {
                Log.e(tag, msg);
            }

            DonkyLoggingController.getInstance().writeLog(msg, DonkyLoggingController.LogLevel.ERROR, exception);

            if (DonkyLoggingController.getInstance().getAutoSubmit()) {
                DonkyLoggingController.getInstance().submitLog(UploadLog.SubmissionReason.AutomaticByDevice, null);
            }

            DonkyCore.publishLocalEvent(new LogMessageEvent(DonkyLoggingController.LogLevel.ERROR, msg, exception));

        } catch (Exception e) {
            Log.e("Donky","Error logging");
        }
    }

    /**
     * Send an WARNING log message. Save that message to internal storage log file.
     *
     * @param msg The message you would like logged.
     */
    public void warning(String msg) {

        if (isWarningLogsEnabled()) {
            Log.w(tag, msg);
        }

        DonkyLoggingController.getInstance().writeLog(msg, DonkyLoggingController.LogLevel.WARNING, null);
        DonkyCore.publishLocalEvent(new LogMessageEvent(DonkyLoggingController.LogLevel.WARNING, msg, null));
    }

    /**
     * Send an INFO log message. Save that message to internal storage log file.
     *
     * @param msg The message you would like logged.
     */
    public void info(String msg) {

        if (isInfoLogsEnabled()) {
            Log.i(tag, msg);
        }

        DonkyLoggingController.getInstance().writeLog(msg, DonkyLoggingController.LogLevel.INFO, null);
        DonkyCore.publishLocalEvent(new LogMessageEvent(DonkyLoggingController.LogLevel.INFO, msg, null));
    }

    /**
     * Send an DEBUG log message. Save that message to internal storage log file.
     *
     * @param msg The message you would like logged.
     */
    public void debug(String msg) {

        if (isDebugLogsEnabled()) {
            Log.d(tag, msg);
        }

        DonkyLoggingController.getInstance().writeLog(msg, DonkyLoggingController.LogLevel.DEBUG, null);
        DonkyCore.publishLocalEvent(new LogMessageEvent(DonkyLoggingController.LogLevel.DEBUG, msg, null));
    }

    /**
     * Send an DEBUG log message. Save that message to internal storage log file.
     *
     * @param msg The message you would like logged.
     */
    public void sensitive(String msg) {

        if (isSensitiveLogsEnabled()) {
            Log.d(tag, msg);
            DonkyLoggingController.getInstance().writeLog(msg, DonkyLoggingController.LogLevel.SENSITIVE, null);
            DonkyCore.publishLocalEvent(new LogMessageEvent(DonkyLoggingController.LogLevel.SENSITIVE, msg, null));
        }

    }

    /**
     * Check if logs with log level 'info' are enabled.
     *
     * @return True if logs with log level 'info' are enabled.
     */
    public boolean isInfoLogsEnabled() {
        return AppSettings.getInstance().isLoggingEnabled() && AppSettings.getInstance().isInfoLogsEnabled();
    }

    /**
     * Check if logs with log level 'info' are enabled.
     *
     * @return True if logs with log level 'info' are enabled.
     */
    public boolean isDebugLogsEnabled() {
        return AppSettings.getInstance().isLoggingEnabled() && AppSettings.getInstance().isDebugLogsEnabled();
    }

    /**
     * Check if if logs with log level 'sensitive' are enabled.
     *
     * @return True if logs with log level 'sensitive' are enabled.
     */
    public boolean isSensitiveLogsEnabled() {
        return AppSettings.getInstance().isLoggingEnabled() && AppSettings.getInstance().isSensitiveLogsEnabled();
    }

    /**
     * Check if if logs with log level 'error' are enabled.
     *
     * @return True if logs with log level 'error' are enabled.
     */
    public boolean isErrorLogsEnabled() {
        return AppSettings.getInstance().isLoggingEnabled() && AppSettings.getInstance().isErrorLogsEnabled();
    }

    /**
     * Check if if logs with log level 'warning' are enabled.
     *
     * @return True if logs with log level 'warning' are enabled.
     */
    public boolean isWarningLogsEnabled() {
        return AppSettings.getInstance().isLoggingEnabled() && AppSettings.getInstance().isWarningLogsEnabled();
    }
}
