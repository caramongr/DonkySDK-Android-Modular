package net.donky.core.network.content.audience;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The audience for the content messages will be the users specified here.
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SpecifiedUsersAudience extends Audience {

    @SerializedName("users")
    private final List<AudienceMember> users;

    /**
     * SpecifiedUsers trigger audience.
     *
     * @param users List of {@link AudienceMember}'s to which the content notification should be send by the server.
     */
    public SpecifiedUsersAudience(List<AudienceMember> users) {
        super("SpecifiedUsers");
        this.users = users;
    }

    /**
     *
     * @return List of external user ids ({@link AudienceMember}'s)  that the trigger applies to.
     */
    public List<AudienceMember> getUsers() {
        return users;
    }
}
