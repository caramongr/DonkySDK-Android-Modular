package net.donky.core.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Part of configuration sets in registration response that hold information about 'standard contacts'.
 *
 * Created by Marcin Swierczek
 * 13/10/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class StandardContacts {

    @SerializedName("type")
    private String type;

    @SerializedName("contacts")
    private List<StandardContact> standardContactsList;

    public class StandardContact {

        @SerializedName("networkProfileId")
        private String networkProfileId;

        @SerializedName("userId")
        private String externalUserId;

        @SerializedName("displayName")
        private String displayName;

        @SerializedName("avatarAssetId")
        private String avatarAssetId;

        @SerializedName("emailAddress")
        private String emailAddress;

        @SerializedName("additionalProperties")
        private Map<String, String> additionalProperties;

        public String getNetworkProfileId() {
            return networkProfileId;
        }

        public String getExternalUserId() {
            return externalUserId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAvatarAssetId() {
            return avatarAssetId;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public Map<String, String> getAdditionalProperties() {
            return additionalProperties;
        }
    }

    public String getType() {
        return type;
    }

    public List<StandardContact> getStandardContactsList() {
        return standardContactsList;
    }
}
