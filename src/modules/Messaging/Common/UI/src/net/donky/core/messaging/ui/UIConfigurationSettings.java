package net.donky.core.messaging.ui;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Marcin Swierczek
 * 20/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class UIConfigurationSettings {

    private static final String KEY_TOOLBAR_BACKGROUND_COLOR = "donkyColorPrimary";

    private int toolbarBackgroundColor;

    // Private constructor. Prevents instantiation from other classes.
    private UIConfigurationSettings() {
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final UIConfigurationSettings INSTANCE = new UIConfigurationSettings();
    }

    public static UIConfigurationSettings getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise all settings. This should be called only by Donky Core module.
     * @param application Instance of Application class.
     */
    public void init(Application application) {
        toolbarBackgroundColor = getColor(application.getApplicationContext(), KEY_TOOLBAR_BACKGROUND_COLOR, 0);
    }

    /**
     * Translate String representation of a boolean to a boolean value.
     *
     * @param context      Application Context.
     * @param key          Key for Donky client setting.
     * @param defaultValue Returned when no string with given key was found in application resources.
     * @return Value of the String application resource.
     */
    private boolean getBoolean(Context context, String key, boolean defaultValue) {
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
    private String getString(Context appContext, String key, String defaultValue) {
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
    private int getInt(Context appContext, String key, int defaultValue) {
        Resources resources = appContext.getResources();
        int id = resources.getIdentifier(key, "string", appContext.getPackageName());
        if (id != 0) {
            return resources.getInteger(id);
        }
        return defaultValue;
    }

    /**
     * Method to get color from application resources.
     *
     * @param appContext Application Context.
     * @param key        Key for String application resource.
     * @return Value of the String application resource.
     */
    private int getColor(Context appContext, String key, int defaultValue) {
        Resources resources = appContext.getResources();
        int id = resources.getIdentifier(key, "color", appContext.getPackageName());
        if (id != 0) {
            return resources.getColor(id);
        }
        return defaultValue;
    }

    public int getToolbarBackgroundColor() {
        return toolbarBackgroundColor;
    }

}
