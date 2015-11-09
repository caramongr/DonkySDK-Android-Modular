package net.donky.core.messaging.logic.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 12/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Contact implements Serializable {

    @SerializedName("networkProfileId")
    private String networkProfileId;

    @SerializedName("externalUserId")
    private String externalUserId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("avatarId")
    private String avatarId;

    @SerializedName("mobilePhone")
    private String mobilePhone;

    @SerializedName("country")
    private String country;

    @SerializedName("email")
    private String email;

    @SerializedName("accountType")
    private String accountType;

    @SerializedName("additionalProperties")
    private Map<String, String> additionalProperties;

    private Long contactCreatedTimeLong;

    private boolean isStandardContact;

    private boolean isSelf;

    private boolean isCratedByIntegrator;

    public String getNetworkProfileId() {
        return networkProfileId;
    }

    public Contact(final String networkProfileId, final String externalUserId) {
        this.networkProfileId = networkProfileId;
        this.externalUserId = externalUserId;
    }

    /*
    public Contact(Parcel in) {
        networkProfileId = in.readString();
        externalUserId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        displayName = in.readString();
        avatarId = in.readString();
        mobilePhone = in.readString();
        country = in.readString();
        email = in.readString();
        accountType = in.readString();
        additionalProperties = new HashMap<>();
        in.readMap(additionalProperties, Properties.CREATOR);
        contactCreatedTimeLong = in.readLong();
        isStandardContact = in.readInt() == 1;
        isSelf = in.readInt() == 1;
        isCratedByIntegrator = in.readInt() == 1;
    }*/

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Long getContactCreatedTimeLong() {
        return contactCreatedTimeLong;
    }

    public void setContactCreatedTimeLong(Long contactCreatedTimeLong) {
        this.contactCreatedTimeLong = contactCreatedTimeLong;
    }

    public boolean isStandardContact() {
        return isStandardContact;
    }

    public void setIsStandardContact(boolean isStandardContact) {
        this.isStandardContact = isStandardContact;
    }

    public boolean isCratedByIntegrator() {
        return isCratedByIntegrator;
    }

    public void setIsCratedByIntegrator(boolean isMessagingIdentity) {
        this.isCratedByIntegrator = isMessagingIdentity;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setIsSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(networkProfileId);
        dest.writeString(externalUserId);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(displayName);
        dest.writeString(avatarId);
        dest.writeString(mobilePhone);
        dest.writeString(country);
        dest.writeString(email);
        dest.writeString(accountType);
        dest.writeMap(additionalProperties);
        dest.writeLong(contactCreatedTimeLong);
        dest.writeInt(isStandardContact ? 1 : 0);
        dest.writeInt(isSelf ? 1 : 0);
        dest.writeInt(isCratedByIntegrator ? 1 : 0);
    }

    public static <T extends Parcelable> void write(Parcel dest,
                                                    Map<String, T> objects, int flags) {
        if (objects == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(objects.keySet().size());
            for (String key : objects.keySet()) {
                dest.writeString(key);
                dest.writeParcelable(objects.get(key), flags);
            }
        }
    }

    public static <T extends Parcelable> Map<String, T> readParcelableMap(
            Parcel source) {
        int numKeys = source.readInt();
        if (numKeys == -1) {
            return null;
        }
        HashMap<String, T> map = new HashMap<String, T>();
        for (int i = 0; i < numKeys; i++) {
            String key = source.readString();
            T value = source.readParcelable(null);
            map.put(key, value);
        }
        return map;
    }*/
}
