package net.donky.core.network;

import net.donky.core.DonkyException;

/**
 * Exception thrown when user was suspended on the network.
 *
 * Created by Marcin Swierczek
 * 24/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserSuspendedException extends DonkyException {

    public UserSuspendedException() {
        super("User suspended on the network.");
    }
}
