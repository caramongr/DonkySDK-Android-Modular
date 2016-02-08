package net.donky.core.sequencing.mock;

import net.donky.core.DonkyException;
import net.donky.core.sequencing.DonkySequenceListener;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 16/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockDonkySequenceListener extends MockDonkyListener implements DonkySequenceListener {

    long taskCreatedTimestamp;
    long taskStartedTimestamp;
    long taskFinishedTimestamp;

    @Override
    public void success(long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
        super.success();
    }

    @Override
    public void error(DonkyException donkyException, Map<String, String> validationErrors, long taskCreatedTimestamp, long taskStartedTimestamp, long taskFinishedTimestamp) {
        super.error(donkyException, validationErrors);
        this.taskCreatedTimestamp = taskCreatedTimestamp;
        this.taskStartedTimestamp = taskStartedTimestamp;
        this.taskFinishedTimestamp = taskFinishedTimestamp;
    }
}
