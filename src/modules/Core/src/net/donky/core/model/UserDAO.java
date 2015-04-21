package net.donky.core.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import net.donky.core.account.UserDetails;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Database Access Object for all user registration data.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UserDAO extends SharedPreferencesBaseDAO {

    /**
     * File name for device details storage.
     */
    protected static final String SHARED_PREFERENCES_FILENAME_USER = "DonkyPreferencesUser";

    /*
     * Key names for Shared Preferences storage.
     */
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NETWORK_ID = "networkId";
    public static final String KEY_USER_DISPLAY_NAME = "displayName";
    public static final String KEY_USER_FIRST_NAME = "firstName";
    public static final String KEY_USER_LAST_NAME = "lastName";
    public static final String KEY_USER_EMAIL_ADDRESS = "emailAddress";
    public static final String KEY_USER_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_USER_COUNTRY_CODE = "countryCode";
    public static final String KEY_USER_IS_ANONYMOUS = "isAnonymous";
    public static final String KEY_USER_AVATAR_ID = "avatarId";
    public static final String KEY_USER_SELECTED_TAGS = "selectedTagsKeySet";
    public static final String KEY_USER_ADDITIONAL_PROPERTIES_KEY_SET = "additionalKeySet";


    public UserDAO(Context context) {
        super(context, SHARED_PREFERENCES_FILENAME_USER);
    }

    /**
     * @return Information about current user.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UserDetails getUserDetails() {

        SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferencesFileName(), Context.MODE_MULTI_PROCESS);

        UserDetails user = new UserDetails();

        user.setUserId(sharedPreferences.getString(KEY_USER_ID, null))
                .setUserDisplayName(sharedPreferences.getString(KEY_USER_DISPLAY_NAME, null))
                .setUserFirstName(sharedPreferences.getString(KEY_USER_FIRST_NAME, null))
                .setUserLastName(sharedPreferences.getString(KEY_USER_LAST_NAME, null))
                .setUserMobileNumber(sharedPreferences.getString(KEY_USER_PHONE_NUMBER, null))
                .setCountryCode(sharedPreferences.getString(KEY_USER_COUNTRY_CODE, null))
                .setUserEmailAddress(sharedPreferences.getString(KEY_USER_EMAIL_ADDRESS, null))
                .setUserAvatarId(sharedPreferences.getString(KEY_USER_AVATAR_ID, null))
                .setAnonymous(sharedPreferences.getBoolean(KEY_USER_IS_ANONYMOUS, true))
                .setSelectedTags(sharedPreferences.getStringSet(KEY_USER_SELECTED_TAGS, null))
                .setUserAdditionalProperties(getStringMap(KEY_USER_ADDITIONAL_PROPERTIES_KEY_SET));

        return user;
    }

    /**
     * Update saved user details and send the update to the Donky Network.
     *
     * @param user {@link UserDetails} instance.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean setUserDetails(UserDetails user) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferencesFileName(), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_USER_ID, user.getUserId())
                .putString(KEY_USER_DISPLAY_NAME, user.getUserDisplayName())
                .putString(KEY_USER_FIRST_NAME, user.getUserFirstName())
                .putString(KEY_USER_LAST_NAME, user.getUserLastName())
                .putString(KEY_USER_EMAIL_ADDRESS, user.getUserEmailAddress())
                .putString(KEY_USER_PHONE_NUMBER, user.getUserMobileNumber())
                .putString(KEY_USER_COUNTRY_CODE, user.getCountryCode())
                .putString(KEY_USER_AVATAR_ID, user.getUserAvatarId())
                .putBoolean(KEY_USER_IS_ANONYMOUS, user.isAnonymous())
                .putStringSet(KEY_USER_SELECTED_TAGS, user.getSelectedTags());

        return editor.commit() && setStringMap(KEY_USER_ADDITIONAL_PROPERTIES_KEY_SET, user.getUserAdditionalProperties());

    }

    /**
     * @return Unique identifier for user registration.
     */
    public String getUserNetworkId() {
        return getString(KEY_USER_NETWORK_ID, null);
    }

    /**
     * Save unique identifier for user registration.
     * @param userNetworkId Unique identifier for user registration.
     * @return Returns true if the new values were successfully written.
     */
    public boolean setUserNetworkId(String userNetworkId) {
        return setString(KEY_USER_NETWORK_ID, userNetworkId);
    }
}
