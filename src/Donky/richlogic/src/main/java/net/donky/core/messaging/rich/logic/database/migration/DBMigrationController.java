package net.donky.core.messaging.rich.logic.database.migration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import net.donky.core.DonkyCore;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.database.BaseDAO;
import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;
import net.donky.core.model.DatabaseSQLHelper;
import net.donky.core.model.DonkyDataController;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is for one time migration between core DB and rich module db.
 *
 * Created by Marcin Swierczek
 * 01/02/2016.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DBMigrationController extends BaseDAO {

    private static final String PREFS_NAME = "RichDBMigration";
    private static final String PREFS_KEY_NAME = "isMigrated";

    private final Context context;

    private DatabaseSQLHelper databaseSQLHelper;

    public DBMigrationController(Context context) {
        this.context = context;
        databaseSQLHelper = DonkyDataController.getInstance().getDatabaseSQLHelper();
    }

    /**
     * Migrates all Rich Messages from core DB to Rich Logic Module DB. Executes only once.
     */
    public void migrateDBContent() {

        if (!isMigrated(context)) {
            DonkyCore.getInstance().processInBackground(new Runnable() {
                @Override
                public void run() {
                    List<RichMessage> richMessages = getRichMessages(null, null);
                    RichMessageDataController.getInstance().getRichMessagesDAO().saveRichMessages(richMessages);
                    removeAllRichMessages();
                    saveIsMigrated(context);
                }
            });
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void saveIsMigrated(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(PREFS_KEY_NAME, true);
        editor.commit();
    }

    private boolean isMigrated(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFS_KEY_NAME, false);
    }

    /**
     * Gets rich messages with given SQLite selection and selectionArgs.
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     * @return List of found rich messages.
     */
    private synchronized List<RichMessage> getRichMessages(String selection, String[] selectionArgs) {

        LinkedList<RichMessage> richMessages = new LinkedList<>();

        SQLiteDatabase db = databaseSQLHelper.getReadableDatabase();

        String[] projection = {

                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageType,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderExternalUserId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiredBody,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canReply,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_externalRef,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canForward,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canShare,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_urlToShare,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_silentNotification,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_msgSentTimeStamp,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardedBy,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardingOverlayMessage,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_conversationId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderAccountType,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_body,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageScope,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderInternalUserId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderMessageId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_contextItems,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_avatarAssetId,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp

        };

        if (db != null) {

            Cursor cursor;

            try {

                cursor = db.query(
                        DatabaseSQLContract.RichMessageEntry.TABLE_NAME,  // The table to query
                        projection,                               // The columns to return
                        selection,                                // The columns for the WHERE clause
                        selectionArgs,                            // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        null                                 // don't sort
                );

                if (cursor != null) {
                    if (cursor.moveToFirst()) {

                        do {

                            RichMessage richMessage = new RichMessage();

                            richMessage.setMessageType(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageType)));

                            richMessage.setAvatarAssetId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_avatarAssetId)));

                            richMessage.setSenderDisplayName(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName)));

                            richMessage.setBody(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_body)));

                            richMessage.setCanForward(toBoolean(cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canForward))));

                            richMessage.setCanReply(toBoolean(cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canReply))));

                            richMessage.setCanShare(toBoolean(cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canShare))));

                            try {

                                richMessage.setContextItems(toMap(cursor.getString(
                                        cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_contextItems))));

                            } catch (JSONException e) {
                                new DLog("DBMigrationController").error("Error translating json to map.", e);
                            }

                            richMessage.setConversationId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_conversationId)));

                            richMessage.setDescription(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description)));

                            richMessage.setExpiredBody(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiredBody)));

                            richMessage.setExternalRef(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_externalRef)));

                            richMessage.setExpiryTimeStamp(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp)));

                            richMessage.setForwardedBy(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardedBy)));

                            richMessage.setForwardingOverlayMessage(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardingOverlayMessage)));

                            richMessage.setInternalId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId)));

                            richMessage.setMessageId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId)));

                            richMessage.setMessageRead(toBoolean(cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead))));

                            richMessage.setSenderAccountType(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderAccountType)));

                            richMessage.setSenderExternalUserId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderExternalUserId)));

                            richMessage.setSenderInternalUserId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderInternalUserId)));

                            richMessage.setUrlToShare(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_urlToShare)));

                            richMessage.setMsgSentTimeStamp(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_msgSentTimeStamp)));

                            richMessage.setSenderMessageId(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderMessageId)));

                            richMessage.setMessageScope(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageScope)));

                            richMessage.setSentTimestamp(cursor.getString(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp)));

                            richMessage.setSilentNotification(toBoolean(cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_silentNotification))));

                            richMessages.add(richMessage);

                        }
                        while (cursor.moveToNext());
                    }
                    if (!cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                new DLog("DBMigrationController").error("Error querying database", e);
            } finally {
                db.close();
            }
        }

        return richMessages;
    }

    /**
     * Delete all saved rich messages.
     */
    private void removeAllRichMessages() {
        SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();
        String command = "DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME;

        try {
            db.execSQL(command);
        } catch (SQLiteException exception) {
            new DLog("DBMigrationController").error("Error removing all rich messages", exception);
        } finally {
            db.close();
        }
    }
}
