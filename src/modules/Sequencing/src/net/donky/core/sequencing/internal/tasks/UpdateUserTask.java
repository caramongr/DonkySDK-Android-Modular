package net.donky.core.sequencing.internal.tasks;

import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.sequencing.DonkySequenceListener;
import net.donky.core.sequencing.internal.DonkySequenceController;

/**
 * Synchronised task to update user details on the network.
 *
 * Created by Marcin Swierczek
 * 15/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UpdateUserTask extends UpdateTask {

    final UserDetails userDetails;

    public UpdateUserTask(final DonkySequenceController donkySequenceController, UserDetails userDetails, DonkySequenceListener listener) {
        super(donkySequenceController, listener);
        this.userDetails = userDetails;
    }

    @Override
    public void performTask() {
        taskStartedTimestamp = System.currentTimeMillis();
        DonkyAccountController.getInstance().updateUserDetails(userDetails, listener);
    }
}
