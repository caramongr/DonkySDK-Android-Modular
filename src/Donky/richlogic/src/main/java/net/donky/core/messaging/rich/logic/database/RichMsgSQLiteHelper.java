package net.donky.core.messaging.rich.logic.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;

/**
 * Implementation of SQLite helper interface for rich logic module to use when creating database.
 *
 * Created by Marcin Swierczek
 * 05/07/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMsgSQLiteHelper extends SQLiteOpenHelper {

    /**
     * If the database schema change, the database version will be incremented.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * File name for SQLite database.
     */
    public static final String DATABASE_NAME = "DonkyRich.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String LONG_TYPE = " LONG";
    private static final String COMMA_SEP = ",";

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

    /**
     * Helper class to create and update SGLite database.
     *
     * @param context Application context.
     */
    public RichMsgSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RICH_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
