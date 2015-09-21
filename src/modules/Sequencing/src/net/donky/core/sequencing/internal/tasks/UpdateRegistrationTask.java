package net.donky.core.sequencing.internal.tasks;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.DonkySequenceController;

/**
 * Synchronised task to update user and device registration details on the network.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateRegistrationTask extends UpdateTask {

    final UserDetails userDetails;

    final DeviceDetails deviceDetails;

    public UpdateRegistrationTask(final DonkySequenceController donkySequenceController, UserDetails userDetails, DeviceDetails deviceDetails, DonkySequenceListener listener) {
        super(donkySequenceController, listener);
        this.userDetails = userDetails;
        this.deviceDetails = deviceDetails;
    }

    @Override
    public void performTask() {
        taskStartedTimestamp = System.currentTimeMillis();
        DonkyAccountController.getInstance().updateRegistrationDetails(userDetails, deviceDetails, listener);
    }
}
