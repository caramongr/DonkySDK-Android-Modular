package net.donky.core.messaging.rich.logic.model;

import android.content.Context;

import net.donky.core.logging.DLog;
import net.donky.core.messaging.rich.logic.database.RichMsgSQLiteHelper;

/**
 * Created by Marcin Swierczek
 * 05/07/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessageDataController {

    private RichMsgSQLiteHelper databaseSQLHelper;

    private RichMessagesDAO richMessagesDAO;

    private Context appContext;

    // Private constructor. Prevents instantiation from other classes.
    private RichMessageDataController() {
    }

    /**
     * Initializes singleton.
     *
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
     * @param context Application context.
     */
    public void init(Context context) {
        if (this.appContext == null) {
            this.appContext = context;
            new DLog("RichMessageDataController").info("Donky Rich Messaging Database version: " + RichMsgSQLiteHelper.DATABASE_VERSION);
        }
    }

    /**
     * Gets data access object for rich messages.
     *
     * @return Data access object for rich messages.
     */
    public RichMessagesDAO getRichMessagesDAO() {
        if (richMessagesDAO == null) {
            richMessagesDAO = new RichMessagesDAO(appContext);
        }
        return richMessagesDAO;
    }

    public RichMsgSQLiteHelper getDatabaseSQLHelper() {
        if (databaseSQLHelper == null) {
            databaseSQLHelper = new RichMsgSQLiteHelper(appContext);
        }
        return databaseSQLHelper;
    }
}
