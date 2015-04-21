package net.donky.core.network.content.audience;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Definition of a member for {@link net.donky.core.network.content.audience.Audience} to define audience for {@link net.donky.core.network.content.ContentNotification}
 *
 * Created by Marcin Swierczek
 * 22/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AudienceMember {

    @SerializedName("userId")
    private final String userId;

    /**
     * Additional properties of audience member.
     */
    @SerializedName("templateData")
    private final Map<String, String> templateData;

    public AudienceMember(String userId, Map<String, String> templateData) {
        this.userId = userId;
        this.templateData = templateData;
    }

    /**
     * @return User Id of the user that the content message should be send to.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return Additional properties of audience member.
     */
    public Map<String, String> getTemplateData() {
        return templateData;
    }
}
