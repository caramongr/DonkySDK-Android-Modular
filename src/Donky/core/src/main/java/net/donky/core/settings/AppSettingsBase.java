package net.donky.core.settings;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Marcin Swierczek
 * 31/08/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AppSettingsBase {

    /**
     * Translate String representation of a boolean to a boolean value.
     *
     * @param context      Application Context.
     * @param key          Key for Donky client setting.
     * @param defaultValue Returned when no string with given key was found in application resources.
     * @return Value of the String application resource.
     */
    protected boolean getBoolean(Context context, String key, boolean defaultValue) {
        String resValueAsString = getString(context, key, null);
        if (resValueAsString != null) {
            if ("true".equalsIgnoreCase(resValueAsString) || "yes".equalsIgnoreCase(resValueAsString) || "1".equalsIgnoreCase(resValueAsString)) {
                return true;
            } else if ("false".equalsIgnoreCase(resValueAsString) || "no".equalsIgnoreCase(resValueAsString) || "0".equalsIgnoreCase(resValueAsString)) {
                return false;
            }
        }
        return defaultValue;
    }

    /**
     * Method to get String from application resources.
     *
     * @param appContext Application Context.
     * @param key        Key for String application resource.
     * @return Value of the String application resource.
     */
    protected String getString(Context appContext, String key, String defaultValue) {
        Resources resources = appContext.getResources();
        int id = resources.getIdentifier(key, "string", appContext.getPackageName());
        if (id != 0) {
            return resources.getString(id);
        }
        return defaultValue;
    }

    /**
     * Method to get integer from application resources.
     *
     * @param appContext Application Context.
     * @param key        Key for String application resource.
     * @return Value of the String application resource.
     */
    protected int getInt(Context appContext, String key, int defaultValue) {
        Resources resources = appContext.getResources();
        int id = resources.getIdentifier(key, "string", appContext.getPackageName());
        if (id != 0) {
            return resources.getInteger(id);
        }
        return defaultValue;
    }

    /**
     * Gets the resource id for given drawable file name.
     *
     * @param context Application Context.
     * @param resourceFileName Name of drawable file.
     * @return The resource id for given drawable file name. Returns 0 if not found.
     */
    protected int getResourceDrawableId(Context context, String resourceFileName) {
        return context.getResources().getIdentifier(resourceFileName , "drawable", context.getPackageName());
    }

}
