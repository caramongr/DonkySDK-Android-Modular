package net.donky.core.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import net.donky.core.ModuleDefinition;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Marcin Swierczek
 * 21/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class SoftwareVersionsDAO extends SharedPreferencesBaseDAO {

    /**
     * File name for device details storage.
     */
    private static final String SHARED_PREFERENCES_FILENAME_SOFTWARE = "DonkyPreferencesSoftware";

    /*
     * Key names for Shared Preferences storage.
     */
    private static final String KEY_SDK_VERSION = "sdkVersion";
    private static final String KEY_OS_VERSION= "operatingSystemVersion";
    private static final String KEY_MODULE_VERSIONS_KEY_SET = "moduleVersions";

    /**
     * Device registration details Database Access Object.
     *
     * @param context Application context.
     */
    public SoftwareVersionsDAO(Context context) {
        super(context, SHARED_PREFERENCES_FILENAME_SOFTWARE);
    }

    /**
     * Save the software version details.
     *
     * @param operatingSystemVersion
     * @param sdkVersion
     * @param moduleVersions
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean setSoftwareVersions(String operatingSystemVersion, String sdkVersion, Map<String, String> moduleVersions) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferencesFileName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_OS_VERSION, operatingSystemVersion)
                .putString(KEY_SDK_VERSION, sdkVersion);

        return editor.commit() && setStringMap(KEY_MODULE_VERSIONS_KEY_SET, moduleVersions);
    }

    /**
     * Save the software version details.
     *
     * @param operatingSystemVersion
     * @param sdkVersion
     * @param modules
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean setSoftwareVersions(String operatingSystemVersion, String sdkVersion, List<ModuleDefinition> modules) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(getSharedPreferencesFileName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_SDK_VERSION, operatingSystemVersion)
                .putString(KEY_OS_VERSION, sdkVersion);

        Map<String, String> newModulesVersions = new TreeMap<>();
        for (ModuleDefinition moduleDefinition : modules) {
            newModulesVersions.put(moduleDefinition.getName(), moduleDefinition.getVersion());
        }

        return editor.commit() && setStringMap(KEY_MODULE_VERSIONS_KEY_SET, newModulesVersions);
    }


    /**
     * Get saved Donky SDK version.
     *
     * @return Saved Donky SDK version.
     */
    public String getSavedDonkySDKVersion() {
        return getString(KEY_SDK_VERSION, "");
    }

    /**
     * Get saved Operating System version.
     *
     * @return Saved Operating System version.
     */
    public String getSavedOperatingSystemVersion() {
        return getString(KEY_SDK_VERSION, "");
    }

    /**
     * Get saved Donky SDK modules versions.
     *
     * @return Saved Donky SDK modules versions.
     */
    public Map<String, String> getSavedDonkySDKModulesVersions() {
        return getStringMap(KEY_MODULE_VERSIONS_KEY_SET);
    }
}
