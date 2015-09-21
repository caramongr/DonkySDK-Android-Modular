package net.donky.core.sequencing;

import net.donky.core.DonkyException;

import java.util.Map;

/**
 * This listener is a replacement for DonkyListener to include the timestamps of task execution
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public interface DonkySequenceListener {

    /**
     * Operation succeeded.
     */
    void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp);

    /**
     * Operation finished with error.
     * @param donkyException Exception or null if N/A.
     * @param validationErrors Validation errors map or null if N/A.
     * @param taskCreatedTimestamp Time when the task was put into a queue
     * @param taskStartedTimestamp Time when the task was picked up from queue and executed
     * @param taskFinishedTimestamp Time when the task was finished
     */
    void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp);
}
