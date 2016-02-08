package net.donky.core.account;

import android.os.Build;

import java.util.Map;
import java.util.TreeMap;

/**
 * Wrapper class for device registration data.
 *
 * Created by Marcin Swierczek
 * 17/03/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DeviceDetails {

    private static final String OPERATING_SYSTEM_NAME = "Android";

    private String deviceType;

    private String deviceName;

    private Map<String, String> additionalProperties;

    public DeviceDetails(String deviceName, String deviceType, Map<String, String> additionalProperties) {
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        this.additionalProperties = additionalProperties;
    }

    /**
     * Type of device.
     *
     * @return Type of device.
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Set device type.
     *
     * @param deviceType Device type.
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Device name.
     *
     * @return Device name.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Set device name.
     *
     * @param deviceName Device name.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * @return Additional properties for device registration.
     */
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Set additional properties for device registration.
     *
     * @param additionalProperties Additional properties for device registration.
     */
    public void setAdditionalProperties(TreeMap<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Operating System name.
     *
     * @return Operating System name.
     */
    public static String getOSName() {
        return OPERATING_SYSTEM_NAME;
    }

    /**
     * Operating System version.
     *
     * @return Operating System version.
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Device model name.
     *
     * @return Device model name.
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * Compares provided device registration details with the one saved on the device.
     *
     * @param deviceDetails Device details to compare with.
     * @return True if new device details are identical to the one saved on the device.
     */
    public boolean equals(DeviceDetails deviceDetails) {

        if (deviceDetails != null) {

            boolean deviceAdditionalPropertiesAreTheSame = (deviceDetails.additionalProperties == null && (deviceDetails == null || additionalProperties.isEmpty())) ||
                    ( deviceDetails.additionalProperties != null && ( (deviceDetails.additionalProperties.equals(additionalProperties)) ||  (deviceDetails.additionalProperties.isEmpty() && additionalProperties == null) ));

            return (((deviceDetails.deviceName == null && deviceName == null) || (deviceDetails.deviceName != null && deviceDetails.deviceName.equals(deviceName))) &&
                    ((deviceDetails.deviceType == null && deviceType == null) || (deviceDetails.deviceType != null && deviceDetails.deviceType.equals(deviceType))) &&
                    deviceAdditionalPropertiesAreTheSame);

        } else {
            return (deviceName == null && deviceType == null && additionalProperties == null);
        }
    }
}
