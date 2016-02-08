package net.donky.core.sequencing.internal.tasks;

import net.donky.core.account.DonkyAccountController;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.DonkySequenceController;

import java.util.TreeMap;

/**
 * Synchronised task to update user additional properties on the network.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateAdditionalPropertiesTask extends UpdateTask {

    final TreeMap<String, String> additionalProperties;

    public UpdateAdditionalPropertiesTask(final DonkySequenceController donkySequenceController, TreeMap<String, String> additionalProperties, DonkySequenceListener listener) {
        super(donkySequenceController, listener);
        this.additionalProperties = additionalProperties;
    }

    @Override
    public void performTask() {
        taskStartedTimestamp = System.currentTimeMillis();
        DonkyAccountController.getInstance().updateUserDetails(DonkyAccountController.getInstance().getCurrentDeviceUser().setUserAdditionalProperties(additionalProperties), listener);
    }
}
