package net.donky.core.sequencing.internal.tasks;

import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.DonkySequenceController;

/**
 * Synchronised task to update device details on the network.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateDeviceTask extends UpdateTask {

    final DeviceDetails deviceDetails;

    public UpdateDeviceTask(final DonkySequenceController donkySequenceController, DeviceDetails deviceDetails, DonkySequenceListener listener) {
        super(donkySequenceController, listener);
        this.deviceDetails = deviceDetails;
    }

    @Override
    public void performTask() {
        taskStartedTimestamp = System.currentTimeMillis();
        DonkyAccountController.getInstance().updateDeviceDetails(deviceDetails, listener);
    }
}
