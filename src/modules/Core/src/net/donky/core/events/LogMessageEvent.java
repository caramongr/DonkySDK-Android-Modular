package net.donky.core.events;

import net.donky.core.logging.DonkyLoggingController;

/**
 * Represent Event raised by Donky Core library or another Donky Module when message has been logged.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class LogMessageEvent extends LocalEvent {

    private DonkyLoggingController.LogLevel logLevel;

    private String message;

    private Exception exception;

    /**
     * Local Donky event delivered to subscribers when the new message was logged.
     *
     * @param logLevel Type of log message {@link net.donky.core.logging.DonkyLoggingController.LogLevel}.
     * @param message The message that was logged.
     * @param exception Exception if stack trace was included into the log message.
     */
    public LogMessageEvent(DonkyLoggingController.LogLevel logLevel, String message, Exception exception) {
        super();
        this.logLevel = logLevel;
        this.message = message;
        this.exception = exception;
    }

    /**
     * Get type of log message {@link net.donky.core.logging.DonkyLoggingController.LogLevel}.
     *
     * @return Type of log message {@link net.donky.core.logging.DonkyLoggingController.LogLevel}.
     */
    public DonkyLoggingController.LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Get the message that was logged.
     *
     * @return The message that was logged.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get exception if included.
     *
     * @return Exception if stack trace was included into the log message.
     */
    public Exception getException() {
        return exception;
    }
}