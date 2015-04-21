package net.donky.core.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import net.donky.core.account.DeviceDetails;

/**
 * Database Access Object for all device registration data.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DeviceDAO extends SharedPreferencesBaseDAO {

    /**
     * File name for device details storage.
     */
    private static final String SHARED_PREFERENCES_FILENAME_DEVICE = "DonkyPreferencesDevice";

    /*
     * Key names for Shared Preferences storage.
     */
    private static final String KEY_DEVICE_ID = "deviceId";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_DEVICE_SECRET = "deviceSecret";
    private static final String KEY_DEVICE_NAME = "deviceName";
    private static final String KEY_DEVICE_ADDITIONAL_PROPERTIES_KEY_SET = "additionalKeySet";

    /**
     * Device registration details Database Access Object.
     *
     * @param context Application context.
     */
    public DeviceDAO(Context context) {
        super(context, SHARED_PREFERENCES_FILENAME_DEVICE);
    }

    /**
     * Save the device registration details.
     *
     * @param deviceDetails Device registration details.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean setDeviceDetails(DeviceDetails deviceDetails) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferencesFileName(), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_DEVICE_NAME, deviceDetails.getDeviceName())
                .putString(KEY_DEVICE_TYPE, deviceDetails.getDeviceType());

        return editor.commit() && setStringMap(KEY_DEVICE_ADDITIONAL_PROPERTIES_KEY_SET, deviceDetails.getAdditionalProperties());
    }

    /**
     * Get the device registration details.
     *
     * @return Device registration details.
     */
    public DeviceDetails getDeviceDetails() {
        return new DeviceDetails(getString(KEY_DEVICE_NAME, null),
        getString(KEY_DEVICE_TYPE, null),
        getStringMap(KEY_DEVICE_ADDITIONAL_PROPERTIES_KEY_SET));
    }

    /**
     * Save the device registration identifier.
     *
     * @param deviceSecret Device registration identifier to be saved.
     */
    public void setDeviceSecret(String deviceSecret) {
        setString(KEY_DEVICE_SECRET, deviceSecret);
    }

    /**
     * @return Device registration identifier.
     */
    public String getDeviceSecret() {
        return getString(KEY_DEVICE_SECRET, null);
    }

    /**
     * Save the unique device id.
     * @param deviceId Unique device id.
     */
    public void setDeviceId(String deviceId) {
        setString(KEY_DEVICE_ID, deviceId);
    }

    /**
     * @return Unique device id.
     */
    public String getDeviceId() {
        return getString(KEY_DEVICE_ID, null);
    }

}
