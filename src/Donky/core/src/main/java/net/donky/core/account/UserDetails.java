package net.donky.core.account;

import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Wrapper class for all user registration data.
 *
 * Created by Marcin Swierczek
 * 20/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserDetails {

    /**
     * Internal user registration identifier.
     */
    private String userId;

    /**
     * Is user registration anonymous one.
     */
    private boolean isAnonymous;

    /**
     * User Name to display in the UI.
     */
    private String userDisplayName;

    /**
     * User first name.
     */
    private String userFirstName;

    /**
     * User last name.
     */
    private String userLastName;

    /**
     * User email address.
     */
    private String userEmailAddress;

    /**
     * User mobile phone number.
     */
    private String userMobileNumber;

    /**
     * Phone number country code.
     */
    private String countryCode;

    /**
     * User avatar image donky id.
     */
    private String userAvatarId;

    /**
     * Additional properties for user registration.
     */
    private TreeMap<String, String> userAdditionalProperties;

    /**
     * Tags selected by the user.
     */
    private Set<String> selectedTags;

    /**
     * UTC timestamp of the last update to the user details.
     */
    private long lastUpdated;

    /**
     * Internal user registration identifier.
     *
     * @return Internal user registration identifier.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Internal user registration identifier.
     *
     * @param userId Internal user registration identifier.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * User Name to display in the UI.
     *
     * @return User Name to display in the UI.
     */
    public String getUserDisplayName() {
        return userDisplayName;
    }

    /**
     * User Name to display in the UI.
     *
     * @param userDisplayName User Name to display in the UI.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
        return this;
    }

    /**
     * User first name.
     *
     * @return User first name.
     */
    public String getUserFirstName() {
        return userFirstName;
    }

    /**
     * User first name.
     *
     * @param userFirstName User first name.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
        return this;
    }

    /**
     * User last name.
     *
     * @return User last name.
     */
    public String getUserLastName() {
        return userLastName;
    }

    /**
     * User last name.
     *
     * @param userLastName User last name.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserLastName(String userLastName) {
        this.userLastName = userLastName;
        return this;
    }

    /**
     * User email address.
     *
     * @return User email address.
     */
    public String getUserEmailAddress() {
        return userEmailAddress;
    }

    /**
     * User email address.
     *
     * @param userEmailAddress User email address.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserEmailAddress(String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
        return this;
    }

    /**
     * User mobile phone number.
     *
     * @return User mobile phone number.
     */
    public String getUserMobileNumber() {
        return userMobileNumber;
    }

    /**
     * User mobile phone number.
     *
     * @param userMobileNumber User mobile phone number.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserMobileNumber(String userMobileNumber) {
        this.userMobileNumber = userMobileNumber;
        return this;
    }

    /**
     * Phone number country code.
     *
     * @param countryCode Phone number country code.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    /**
     * User avatar image donky id.
     *
     * @return User avatar image donky id.
     */
    public String getUserAvatarId() {
        return userAvatarId;
    }

    /**
     * User avatar image donky id.
     *
     * @param userAvatarId User avatar image donky id.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserAvatarId(String userAvatarId) {
        this.userAvatarId = userAvatarId;
        return this;
    }

    /**
     * Additional properties for user registration.
     *
     * @return Additional properties for user registration.
     */
    public TreeMap<String, String> getUserAdditionalProperties() {
        return userAdditionalProperties;
    }

    /**
     * Additional properties for user registration.
     *
     * @param userAdditionalProperties Additional properties for user registration.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setUserAdditionalProperties(TreeMap<String, String> userAdditionalProperties) {
        this.userAdditionalProperties = userAdditionalProperties;
        return this;
    }

    /**
     * Is user registration anonymous one.
     *
     * @return Is user registration anonymous one.
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }

    /**
     * Is user registration anonymous one.
     *
     * @param isAnonymous Is user registration anonymous one.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
        return this;
    }

    /**
     * Phone number country code.
     *
     * @return Phone number country code.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Set ISO country code.
     *
     * @param countryCode ISO country code.
     * @return Instance of updated {@link UserDetails}
     */
    public UserDetails setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    /**
     * Tags selected by the user.
     * @deprecated This method will use only local storage. For consistency use {@link net.donky.core.network.DonkyNetworkController#getTags(DonkyResultListener)}} method and store the tags by yourself.
     * @return Tags selected by the user.
     */
    @Deprecated
    public Set<String> getSelectedTags() {
        return selectedTags;
    }

    /**
     * Tags selected by the user.
     * @deprecated This method will update only local user state. For consistency use {@link net.donky.core.network.DonkyNetworkController#updateTags(List, DonkyListener)} method and store the tags by yourself.
     * @param selectedTags Tags selected by the user.
     * @return Instance of updated {@link UserDetails}
     */
    @Deprecated
    public UserDetails setSelectedTags(Set<String> selectedTags) {
        this.selectedTags = selectedTags;
        return this;
    }

    /**
     * Gets the timestamp of last modification of local user details. This is for internal usage to rule out outdated updates.
     * @return UTC timestamp of last modification of user details.
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the timestamp of last modification of local user details. This is for internal usage to rule out outdated updates. Shouldn't be modified by integrators.
     * @param lastUpdated UTC timestamp of last modification of user details.
     */
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Compares provided user details with the one saved on the device.
     *
     * @param userDetails User details to compare with.
     * @return True if new user details are identical to the one saved on the device.
     */
    public boolean equals(UserDetails userDetails) {

        if (userDetails != null) {

            boolean userSelectedTagsAreTheSame = (userDetails.selectedTags == null && (selectedTags == null || selectedTags.isEmpty())) ||
                    ( userDetails.selectedTags != null && ( (userDetails.selectedTags.equals(selectedTags)) ||  (userDetails.selectedTags.isEmpty() && selectedTags == null) ));

            boolean userAdditionalPropertiesAreTheSame = (userDetails.userAdditionalProperties == null && (userAdditionalProperties == null || userAdditionalProperties.isEmpty())) ||
                    ( userDetails.userAdditionalProperties != null && ( (userDetails.userAdditionalProperties.equals(userAdditionalProperties)) ||  (userDetails.userAdditionalProperties.isEmpty() && userAdditionalProperties == null) ));

            return (((userDetails.userId == null && userId == null) || (userDetails.userId != null && userDetails.userId.equals(userId))) &&
                    ((userDetails.userDisplayName == null && userDisplayName == null) || (userDetails.userDisplayName != null && userDetails.userDisplayName.equals(userDisplayName))) &&
                    ((userDetails.userEmailAddress == null && userEmailAddress == null) || (userDetails.userEmailAddress != null && userDetails.userEmailAddress.equals(userEmailAddress))) &&
                    ((userDetails.userFirstName == null && userFirstName == null) || (userDetails.userFirstName != null && userDetails.userFirstName.equals(userFirstName))) &&
                    ((userDetails.userLastName == null && userLastName == null) || (userDetails.userLastName != null && userDetails.userLastName.equals(userLastName))) &&
                    ((userDetails.userMobileNumber == null && userMobileNumber == null) || (userDetails.userMobileNumber != null && userDetails.userMobileNumber.equals(userMobileNumber))) &&
                    ((userDetails.countryCode == null && countryCode == null) || (userDetails.countryCode != null && userDetails.countryCode.equals(countryCode))) &&
                    ((userDetails.userAvatarId == null && userAvatarId == null) || (userDetails.userAvatarId != null && userDetails.userAvatarId.equals(userAvatarId))) &&
                    userAdditionalPropertiesAreTheSame && userSelectedTagsAreTheSame);
        } else {

            return (userId == null &&
                    userEmailAddress == null &&
                    userFirstName == null &&
                    userLastName == null &&
                    userMobileNumber == null &&
                    countryCode == null &&
                    userAvatarId == null &&
                    userDisplayName == null &&
                    selectedTags == null &&
                    userAdditionalProperties == null);
        }
    }
}
