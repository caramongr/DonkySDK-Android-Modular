package net.donky.core.messaging.rich.logic.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.database.BaseDAO;
import net.donky.core.model.DatabaseSQLHelper;
import net.donky.core.model.DonkyDataController;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Database Access Object for Rich Messages.
 * <p/>
 * Created by Marcin Swierczek
 * 21/02/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessagesDAO extends BaseDAO {

    private final DLog log;

    private final DatabaseSQLHelper databaseSQLHelper;

    /**
     * Database Access Object for Donky Client Notifications.
     *
     * @param databaseSQLHelper Helper class instance to translate to SQL
     */
    public RichMessagesDAO(DatabaseSQLHelper databaseSQLHelper) {

        log = new DLog("RichMessagesDAO");
        this.databaseSQLHelper = databaseSQLHelper;
    }

    /**
     * Gets all rich messages marked as unread.
     *
     * @return All rich messages marked as unread.
     */
    public List<RichMessage> getUnreadRichMessages() {

        return getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead + " = ?", new String[]{Integer.toString(0)});

    }

    /**
     * Get rich message with given internal id. Internal id is known only by the client SDK, not on the network.
     *
     * @param internalId Rich message internal id.
     * @return Rich message with given internal id.
     */
    public RichMessage getRichMessage(String internalId) {

        List<RichMessage> list = getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ?", new String[]{internalId});

        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Get rich message with given external message id. Rich message external id is given from the network.
     *
     * @param externalId Rich message external id.
     * @return Rich message with given external id.
     */
    public RichMessage getRichMessageWithMessageId(String externalId) {

        List<RichMessage> list = getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " = ?", new String[]{externalId});

        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Mark rich message as read.
     *
     * @param internalId Rich message internal id.
     * @return The number of rows affected.
     */
    public int markAsRead(String internalId) {

        SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead, 1);

        try {
            return db.update(DatabaseSQLContract.RichMessageEntry.TABLE_NAME, cv, DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ?", new String[]{internalId});
        } catch (SQLiteException exception) {
            log.error("Error removing all rich messages",exception);
        }

        return 0;
    }

    /**
     * Gets all rich messages.
     *
     * @return All rich messages.
     */
    public List<RichMessage> getAllRichMessages() {

        return getRichMessages(null, null);

    }

    /**
     * Gets rich messages with given SQLite selection and selectionArgs.
     *
     * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
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

            } catch (Exception e) {
                log.error("Error querying database", e);
                cursor = null;
            }


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
                            log.error("Error translating json to map.", e);
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
        }

        return richMessages;
    }

    /**
     * Add Rich Messages to database.
     *
     * @param richMessageList Rich Message list to be saved.
     */
    public synchronized void saveRichMessages(List<RichMessage> richMessageList) {

        if (richMessageList != null && !richMessageList.isEmpty()) {

            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            for (RichMessage richMessage : richMessageList) {
                saveRichMessage(db, richMessage);
            }
        }
    }

    /**
     * Add Rich Message to database.
     *
     * @param richMessage Rich Message to be saved.
     */
    public synchronized void saveRichMessage(RichMessage richMessage) {
        if (richMessage != null) {
            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();
            saveRichMessage(db, richMessage);
        }
    }

    /**
     * Remove Rich Messages from database.
     *
     * @param richMessageList Rich Messages to be removed.
     */
    public synchronized void removeRichMessages(List<RichMessage> richMessageList) {

        if (richMessageList != null && !richMessageList.isEmpty()) {

            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            String[] selectionArgs = new String[richMessageList.size()];

            for (int i = 0; i < richMessageList.size(); i++) {
                selectionArgs[i] = "'" + richMessageList.get(i).getInternalId() + "'";
            }

            String args = TextUtils.join(", ", selectionArgs);

            try {
                db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IN (%s);", args));
                db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IS NULL;"));
            } catch (SQLiteException exception) {
                log.error("Error removing rich messages",exception);
            }


        }
    }

    /**
     * Remove Rich Messages from database.
     *
     * @param richMessageIdList Rich Messages to be removed.
     */
    public synchronized void removeRichMessagesWithInternalIds(List<String> richMessageIdList) {

        if (richMessageIdList != null && !richMessageIdList.isEmpty()) {

            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            String[] selectionArgs = new String[richMessageIdList.size()];

            for (int i = 0; i < richMessageIdList.size(); i++) {
                selectionArgs[i] = "'" + richMessageIdList.get(i) + "'";
            }

            String args = TextUtils.join(", ", selectionArgs);

            try {
                db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IN (%s);", args));
                db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IS NULL;"));
            } catch (SQLiteException exception) {
                log.error("Error removing rich messages",exception);
            }


        }
    }

    /**
     * Remove Rich Message from database.
     *
     * @param richMessage Rich Message to be removed.
     */
    public synchronized void removeRichMessage(RichMessage richMessage) {

        if (richMessage != null) {

            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            String arg = "'" + richMessage.getInternalId() + "'";

            db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IS %s;", arg));

        }

    }

    /**
     * Remove Rich Message from database.
     *
     * @param richMessageInternalId Internal ID of a Rich Message to be removed.
     */
    public synchronized void removeRichMessage(String richMessageInternalId) {

        if (richMessageInternalId != null) {

            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            String arg = "'" + richMessageInternalId + "'";

            db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.RichMessageEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IS %s;", arg));

        }

    }

    /**
     * Gets rich message cursor containing all rich messages that didn't exceeded maximum availability days.
     *
     * @param filter String to filter the rich message sender display name and description
     * @return Cursor with filtered rich messages.
     */
    public Cursor getRichMessagesCursorForUI(String filter) {

        SQLiteDatabase db = databaseSQLHelper.getReadableDatabase();

        Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

        final long currentTimeUTC = System.currentTimeMillis();
        final long acceptableSentTimeMillisUTC;

        acceptableSentTimeMillisUTC = currentTimeUTC - TimeUnit.DAYS.toMillis(availabilityDays);

        String[] projection = {
                DatabaseSQLContract.RichMessageEntry._ID,
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
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp,
                DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong

        };

        String selection = null;
        String arg;
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(filter)) {
            selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description + " LIKE ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName + " LIKE ? AND " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " > ?";
            arg = "%" + filter + "%";
            selectionArgs = new String[]{arg, arg, String.valueOf(acceptableSentTimeMillisUTC)};
        }

        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseSQLContract.RichMessageEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " DESC" //  sort
            );
        } catch (SQLiteException exception) {
            log.error("Error obtaining rich messages cursor.",exception);
        }

        return cursor;
    }

    /**
     * Clean the database deleting unavailable rich messages.
     */
    public synchronized void removeMessagesThatExceededTheAvailabilityPeriod() {

        Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

        final long currentTimeUTC = System.currentTimeMillis();
        final long acceptableSentTimeMillisUTC = currentTimeUTC - TimeUnit.DAYS.toMillis(availabilityDays);

        SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

        String arg = String.valueOf(acceptableSentTimeMillisUTC);

        String command = String.format(new StringBuilder().append("DELETE FROM ").append(DatabaseSQLContract.RichMessageEntry.TABLE_NAME).append(" WHERE ").append(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong).append(" < %s").toString(), arg);

        try {
            db.execSQL(command);
        } catch (SQLiteException exception) {
            log.error("Error clearing unavailable rich messages",exception);
        }

    }

    /**
     * Add single Donky client notification to database.
     *
     * @param db          SQLite Database.
     * @param richMessage Rich Message to be saved.
     */
    private synchronized void saveRichMessage(SQLiteDatabase db, RichMessage richMessage) {

        if (richMessage != null) {

            // New value for one column
            ContentValues values = new ContentValues();

            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId, richMessage.getInternalId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageType, richMessage.getMessageType());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderExternalUserId, richMessage.getSenderExternalUserId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_externalRef, richMessage.getExternalRef());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description, richMessage.getDescription());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiredBody, richMessage.getExpiredBody());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canReply, richMessage.isCanReply());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canForward, richMessage.isCanForward());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_canShare, richMessage.isCanShare());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_urlToShare, richMessage.getUrlToShare());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_silentNotification, richMessage.isSilentNotification());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_msgSentTimeStamp, richMessage.getMsgSentTimeStamp());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardedBy, richMessage.getForwardedBy());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_forwardingOverlayMessage, richMessage.getForwardingOverlayMessage());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_conversationId, richMessage.getConversationId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderAccountType, richMessage.getSenderAccountType());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName, richMessage.getSenderDisplayName());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_body, richMessage.getBody());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageScope, richMessage.getMessageScope());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderInternalUserId, richMessage.getSenderInternalUserId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderMessageId, richMessage.getSenderMessageId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId, richMessage.getMessageId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_contextItems, toJson(richMessage.getContextItems()));
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_avatarAssetId, richMessage.getAvatarAssetId());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestamp, richMessage.getSentTimestamp());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong, DateAndTimeHelper.parseUTCStringToUTCLong(richMessage.getSentTimestamp()));
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_expiryTimeStamp, richMessage.getExpiryTimeStamp());
            values.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead, richMessage.isMessageRead());

            // Insert the new row, returning the primary key value of the new row
            long newRowId;

            try {

                newRowId = db.insert(
                        DatabaseSQLContract.RichMessageEntry.TABLE_NAME,
                        null,
                        values);

                if (newRowId == -1) {
                    log.error("Error when inserting Rich Message to DB.");
                }

            } catch (SQLiteException exception) {
                log.error("Error when inserting Rich Message to DB.",exception);
            }

        }
    }

    /**
     * Delete all saved rich messages.
     */
    public void removeAllRichMessages() {
        SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();
        String command = new StringBuilder().append("DELETE FROM ").append(DatabaseSQLContract.RichMessageEntry.TABLE_NAME).toString();

        try {
            db.execSQL(command);
        } catch (SQLiteException exception) {
            log.error("Error removing all rich messages",exception);
        }

    }
}