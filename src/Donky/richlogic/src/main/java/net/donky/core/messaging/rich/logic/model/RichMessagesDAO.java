package net.donky.core.messaging.rich.logic.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.DonkyResultListener;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.logging.DLog;
import net.donky.core.messaging.logic.MessagingInternalController;
import net.donky.core.messaging.logic.database.BaseDAO;
import net.donky.core.messaging.logic.database.DonkyAsyncQueryHandler;
import net.donky.core.messaging.logic.database.listeners.DonkyDeleteListener;
import net.donky.core.messaging.logic.database.listeners.DonkyInsertListener;
import net.donky.core.messaging.logic.database.listeners.DonkyQueryListener;
import net.donky.core.messaging.logic.database.listeners.DonkyUpdateListener;
import net.donky.core.messaging.rich.logic.database.RichMsgContentProvider;
import net.donky.core.model.DonkyDataController;
import net.donky.core.network.DonkyNetworkController;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Database Access Object for Rich Messages.
 * <p/>
 * Created by Marcin Swierczek
 * 21/02/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMessagesDAO extends BaseDAO {

    private static final int REQUEST_CODE_QUERY_RICH_MESSAGES = 1;
    private static final int REQUEST_CODE_DELETE_RICH_MESSAGES = 2;
    private static final int REQUEST_CODE_UPDATE_RICH_MESSAGES = 3;
    private static final int REQUEST_CODE_INSERT_RICH_MESSAGE = 4;

    private final DLog log;

    Context context;

    /**
     * Database Access Object for Donky Client Notifications.
     */
    RichMessagesDAO(Context context) {
        log = new DLog("RichMessagesDAOv2");
        this.context = context;
    }

    /**
     * Gets all rich messages marked as unread. This method is blocking.
     *
     * @return All rich messages marked as unread.
     */
    public List<RichMessage> getUnreadRichMessages() {
        return getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead + " = ?", new String[]{Integer.toString(0)}, null);
    }

    /**
     * Gets all rich messages marked as unread. This method is non-blocking.
     *
     * @param listener Callback with all the unread rich messages in local database.
     */
    public void getUnreadRichMessages(final DonkyResultListener<List<RichMessage>> listener) {

        getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead + " = ?", new String[]{Integer.toString(0)}, null, new DonkyResultListener<List<RichMessage>>() {

            @Override
            public void success(List<RichMessage> result) {
                if (listener != null) {
                    listener.success(result);
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                log.error("Error querying rich messages in db", donkyException);
                reportError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Get rich message with given internal (internalId) or external id (messageId). InternalId is known only by the client SDK, not on the network. MessageId is given from the network. This method is blocking.
     *
     * @param id Rich message internal or external id.
     * @return Rich message with given internal or external id.
     */
    public RichMessage getRichMessage(final String id) {

        List<RichMessage> list = getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " = ?", new String[]{id, id}, null);

        if (!list.isEmpty()) {
            if (list.size() > 1) {
                log.warning("More than one message with internalId/messageId " + id);
            }
            return list.get(0);
        }

        return null;
    }

    /**
     * Get rich message with given internal (internalId) or external id (messageId). InternalId is known only by the client SDK, not on the network. MessageId is given from the network. This method is non-blocking.
     *
     * @param id       Rich message internal or external id.
     * @param listener Callback with the Rich Message loaded from local database.
     */
    public void getRichMessage(final String id, final DonkyResultListener<RichMessage> listener) {

        getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " = ?", new String[]{id, id}, null, new DonkyResultListener<List<RichMessage>>() {

            @Override
            public void success(List<RichMessage> result) {

                RichMessage richMessage = null;

                if (!result.isEmpty()) {
                    if (result.size() > 1) {
                        log.warning("More than one message with id " + id);
                    }
                    richMessage = result.get(0);
                }

                if (listener != null) {
                    listener.success(richMessage);
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                log.error("Error querying rich messages in db", donkyException);
                reportError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Get rich message with given external message id. Rich message external id is given from the network. This method is blocking.
     *
     * @param messageId Rich message external id.
     * @return Rich message with given external id.
     * @deprecated please use {@link #getRichMessage(String)} method instead.
     */
    @Deprecated
    public RichMessage getRichMessageWithMessageId(final String messageId) {

        List<RichMessage> list = getRichMessages(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " = ?", new String[]{messageId}, null);

        if (!list.isEmpty()) {
            if (list.size() > 1) {
                log.warning("More than one message with externalId " + messageId);
            }
            return list.get(0);
        }

        return null;
    }

    /**
     * Gets all rich messages. This method is blocking.
     *
     * @return All rich messages.
     */
    public List<RichMessage> getAllRichMessages() {
        return getRichMessages(null, null, null);
    }

    /**
     * Gets all rich messages. This method is non-blocking.
     *
     * @param listener Callback with all rich messages in local database.
     */
    public void getAllRichMessages(final DonkyResultListener<List<RichMessage>> listener) {
        getRichMessages(null, null, null, new DonkyResultListener<List<RichMessage>>() {
            @Override
            public void success(List<RichMessage> result) {
                if (listener != null) {
                    listener.success(result);
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                log.error("Error updating rich messages from db", donkyException);
                reportError(donkyException, validationErrors, listener);
            }
        });
    }

    /**
     * Mark rich message as read. This method is blocking.
     *
     * @param id Rich message internal/external id.
     * @return The number of rows affected.
     */
    public int markAsRead(String id) {

        int numberOfRowsUpdated = 0;

        if (!TextUtils.isEmpty(id)) {

            try {

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " = ?";
                String[] selectionArgs = {id, id};

                ContentValues cv = new ContentValues();
                cv.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead, 1);

                numberOfRowsUpdated = context.getContentResolver().update(RichMsgContentProvider.getContentUri(context), cv, selection, selectionArgs);

                if (numberOfRowsUpdated > 1) {
                    log.warning("More than one rich message with the same user internal/external id.");
                }
            } catch (Exception exception) {
                log.error("Error updating rich message in db", exception);
            }
        }
        return numberOfRowsUpdated;
    }

    /**
     * Mark rich message as read. This method is non-blocking.
     *
     * @param id       Rich message internal id (internalId).
     * @param listener Callback with the number of rows affected.
     */
    public void markAsRead(final String id, final DonkyResultListener<Integer> listener) {

        if (!TextUtils.isEmpty(id)) {

            try {

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " = ?";
                String[] selectionArgs = {id, id};

                ContentValues cv = new ContentValues();
                cv.put(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageRead, 1);

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyUpdateListener() {
                    @Override
                    public void onUpdateComplete(int token, Object cookie, int result) {

                        if (result > 1) {
                            log.warning("More than one rich message with the same user internal/message id.");
                        }

                        if (listener != null && token == REQUEST_CODE_UPDATE_RICH_MESSAGES) {
                            listener.success(result);
                        }
                    }
                });

                queryHandler.startUpdate(REQUEST_CODE_UPDATE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), cv, selection, selectionArgs);

            } catch (Exception exception) {
                log.error("Error updating rich messages in db", exception);
                reportError(exception, listener);
            }
        }
    }

    /**
     * Gets rich messages with given SQLite selection and selectionArgs. This method is blocking.
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     * @param sortOrder     How to order the rows, formatted as an SQL ORDER BY
     *                      clause (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @return List of found rich messages.
     */
    private List<RichMessage> getRichMessages(String selection, String[] selectionArgs, String sortOrder) {

        LinkedList<RichMessage> richMessages = new LinkedList<>();

        Cursor cursor = null;

        try {

            cursor = context.getContentResolver().query(RichMsgContentProvider.getContentUri(context), null, selection, selectionArgs, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    richMessages.add(getRichMessage(cursor));
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            log.error("Error loading db content to rich message obj.", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return richMessages;
    }

    /**
     * Gets rich messages with given SQLite selection and selectionArgs. This method is non-blocking.
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     * @param sortOrder     How to order the rows, formatted as an SQL ORDER BY
     *                      clause (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @param listener      Callback with list of found rich messages.
     */
    private void getRichMessages(String selection, String[] selectionArgs, String sortOrder, final DonkyResultListener<List<RichMessage>> listener) {

        try {

            DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyQueryListener() {

                @Override
                public void onQueryComplete(int token, Object cookie, Cursor cursor) {

                    LinkedList<RichMessage> richMessages = new LinkedList<>();

                    if (cursor != null) {

                        try {

                            if (token == REQUEST_CODE_QUERY_RICH_MESSAGES && cursor.moveToFirst()) {
                                do {
                                    richMessages.add(getRichMessage(cursor));
                                } while (cursor.moveToNext());
                            }

                        } catch (Exception e) {
                            log.error("Error loading db content to rich message obj.", e);
                        } finally {
                            if (!cursor.isClosed()) {
                                cursor.close();
                            }
                        }
                    }

                    if (listener != null) {
                        listener.success(richMessages);
                    }
                }
            });

            queryHandler.startQuery(REQUEST_CODE_QUERY_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), null, selection, selectionArgs, sortOrder);

        } catch (Exception exception) {
            log.error("Error querying rich messages from db", exception);
            reportError(exception, listener);
        }
    }

    /**
     * Creates rich message from cursors current position.
     */
    private RichMessage getRichMessage(Cursor cursor) {

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

        return richMessage;
    }

    /**
     * Save Rich Messages into database. This method is blocking.
     *
     * @param richMessageList Rich Message list to be saved.
     */
    public void saveRichMessages(List<RichMessage> richMessageList) {
        if (richMessageList != null && !richMessageList.isEmpty()) {
            for (RichMessage richMessage : richMessageList) {
                saveRichMessage(richMessage);
            }
        }
    }

    /**
     * Remove Rich Messages from database. This method is blocking.
     *
     * @param richMessageList Rich Messages to be removed.
     */
    public int removeRichMessages(List<RichMessage> richMessageList) {

        if (richMessageList != null && !richMessageList.isEmpty()) {

            List<String> richMessageIdList = new ArrayList<>();

            for (RichMessage rm : richMessageList) {
                richMessageIdList.add(rm.getInternalId());
                MessagingInternalController.getInstance().queueMessageDeletedNotification(rm);
            }

            DonkyNetworkController.getInstance().synchronise();

            return removeRichMessagesWithInternalIds(richMessageIdList, false);
        }

        return 0;
    }

    /**
     * Remove Rich Messages from database. This method is non-blocking.
     *
     * @param richMessageList Rich Messages to be removed.
     * @param listener        Callback with number of rows affected.
     */
    public void removeRichMessages(List<RichMessage> richMessageList, DonkyResultListener<Integer> listener) {

        List<String> richMessageIdList = new ArrayList<>();

        for (RichMessage rm : richMessageList) {
            richMessageIdList.add(rm.getInternalId());
            MessagingInternalController.getInstance().queueMessageDeletedNotification(rm);
        }

        DonkyNetworkController.getInstance().synchronise();

        removeRichMessagesWithInternalIds(richMessageIdList, false, listener);
    }

    /**
     * Queue client notifications for deleted messages so it can be synchronised with other devices. Non-blocking method.
     *
     * @param richMessageIdList Internal ids of messages
     * @param listener          Completion callback
     */
    private void reportMessageDeleted(List<String> richMessageIdList, final DonkyListener listener) {

        if (richMessageIdList != null && !richMessageIdList.isEmpty()) {

            try {

                String[] selectionArgs = new String[richMessageIdList.size()];

                StringBuilder inList = new StringBuilder();
                for (int i = 0; i < richMessageIdList.size(); i++) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                    selectionArgs[i] = richMessageIdList.get(i);
                }

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IN (" + inList + ")";

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyQueryListener() {
                    @Override
                    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        if (cursor != null) {

                            try {

                                if (token == REQUEST_CODE_QUERY_RICH_MESSAGES && cursor.moveToFirst()) {
                                    do {
                                        MessagingInternalController.getInstance().queueMessageDeletedNotification(getRichMessage(cursor));
                                    } while (cursor.moveToNext());
                                }

                                if (listener != null) {
                                    listener.success();
                                }

                            } catch (Exception e) {
                                log.error("Error loading db content to rich message obj.", e);
                                reportError(e, listener);
                            } finally {
                                if (!cursor.isClosed()) {
                                    cursor.close();
                                }
                            }
                            DonkyNetworkController.getInstance().synchronise();
                        }
                    }
                });

                queryHandler.startQuery(REQUEST_CODE_QUERY_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), null, selection, selectionArgs, null);

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
                reportError(exception, listener);
            }
        }
    }

    /**
     * Queue client notifications for deleted messages so it can be synchronised with other devices. Non-blocking method.
     *
     * @param richMessageIdList Internal ids of messages
     */
    private void reportMessageDeleted(List<String> richMessageIdList) {

        if (richMessageIdList != null && !richMessageIdList.isEmpty()) {

            try {

                String[] selectionArgs = new String[richMessageIdList.size()];

                StringBuilder inList = new StringBuilder();
                for (int i = 0; i < richMessageIdList.size(); i++) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                    selectionArgs[i] = richMessageIdList.get(i);
                }

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IN (" + inList + ")";

                try {

                    Cursor cursor = context.getContentResolver().query(RichMsgContentProvider.getContentUri(context), null, selection, selectionArgs, null);

                    if (cursor != null) {

                        try {

                            if (cursor.moveToFirst()) {
                                do {
                                    MessagingInternalController.getInstance().queueMessageDeletedNotification(getRichMessage(cursor));
                                } while (cursor.moveToNext());
                            }

                        } catch (Exception e) {
                            log.error("Error loading db content to rich message obj.", e);
                        } finally {
                            if (!cursor.isClosed()) {
                                cursor.close();
                            }
                        }
                        DonkyNetworkController.getInstance().synchronise();
                    }

                } catch (Exception exception) {
                    log.error("Error loading rich message from db", exception);
                }

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
            }
        }
    }

    /**
     * Remove Rich Messages from database. This method is blocking.
     *
     * @param richMessageIdList Rich Messages to be removed.
     * @return Number of removed rich messages.
     */
    public int removeRichMessagesWithInternalIds(List<String> richMessageIdList) {
        return removeRichMessagesWithInternalIds(richMessageIdList, true);
    }

    /**
     * Remove Rich Messages from database. This method is blocking.
     *
     * @param richMessageIdList   Rich Messages to be removed.
     * @param shouldReportDeleted
     * @return Number of removed rich messages.
     */
    private int removeRichMessagesWithInternalIds(List<String> richMessageIdList, boolean shouldReportDeleted) {

        if (richMessageIdList != null && !richMessageIdList.isEmpty()) {

            if (shouldReportDeleted) {
                reportMessageDeleted(richMessageIdList);
            }

            try {

                String[] selectionArgs = new String[richMessageIdList.size()];

                StringBuilder inList = new StringBuilder();
                for (int i = 0; i < richMessageIdList.size(); i++) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                    selectionArgs[i] = richMessageIdList.get(i);
                }

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IN (" + inList + ")";

                return context.getContentResolver().delete(RichMsgContentProvider.getContentUri(context), selection, selectionArgs);

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
            }
        }

        return 0;
    }

    /**
     * Remove Rich Messages from database. This method is non-blocking.
     *
     * @param richMessageIdList Rich Messages to be removed.
     * @param listener          Callback with number of rows affected.
     */
    public void removeRichMessagesWithInternalIds(final List<String> richMessageIdList, final DonkyResultListener<Integer> listener) {
        removeRichMessagesWithInternalIds(richMessageIdList, true, listener);
    }

    /**
     * Remove Rich Messages from database. This method is non-blocking.
     *
     * @param richMessageIdList   Rich Messages to be removed.
     * @param shouldReportDeleted Should SDK syncronise this operation across devices
     * @param listener            Callback with number of rows affected.
     */
    private void removeRichMessagesWithInternalIds(final List<String> richMessageIdList, boolean shouldReportDeleted, final DonkyResultListener<Integer> listener) {

        if (shouldReportDeleted) {
            reportMessageDeleted(richMessageIdList, new DonkyListener() {
                @Override
                public void success() {
                    doRemoveRichMessagesWithInternalIds(richMessageIdList, listener);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    doRemoveRichMessagesWithInternalIds(richMessageIdList, listener);
                }
            });

        } else {
            doRemoveRichMessagesWithInternalIds(richMessageIdList, listener);
        }

    }

    /**
     * Remove Rich Messages from database. This method is non-blocking.
     *
     * @param richMessageIdList Rich Messages to be removed.
     * @param listener          Callback with number of rows affected.
     */
    private void doRemoveRichMessagesWithInternalIds(final List<String> richMessageIdList, final DonkyResultListener<Integer> listener) {
        if (richMessageIdList != null && !richMessageIdList.isEmpty()) {

            try {

                String[] selectionArgs = new String[richMessageIdList.size()];

                StringBuilder inList = new StringBuilder();
                for (int i = 0; i < richMessageIdList.size(); i++) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                    selectionArgs[i] = richMessageIdList.get(i);
                }

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " IN (" + inList + ")";

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyDeleteListener() {
                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        if (listener != null && token == REQUEST_CODE_DELETE_RICH_MESSAGES) {
                            listener.success(result);
                        }
                    }
                });

                queryHandler.startDelete(REQUEST_CODE_DELETE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), selection, selectionArgs);

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
                if (listener != null) {
                    reportError(exception, listener);
                }
            }

        } else if (listener != null) {
            listener.success(0);
        }
    }

    /**
     * Remove Rich Messages from database. This method is non-blocking.
     *
     * @param richMessageIdList Rich Messages to be removed.
     * @param listener          Callback with number of rows affected.
     */
    public void removeRichMessagesToSyncState(final List<String> richMessageIdList, final DonkyResultListener<Integer> listener) {

        if (richMessageIdList != null && !richMessageIdList.isEmpty()) {

            try {

                String[] selectionArgs = new String[richMessageIdList.size()];

                StringBuilder inList = new StringBuilder();
                for (int i = 0; i < richMessageIdList.size(); i++) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");
                    selectionArgs[i] = richMessageIdList.get(i);
                }

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_messageId + " IN (" + inList + ")";

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyDeleteListener() {
                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        if (listener != null && token == REQUEST_CODE_DELETE_RICH_MESSAGES) {
                            listener.success(result);
                        }
                    }
                });

                queryHandler.startDelete(REQUEST_CODE_DELETE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), selection, selectionArgs);

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
                if (listener != null) {
                    reportError(exception, listener);
                }
            }

        } else if (listener != null) {
            listener.success(0);
        }
    }

    /**
     * Remove Rich Message from database. This method is blocking.
     *
     * @param richMessage Rich Message to be removed.
     */
    public int removeRichMessage(final RichMessage richMessage) {

        if (richMessage != null) {
            MessagingInternalController.getInstance().queueMessageDeletedNotification(richMessage);
            return removeRichMessage(richMessage.getInternalId(), false);
        } else {
            return 0;
        }
    }

    /**
     * Remove Rich Message from database. This method is non-blocking.
     *
     * @param richMessage Rich Message to be removed.
     * @param listener    Callback with number of rows affected.
     */
    public void removeRichMessage(final RichMessage richMessage, final DonkyResultListener<Integer> listener) {
        if (richMessage != null) {
            MessagingInternalController.getInstance().queueMessageDeletedNotification(richMessage);
            removeRichMessage(richMessage.getInternalId(), false, listener);
        } else if (listener != null) {
            listener.success(0);
        }
    }

    /**
     * Remove Rich Message from database. This method is blocking.
     *
     * @param richMessageInternalId Internal ID of a Rich Message to be removed.
     */
    private int removeRichMessage(final String richMessageInternalId, final boolean shouldReportDeleted) {
        if (shouldReportDeleted) {
            List<String> map = new LinkedList<>();
            map.add(richMessageInternalId);
            reportMessageDeleted(map);
        }

        if (!TextUtils.isEmpty(richMessageInternalId)) {

            try {

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ?";
                String[] arg = {richMessageInternalId};

                return context.getContentResolver().delete(RichMsgContentProvider.getContentUri(context), selection, arg);

            } catch (Exception exception) {
                log.error("Error deleting rich message from db", exception);
            }
        }

        return 0;

    }

    /**
     * Remove Rich Message from database. This method is blocking.
     *
     * @param richMessageInternalId Internal ID of a Rich Message to be removed.
     */
    public int removeRichMessage(final String richMessageInternalId) {
        return removeRichMessage(richMessageInternalId, true);
    }

    /**
     * Remove Rich Message from database. This method is non-blocking.
     *
     * @param richMessageInternalId Internal ID of a Rich Message to be removed.
     * @param listener              Completion callback
     */
    private void removeRichMessage(final String richMessageInternalId, final boolean shouldReportDeleted, final DonkyResultListener<Integer> listener) {

        if (shouldReportDeleted) {
            List<String> map = new LinkedList<>();
            map.add(richMessageInternalId);
            reportMessageDeleted(map, new DonkyListener() {
                @Override
                public void success() {
                    doRemoveRichMessage(richMessageInternalId, listener);
                }

                @Override
                public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                    doRemoveRichMessage(richMessageInternalId, listener);
                }
            });

        } else {
            doRemoveRichMessage(richMessageInternalId, listener);
        }
    }

    /**
     * Remove Rich Message from database. This method is non-blocking.
     *
     * @param richMessageInternalId Internal ID of a Rich Message to be removed.
     * @param listener              Completion callback
     */
    private void doRemoveRichMessage(final String richMessageInternalId, final DonkyResultListener<Integer> listener) {
        if (!TextUtils.isEmpty(richMessageInternalId)) {

            try {

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ?";
                String[] arg = {richMessageInternalId};

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyDeleteListener() {
                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        if (listener != null && token == REQUEST_CODE_DELETE_RICH_MESSAGES) {
                            listener.success(result);
                        }
                    }
                });

                queryHandler.startDelete(REQUEST_CODE_DELETE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), selection, arg);

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
                if (listener != null) {
                    reportError(exception, listener);
                }
            }
        } else if (listener != null) {
            listener.success(0);
        }
    }

    /**
     * Remove Rich Message from database. This method is non-blocking.
     *
     * @param richMessageInternalId Internal ID of a Rich Message to be removed.
     */
    public void removeRichMessage(final String richMessageInternalId, final DonkyResultListener<Integer> listener) {
        removeRichMessage(richMessageInternalId, true, listener);
    }

    /**
     * Gets rich message cursor containing all rich messages that didn't exceeded maximum availability days. This method is non-blocking.
     *
     * @param filter String to filter the rich message sender display name and description
     * @return Cursor with filtered rich messages.
     */
    public void getRichMessagesCursorForUI(final String filter, final DonkyResultListener<Cursor> listener) {

        try {

            DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyQueryListener() {

                @Override
                public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    if (listener != null) {
                        listener.success(cursor);
                    }
                }
            });

            Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

            final long currentTimeUTC = System.currentTimeMillis();
            final long acceptableSentTimeMillisUTC;

            acceptableSentTimeMillisUTC = currentTimeUTC - TimeUnit.DAYS.toMillis(availabilityDays);

            String selection = null;
            String arg;
            String[] selectionArgs = null;

            if (!TextUtils.isEmpty(filter)) {
                selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description + " LIKE ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName + " LIKE ? AND " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " > ?";
                arg = "%" + filter + "%";
                selectionArgs = new String[]{arg, arg, String.valueOf(acceptableSentTimeMillisUTC)};
            }

            queryHandler.startQuery(REQUEST_CODE_QUERY_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), null, selection, selectionArgs, DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " DESC");

        } catch (Exception exception) {
            log.error("Error querying rich messages from db", exception);
            reportError(exception, listener);
        }
    }

    /**
     * Gets rich message cursor containing all rich messages that didn't exceeded maximum availability days. This method is blocking.
     *
     * @param filter String to filter the rich message sender display name and description
     * @return Cursor with filtered rich messages.
     */
    public Cursor getRichMessagesCursorForUI(String filter) {

        Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

        final long currentTimeUTC = System.currentTimeMillis();
        final long acceptableSentTimeMillisUTC;

        acceptableSentTimeMillisUTC = currentTimeUTC - TimeUnit.DAYS.toMillis(availabilityDays);

        String selection = null;
        String arg;
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(filter)) {
            selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description + " LIKE ? OR " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName + " LIKE ? AND " + DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " > ?";
            arg = "%" + filter + "%";
            selectionArgs = new String[]{arg, arg, String.valueOf(acceptableSentTimeMillisUTC)};
        }

        try {
            return context.getContentResolver().query(RichMsgContentProvider.getContentUri(context), null, selection, selectionArgs, DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " DESC");
        } catch (Exception exception) {
            log.error("Error loading rich message from db", exception);
        }

        return null;
    }

    /**
     * Clean the database deleting unavailable rich messages. This method is blocking.
     */
    public int removeMessagesThatExceededTheAvailabilityPeriod() {

        String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " < ?";

        final long acceptableSentTimeMillisUTC = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays());

        String[] arg = {String.valueOf(acceptableSentTimeMillisUTC)};

        try {
            return context.getContentResolver().delete(RichMsgContentProvider.getContentUri(context), selection, arg);
        } catch (Exception exception) {
            log.error("Error deleting rich message from db", exception);
        }

        return 0;
    }

    /**
     * Clean the database deleting unavailable rich messages. This method is non-blocking.
     *
     * @param listener Callback with number of rows affected.
     */
    public void removeMessagesThatExceededTheAvailabilityPeriod(final DonkyResultListener<Integer> listener) {

        final Integer availabilityDays = DonkyDataController.getInstance().getConfigurationDAO().getMaxAvailabilityDays();

        if (availabilityDays != null) {

            String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_sentTimestampLong + " < ?";

            final long acceptableSentTimeMillisUTC = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(availabilityDays);

            String[] arg = {String.valueOf(acceptableSentTimeMillisUTC)};

            try {

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyDeleteListener() {
                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        log.info("Deleted " + result + " Rich Messages that exceeded maximum " + availabilityDays + " availability days.");
                        if (listener != null && token == REQUEST_CODE_DELETE_RICH_MESSAGES) {
                            listener.success(result);
                        }
                    }
                });

                queryHandler.startDelete(REQUEST_CODE_DELETE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), selection, arg);

            } catch (Exception exception) {
                log.error("Error deleting rich messages from db", exception);
                if (listener != null) {
                    reportError(exception, listener);
                }
            }
        } else if (listener != null) {
            listener.success(0);
        }
    }

    /**
     * Add Rich Message to database. This method is blocking.
     *
     * @param richMessage Rich Message to be saved.
     * @return Uri of database row that was inserted.
     */
    public Uri saveRichMessage(RichMessage richMessage) {

        if (richMessage != null && !TextUtils.isEmpty(richMessage.getInternalId())) {

            try {
                return context.getContentResolver().insert(RichMsgContentProvider.getContentUri(context), getContentValues(richMessage));
            } catch (Exception exception) {
                log.error("Error updating contact in db", exception);
            }
        }

        return null;
    }

    /**
     * Add Rich Message to database. This method is non-blocking.
     *
     * @param richMessage Rich Message to be saved.
     * @param listener    Callback with uri of inserted database row.
     */
    public void saveRichMessage(RichMessage richMessage, final DonkyResultListener<Uri> listener) {

        if (richMessage != null && !TextUtils.isEmpty(richMessage.getInternalId())) {

            try {

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyInsertListener() {
                    @Override
                    public void onInsertComplete(int token, Object cookie, Uri uri) {
                        if (listener != null && token == REQUEST_CODE_INSERT_RICH_MESSAGE) {
                            listener.success(uri);
                        }
                    }
                });

                queryHandler.startInsert(REQUEST_CODE_INSERT_RICH_MESSAGE, null, RichMsgContentProvider.getContentUri(context), getContentValues(richMessage));

            } catch (Exception exception) {
                log.error("Error inserting contact in db", exception);
                if (listener != null) {
                    reportError(exception, listener);
                }
            }
        } else if (listener != null) {
            listener.success(null);
        }
    }

    /**
     * Update Rich Message in database. This method is blocking.
     *
     * @param richMessage Rich Message to be saved.
     * @return Number of rows updated.
     */
    public int updateRichMessage(RichMessage richMessage) {

        if (richMessage != null && !TextUtils.isEmpty(richMessage.getInternalId())) {

            try {

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ?";
                String[] arg = {richMessage.getInternalId()};

                return context.getContentResolver().update(RichMsgContentProvider.getContentUri(context), getContentValues(richMessage), selection, arg);

            } catch (Exception exception) {
                log.error("Error updating contact in db", exception);
            }
        }

        return 0;
    }

    /**
     * Update Rich Message in database. This method is non-blocking.
     *
     * @param richMessage Rich Message to be saved.
     * @param listener    Callback with uri of inserted database row.
     */
    public void updateRichMessage(RichMessage richMessage, final DonkyResultListener<Integer> listener) {

        if (richMessage != null && !TextUtils.isEmpty(richMessage.getInternalId())) {

            try {

                DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyUpdateListener() {
                    @Override
                    public void onUpdateComplete(int token, Object cookie, int result) {
                        if (listener != null && token == REQUEST_CODE_UPDATE_RICH_MESSAGES) {
                            listener.success(result);
                        }
                    }
                });

                String selection = DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId + " = ?";
                String[] arg = {richMessage.getInternalId()};

                queryHandler.startUpdate(REQUEST_CODE_UPDATE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), getContentValues(richMessage), selection, arg);

            } catch (Exception exception) {
                log.error("Error inserting contact in db", exception);
                if (listener != null) {
                    reportError(exception, listener);
                }
            }
        } else if (listener != null) {
            listener.success(null);
        }
    }

    /**
     * Update Rich Message or if not found add Rich Message to database. This method is blocking.
     *
     * @param richMessage Rich Message to be saved.
     */
    public int updateOrSaveRichMessage(final RichMessage richMessage) {

        int result = 0;

        if (richMessage != null && !TextUtils.isEmpty(richMessage.getInternalId())) {

            if (getRichMessage(richMessage.getInternalId()) != null) {

                result = updateRichMessage(richMessage);

                if (result < 1) {
                    if (saveRichMessage(richMessage) != null) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Update Rich Message or if not found add Rich Message to database. This method is non-blocking.
     *
     * @param richMessage Rich Message to be updated/saved.
     * @param listener    Callback with the number of rows affected.
     */
    public void updateOrSaveRichMessage(final RichMessage richMessage, final DonkyResultListener<Integer> listener) {

        updateRichMessage(richMessage, new DonkyResultListener<Integer>() {

            @Override
            public void success(Integer result) {

                if (result < 1) {

                    saveRichMessage(richMessage, new DonkyResultListener<Uri>() {

                        @Override
                        public void success(Uri result) {

                            if (listener != null) {
                                if (result != null) {
                                    listener.success(1);
                                } else {
                                    listener.success(0);
                                }
                            }
                        }

                        @Override
                        public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                            reportError(donkyException, validationErrors, listener);
                        }
                    });
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                reportError(donkyException, validationErrors, listener);
            }
        });

    }

    /**
     * Delete all saved rich messages. This method is blocking.
     */
    public int removeAllRichMessages() {

        List<RichMessage> richMessages = getAllRichMessages();
        if (!richMessages.isEmpty()) {
            for (RichMessage rm : richMessages) {
                MessagingInternalController.getInstance().queueMessageDeletedNotification(rm);
            }
        }

        try {
            return context.getContentResolver().delete(RichMsgContentProvider.getContentUri(context), null, null);
        } catch (Exception exception) {
            log.error("Error deleting rich message from db", exception);
        }

        DonkyNetworkController.getInstance().synchronise();

        return 0;
    }

    /**
     * Delete all saved rich messages. This method is non-blocking.
     *
     * @param listener Callback with number of deleted rows from database.
     */
    public void removeAllRichMessages(final DonkyResultListener<Integer> listener) {

        List<RichMessage> richMessages = getAllRichMessages();
        if (!richMessages.isEmpty()) {
            for (RichMessage rm : richMessages) {
                MessagingInternalController.getInstance().queueMessageDeletedNotification(rm);
            }
        }

        try {

            DonkyAsyncQueryHandler queryHandler = new DonkyAsyncQueryHandler(context, new DonkyDeleteListener() {
                @Override
                public void onDeleteComplete(int token, Object cookie, int result) {
                    if (listener != null && token == REQUEST_CODE_DELETE_RICH_MESSAGES) {
                        listener.success(result);
                    }
                }
            });

            queryHandler.startDelete(REQUEST_CODE_DELETE_RICH_MESSAGES, null, RichMsgContentProvider.getContentUri(context), null, null);

        } catch (Exception exception) {
            log.error("Error deleting rich messages from db", exception);
            if (listener != null) {
                reportError(exception, listener);
            }
        }
    }

    /**
     * Creates content values for rich message to be saved in database.
     */
    private ContentValues getContentValues(RichMessage richMessage) {

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

        return values;
    }
}