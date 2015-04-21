package net.donky.core.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    public static final int DATABASE_VERSION = 1;

    /**
     * File name for SQLite database.
     */
    public static final String DATABASE_NAME = "DonkyCore.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String LONG_TYPE = " LONG";
    private static final String COMMA_SEP = ",";

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

    private static final String SQL_CREATE_RICH_MESSAGES_TABLE =
            "CREATE TABLE " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + "(" +
                    DatabaseSQLContract.RichMessageEntry._ID + " INTEGER PRIMARY KEY," +

                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead+ INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + TEXT_TYPE + COMMA_SEP +

                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageType + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderExternalUserId + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiredBody + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canReply + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_externalRef + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canForward + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canShare + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_urlToShare + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_silentNotification + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_msgSentTimeStamp + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardedBy + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardingOverlayMessage + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_conversationId + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderAccountType + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_body + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageScope + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderInternalUserId + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderMessageId + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_contextItems + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_avatarAssetId + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp + TEXT_TYPE + ")";

    private static final String SQL_DELETE_RICH_MESSAGES_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME;

    /**
     * Helper class to create and update SGLite database.
     * @param context Application context.
     */
    public DatabaseSQLHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create database.
     */
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_CLIENT_NOTIFICATIONS_TABLE);

        db.execSQL(SQL_CREATE_RICH_MESSAGES_TABLE);


    }

    /**
     * Upgrade database table.
     *
     * @param db SQL database.
     * @param oldVersion Old version number of the database.
     * @param newVersion New version number of the database.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_CLIENT_NOTIFICATIONS_ENTRIES);

        db.execSQL(SQL_DELETE_RICH_MESSAGES_ENTRIES);

        onCreate(db);
    }

    /**
     * Downgrade the database.
     *
     * @param db SQL database.
     * @param oldVersion Old version number of the database.
     * @param newVersion New version number of the database.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        onUpgrade(db, oldVersion, newVersion);
    }
}