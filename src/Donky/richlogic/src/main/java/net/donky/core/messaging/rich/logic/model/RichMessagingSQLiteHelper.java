package net.donky.core.messaging.rich.logic.model;

import android.database.sqlite.SQLiteDatabase;

import net.donky.core.model.AbstractDonkySQLiteHelper;

/**
 * Implementation of SQLite helper interface for Core module to use when creating database.
 *
 * Created by Marcin Swierczek
 * 05/07/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
@Deprecated
public class RichMessagingSQLiteHelper extends AbstractDonkySQLiteHelper {

    /**
     * If the database schema change, the database version will be incremented.
     */
    public static final int DATABASE_VERSION = 2;

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
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + LONG_TYPE +")";

    private static final String SQL_DELETE_RICH_MESSAGES_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RICH_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE '" + net.donky.core.model.DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME + "' ADD COLUMN " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + LONG_TYPE+";");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
