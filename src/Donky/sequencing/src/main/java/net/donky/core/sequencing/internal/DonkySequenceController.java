package net.donky.core.sequencing.internal;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.logging.DLog;
import net.donky.core.network.TagDescription;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.tasks.UpdateAdditionalPropertiesTask;
import net.donky.core.sequencing.internal.tasks.UpdateDeviceTask;
import net.donky.core.sequencing.internal.tasks.UpdateRegistrationTask;
import net.donky.core.sequencing.internal.tasks.UpdateTagsTask;
import net.donky.core.sequencing.internal.tasks.UpdateTask;
import net.donky.core.sequencing.internal.tasks.UpdateUserTask;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Controller for updating account updates queue. This class is to synchronise all calls from DonkySequenceAccountController
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkySequenceController {

    ConcurrentLinkedQueue<UpdateTask> queue;

    private boolean isInProgress;

    DLog log;

    public DonkySequenceController() {
        queue = new ConcurrentLinkedQueue<>();
        isInProgress = false;
        log = new DLog("DonkySequenceController");
    }

    /**
     * Update any custom data related to the registration.
     *
     * @param userDetails   User details to be updated.
     * @param deviceDetails Device details to be updated.
     * @param listener      The callback to invoke when the command has executed. Registration errors will be fed back through this.
     */
    public void addRegistrationUpdateTask(final UserDetails userDetails, final DeviceDetails deviceDetails, final DonkySequenceListener listener) {
        queue.add(new UpdateRegistrationTask(this, userDetails, deviceDetails, listener));
        tryExecuteNext();
    }

    /**
     * Update user registration details.
     *
     * @param user     New user details.
     * @param listener Callback to invoke when task is completed.
     */
    public void addUserUpdateTask(final UserDetails user, final DonkySequenceListener listener) {
        queue.add(new UpdateUserTask(this, user, listener));
        tryExecuteNext();
    }

    /**
     * Update device registration details.
     *
     * @param deviceDetails New device details.
     * @param listener      Callback to invoke when task is completed.
     */
    public void addDeviceUpdateTask(final DeviceDetails deviceDetails, final DonkySequenceListener listener) {
        queue.add(new UpdateDeviceTask(this, deviceDetails, listener));
        tryExecuteNext();
    }


    /**
     * Updates the list of tags selected by the user on Donky Network.
     *
     * @param listener Callback to be invoked when completed.
     */
    public void addUpdateTagsTask(List<TagDescription> tags, final DonkySequenceListener listener) {
        queue.add(new UpdateTagsTask(this, tags, listener));
        tryExecuteNext();
    }

    /**
     * Set additional properties for device registration.
     *
     * @param additionalProperties Additional properties for device registration.
     */
    public void addUpdateAdditionalPropertiesTask(TreeMap<String, String> additionalProperties, final DonkySequenceListener listener) {
        queue.add(new UpdateAdditionalPropertiesTask(this, additionalProperties, listener));
        tryExecuteNext();
    }

    /**
     * For internal use only. Execute next request if currently no other request is in progress.
     */
    public void tryExecuteNext() {
        if (!isInProgress && !queue.isEmpty()) {
            isInProgress = true;
            UpdateTask updateTask = queue.poll();
            if (updateTask != null) {
                updateTask.performTask();
            }
        }
    }

    /**
     * For internal use only. Tell the controller that previous task has been completed and execute next one.
     */
    public void forceExecuteNext() {
        isInProgress = false;
        tryExecuteNext();
    }

}
