package net.donky.core.messaging.rich.logic.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import net.donky.core.logging.DLog;
import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;

/**
 * {@link ContentProvider} for rich messages database.
 *
 * Created by Marcin Swierczek
 * 09/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class RichMsgContentProvider extends ContentProvider {

    private final static String AUTHORITY_POSTFIX = ".donky.rich.provider";

    private static String AUTHORITY;

    private static String BASE_PATH;

    private static UriMatcher uriMatcher;

    private static final int RICH = 1;

    private RichMsgSQLiteHelper databaseSQLHelper;

    private static Uri CONTENT_URI_RICH;

    @Override
    public boolean onCreate() {

        getContentUri(getContext());
        RichMessageDataController.getInstance().init(getContext());

        databaseSQLHelper = RichMessageDataController.getInstance().getDatabaseSQLHelper();

        return true;
    }

    public synchronized static Uri getContentUri(Context context) {

        if (context != null) {
            if (AUTHORITY == null) {
                AUTHORITY = context.getPackageName() + AUTHORITY_POSTFIX;
            }
            if (BASE_PATH == null) {
                BASE_PATH = "content://" + AUTHORITY + "/";
            }
            if (uriMatcher == null) {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, DatabaseSQLContract.RichMessageEntry.TABLE_NAME, RICH);
            }
            if (CONTENT_URI_RICH == null) {
                CONTENT_URI_RICH = Uri.parse(BASE_PATH + DatabaseSQLContract.RichMessageEntry.TABLE_NAME);
            }
        }

        return CONTENT_URI_RICH;
    }

    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case RICH:
                queryBuilder.setTables(DatabaseSQLContract.RichMessageEntry.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = databaseSQLHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
            new DLog("RichMessagesContentProvider").error("No context available.");
        }

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseSQLHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case RICH:
                id = sqlDB.insert(DatabaseSQLContract.RichMessageEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            new DLog("RichMessagesContentProvider").error("No context available.");
        }
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseSQLHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case RICH:
                rowsDeleted = sqlDB.delete(DatabaseSQLContract.RichMessageEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            new DLog("RichMessagesContentProvider").error("No context available.");
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseSQLHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case RICH:
                rowsUpdated = sqlDB.update(DatabaseSQLContract.RichMessageEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            new DLog("RichMessagesContentProvider").error("No context available.");
        }
        return rowsUpdated;
    }
}