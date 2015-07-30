package net.donky.core.messaging.rich.logic.model;

import android.app.Application;

import net.donky.core.logging.DLog;
import net.donky.core.model.DatabaseSQLHelper;
import net.donky.core.model.DonkyDataController;

/**
 * Created by Marcin Swierczek
 * 05/07/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageDataController {

    private final DLog log;

    private DatabaseSQLHelper databaseSQLHelper;

    private RichMessagesDAO richMessagesDAO;

    // Private constructor. Prevents instantiation from other classes.
    private RichMessageDataController() {
        log = new DLog("RichMessageDataController");
    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final RichMessageDataController INSTANCE = new RichMessageDataController();
    }

    public static RichMessageDataController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise controller instance. This method should only be used by Donky Core.
     *
     * @param application Application instance.
     */
    public void init(Application application) {
        log.info("Donky Rich Message Database version: " + RichMessagingSQLiteHelper.DATABASE_VERSION);
    }

    public RichMessagesDAO getRichMessagesDAO() {

        if (databaseSQLHelper == null) {
            databaseSQLHelper = DonkyDataController.getInstance().getDatabaseSQLHelper();
        }

        if (richMessagesDAO == null && databaseSQLHelper != null) {

            richMessagesDAO = new RichMessagesDAO(databaseSQLHelper);
        }

        return richMessagesDAO;
    }
}
