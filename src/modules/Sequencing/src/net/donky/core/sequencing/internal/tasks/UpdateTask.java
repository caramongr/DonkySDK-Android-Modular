package net.donky.core.sequencing.internal.tasks;

import android.os.Handler;
import android.os.Looper;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.DonkySequenceController;

import java.util.Map;

/**
 * Generic update account task. It will execute next task when this one is completed.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public abstract class UpdateTask {

    final protected long taskCreatedTimestamp;

    protected long taskStartedTimestamp;

    final DonkyListener listener;

    final DonkySequenceController donkySequenceController;

    UpdateTask(final DonkySequenceController donkySequenceController, final DonkySequenceListener listener) {

        this.donkySequenceController = donkySequenceController;

        taskCreatedTimestamp = System.currentTimeMillis();

        if (listener != null) {

            this.listener = new DonkyListener() {

                @Override
                public void success() {
                    listener.success(taskCreatedTimestamp, taskStartedTimestamp, System.currentTimeMillis());
                    donkySequenceController.forceExecuteNext();
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    listener.error(donkyException, validationErrors, taskCreatedTimestamp, taskStartedTimestamp, System.currentTimeMillis());
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            donkySequenceController.forceExecuteNext();
                        }
                    }, 2000);

                }

            };

        } else {

            this.listener = new DonkyListener() {

                @Override
                public void success() {
                    donkySequenceController.forceExecuteNext();
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    donkySequenceController.forceExecuteNext();
                }
            };

        }

    }

    /**
     * Perform appropriate call in {@link DonkyAccountController}
     */
    abstract public void performTask();
}
