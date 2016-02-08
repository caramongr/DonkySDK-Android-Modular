package net.donky.core.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.donky.core.DonkyCore;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to crate and update SQL database.
 *
 * Created by Marcin Swierczek
 * 21/02/2015
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DatabaseSQLHelper extends SQLiteOpenHelper {

    /**
     * If the database schema change, the database version will be incremented.
     */
    public static final int DATABASE_VERSION = 2;

    /**
     * File name for SQLite database.
     */
    public static final String DATABASE_NAME = "DonkyCore.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String LONG_TYPE = " LONG";
    private static final String COMMA_SEP = ",";

    private List<AbstractDonkySQLiteHelper> additionalQSLiteHelperInterfaces;

    private static final String SQL_CREATE_CLIENT_NOTIFICATIONS_TABLE =
            "CREATE TABLE " + DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME + "(" +
                    DatabaseSQLContract.ClientNotificationEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_TYPE + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_JSON_STRING + TEXT_TYPE + COMMA_SEP +

                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_TYPE + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_RESULT + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_SENT_TIME + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_CUSTOM_NOTIFICATION_TYPE + TEXT_TYPE + ")";

    private static final String SQL_DELETE_CLIENT_NOTIFICATIONS_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME;

    /**
     * Helper class to create and update SGLite database.
     * @param context Application context.
     */
    public DatabaseSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        additionalQSLiteHelperInterfaces = new LinkedList<>();
        Map<String, Object> helpers = DonkyCore.getInstance().getServices(AbstractDonkySQLiteHelper.SERVICE_CATEGORY_SQLITE_HELPER);
        if (helpers != null) {
            for (Object helper : helpers.values()) {
                additionalQSLiteHelperInterfaces.add((AbstractDonkySQLiteHelper)helper);
            }
        }
    }

    /**
     * Create database.
     */
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_CLIENT_NOTIFICATIONS_TABLE);

        for (AbstractDonkySQLiteHelper helper : additionalQSLiteHelperInterfaces) {
            helper.onCreate(db);
        }

    }

    /**
     * Upgrade database table.
     *
     * @param db SQL database.
     * @param oldVersion Old version number of the database.
     * @param newVersion New version number of the database.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (AbstractDonkySQLiteHelper helper : additionalQSLiteHelperInterfaces) {
            helper.onUpgrade(db, oldVersion, newVersion);
        }

    }

    /**
     * Downgrade the database.
     *
     * @param db SQL database.
     * @param oldVersion Old version number of the database.
     * @param newVersion New version number of the database.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (AbstractDonkySQLiteHelper helper : additionalQSLiteHelperInterfaces) {
            helper.onDowngrade(db, oldVersion, newVersion);
        }

    }
}