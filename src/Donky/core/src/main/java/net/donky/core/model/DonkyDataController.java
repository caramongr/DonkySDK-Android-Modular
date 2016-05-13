package net.donky.core.model;

import android.app.Application;

import net.donky.core.logging.DLog;
import net.donky.core.logging.DonkyLoggingController;

/**
 * controller class for all database related operations.
 *
 * Created by Marcin Swierczek
 * 25/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyDataController {

    private final DLog log;

    /**
     * Provides access to device/account related data.
     */
    private DeviceDAO deviceDAO;

    /**
     * Provides access to user/account related data.
     */
    private UserDAO userDAO;

    /**
     * Provides access to internal configuration data.
     */
    private ConfigurationDAO configurationDAO;

    /**
     * Provides access to saved software version numbers.
     */
    private SoftwareVersionsDAO softwareVersionsDAO;

    private NotificationDAO notificationDAO;

    private DatabaseSQLHelper databaseSQLHelper;

    // Private constructor. Prevents instantiation from other classes.
    private DonkyDataController() {
        log = new DLog("DataController");
    }

    /**
     * Initializes singleton.
     *
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyDataController INSTANCE = new DonkyDataController();
    }

    public static DonkyDataController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application) {
        log.info("Donky Core Database version: " + DatabaseSQLHelper.DATABASE_VERSION);
        databaseSQLHelper = new DatabaseSQLHelper(application.getApplicationContext());

        notificationDAO = new NotificationDAO(databaseSQLHelper);
        deviceDAO = new DeviceDAO(application.getApplicationContext());
        userDAO = new UserDAO(application.getApplicationContext());
        configurationDAO = new ConfigurationDAO(application.getApplicationContext());
        softwareVersionsDAO = new SoftwareVersionsDAO(application.getApplicationContext());

        DonkyLoggingController.getInstance().setAutoSubmit(Boolean.getBoolean(DonkyDataController.getInstance().getConfigurationDAO().getConfigurationItems().get(ConfigurationDAO.KEY_CONFIGURATION_AlwaysSubmitErrors)));
    }

    /**
     * @return Device registration details Database Access Object.
     */
    public DeviceDAO getDeviceDAO() {
        return deviceDAO;
    }

    /**
     * @return User registration details Database Access Object.
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     * @return Configuration settings Database Access Object.
     */
    public ConfigurationDAO getConfigurationDAO() {
        return configurationDAO;
    }

    /**
     * @return Database Access Object for client and content notifications.
     */
    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }

    /**
     * Gets Database open helper
     * @return SQLite Database helper
     */
    public DatabaseSQLHelper getDatabaseSQLHelper() {
        return databaseSQLHelper;
    }

    /**
     * @return Database Access Object for software version numbers.
     */
    public SoftwareVersionsDAO getSoftwareVersionsDAO() {
        return softwareVersionsDAO;
    }
}
