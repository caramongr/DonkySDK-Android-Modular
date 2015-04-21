package net.donky.core.logging;

import android.app.Application;
import android.content.Context;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.events.LogMessageEvent;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.model.ConfigurationDAO;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.restapi.secured.UploadLog;
import net.donky.core.network.restapi.secured.UploadLogResponse;
import net.donky.core.settings.AppSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for all logging related functionality.
 * <p/>
 * Created by Marcin Swierczek
 * 16/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyLoggingController {

    private static final int LOG_FILE_SIZE_LIMIT_KB = 2;

    private static final String LOG_FILE_NAME_FIRST = "DonkySdkLogs";

    private static final String LOG_FILE_NAME_SECOND = "DonkySdkLogs2";

    private long lastSubmissionTimestamp;

    /**
     * Object to lock the thread on
     */
    private static final Object sharedLock = new Object();

    private final AtomicBoolean autoSubmit;

    public void setAutoSubmit(boolean autoSubmit) {
        this.autoSubmit.set(autoSubmit);
    }

    public boolean getAutoSubmit() {
        return this.autoSubmit.get();
    }

    /**
     * Defines types of log messages.
     */
    public enum LogLevel {
        SENSITIVE("[SENSITIVE]", 0),
        DEBUG("[DEBUG]", 1),
        INFO("[INFO]", 2),
        WARNING("[WARNING]", 3),
        ERROR("[ERROR]", 4);

        private final String stringValue;
        private final int intValue;

        private LogLevel(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    /**
     * File output stream to save Donky SDK logs.
     */
    private FileOutputStream outputStream;

    /**
     * File input stream to load Donky SDK logs.
     */
    private FileInputStream inputStream;

    /**
     * Application context.
     */
    private Context context;

    // Private constructor. Prevents instantiation from other classes.
    private DonkyLoggingController() {
        autoSubmit = new AtomicBoolean(true);
        lastSubmissionTimestamp = 0;
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyLoggingController INSTANCE = new DonkyLoggingController();
    }

    /**
     * @return Instance of Logging Controller singleton.
     */
    public static DonkyLoggingController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application) {
        this.context = application.getApplicationContext();
    }

    /**
     * Add log to internal logging file.
     *
     * @param message Log text that should be added to internal log file.
     */
    public void writeLog(final String message, final LogLevel logLevel, final Exception exception) {

        synchronized (sharedLock) {
            try {

                File dir = context.getFilesDir();

                File fileFirst = new File(dir, LOG_FILE_NAME_FIRST);
                File fileSecond = new File(dir, LOG_FILE_NAME_SECOND);

                float fileSizeFirst = fileFirst.length() / 1024.0f; //In kilobytes
                float fileSizeSecond = fileSecond.length() / 1024.0f; //In kilobytes

                if (fileSizeFirst < LOG_FILE_SIZE_LIMIT_KB) {
                    outputStream = context.openFileOutput(LOG_FILE_NAME_FIRST, Context.MODE_APPEND);
                } else if (fileSizeSecond < LOG_FILE_SIZE_LIMIT_KB) {
                    outputStream = context.openFileOutput(LOG_FILE_NAME_SECOND, Context.MODE_APPEND);
                } else {
                    fileFirst.delete();
                    fileSecond.renameTo(fileFirst);
                    outputStream = context.openFileOutput(LOG_FILE_NAME_FIRST, Context.MODE_APPEND);
                }

                StringBuilder sb = new StringBuilder();

                sb.append(DateAndTimeHelper.getCurrentLocalTime());
                sb.append(" ");
                sb.append(logLevel.toString());
                sb.append(": ");
                sb.append(message);
                sb.append('\n');

                if (exception != null) {

                    StackTraceElement[] stackTrace = exception.getStackTrace();
                    for (StackTraceElement element : stackTrace) {
                        sb.append(element.toString());
                        sb.append('\n');
                    }

                    if (exception.getCause() != null) {

                        StackTraceElement[] stackTraceCause = exception.getCause().getStackTrace();
                        for (StackTraceElement element : stackTraceCause) {
                            sb.append(element.toString());
                            sb.append('\n');
                        }

                    }
                }

                if (outputStream != null) {
                    outputStream.write(sb.toString().getBytes(Charset.forName("UTF-8")));
                    outputStream.flush();
                    outputStream.close();
                }

                DonkyCore.publishLocalEvent(new LogMessageEvent(logLevel, message, exception));

            } catch (Exception e) {
                e.printStackTrace();
            }
            sharedLock.notifyAll();
        }
    }

    /**
     * Get recent Donky SDK logs.
     *
     * @return Content of recent log file.
     */
    public String getLog() {

        StringBuilder sb = new StringBuilder();

        synchronized (sharedLock) {

            try {

                File dir = context.getFilesDir();

                File fileFirst = new File(dir, LOG_FILE_NAME_FIRST);

                if (fileFirst.exists()) {

                    inputStream = new FileInputStream(fileFirst);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    reader.close();

                }

                File fileSecond = new File(dir, LOG_FILE_NAME_SECOND);

                if (fileSecond.exists()) {

                    inputStream = new FileInputStream(fileSecond);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            sharedLock.notifyAll();
        }

        return sb.toString();
    }

    /**
     * Delete files storing last logging activity.
     */
    private void clearLogFiles() {

        synchronized (sharedLock) {

            File dir = context.getFilesDir();

            File fileFirst = new File(dir, LOG_FILE_NAME_FIRST);
            File fileSecond = new File(dir, LOG_FILE_NAME_SECOND);

            fileFirst.delete();
            fileSecond.delete();

            sharedLock.notifyAll();
        }
    }

    /**
     * Submit saved log entries to the Donky Network, reset log files and update the automatic logging configuration.
     *
     * @param reason   The reason {@link net.donky.core.network.restapi.secured.UploadLog.SubmissionReason} to submit logs to the notwork
     * @param listener The callback to invoke when the process completes.
     */
    public void submitLog(UploadLog.SubmissionReason reason, final DonkyListener listener) {

        if (canSubmitLogs()) {

            lastSubmissionTimestamp = System.currentTimeMillis();

            DonkyNetworkController.getInstance().submitLog(getLog(), reason, new DonkyResultListener<UploadLogResponse>() {

                @Override
                public void success(UploadLogResponse result) {


                    setAutoSubmit(result.isAlwaysSubmitErrors());

                    try {

                        DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().put(ConfigurationDAO.KEY_CONFIGURATION_AlwaysSubmitErrors, Boolean.valueOf(result.isAlwaysSubmitErrors()).toString());

                        clearLogFiles();

                        if (listener != null) {
                            listener.success();
                        }

                    } catch (Exception e) {

                        DonkyException donkyException = new DonkyException("Error in submit log success callback.");
                        donkyException.initCause(e);

                        if (listener != null) {
                            listener.error(donkyException, null);
                        }

                    }
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

    /**
     * Check if delay since last submission of logs was greater then the value defined by settings.
     * @return
     */
    private boolean canSubmitLogs() {

        return ((System.currentTimeMillis() - lastSubmissionTimestamp) > 1000 * AppSettings.getInstance().getMinTimeSubmittingLogs());

    }

    /**
     * Submit saved log entries to the Donky Network, reset log files and update the automatic logging configuration.
     *
     * @param listener The callback to invoke when the process completes.
     */
    public void submitLog(final DonkyListener listener) {

        submitLog(UploadLog.SubmissionReason.ManualRequest, listener);

    }
}