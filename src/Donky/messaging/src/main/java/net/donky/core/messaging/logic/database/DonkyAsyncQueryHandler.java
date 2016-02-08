package net.donky.core.messaging.logic.database;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import net.donky.core.messaging.logic.database.listeners.DonkyDeleteListener;
import net.donky.core.messaging.logic.database.listeners.DonkyInsertListener;
import net.donky.core.messaging.logic.database.listeners.DonkyQueryListener;
import net.donky.core.messaging.logic.database.listeners.DonkyUpdateListener;

/**
 * Created by Marcin Swierczek
 * 09/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyAsyncQueryHandler extends AsyncQueryHandler {

    DonkyQueryListener queryListener;

    DonkyInsertListener insertListener;

    DonkyUpdateListener updateListener;

    DonkyDeleteListener deleteListener;

    public DonkyAsyncQueryHandler(Context context){
        super(context.getContentResolver());
    }

    public DonkyAsyncQueryHandler(Context context, DonkyQueryListener queryListener){
        super(context.getContentResolver());
        this.queryListener = queryListener;
    }

    public DonkyAsyncQueryHandler(Context context, DonkyInsertListener insertListener){
        super(context.getContentResolver());
        this.insertListener = insertListener;
    }

    public DonkyAsyncQueryHandler(Context context, DonkyUpdateListener updateListener){
        super(context.getContentResolver());
        this.updateListener = updateListener;
    }

    public DonkyAsyncQueryHandler(Context context, DonkyDeleteListener deleteListener){
        super(context.getContentResolver());
        this.deleteListener = deleteListener;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (queryListener != null) {
            queryListener.onQueryComplete(token, cookie, cursor);
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        if (insertListener != null) {
            insertListener.onInsertComplete(token, cookie, uri);
        }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onUpdateComplete(token, cookie, result);
        if (updateListener != null) {
            updateListener.onUpdateComplete(token, cookie, result);
        }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
        if (deleteListener != null) {
            deleteListener.onDeleteComplete(token, cookie, result);
        }
    }
}
