package net.donky.core.helpers;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.UUID;

/**
 * Helper class to generate unique id's.
 *
 * Created by Marcin Swierczek
 * 24/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class IdHelper {

    /**
     * Generate unique device id.
     * @param context Application context.
     * @return Identification string.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String generateDeviceId(Context context) {
        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    deviceId = wifiInfo.getMacAddress();
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && TextUtils.isEmpty(deviceId)) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter != null) {
                    deviceId = bluetoothAdapter.getAddress();
                }
            }
        }
        if (TextUtils.isEmpty(deviceId)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                deviceId = telephonyManager.getDeviceId();
            }
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = generateId();
        }
        return deviceId;
    }

    /**
     * @return Random unique identifier.
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
