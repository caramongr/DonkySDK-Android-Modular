package net.donky.core.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import net.donky.core.logging.DLog;
import net.donky.core.network.AcknowledgementDetail;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.content.ContentNotification;

import java.util.LinkedList;
import java.util.List;

/**
 * Database Access Object for outbound notifications.
 *
 * Created by Marcin Swierczek
 * 21/02/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class NotificationDAO {

    private final DLog log;

    private final DatabaseSQLHelper databaseSQLHelper;

    /**
     * Database Access Object for Donky Client Notifications.
     *
     * @param databaseSQLHelper Helper class instance to translate to SQL
     */
    public NotificationDAO(DatabaseSQLHelper databaseSQLHelper) {

        log = new DLog("ClientNotificationDAO");
        this.databaseSQLHelper = databaseSQLHelper;
    }

    /**
     * @return List of saved Client notifications.
     */
    public synchronized List<ClientNotification> getNotifications() {

        LinkedList<ClientNotification> clientNotifications = new LinkedList<>();

        SQLiteDatabase db = databaseSQLHelper.getReadableDatabase();

        String[] projection = {
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_TYPE,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_JSON_STRING,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_ID,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_TYPE,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_RESULT,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_SENT_TIME,
                DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_CUSTOM_NOTIFICATION_TYPE
        };


        if (db != null) {

            Cursor cursor;

            try {
                cursor = db.query(
                        DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME,  // The table to query
                        projection,                               // The columns to return
                        null,                                // The columns for the WHERE clause
                        null,                            // The values for the WHERE clause
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

                        AcknowledgementDetail acknowledgementDetail = new AcknowledgementDetail();

                        acknowledgementDetail.setServerNotificationId(cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_ID)));

                        acknowledgementDetail.setType(cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_TYPE)));

                        acknowledgementDetail.setResult(cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_RESULT)));

                        acknowledgementDetail.setSentTime(cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_SENT_TIME)));

                        acknowledgementDetail.setCustomNotificationType(cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_CUSTOM_NOTIFICATION_TYPE)));

                        String type = cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_TYPE));

                        String id = cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID));

                        String json = cursor.getString(
                                cursor.getColumnIndexOrThrow(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_JSON_STRING));


                        ClientNotification notification = ClientNotification.createClientNotification(type, id, acknowledgementDetail, json);

                        clientNotifications.add(notification);

                    }
                    while (cursor.moveToNext());
                }
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }

        return clientNotifications;
    }

    /**
     * Add Donky client notifications to database.
     *
     * @param clientNotifications Donky Client notifications to be saved.
     */
    public synchronized void addNotifications(List<ClientNotification> clientNotifications) {

        if (clientNotifications != null && !clientNotifications.isEmpty()) {
            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            for (ClientNotification clientNotification : clientNotifications) {
                addClientNotification(db, clientNotification);
            }
        }
    }

    /**
     * Add single Donky client notification to database.
     *
     * @param clientNotification Donky Client notification to be saved.
     */
    public synchronized void addNotification(ClientNotification clientNotification) {
        if (clientNotification != null) {
            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();
            addClientNotification(db, clientNotification);
        }
    }

    /**
     * Remove Donky client notifications from database.
     *
     * @param clientNotifications Donky Client notifications to be removed.
     */
    public synchronized void removeNotifications(List<ClientNotification> clientNotifications) {

        if (clientNotifications != null && !clientNotifications.isEmpty()) {

            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            String[] selectionArgs = new String[clientNotifications.size()];

            for (int i = 0; i < clientNotifications.size(); i++) {
                selectionArgs[i] = "'" + clientNotifications.get(i).getId() + "'";
            }

            String args = TextUtils.join(", ", selectionArgs);

            db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID + " IN (%s);", args));

            db.execSQL(String.format("DELETE FROM " + DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME + " WHERE " + DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID + " IS NULL;"));

        }
    }

    /**
     * @return True if there are any Donky client notifications stored in the database.
     */
    public synchronized boolean isNotificationPending() {

        SQLiteDatabase db = databaseSQLHelper.getReadableDatabase();

        if (db != null) {
            Cursor cursor = db.query(
                    DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME,  // The table to query
                    new String[]{DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_TYPE}, null, null, null, null, null);

            if (cursor != null) {

                int count = cursor.getCount();

                if (!cursor.isClosed()) {
                    cursor.close();
                }

                if (count > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Add Donky content notifications to database.
     *
     * @param contentNotifications Donky content notifications to be saved.
     */
    public void addContentNotifications(List<ContentNotification> contentNotifications) {

        if (contentNotifications != null && !contentNotifications.isEmpty()) {
            SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();

            for (ContentNotification contentNotification : contentNotifications) {
                addContentNotification(db, contentNotification);
            }
        }
    }

    /**
     * Add Donky content notification to database.
     *
     * @param db                  SQLite Database.
     * @param contentNotification Donky content notification to be added.
     */
    private void addContentNotification(SQLiteDatabase db, ContentNotification contentNotification) {

        if (contentNotification != null) {

            // New value for one column
            ContentValues values = new ContentValues();

            values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_JSON_STRING, contentNotification.getJsonString());
            values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_TYPE, contentNotification.getBaseNotificationType());
            values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID, contentNotification.getId());

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME,
                    null,
                    values);

            if (newRowId == -1) {
                log.error("Error when inserting to DB content notification.");
            }

        }
    }

    /**
     * Add single Donky client notification to database.
     *
     * @param db                 SQLite Database.
     * @param clientNotification Donky Client notification to be saved.
     */
    private synchronized void addClientNotification(SQLiteDatabase db, ClientNotification clientNotification) {

        if (clientNotification != null) {

            // New value for one column
            ContentValues values = new ContentValues();

            values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_TYPE, clientNotification.getBaseNotificationType());
            values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_ID, clientNotification.getId());
            values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_NOTIFICATION_JSON_STRING, clientNotification.getJsonString());

            if (clientNotification.getAcknowledgementDetail() != null) {
                values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_ID, clientNotification.getAcknowledgementDetail().getServerNotificationId());
                values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_TYPE, clientNotification.getAcknowledgementDetail().getType());
                values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_RESULT, clientNotification.getAcknowledgementDetail().getResult());
                values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_SENT_TIME, clientNotification.getAcknowledgementDetail().getSentTime());
                values.put(DatabaseSQLContract.ClientNotificationEntry.COLUMN_NAME_ACK_NOTIFICATION_CUSTOM_NOTIFICATION_TYPE, clientNotification.getAcknowledgementDetail().getCustomNotificationType());
            }

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    DatabaseSQLContract.ClientNotificationEntry.TABLE_NAME,
                    null,
                    values);

            if (newRowId == -1) {
                log.error("Error when inserting to DB client notification of type " + clientNotification.getBaseNotificationType());
            }

        }
    }
}