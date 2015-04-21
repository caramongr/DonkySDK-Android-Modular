package net.donky.core.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Base class for all Database Access Objects based on Android Shared Preferences.
 *
 * Created by Marcin Swierczek
 * 25/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
class SharedPreferencesBaseDAO {

    private final String sharedPreferencesFileName;

    final Context context;

    SharedPreferencesBaseDAO(Context context, String sharedPreferencesFileName) {
        this.sharedPreferencesFileName = sharedPreferencesFileName;
        this.context = context;
    }

    /**
     * Get Boolean value from internal shared preferences file.
     *
     * @param key          Key for preference entry.
     * @param defaultValue Default value that should be used when no value was saved.
     * @return Setting from internal shared preferences file.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName,Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Get Integer value from Shared Preferences file.
     *
     * @param key Key for preference entry.
     * @param defaultValue Default value returned if no entry was found.
     * @return Setting from internal shared preferences file.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    int getInteger(String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Get Long value from Shared Preferences file.
     *
     * @param key Key for preference entry.
     * @param defaultValue Default value returned if no entry was found.
     * @return Setting from internal shared preferences file.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    long getLong(String key, long defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getLong(key, defaultValue);
    }

    /**
     * Get String value from internal shared preferences file.
     *
     * @param key          Key for preference entry.
     * @param defaultValue Default value that should be used when no value was saved.
     * @return Setting from internal shared preferences file.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    String getString(String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Set Boolean in internal shared preferences file.
     *
     * @param key   Key for internal preference entry.
     * @param value Value for internal preference entry.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean setBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * Save Integer value in Shared Preferences file.
     *
     * @param key Key for shared preference entry.
     * @param value Value for shared preference entry.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean setInteger(String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * Save Laong value in Shared Preferences file.
     *
     * @param key Key for shared preference entry.
     * @param value Value for shared preference entry.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean setLong(String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * Set String in internal shared preferences file.
     *
     * @param key   Key for internal preference entry.
     * @param value Value for internal preference entry.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean setString(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Get dictionary saved in Shared Preferences file.
     *
     * @param keyForKeySet Key identifying the dictionary in Shared Preferences file.
     * @return Dictionary saved in Shared Preferences file.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    TreeMap<String, String> getStringMap(String keyForKeySet) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);

        TreeMap<String, String> additionalProperties = new TreeMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Set<String> additionalPropertiesKeySet = sharedPreferences.getStringSet(keyForKeySet, null);
            if (additionalPropertiesKeySet != null) {
                for (String key : additionalPropertiesKeySet) {
                    additionalProperties.put(key, sharedPreferences.getString(key, null));
                }
            }
        }
        return additionalProperties;
    }

    /**
     * Save dictionary to Shared Preferences file.
     *
     * @param keyToObtainSetOfKeys Key used to identify dictionary in Shared Preferences file.
     * @param mapToAdd Dictionary to be saved.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean setStringMap(String keyToObtainSetOfKeys, Map<String, String> mapToAdd) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            Set<String> additionalPropertiesKeySet = sharedPreferences.getStringSet(keyToObtainSetOfKeys, null);

            if (additionalPropertiesKeySet != null) {

                for (String key : additionalPropertiesKeySet) {

                    editor.remove(key);

                }

            }

            editor.putStringSet(keyToObtainSetOfKeys, null);

            if (mapToAdd != null) {

                Set<String> keySet = mapToAdd.keySet();

                editor.putStringSet(keyToObtainSetOfKeys, keySet);

                for (String key : keySet) {

                    editor.putString(key, mapToAdd.get(key));

                }

            }


            return editor.commit();

        }

        return false;

    }

    /**
     * Add single entry to dictionary saved in Shared Preferences file.
     *
     * @param keyToObtainSetOfKeys Key used to identify dictionary in Shared Preferences file.
     * @param key Key for dictionary entry to save.
     * @param value Value for dictionary entry to save.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean addToStringMap(String keyToObtainSetOfKeys, String key, String value) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !TextUtils.isEmpty(key)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
            Set<String> newSetOfKeys = sharedPreferences.getStringSet(keyToObtainSetOfKeys, new LinkedHashSet<String>());
            newSetOfKeys.add(key);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putStringSet(keyToObtainSetOfKeys, newSetOfKeys);
            editor.putString(key, value);

            return editor.commit();
        }
        return false;
    }

    /**
     * Add map to dictionary to Shared Preferences file.
     *
     * @param keyToObtainSetOfKeys Key used to identify dictionary in Shared Preferences file.
     * @param mapToAdd Dictionary to be saved.
     * @return Returns true if the new values were successfully written
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    boolean addToStringMap(String keyToObtainSetOfKeys, Map<String, String> mapToAdd) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mapToAdd != null) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_MULTI_PROCESS);
            Set<String> newSetOfKeys = sharedPreferences.getStringSet(keyToObtainSetOfKeys, new LinkedHashSet<String>());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String key : mapToAdd.keySet()) {
                newSetOfKeys.add(key);
                editor.putString(key, mapToAdd.get(key));
            }

            editor.putStringSet(keyToObtainSetOfKeys, newSetOfKeys);
            return editor.commit();
        }
        return false;
    }

    /**
     * @return File name where Shared Preferences are being stored.
     */
    String getSharedPreferencesFileName() {
        return sharedPreferencesFileName;
    }
}
