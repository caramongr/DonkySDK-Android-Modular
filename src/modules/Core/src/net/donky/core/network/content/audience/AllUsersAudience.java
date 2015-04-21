package net.donky.core.network.content.audience;

/**
 * The audience for the content messages will be all users in the app space.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AllUsersAudience extends Audience {

    /**
     * All users trigger audience.
     */
    public AllUsersAudience() {
        super("AllUsers");
    }

}
